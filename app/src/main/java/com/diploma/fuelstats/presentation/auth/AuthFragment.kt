package com.diploma.fuelstats.presentation.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.diploma.fuelstats.R
import com.diploma.fuelstats.di.ServiceLocator
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var isLoginMode = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle: TextView = view.findViewById(R.id.tvAuthTitle)
        val etEmail: EditText = view.findViewById(R.id.etEmail)
        val etPassword: EditText = view.findViewById(R.id.etPassword)
        val tvAuthError: TextView = view.findViewById(R.id.tvAuthError)
        val btnSubmit: Button = view.findViewById(R.id.btnSubmitAuth)
        val btnSwitchMode: Button = view.findViewById(R.id.btnSwitchMode)

        val authStorage = ServiceLocator.authSessionStorage
        val authRepository = ServiceLocator.authRepository

        fun renderMode() {
            if (isLoginMode) {
                tvTitle.text = "Вход в аккаунт"
                btnSubmit.text = "Войти"
                btnSwitchMode.text = "Нет аккаунта? Зарегистрироваться"
            } else {
                tvTitle.text = "Регистрация"
                btnSubmit.text = "Зарегистрироваться"
                btnSwitchMode.text = "Уже есть аккаунт? Войти"
            }
        }

        fun showError(text: String) {
            tvAuthError.text = text
            tvAuthError.visibility = View.VISIBLE
        }

        fun clearError() {
            tvAuthError.text = ""
            tvAuthError.visibility = View.GONE
        }

        renderMode()

        btnSwitchMode.setOnClickListener {
            isLoginMode = !isLoginMode
            clearError()
            renderMode()
        }

        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            clearError()

            if (email.isBlank() || password.isBlank()) {
                showError("Заполните email и пароль")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Введите корректный email")
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                btnSubmit.isEnabled = false
                btnSwitchMode.isEnabled = false

                val oldSubmitText = btnSubmit.text
                btnSubmit.text = if (isLoginMode) "Входим..." else "Регистрируем..."

                try {
                    val token = if (isLoginMode) {
                        authRepository.login(email, password)
                    } else {
                        authRepository.register(email, password)
                    }

                    authStorage.saveAccessToken(token)
                    authStorage.saveEmail(email)

                    findNavController().navigateUp()
                } catch (e: HttpException) {
                    val body = try {
                        e.response()?.errorBody()?.string().orEmpty()
                    } catch (_: Exception) {
                        ""
                    }

                    val errorText = when {
                        e.code() == 401 || body.contains("invalid credentials", ignoreCase = true) ->
                            "Неверный email или пароль"

                        body.contains("duplicate key value", ignoreCase = true) ||
                                body.contains("already exists", ignoreCase = true) ->
                            "Пользователь с таким email уже существует"

                        else ->
                            "Ошибка сервера: ${e.code()}"
                    }

                    showError(errorText)
                } catch (e: Exception) {
                    val message = e.message.orEmpty()

                    val errorText = when {
                        message.contains("invalid credentials", ignoreCase = true) ->
                            "Неверный email или пароль"

                        message.contains("duplicate key value", ignoreCase = true) ||
                                message.contains("already exists", ignoreCase = true) ->
                            "Пользователь с таким email уже существует"

                        message.contains("Parameter specified as non-null is null", ignoreCase = true) ->
                            "Нужно поправить AuthResponseDto под ответ сервера"

                        else ->
                            "Не удалось выполнить запрос"
                    }

                    showError(errorText)
                } finally {
                    btnSubmit.isEnabled = true
                    btnSwitchMode.isEnabled = true
                    btnSubmit.text = oldSubmitText
                }
            }
        }
    }
}