package com.example.gymtracker.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymtracker.databinding.FragmentDayEditorBinding
import com.example.gymtracker.ui.adapter.ExercisePickerAdapter
import com.example.gymtracker.ui.adapter.ScheduledExerciseAdapter
import com.google.android.material.snackbar.Snackbar

class DayEditorFragment : Fragment() {

    private var _b: FragmentDayEditorBinding? = null
    private val b get() = _b!!
    private val vm: com.example.gymtracker.viewmodel.MainViewModel by activityViewModels()

    private lateinit var searchAdapter: ExercisePickerAdapter
    private lateinit var dayAdapter: ScheduledExerciseAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentDayEditorBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Search results adapter — reads scheduled IDs live from VM
        searchAdapter = ExercisePickerAdapter(
            scheduledIds = { vm.editingScheduled.value?.map { it.exerciseId }?.toSet() ?: emptySet() },
            onToggle = { ex ->
                val msg = vm.toggleExercise(ex)
                Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()
                searchAdapter.notifyDataSetChanged() // refresh checkmarks
            }
        )
        b.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        b.rvSearchResults.adapter = searchAdapter

        // Day exercises adapter
        dayAdapter = ScheduledExerciseAdapter(
            onRemove = { exerciseId -> vm.removeFromDay(exerciseId) },
            onToggleWarmup = { scheduledId, isWarmup -> vm.setWarmupInEditor(scheduledId, isWarmup) }
        )
        b.rvDayExercises.layoutManager = LinearLayoutManager(requireContext())
        b.rvDayExercises.adapter = dayAdapter

        // Observe day title
        vm.editingDayId.observe(viewLifecycleOwner) { dayId ->
            val label = vm.dayLabels.value?.find { it.dayId == dayId }?.label ?: ""
            b.tvDayTitle.text = label
        }

        // Observe day's exercises
        vm.editingScheduled.observe(viewLifecycleOwner) { list ->
            dayAdapter.submitList(list)
            b.tvDividerLabel.text = "${b.tvDayTitle.text}'s exercises (${list.size})"
            b.tvDayEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            b.rvDayExercises.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            // Refresh search checkmarks when day list changes
            searchAdapter.notifyDataSetChanged()
        }

        // Search
        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { vm.search(s?.toString() ?: "") }
        })

        vm.searchResults.observe(viewLifecycleOwner) { results ->
            val query = b.etSearch.text?.toString() ?: ""
            if (query.isBlank()) {
                b.rvSearchResults.visibility = View.GONE
                b.tvNoResults.visibility = View.GONE
            } else if (results.isEmpty()) {
                b.rvSearchResults.visibility = View.GONE
                b.tvNoResults.visibility = View.VISIBLE
            } else {
                searchAdapter.submitList(results)
                b.rvSearchResults.visibility = View.VISIBLE
                b.tvNoResults.visibility = View.GONE
            }
        }

        // Back
        b.btnBack.setOnClickListener {
            vm.closeDayEditor()
            parentFragmentManager.popBackStack()
        }

        // Create new exercise
        b.btnCreate.setOnClickListener {
            CreateExerciseDialog { name, muscle, equip ->
                val msg = vm.createExercise(name, muscle, equip)
                Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()
            }.show(parentFragmentManager, "create")
        }

        // See all exercises
        b.btnSeeAll.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    com.example.gymtracker.R.anim.slide_in_right,
                    com.example.gymtracker.R.anim.slide_out_left,
                    com.example.gymtracker.R.anim.slide_in_left,
                    com.example.gymtracker.R.anim.slide_out_right
                )
                .replace(com.example.gymtracker.R.id.fragmentContainer, AllExercisesFragment())
                .addToBackStack("all")
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
