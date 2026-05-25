package com.diploma.fuelstats.presentation.car

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.diploma.fuelstats.R
import com.diploma.fuelstats.di.ServiceLocator
import com.diploma.fuelstats.domain.model.Car
import kotlinx.coroutines.launch
import java.util.UUID

class CarProfileEditFragment : Fragment(R.layout.fragment_car_profile_edit) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etBrand: EditText = view.findViewById(R.id.etBrand)
        val etModel: EditText = view.findViewById(R.id.etModel)
        val etCarYear: EditText = view.findViewById(R.id.etCarYear)
        val etTankCapacity: EditText = view.findViewById(R.id.etTankCapacity)
        val etCurrentOdometer: EditText = view.findViewById(R.id.etCurrentOdometer)
        val spVehicleType: Spinner = view.findViewById(R.id.spVehicleType)
        val btnSave: Button = view.findViewById(R.id.btnSaveCarProfile)

        val existingCar = ServiceLocator.currentCar

        val vehicleTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            VehicleTypeUi.options
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spVehicleType.adapter = vehicleTypeAdapter

        if (existingCar != null) {
            etBrand.setText(existingCar.brand)
            etModel.setText(existingCar.model)
            etCarYear.setText(existingCar.year.toString())
            etCurrentOdometer.setText(existingCar.currentOdometerKm.toString())
            etTankCapacity.setText(existingCar.tankCapacityLiters?.toString() ?: "")


            spVehicleType.setSelection(
                VehicleTypeUi.indexOf(existingCar.vehicleType)
            )
        }

        btnSave.setOnClickListener {
            val brandText = etBrand.text.toString().trim()
            val modelText = etModel.text.toString().trim()
            val yearText = etCarYear.text.toString().trim()
            val tankText = etTankCapacity.text.toString().trim()
            val odometerText = etCurrentOdometer.text.toString().trim()

            if (brandText.isEmpty() || modelText.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Заполните марку и модель автомобиля",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val year = try {
                yearText.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    requireContext(),
                    "Некорректный год выпуска",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val newOdometerKm = try {
                odometerText.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    requireContext(),
                    "Некорректный пробег",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (newOdometerKm < 0) {
                Toast.makeText(
                    requireContext(),
                    "Пробег не может быть отрицательным",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val tankCapacityLiters: Double? = if (tankText.isEmpty()) {
                null
            } else {
                try {
                    tankText.replace(',', '.').toDouble()
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        requireContext(),
                        "Некорректный объём бака",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }

            val selectedVehicleType = (spVehicleType.selectedItem as VehicleTypeOption).value

            val carId = existingCar?.id ?: 1L

            val syncVehicleId = existingCar?.syncVehicleId
                ?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()

            viewLifecycleOwner.lifecycleScope.launch {
                val maxOdomFromEntries = if (existingCar != null) {
                    ServiceLocator.fuelRepository
                        .getEntries(existingCar.id)
                        .maxOfOrNull { it.odometerKm } ?: 0
                } else {
                    0
                }

                if (newOdometerKm < maxOdomFromEntries) {
                    Toast.makeText(
                        requireContext(),
                        "Пробег не может быть меньше максимального из заправок ($maxOdomFromEntries км)",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val updatedCar = Car(
                    id = carId,
                    brand = brandText,
                    model = modelText,
                    year = year,
                    tankCapacityLiters = tankCapacityLiters,
                    currentOdometerKm = newOdometerKm,
                    vehicleType = selectedVehicleType,
                    syncVehicleId = syncVehicleId
                )

                ServiceLocator.carLocalDataSource.saveCar(updatedCar)

                val savedCar = ServiceLocator.carLocalDataSource.getCurrentCar()
                ServiceLocator.currentCar = savedCar ?: updatedCar

                Toast.makeText(
                    requireContext(),
                    "Профиль автомобиля сохранён",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().popBackStack()
            }
        }
    }
}