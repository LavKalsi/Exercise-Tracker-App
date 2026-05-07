package com.example.gymtracker.ui.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymtracker.R
import com.example.gymtracker.databinding.FragmentHistoryBinding
import com.example.gymtracker.ui.adapter.HistoryEntryAdapter
import com.example.gymtracker.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class HistoryFragment : Fragment() {

    private var _b: FragmentHistoryBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    private val entryAdapter = HistoryEntryAdapter()
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy")
    private var displayMonth = YearMonth.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentHistoryBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.rvEntries.layoutManager = LinearLayoutManager(requireContext())
        b.rvEntries.adapter = entryAdapter

        buildDowHeaders()
        renderCalendar()

        b.btnPrevMonth.setOnClickListener {
            displayMonth = displayMonth.minusMonths(1)
            renderCalendar()
        }
        b.btnNextMonth.setOnClickListener {
            if (displayMonth < YearMonth.now()) {
                displayMonth = displayMonth.plusMonths(1)
                renderCalendar()
            }
        }

        vm.historyDates.observe(viewLifecycleOwner) { renderCalendar() }

        // Re-render calendar whenever selected date changes so highlight moves
        vm.selectedDate.observe(viewLifecycleOwner) { renderCalendar() }

        vm.historyEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isEmpty()) {
                b.detailPanel.visibility = View.GONE
            } else {
                b.detailPanel.visibility = View.VISIBLE
                val planned   = entries.size
                val completed = entries.count { it.isCompleted }
                val rate      = if (planned > 0) completed * 100 / planned else 0
                b.tvDetailDate.text = vm.selectedDate.value ?: ""
                b.tvPlanned.text    = planned.toString()
                b.tvCompleted.text  = completed.toString()
                b.tvRate.text       = "$rate%"
                entryAdapter.submitList(entries)
            }
        }

        vm.refreshHistory()
    }

    private fun buildDowHeaders() {
        val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
        val outValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.appOnSurfaceVariantColor, outValue, true)
        val variantColor = outValue.data

        days.forEach { d ->
            val tv = TextView(requireContext()).apply {
                text = d
                textSize = 11f
                setTextColor(variantColor)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            b.rowDowHeaders.addView(tv)
        }
    }

    private fun renderCalendar() {
        b.tvMonthYear.text = displayMonth.format(monthFmt)
        b.calendarGrid.removeAllViews()

        val historyDates = vm.historyDates.value ?: emptySet()
        val selectedDate = vm.selectedDate.value ?: ""
        val today        = LocalDate.now()
        val firstDay     = displayMonth.atDay(1)
        val startOffset  = firstDay.dayOfWeek.value - 1  // Mon=0
        val daysInMonth  = displayMonth.lengthOfMonth()
        val totalCells   = startOffset + daysInMonth
        val rows         = (totalCells + 6) / 7
        val ctx          = requireContext()
        val cellSize     = (resources.displayMetrics.widthPixels - dpToPx(60)) / 7

        val outValue = TypedValue()
        ctx.theme.resolveAttribute(R.attr.appOnSurfaceColor, outValue, true)
        val onSurfaceColor = outValue.data

        for (row in 0 until rows) {
            for (col in 0 until 7) {
                val cellIndex = row * 7 + col
                val dayNum    = cellIndex - startOffset + 1

                val spec = GridLayout.spec(row, 1f)
                val colSpec = GridLayout.spec(col, 1f)
                val params = GridLayout.LayoutParams(spec, colSpec).apply {
                    width  = 0
                    height = cellSize
                    setMargins(2, 2, 2, 2)
                }

                if (dayNum < 1 || dayNum > daysInMonth) {
                    val empty = View(ctx)
                    empty.layoutParams = params
                    b.calendarGrid.addView(empty)
                    continue
                }

                val date    = displayMonth.atDay(dayNum)
                val dateKey = date.format(dateFmt)
                val hasData = historyDates.contains(dateKey)
                val isSel   = dateKey == selectedDate
                val isToday = date == today

                val cell = TextView(ctx).apply {
                    text = dayNum.toString()
                    textSize = 12f
                    gravity = Gravity.CENTER
                    layoutParams = params
                    setTextColor(when {
                        isSel   -> Color.WHITE
                        isToday -> ContextCompat.getColor(ctx, R.color.slate_primary)
                        else    -> onSurfaceColor
                    })
                    if (isSel) {
                        setBackgroundResource(R.drawable.bg_chip_selected)
                    } else if (isToday) {
                        setBackgroundResource(R.drawable.bg_today_indicator)
                    }
                    if (hasData && !isSel) {
                        // Underline dot via compound drawable would need a custom view;
                        // use bold as a simple indicator
                        setTypeface(null, Typeface.BOLD)
                    }
                    setOnClickListener { vm.selectDate(dateKey) }
                }
                b.calendarGrid.addView(cell)
            }
        }
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
