package com.diploma.fuelstats.presentation.refuels

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diploma.fuelstats.R
import com.diploma.fuelstats.di.ServiceLocator
import com.diploma.fuelstats.domain.model.Car
import kotlinx.coroutines.launch
import java.util.UUID

class RefuelsListFragment : Fragment(R.layout.fragment_refuels_list) {

    private lateinit var adapter: FuelEntriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val infoTextView: TextView = view.findViewById(R.id.tvRefuelsInfo)
        val recyclerView: RecyclerView = view.findViewById(R.id.rvRefuels)
        val fabAddRefuel: View = view.findViewById(R.id.fabAddRefuel)

        adapter = FuelEntriesAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val divider = DividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        )

        ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)?.let { drawable ->
            divider.setDrawable(drawable)
        }

        recyclerView.addItemDecoration(divider)

        setupSwipeToDelete(infoTextView, recyclerView)

        fabAddRefuel.setOnClickListener {
            findNavController().navigate(
                R.id.action_refuelsListFragment_to_addRefuelFragment
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val carFromDb = ServiceLocator.carLocalDataSource.getCurrentCar()

            if (carFromDb != null) {
                ServiceLocator.currentCar = carFromDb
                updateList(infoTextView)
            } else {
                showInitialOdometerDialog(
                    onCarSaved = {
                        updateList(infoTextView)
                    }
                )
            }
        }
    }

    private fun setupSwipeToDelete(
        infoTextView: TextView,
        recyclerView: RecyclerView
    ) {
        val fuelRepository = ServiceLocator.fuelRepository

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return
                }

                val entry = adapter.getItemAt(position)
                if (entry == null) {
                    adapter.notifyItemChanged(position)
                    return
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Удалить заправку")
                    .setMessage("Вы уверены, что хотите удалить эту заправку?")
                    .setPositiveButton("Удалить") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            fuelRepository.deleteEntry(entry.id)

                            Toast.makeText(
                                requireContext(),
                                "Заправка удалена",
                                Toast.LENGTH_SHORT
                            ).show()

                            updateList(infoTextView)
                        }
                    }
                    .setNegativeButton("Отмена") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    private fun showInitialOdometerDialog(
        onCarSaved: () -> Unit
    ) {
        val input = EditText(requireContext()).apply {
            hint = "Например, 120000"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Начальный пробег")
            .setMessage(
                "Чтобы начать пользоваться приложением, введите текущий пробег автомобиля."
            )
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Сохранить", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            button.setOnClickListener {
                val text = input.text.toString().trim()
                val odometerKm = text.toIntOrNull()

                if (odometerKm == null || odometerKm < 0) {
                    input.error = "Введите корректный пробег"
                    return@setOnClickListener
                }

                val car = Car(
                    id = 1L,
                    brand = "",
                    model = "",
                    year = 0,
                    tankCapacityLiters = null,
                    currentOdometerKm = odometerKm,
                    vehicleType = "other",
                    syncVehicleId = UUID.randomUUID().toString()
                )

                ServiceLocator.currentCar = car

                viewLifecycleOwner.lifecycleScope.launch {
                    ServiceLocator.carLocalDataSource.saveCar(car)

                    Toast.makeText(
                        requireContext(),
                        "Начальный пробег сохранён",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                    onCarSaved()
                }
            }
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()

        view?.findViewById<TextView>(R.id.tvRefuelsInfo)?.let { infoTextView ->
            updateList(infoTextView)
        }
    }

    private fun updateList(infoTextView: TextView) {
        val fuelRepository = ServiceLocator.fuelRepository
        val carId = ServiceLocator.currentCar?.id ?: 1L

        viewLifecycleOwner.lifecycleScope.launch {
            val entries = fuelRepository.getEntries(carId)

            infoTextView.text = "Всего заправок: ${entries.size}"
            adapter.submitList(entries)

            val tvEmptyState = view?.findViewById<TextView>(R.id.tvEmptyState)
            val rvRefuels = view?.findViewById<RecyclerView>(R.id.rvRefuels)

            if (entries.isEmpty()) {
                tvEmptyState?.visibility = View.VISIBLE
                rvRefuels?.visibility = View.INVISIBLE
            } else {
                tvEmptyState?.visibility = View.GONE
                rvRefuels?.visibility = View.VISIBLE
            }
        }
    }
}