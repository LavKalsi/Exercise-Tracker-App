package com.example.gymtracker.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymtracker.databinding.FragmentAllExercisesBinding
import com.example.gymtracker.ui.adapter.ExercisePickerAdapter
import com.google.android.material.snackbar.Snackbar

class AllExercisesFragment : Fragment() {

    private var _b: FragmentAllExercisesBinding? = null
    private val b get() = _b!!
    private val vm: com.example.gymtracker.viewmodel.MainViewModel by activityViewModels()
    private lateinit var adapter: ExercisePickerAdapter

    private val filters = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Core", "Cardio")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentAllExercisesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ExercisePickerAdapter(
            scheduledIds = { vm.editingScheduled.value?.map { it.exerciseId }?.toSet() ?: emptySet() },
            onToggle = { ex ->
                val msg = vm.toggleExercise(ex)
                Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
        )

        b.rvAll.layoutManager = LinearLayoutManager(requireContext())
        b.rvAll.adapter = adapter

        vm.filteredExercises.observe(viewLifecycleOwner) { adapter.submitList(it) }
        vm.editingScheduled.observe(viewLifecycleOwner) { adapter.notifyDataSetChanged() }
        
        vm.selectedFilterMuscle.observe(viewLifecycleOwner) { buildFilterChips() }

        b.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun buildFilterChips() {
        val container = b.filterContainer
        container.removeAllViews()
        
        val ctx = requireContext()
        val currentFilter = vm.selectedFilterMuscle.value ?: "All"
        
        val dp12 = (12 * ctx.resources.displayMetrics.density).toInt()
        val dp8 = (8 * ctx.resources.displayMetrics.density).toInt()
        val dp6 = (6 * ctx.resources.displayMetrics.density).toInt()

        val outValue = TypedValue()
        ctx.theme.resolveAttribute(com.example.gymtracker.R.attr.appOnSurfaceVariantColor, outValue, true)
        val variantColor = outValue.data

        filters.forEach { filter ->
            val isSelected = filter == currentFilter
            val tv = TextView(ctx).apply {
                text = filter
                textSize = 12f
                setPadding(dp12, dp8, dp12, dp8)
                setTextColor(if (isSelected) Color.WHITE else variantColor)
                setBackgroundResource(
                    if (isSelected) com.example.gymtracker.R.drawable.bg_chip_selected
                    else com.example.gymtracker.R.drawable.bg_chip_unselected
                )
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = dp6
                layoutParams = lp
                setOnClickListener { vm.setMuscleFilter(filter) }
            }
            container.addView(tv)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
