package com.example.gymtracker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtracker.data.DayLabel
import com.example.gymtracker.databinding.ItemDayBinding

class DayAdapter(
    private val todayDayId: Int,
    private val onClick: (DayLabel) -> Unit
) : ListAdapter<DayLabel, DayAdapter.VH>(DIFF) {

    inner class VH(val b: ItemDayBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val day = getItem(position)
        holder.b.tvDayName.text = day.label
        holder.b.tvToday.visibility = if (day.dayId == todayDayId) View.VISIBLE else View.GONE
        holder.b.root.setOnClickListener { onClick(day) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<DayLabel>() {
            override fun areItemsTheSame(a: DayLabel, b: DayLabel) = a.dayId == b.dayId
            override fun areContentsTheSame(a: DayLabel, b: DayLabel) = a == b
        }
    }
}
