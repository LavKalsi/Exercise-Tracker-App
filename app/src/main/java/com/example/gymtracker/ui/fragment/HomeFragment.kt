package com.example.gymtracker.ui.fragment

import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymtracker.R
import com.example.gymtracker.databinding.FragmentHomeBinding
import com.example.gymtracker.ui.adapter.DoneAdapter
import com.example.gymtracker.ui.adapter.TodoAdapter
import com.example.gymtracker.viewmodel.MainViewModel

class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    // Track which IDs belong to which list so the drop targets know what to do
    private val todoIds = mutableSetOf<Long>()
    private val doneIds = mutableSetOf<Long>()

    private val todoAdapter = TodoAdapter { id, reps, weight -> vm.markCompletedWithRepsAndWeight(id, reps, weight) }
    private val doneAdapter = DoneAdapter { vm.markPending(it) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.rvTodo.layoutManager = LinearLayoutManager(requireContext())
        b.rvTodo.adapter = todoAdapter

        b.rvDone.layoutManager = LinearLayoutManager(requireContext())
        b.rvDone.adapter = doneAdapter

        vm.todayExercises.observe(viewLifecycleOwner) { list ->
            val todo = list.filter { !it.isCompleted }
            val done = list.filter { it.isCompleted }

            todoIds.clear(); todoIds.addAll(todo.map { it.id })
            doneIds.clear(); doneIds.addAll(done.map { it.id })

            todoAdapter.submitList(todo)
            b.rvTodo.visibility      = if (todo.isEmpty()) View.GONE    else View.VISIBLE
            b.tvTodoEmpty.visibility = if (todo.isEmpty()) View.VISIBLE else View.GONE
            b.tvTodoCount.text       = todo.size.toString()

            doneAdapter.submitList(done)
            b.rvDone.visibility      = if (done.isEmpty()) View.GONE    else View.VISIBLE
            b.tvDoneEmpty.visibility = if (done.isEmpty()) View.VISIBLE else View.GONE
            b.tvDoneCount.text       = done.size.toString()
        }

        // ── Done panel: accepts drags from Todo ───────────────────────────────
        b.panelDone.setOnDragListener { _, event ->
            val id = event.clipData?.getItemAt(0)?.text?.toString()?.toLongOrNull()
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Only highlight if the dragged item is a todo item
                    if (id != null && todoIds.contains(id)) {
                        b.panelDone.setBackgroundResource(R.drawable.bg_glass_panel_highlight)
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    if (id != null && todoIds.contains(id))
                        b.panelDone.setBackgroundResource(R.drawable.bg_glass_panel_highlight)
                    true
                }
                DragEvent.ACTION_DRAG_EXITED,
                DragEvent.ACTION_DRAG_ENDED -> {
                    b.panelDone.setBackgroundResource(R.drawable.bg_glass_panel)
                    true
                }
                DragEvent.ACTION_DROP -> {
                    b.panelDone.setBackgroundResource(R.drawable.bg_glass_panel)
                    if (id != null && todoIds.contains(id)) {
                        // Show reps dialog for drag-to-done as well
                        val ex = vm.todayExercises.value?.find { it.id == id }
                        val name = ex?.exerciseName ?: ""
                        com.example.gymtracker.ui.fragment.RepsInputDialog(
                            exerciseName = name,
                            onConfirm = { reps, weight -> vm.markCompletedWithRepsAndWeight(id, reps, weight) },
                            onSkip = { vm.markCompletedWithRepsAndWeight(id, 0, 0f) }
                        ).show(parentFragmentManager, "reps_drag_$id")
                    }
                    true
                }
                else -> false
            }
        }

        // ── Todo panel: accepts drags back from Done ──────────────────────────
        b.panelTodo.setOnDragListener { _, event ->
            val id = event.clipData?.getItemAt(0)?.text?.toString()?.toLongOrNull()
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (id != null && doneIds.contains(id)) {
                        b.panelTodo.setBackgroundResource(R.drawable.bg_glass_panel_highlight)
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    if (id != null && doneIds.contains(id))
                        b.panelTodo.setBackgroundResource(R.drawable.bg_glass_panel_highlight)
                    true
                }
                DragEvent.ACTION_DRAG_EXITED,
                DragEvent.ACTION_DRAG_ENDED -> {
                    b.panelTodo.setBackgroundResource(R.drawable.bg_glass_panel)
                    true
                }
                DragEvent.ACTION_DROP -> {
                    b.panelTodo.setBackgroundResource(R.drawable.bg_glass_panel)
                    if (id != null && doneIds.contains(id)) {
                        vm.markPending(id)
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshToday()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
