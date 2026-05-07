package com.example.gymtracker.ui.adapter

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtracker.data.ScheduledExercise
import com.example.gymtracker.databinding.ItemDoneExerciseBinding

class DoneAdapter(private val onUndo: (Long) -> Unit) :
    ListAdapter<ScheduledExercise, DoneAdapter.VH>(DIFF) {

    inner class VH(val b: ItemDoneExerciseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemDoneExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ex = getItem(position)
        holder.b.tvName.text   = ex.exerciseName
        holder.b.tvMuscle.text = buildString {
            append(ex.muscleGroup)
            if (ex.reps > 0) append(" · ${ex.reps} reps")
            if (ex.weight > 0f) {
                val w = if (ex.weight == ex.weight.toLong().toFloat())
                    ex.weight.toLong().toString() else String.format("%.1f", ex.weight)
                append(" · ${w}kg")
            }
        }

        // Tap undo icon
        holder.b.btnUndo.setOnClickListener { onUndo(ex.id) }

        // Long-press to drag back to Todo panel
        holder.b.root.setOnLongClickListener { view ->
            val clipData = ClipData.newPlainText("scheduledId", ex.id.toString())
            val shadow   = View.DragShadowBuilder(view)
            view.startDragAndDrop(clipData, shadow, ex.id, 0)
            true
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ScheduledExercise>() {
            override fun areItemsTheSame(a: ScheduledExercise, b: ScheduledExercise) = a.id == b.id
            override fun areContentsTheSame(a: ScheduledExercise, b: ScheduledExercise) = a == b
        }
    }
}
