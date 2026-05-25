package com.diploma.fuelstats.presentation.refuels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diploma.fuelstats.R
import com.diploma.fuelstats.domain.model.FuelEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FuelEntriesAdapter : RecyclerView.Adapter<FuelEntriesAdapter.FuelEntryViewHolder>() {
    private var items: List<FuelEntry> = emptyList()

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun submitList(newItems: List<FuelEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): FuelEntry? =
        items.getOrNull(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fuel_entry, parent, false)
        return FuelEntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: FuelEntryViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, dateFormat)
    }

    override fun getItemCount(): Int = items.size

    class FuelEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPrimary: TextView = itemView.findViewById(R.id.tvPrimary)
        private val tvSecondary: TextView = itemView.findViewById(R.id.tvSecondary)

        fun bind(entry: FuelEntry, dateFormat: SimpleDateFormat) {
            val dateText = dateFormat.format(Date(entry.timestampMillis))

            val primaryText = "$dateText - ${entry.litersAdded} л"
            val fullText = if (entry.isFullTank) "Полный бак" else "Не полный бак"

            val secondaryText = "Пробег: ${entry.odometerKm} км - $fullText"

            tvPrimary.text = primaryText
            tvSecondary.text = secondaryText
        }
    }
}