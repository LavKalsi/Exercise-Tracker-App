package com.example.gymtracker.ui.adapter

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtracker.data.HistoryEntry
import com.example.gymtracker.databinding.ItemHistoryEntryBinding

class HistoryEntryAdapter :
    ListAdapter<HistoryEntry, HistoryEntryAdapter.VH>(DIFF) {

    inner class VH(val b: ItemHistoryEntryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemHistoryEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = getItem(position)
        holder.b.tvName.text = entry.exerciseName
        if (entry.isCompleted) {
            val parts = mutableListOf("Done")
            if (entry.reps > 0) parts.add("${entry.reps} reps")
            if (entry.weight > 0f) {
                val w = if (entry.weight == entry.weight.toLong().toFloat())
                    entry.weight.toLong().toString() else String.format("%.1f", entry.weight)
                parts.add("${w}kg")
            }
            holder.b.tvStatus.text = parts.joinToString(" · ")
            holder.b.tvStatus.setTextColor(holder.itemView.context.getColor(com.example.gymtracker.R.color.color_done))
            holder.b.dot.setBackgroundResource(com.example.gymtracker.R.drawable.bg_chip_selected)
        } else {
            val outValue = TypedValue()
            holder.itemView.context.theme.resolveAttribute(com.example.gymtracker.R.attr.appOnSurfaceVariantColor, outValue, true)
            val variantColor = outValue.data

            holder.b.tvStatus.text = "Missed"
            holder.b.tvStatus.setTextColor(variantColor)
            holder.b.dot.setBackgroundColor(variantColor)
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<HistoryEntry>() {
            override fun areItemsTheSame(a: HistoryEntry, b: HistoryEntry) = a.id == b.id
            override fun areContentsTheSame(a: HistoryEntry, b: HistoryEntry) = a == b
        }
    }
}
