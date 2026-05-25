package com.diploma.fuelstats.presentation.refuels

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.diploma.fuelstats.R
import com.diploma.fuelstats.di.ServiceLocator
import com.diploma.fuelstats.domain.model.FuelEntry
import kotlinx.coroutines.launch

class AddRefuelFragment : Fragment(R.layout.fragment_add_refuel) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etOdometer: EditText = view.findViewById(R.id.etOdometer)
        val etLiters: EditText = view.findViewById(R.id.etLiters)
        val cbFullTank: CheckBox = view.findViewById(R.id.cbFullTank)
        val btnSave: Button = view.findViewById(R.id.btnSave)

        val fuelRepository = ServiceLocator.fuelRepository
        val currentCar = ServiceLocator.currentCar

        if (currentCar == null) {
            Toast.makeText(
                requireContext(),
                "Сначала создайте профиль автомобиля",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigateUp()
            return
        }

        etOdometer.setText(currentCar.currentOdometerKm.toString())

        btnSave.setOnClickListener {
            val odometerText = etOdometer.text.toString().trim()
            val litersText = etLiters.text.toString().trim()
            val isFullTank = cbFullTank.isChecked

            if (odometerText.isEmpty() || litersText.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Заполните пробег и количество литров",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val odometerKm = try {
                odometerText.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    requireContext(),
                    "Некорректный формат пробега",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (odometerKm < currentCar.currentOdometerKm) {
                Toast.makeText(
                    requireContext(),
                    "Пробег не может быть меньше текущего (${currentCar.currentOdometerKm} км)",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val litersAdded = try {
                litersText.replace(',', '.').toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    requireContext(),
                    "Некорректный формат литров",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val carId = 1L
            val newEntry = FuelEntry(
                id = 0L,
                carId = carId,
                odometerKm = odometerKm,
                litersAdded = litersAdded,
                isFullTank = isFullTank
            )


            viewLifecycleOwner.lifecycleScope.launch {
                fuelRepository.addEntry(newEntry)

                val updatedCar = currentCar.copy(
                    currentOdometerKm = odometerKm
                )
                ServiceLocator.currentCar = updatedCar

                ServiceLocator.carLocalDataSource.saveCar(updatedCar)

                Toast.makeText(
                    requireContext(),
                    "Заправка добавлена",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigateUp()

            }

        }




    }


}