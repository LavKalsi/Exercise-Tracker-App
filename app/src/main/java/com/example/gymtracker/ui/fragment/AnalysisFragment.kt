package com.example.gymtracker.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.gymtracker.R
import com.example.gymtracker.databinding.FragmentAnalysisBinding
import com.example.gymtracker.viewmodel.AnalysisBar
import com.example.gymtracker.viewmodel.AnalysisRange
import com.example.gymtracker.viewmodel.ExerciseStat
import com.example.gymtracker.viewmodel.MainViewModel
import com.example.gymtracker.viewmodel.MuscleSlice
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class AnalysisFragment : Fragment() {

    private var _b: FragmentAnalysisBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    // Palette for charts — works on dark and light backgrounds
    private val chartColors = intArrayOf(
        Color.parseColor("#64748B"), // slate
        Color.parseColor("#3B82F6"), // blue
        Color.parseColor("#10B981"), // emerald
        Color.parseColor("#F59E0B"), // amber
        Color.parseColor("#EF4444"), // red
        Color.parseColor("#8B5CF6"), // violet
        Color.parseColor("#EC4899"), // pink
        Color.parseColor("#06B6D4")  // cyan
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentAnalysisBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRangeSelector()
        observeData()
        vm.refreshAnalysis()
    }

    // ── Range selector ────────────────────────────────────────────────────────

    private fun setupRangeSelector() {
        b.rangeDay.setOnClickListener   { vm.setRange(AnalysisRange.DAY) }
        b.rangeMonth.setOnClickListener { vm.setRange(AnalysisRange.MONTH) }
        b.rangeYear.setOnClickListener  { vm.setRange(AnalysisRange.YEAR) }

        vm.analysisRange.observe(viewLifecycleOwner) { range ->
            val variantColor = resolveAttrColor(R.attr.appOnSurfaceVariantColor)
            listOf(
                b.rangeDay   to AnalysisRange.DAY,
                b.rangeMonth to AnalysisRange.MONTH,
                b.rangeYear  to AnalysisRange.YEAR
            ).forEach { (tv, r) ->
                if (r == range) {
                    tv.setBackgroundResource(R.drawable.bg_nav_selected)
                    tv.setTextColor(Color.WHITE)
                } else {
                    tv.setBackgroundColor(Color.TRANSPARENT)
                    tv.setTextColor(variantColor)
                }
            }
        }
    }

    // ── Observe all LiveData ──────────────────────────────────────────────────

    private fun observeData() {
        vm.analysisSummary.observe(viewLifecycleOwner) { s ->
            b.tvSummaryTotal.text = s.total.toString()
            b.tvSummaryBest.text  = "${s.bestLabel}\n(${s.bestValue})"
            b.tvSummaryAvg.text   = String.format("%.1f", s.average)
            b.tvSummaryReps.text  = formatLargeNumber(s.totalReps)
            b.tvStreak.text       = s.streak.toString()
        }

        vm.analysisBars.observe(viewLifecycleOwner) { bars ->
            renderBarChart(b.barChartWorkouts, bars, "Exercises", chartColors[0])
        }

        vm.repsBars.observe(viewLifecycleOwner) { bars ->
            renderBarChart(b.barChartReps, bars, "Reps", chartColors[1])
        }

        vm.muscleBreakdown.observe(viewLifecycleOwner) { slices ->
            renderPieChart(slices)
            renderHorizontalBarChart(slices)
        }

        vm.topExercises.observe(viewLifecycleOwner) { stats ->
            renderTopExercises(stats)
        }
    }

    // ── Bar chart (workouts or reps) ──────────────────────────────────────────

    private fun renderBarChart(chart: BarChart, bars: List<AnalysisBar>, label: String, color: Int) {
        if (bars.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }

        val textColor = resolveAttrColor(R.attr.appOnSurfaceVariantColor)
        val entries   = bars.mapIndexed { i, bar -> BarEntry(i.toFloat(), bar.value.toFloat()) }
        val labels    = bars.map { it.label }

        val dataSet = BarDataSet(entries, label).apply {
            this.color = color
            valueTextColor = resolveAttrColor(R.attr.appOnSurfaceColor)
            valueTextSize  = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) =
                    if (value == 0f) "" else value.toInt().toString()
            }
        }

        chart.apply {
            data = BarData(dataSet).apply { barWidth = 0.55f }
            description.isEnabled = false
            legend.isEnabled      = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setExtraOffsets(8f, 16f, 8f, 8f)
            setNoDataText("No data yet")
            setNoDataTextColor(textColor)

            xAxis.apply {
                position          = XAxis.XAxisPosition.BOTTOM
                granularity       = 1f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                valueFormatter    = IndexAxisValueFormatter(labels)
                setTextColor(this@AnalysisFragment.resolveAttrColor(R.attr.appOnSurfaceVariantColor))
                setTextSize(10f)
                labelCount        = labels.size
            }
            axisLeft.apply {
                setDrawGridLines(true)
                setGridColor(Color.argb(40, 148, 163, 184))
                setDrawAxisLine(false)
                setTextColor(this@AnalysisFragment.resolveAttrColor(R.attr.appOnSurfaceVariantColor))
                setTextSize(10f)
                axisMinimum       = 0f
                granularity       = 1f
            }
            axisRight.isEnabled = false

            animateY(600, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    // ── Pie chart ─────────────────────────────────────────────────────────────

    private fun renderPieChart(slices: List<MuscleSlice>) {
        if (slices.isEmpty()) {
            b.pieChartMuscle.visibility = View.GONE
            b.tvPieEmpty.visibility     = View.VISIBLE
            return
        }
        b.pieChartMuscle.visibility = View.VISIBLE
        b.tvPieEmpty.visibility     = View.GONE

        val entries = slices.mapIndexed { i, s ->
            PieEntry(s.count.toFloat(), s.muscle)
        }
        val colors = slices.indices.map { chartColors[it % chartColors.size] }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors          = colors
            sliceSpace           = 2f
            selectionShift       = 6f
            valueTextSize        = 11f
            valueTextColor       = Color.WHITE
            valueFormatter       = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) =
                    if (value < 1f) "" else "${value.toInt()}"
            }
        }

        b.pieChartMuscle.apply {
            data = PieData(dataSet)
            description.isEnabled  = false
            isDrawHoleEnabled      = true
            holeRadius             = 42f
            transparentCircleRadius = 47f
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.TRANSPARENT)
            setDrawCenterText(true)
            centerText             = "Muscle\nFocus"
            setCenterTextColor(resolveAttrColor(R.attr.appOnSurfaceColor))
            setCenterTextSize(13f)
            legend.apply {
                isEnabled   = true
                textColor   = resolveAttrColor(R.attr.appOnSurfaceVariantColor)
                textSize    = 11f
                formSize    = 10f
                xEntrySpace = 12f
            }
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(10f)
            animateY(700, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    // ── Horizontal bar chart — reps per muscle ────────────────────────────────

    private fun renderHorizontalBarChart(slices: List<MuscleSlice>) {
        // We need reps per muscle — query from topExercises isn't enough,
        // so we use the muscle breakdown counts as a proxy (completions, not reps).
        // The VM will push repsByMuscle when available; for now use slice counts.
        if (slices.isEmpty()) {
            b.hBarChartMuscleReps.visibility = View.GONE
            return
        }
        b.hBarChartMuscleReps.visibility = View.VISIBLE

        val sorted  = slices.sortedBy { it.count }
        val entries = sorted.mapIndexed { i, s -> BarEntry(i.toFloat(), s.count.toFloat()) }
        val labels  = sorted.map { it.muscle }
        val colors  = sorted.indices.map { chartColors[it % chartColors.size] }

        val dataSet = BarDataSet(entries, "Completions").apply {
            this.colors    = colors
            valueTextColor = resolveAttrColor(R.attr.appOnSurfaceColor)
            valueTextSize  = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) =
                    if (value == 0f) "" else value.toInt().toString()
            }
        }

        b.hBarChartMuscleReps.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled      = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setExtraOffsets(8f, 8f, 24f, 8f)

            xAxis.apply {
                position       = XAxis.XAxisPosition.BOTTOM_INSIDE
                granularity    = 1f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                valueFormatter = IndexAxisValueFormatter(labels)
                setTextColor(this@AnalysisFragment.resolveAttrColor(R.attr.appOnSurfaceVariantColor))
                setTextSize(10f)
                labelCount     = labels.size
            }
            axisLeft.apply {
                setDrawGridLines(true)
                setGridColor(Color.argb(40, 148, 163, 184))
                setDrawAxisLine(false)
                setTextColor(this@AnalysisFragment.resolveAttrColor(R.attr.appOnSurfaceVariantColor))
                setTextSize(10f)
                axisMinimum    = 0f
                granularity    = 1f
            }
            axisRight.isEnabled = false

            animateX(600, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    // ── Top exercises list ────────────────────────────────────────────────────

    private fun renderTopExercises(stats: List<ExerciseStat>) {
        b.containerTopExercises.removeAllViews()
        if (stats.isEmpty()) {
            b.tvTopEmpty.visibility = View.VISIBLE
            return
        }
        b.tvTopEmpty.visibility = View.GONE

        val maxCompletions = stats.maxOf { it.completions }.coerceAtLeast(1)
        val textColor    = resolveAttrColor(R.attr.appOnSurfaceColor)
        val variantColor = resolveAttrColor(R.attr.appOnSurfaceVariantColor)

        stats.forEachIndexed { index, stat ->
            val barColor = chartColors[index % chartColors.size]
            val fraction = stat.completions.toFloat() / maxCompletions

            val row = LinearLayout(requireContext()).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 14 }
            }

            // Name row
            val nameRow = LinearLayout(requireContext()).apply {
                orientation  = LinearLayout.HORIZONTAL
                gravity      = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 6 }
            }

            nameRow.addView(TextView(requireContext()).apply {
                text      = "#${index + 1}"
                textSize  = 11f
                setTextColor(barColor)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 8 }
            })

            nameRow.addView(TextView(requireContext()).apply {
                text      = stat.name
                textSize  = 13f
                setTextColor(textColor)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                maxLines  = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            })

            nameRow.addView(TextView(requireContext()).apply {
                text = "${stat.completions}×" +
                    if (stat.totalReps > 0) " · ${formatLargeNumber(stat.totalReps)} reps" else ""
                textSize = 11f
                setTextColor(variantColor)
                gravity  = Gravity.END
            })

            row.addView(nameRow)

            // Progress bar
            val barContainer = LinearLayout(requireContext()).apply {
                orientation  = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 6
                ).apply { bottomMargin = 2 }
            }
            barContainer.addView(View(requireContext()).apply {
                setBackgroundColor(barColor)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, fraction)
            })
            barContainer.addView(View(requireContext()).apply {
                setBackgroundColor(Color.argb(30, 100, 116, 139))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f - fraction)
            })
            row.addView(barContainer)

            b.containerTopExercises.addView(row)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun resolveAttrColor(attr: Int): Int {
        val outValue = TypedValue()
        requireContext().theme.resolveAttribute(attr, outValue, true)
        return outValue.data
    }

    private fun formatLargeNumber(n: Int): String = when {
        n >= 1_000_000 -> String.format("%.1fM", n / 1_000_000f)
        n >= 1_000     -> String.format("%.1fK", n / 1_000f)
        else           -> n.toString()
    }

    override fun onResume() {
        super.onResume()
        vm.refreshAnalysis()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
