package com.example.gymtracker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtracker.data.ScheduledExercise
import com.example.gymtracker.databinding.ItemScheduledExerciseBinding

class ScheduledExerciseAdapter(
    private val onRemove: (Long) -> Unit,
    private val onToggleWarmup: (scheduledId: Long, isWarmup: Boolean) -> Unit = { _, _ -> }
) : ListAdapter<ScheduledExercise, ScheduledExerciseAdapter.VH>(DIFF) {

    inner class VH(val b: ItemScheduledExerciseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemScheduledExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ex = getItem(position)
        holder.b.tvName.text = ex.exerciseName
        holder.b.tvMeta.text = "${ex.muscleGroup} · ${ex.equipment}"

        // Warmup badge
        holder.b.tvWarmupBadge.visibility = if (ex.isWarmup) View.VISIBLE else View.GONE

        // Warmup button — filled style when active
        holder.b.btnWarmup.alpha = if (ex.isWarmup) 1.0f else 0.4f
        holder.b.btnWarmup.setOnClickListener {
            onToggleWarmup(ex.id, !ex.isWarmup)
        }

        holder.b.btnRemove.setOnClickListener { onRemove(ex.exerciseId) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ScheduledExercise>() {
            override fun areItemsTheSame(a: ScheduledExercise, b: ScheduledExercise) = a.id == b.id
            override fun areContentsTheSame(a: ScheduledExercise, b: ScheduledExercise) = a == b
        }
    }
}
