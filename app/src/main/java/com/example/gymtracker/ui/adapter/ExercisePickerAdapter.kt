package com.example.gymtracker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtracker.data.Exercise
import com.example.gymtracker.databinding.ItemExercisePickerBinding

class ExercisePickerAdapter(
    private val scheduledIds: () -> Set<Long>,
    private val onToggle: (Exercise) -> Unit
) : ListAdapter<Exercise, ExercisePickerAdapter.VH>(DIFF) {

    inner class VH(val b: ItemExercisePickerBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemExercisePickerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ex = getItem(position)
        holder.b.tvName.text = ex.name
        holder.b.tvMeta.text = "${ex.muscleGroup} · ${ex.equipment}"
        holder.b.tvCheck.visibility = if (scheduledIds().contains(ex.id)) View.VISIBLE else View.INVISIBLE
        holder.b.root.setOnClickListener { onToggle(ex) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Exercise>() {
            override fun areItemsTheSame(a: Exercise, b: Exercise) = a.id == b.id
            override fun areContentsTheSame(a: Exercise, b: Exercise) = a == b
        }
    }
}
