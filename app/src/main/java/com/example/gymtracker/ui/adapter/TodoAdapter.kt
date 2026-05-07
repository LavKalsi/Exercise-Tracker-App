package com.example.gymtracker.ui.adapter

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtracker.data.ScheduledExercise
import com.example.gymtracker.databinding.ItemTodoExerciseBinding

class TodoAdapter(private val onDone: (id: Long, reps: Int, weight: Float) -> Unit) :
    ListAdapter<ScheduledExercise, TodoAdapter.VH>(DIFF) {

    inner class VH(val b: ItemTodoExerciseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTodoExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ex = getItem(position)

        // Warmup badge
        if (ex.isWarmup) {
            holder.b.tvWarmupBadge.visibility = View.VISIBLE
            holder.b.tvName.text = ex.exerciseName
        } else {
            holder.b.tvWarmupBadge.visibility = View.GONE
            holder.b.tvName.text = ex.exerciseName
        }
        holder.b.tvMeta.text = "${ex.muscleGroup} · ${ex.equipment}"

        // Tap "Done" button — show reps+weight dialog via fragment manager
        holder.b.btnDone.setOnClickListener {
            val fm = (holder.b.root.context as? androidx.fragment.app.FragmentActivity)
                ?.supportFragmentManager ?: return@setOnClickListener
            com.example.gymtracker.ui.fragment.RepsInputDialog(
                exerciseName = ex.exerciseName,
                onConfirm = { reps, weight -> onDone(ex.id, reps, weight) },
                onSkip = { onDone(ex.id, 0, 0f) }
            ).show(fm, "reps_${ex.id}")
        }

        // Long-press anywhere on the card to start drag
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
