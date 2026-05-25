package com.diploma.fuelstats.presentation.car

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.diploma.fuelstats.R
import com.diploma.fuelstats.di.ServiceLocator

class CarProfileViewFragment : Fragment(R.layout.fragment_car_profile_view) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvBrandModel: TextView = view.findViewById(R.id.tvCarBrandModel)
        val tvVehicleType: TextView = view.findViewById(R.id.tvVehicleType)
        val tvYear: TextView = view.findViewById(R.id.tvCarYear)
        val tvTankCapacity: TextView = view.findViewById(R.id.tvTankCapacity)
        val tvCurrentOdometer: TextView = view.findViewById(R.id.tvCurrentOdometer)
        val tvHint: TextView = view.findViewById(R.id.tvCarProfileHint)
        val btnEdit: Button = view.findViewById(R.id.btnEditCarProfile)

        val tvAccountStatus: TextView = view.findViewById(R.id.tvAccountStatus)
        val btnAuthAction: Button = view.findViewById(R.id.btnAuthAction)

        val car = ServiceLocator.currentCar
        val authStorage = ServiceLocator.authSessionStorage

        if (car == null) {
            tvBrandModel.text = ""
            tvVehicleType.text = ""
            tvYear.text = ""
            tvTankCapacity.text = ""
            tvCurrentOdometer.text = ""
            tvHint.text =
                "Профиль ещё не заполнен. Нажмите «Редактировать», чтобы добавить автомобиль."
            tvHint.visibility = View.VISIBLE
        } else {
            val brandPart = car.brand.takeIf { it.isNotBlank() } ?: "не указана"
            val modelPart = car.model.takeIf { it.isNotBlank() } ?: ""
            tvBrandModel.text = "$brandPart ${modelPart}".trim()

            tvVehicleType.text = VehicleTypeUi.labelOf(car.vehicleType)

            tvYear.text = if (car.year > 0) {
                "${car.year}"
            } else {
                "не указан"
            }

            tvTankCapacity.text = if (car.tankCapacityLiters != null) {
                String.format("%.1f", car.tankCapacityLiters)
            } else {
                "не указан"
            }

            tvCurrentOdometer.text = car.currentOdometerKm.toString()

            tvHint.text = ""
            tvHint.visibility = View.GONE
        }

        if (authStorage.isAuthorized()) {
            val email = authStorage.getEmail().orEmpty()
            tvAccountStatus.text = if (email.isNotBlank()) {
                "Вы вошли как $email"
            } else {
                "Вы авторизованы"
            }
            btnAuthAction.text = "Выйти"
        } else {
            tvAccountStatus.text = "Вы не вошли в аккаунт"
            btnAuthAction.text = "Войти / Зарегистрироваться"
        }

        btnEdit.setOnClickListener {
            findNavController().navigate(
                R.id.action_carProfileViewFragment_to_carProfileEditFragment
            )
        }

        btnAuthAction.setOnClickListener {
            if (authStorage.isAuthorized()) {
                authStorage.clear()
                tvAccountStatus.text = "Вы не вошли в аккаунт"
                btnAuthAction.text = "Войти / Зарегистрироваться"
            } else {
                findNavController().navigate(
                    R.id.action_carProfileViewFragment_to_authFragment
                )
            }
        }
    }

}