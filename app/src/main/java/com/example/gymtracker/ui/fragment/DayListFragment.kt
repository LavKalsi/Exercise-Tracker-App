package com.example.gymtracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymtracker.R
import com.example.gymtracker.databinding.FragmentDayListBinding
import com.example.gymtracker.ui.adapter.DayAdapter
import com.example.gymtracker.viewmodel.MainViewModel

class DayListFragment : Fragment() {

    private var _b: FragmentDayListBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentDayListBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = DayAdapter(vm.todayDayId) { day ->
            vm.openDayEditor(day.dayId)
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragmentContainer, DayEditorFragment())
                .addToBackStack("editor")
                .commit()
        }
        b.rvDays.layoutManager = LinearLayoutManager(requireContext())
        b.rvDays.adapter = adapter

        vm.dayLabels.observe(viewLifecycleOwner) { adapter.submitList(it) }

        b.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
