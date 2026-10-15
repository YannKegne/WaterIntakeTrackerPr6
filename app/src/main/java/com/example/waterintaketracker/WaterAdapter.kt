package com.example.waterintaketracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Binds WaterEntry items to item_water_entry.xml rows. */
class WaterAdapter : ListAdapter<WaterEntry, WaterAdapter.WaterViewHolder>(DIFF) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    class WaterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val amountText: TextView = view.findViewById(R.id.amountText)
        val notesText: TextView = view.findViewById(R.id.notesText)
        val timestampText: TextView = view.findViewById(R.id.timestampText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_water_entry, parent, false)
        return WaterViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaterViewHolder, position: Int) {
        val entry = getItem(position)
        holder.amountText.text = holder.itemView.context.getString(
            R.string.amount_format, formatOz(entry.amountOz)
        )
        if (entry.notes.isNullOrBlank()) {
            holder.notesText.visibility = View.GONE
        } else {
            holder.notesText.visibility = View.VISIBLE
            holder.notesText.text = entry.notes
        }
        holder.timestampText.text = dateFormat.format(Date(entry.timestamp))
    }

    companion object {
        fun formatOz(value: Double): String =
            if (value % 1.0 == 0.0) value.toLong().toString() else String.format(Locale.getDefault(), "%.1f", value)

        private val DIFF = object : DiffUtil.ItemCallback<WaterEntry>() {
            override fun areItemsTheSame(oldItem: WaterEntry, newItem: WaterEntry) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: WaterEntry, newItem: WaterEntry) = oldItem == newItem
        }
    }
}
