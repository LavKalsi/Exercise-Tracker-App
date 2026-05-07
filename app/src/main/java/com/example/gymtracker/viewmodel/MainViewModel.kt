package com.example.gymtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gymtracker.data.BgStyle
import com.example.gymtracker.data.DayLabel
import com.example.gymtracker.data.Exercise
import com.example.gymtracker.data.ExerciseRepository
import com.example.gymtracker.data.HistoryEntry
import com.example.gymtracker.data.ScheduledExercise
import com.example.gymtracker.data.ThemePreset
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExerciseRepository(application)
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ── Today ─────────────────────────────────────────────────────────────────
    val todayDayId: Int = LocalDate.now().dayOfWeek.value   // Mon=1 … Sun=7
    val todayName: String = LocalDate.now().dayOfWeek.name
        .lowercase().replaceFirstChar { it.uppercase() }

    // ── Theme ─────────────────────────────────────────────────────────────────
    private val _theme = MutableLiveData(
        ThemePreset.valueOf(repo.getSetting("theme", ThemePreset.SLATE.name))
    )
    val theme: LiveData<ThemePreset> = _theme
    fun setTheme(preset: ThemePreset) { _theme.value = preset; repo.setSetting("theme", preset.name) }

    private val _bgStyle = MutableLiveData(
        BgStyle.valueOf(repo.getSetting("bg_style", BgStyle.DARK.name))
    )
    val bgStyle: LiveData<BgStyle> = _bgStyle
    fun setBgStyle(style: BgStyle) { _bgStyle.value = style; repo.setSetting("bg_style", style.name) }

    // ── Home – today's exercises ──────────────────────────────────────────────
    private val _todayExercises = MutableLiveData<List<ScheduledExercise>>(emptyList())
    val todayExercises: LiveData<List<ScheduledExercise>> = _todayExercises

    fun refreshToday() {
        checkDailyReset()
        _todayExercises.value = repo.getScheduledForDay(todayDayId)
    }

    fun markCompleted(id: Long) {
        repo.setExerciseCompleted(id, true)
        refreshToday()
        snapshotToday()
    }

    fun markCompletedWithRepsAndWeight(id: Long, reps: Int, weight: Float) {
        repo.setExerciseCompletedWithRepsAndWeight(id, true, reps, weight)
        refreshToday()
        snapshotToday()
    }

    fun markPending(id: Long) {
        repo.setExerciseCompleted(id, false)
        refreshToday()
        snapshotToday()
    }

    fun toggleWarmup(scheduledId: Long) {
        val current = _todayExercises.value?.find { it.id == scheduledId }?.isWarmup ?: false
        repo.setWarmup(scheduledId, !current)
        refreshToday()
    }

    fun setWarmupInEditor(scheduledId: Long, isWarmup: Boolean) {
        repo.setWarmup(scheduledId, isWarmup)
        val dayId = _editingDayId.value ?: return
        _editingScheduled.value = repo.getScheduledForDay(dayId)
    }

    private fun snapshotToday() {
        repo.snapshotDayToHistory(LocalDate.now().format(dateFmt), todayDayId)
    }

    // ── Settings – day list ───────────────────────────────────────────────────
    private val _dayLabels = MutableLiveData<List<DayLabel>>(emptyList())
    val dayLabels: LiveData<List<DayLabel>> = _dayLabels
    fun refreshDayLabels() { _dayLabels.value = repo.getDayLabels() }

    // ── Settings – day editor ─────────────────────────────────────────────────
    private val _editingDayId = MutableLiveData<Int?>(null)
    val editingDayId: LiveData<Int?> = _editingDayId

    private val _editingScheduled = MutableLiveData<List<ScheduledExercise>>(emptyList())
    val editingScheduled: LiveData<List<ScheduledExercise>> = _editingScheduled

    fun openDayEditor(dayId: Int) {
        _editingDayId.value = dayId
        _editingScheduled.value = repo.getScheduledForDay(dayId)
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun closeDayEditor() {
        _editingDayId.value = null
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        refreshToday()
    }

    fun removeFromDay(exerciseId: Long) {
        val dayId = _editingDayId.value ?: return
        repo.removeScheduledExercise(dayId, exerciseId)
        _editingScheduled.value = repo.getScheduledForDay(dayId)
    }

    fun toggleExercise(exercise: Exercise): String {
        val dayId = _editingDayId.value ?: return ""
        return if (repo.isExerciseScheduled(dayId, exercise.id)) {
            repo.removeScheduledExercise(dayId, exercise.id)
            _editingScheduled.value = repo.getScheduledForDay(dayId)
            "Removed \"${exercise.name}\""
        } else {
            repo.addScheduledExercise(dayId, exercise)
            _editingScheduled.value = repo.getScheduledForDay(dayId)
            "Added \"${exercise.name}\""
        }
    }

    fun isInDay(exerciseId: Long): Boolean =
        _editingScheduled.value?.any { it.exerciseId == exerciseId } == true

    // ── Exercise search ───────────────────────────────────────────────────────
    private val _allExercises = MutableLiveData<List<Exercise>>(emptyList())
    val allExercises: LiveData<List<Exercise>> = _allExercises
    fun refreshAllExercises() { _allExercises.value = repo.getAllExercises(); updateFilteredExercises() }

    private val _selectedFilterMuscle = MutableLiveData("All")
    val selectedFilterMuscle: LiveData<String> = _selectedFilterMuscle

    private val _filteredExercises = MutableLiveData<List<Exercise>>(emptyList())
    val filteredExercises: LiveData<List<Exercise>> = _filteredExercises

    fun setMuscleFilter(muscle: String) {
        _selectedFilterMuscle.value = muscle
        updateFilteredExercises()
    }

    private fun updateFilteredExercises() {
        val all = _allExercises.value ?: emptyList()
        val filter = _selectedFilterMuscle.value ?: "All"
        _filteredExercises.value = if (filter == "All") all
        else all.filter { it.muscleGroup.equals(filter, ignoreCase = true) }
    }

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _searchResults = MutableLiveData<List<Exercise>>(emptyList())
    val searchResults: LiveData<List<Exercise>> = _searchResults

    fun search(query: String) {
        _searchQuery.value = query
        val q = query.trim().lowercase()
        _searchResults.value = if (q.isEmpty()) emptyList()
        else (_allExercises.value ?: emptyList()).filter {
            it.name.lowercase().contains(q) || it.muscleGroup.lowercase().contains(q)
        }
    }

    fun createExercise(name: String, muscleGroup: String, equipment: String): String {
        if (name.isBlank()) return "Name cannot be empty"
        val id = repo.insertExercise(name.trim(), muscleGroup, equipment)
        refreshAllExercises()
        val dayId = _editingDayId.value ?: return "Created \"$name\""
        val ex = _allExercises.value?.find { it.id == id } ?: return "Created \"$name\""
        repo.addScheduledExercise(dayId, ex)
        _editingScheduled.value = repo.getScheduledForDay(dayId)
        return "Created and added \"$name\""
    }

    // ── History ───────────────────────────────────────────────────────────────
    private val _historyDates = MutableLiveData<Set<String>>(emptySet())
    val historyDates: LiveData<Set<String>> = _historyDates

    private val _selectedDate = MutableLiveData(LocalDate.now().format(dateFmt))
    val selectedDate: LiveData<String> = _selectedDate

    private val _historyEntries = MutableLiveData<List<HistoryEntry>>(emptyList())
    val historyEntries: LiveData<List<HistoryEntry>> = _historyEntries

    fun selectDate(dateKey: String) {
        _selectedDate.value = dateKey
        _historyEntries.value = repo.getHistoryForDate(dateKey)
    }

    fun refreshHistory() {
        _historyDates.value = repo.getDistinctHistoryDates().toSet()
        _historyEntries.value = repo.getHistoryForDate(_selectedDate.value ?: "")
    }

    // ── Analysis ─────────────────────────────────────────────────────────────
    private val _analysisRange = MutableLiveData(AnalysisRange.MONTH)
    val analysisRange: LiveData<AnalysisRange> = _analysisRange

    private val _analysisBars = MutableLiveData<List<AnalysisBar>>(emptyList())
    val analysisBars: LiveData<List<AnalysisBar>> = _analysisBars

    private val _analysisSummary = MutableLiveData(AnalysisSummary())
    val analysisSummary: LiveData<AnalysisSummary> = _analysisSummary

    private val _muscleBreakdown = MutableLiveData<List<MuscleSlice>>(emptyList())
    val muscleBreakdown: LiveData<List<MuscleSlice>> = _muscleBreakdown

    private val _repsBars = MutableLiveData<List<AnalysisBar>>(emptyList())
    val repsBars: LiveData<List<AnalysisBar>> = _repsBars

    private val _topExercises = MutableLiveData<List<ExerciseStat>>(emptyList())
    val topExercises: LiveData<List<ExerciseStat>> = _topExercises

    fun setRange(r: AnalysisRange) { _analysisRange.value = r; refreshAnalysis() }

    fun refreshAnalysis() {
        val counts    = repo.getCompletedCountsByDate()
        val repsCounts = repo.getTotalRepsByDate()
        val today     = LocalDate.now()
        val range     = _analysisRange.value ?: AnalysisRange.MONTH

        // ── Workout completion bars ───────────────────────────────────────────
        val bars = when (range) {
            AnalysisRange.DAY -> (6 downTo 0).map { off ->
                val d = today.minusDays(off.toLong())
                AnalysisBar(d.dayOfWeek.name.take(3), counts[d.format(dateFmt)] ?: 0)
            }
            AnalysisRange.MONTH -> (5 downTo 0).map { off ->
                val d = today.minusMonths(off.toLong())
                val prefix = d.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val total = counts.entries.filter { it.key.startsWith(prefix) }.sumOf { it.value }
                AnalysisBar(d.format(DateTimeFormatter.ofPattern("MMM")), total)
            }
            AnalysisRange.YEAR -> (3 downTo 0).map { off ->
                val d = today.minusYears(off.toLong())
                val prefix = d.format(DateTimeFormatter.ofPattern("yyyy"))
                val total = counts.entries.filter { it.key.startsWith(prefix) }.sumOf { it.value }
                AnalysisBar(prefix, total)
            }
        }
        _analysisBars.value = bars

        // ── Reps bars (same bucketing) ────────────────────────────────────────
        val rBars = when (range) {
            AnalysisRange.DAY -> (6 downTo 0).map { off ->
                val d = today.minusDays(off.toLong())
                AnalysisBar(d.dayOfWeek.name.take(3), repsCounts[d.format(dateFmt)] ?: 0)
            }
            AnalysisRange.MONTH -> (5 downTo 0).map { off ->
                val d = today.minusMonths(off.toLong())
                val prefix = d.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val total = repsCounts.entries.filter { it.key.startsWith(prefix) }.sumOf { it.value }
                AnalysisBar(d.format(DateTimeFormatter.ofPattern("MMM")), total)
            }
            AnalysisRange.YEAR -> (3 downTo 0).map { off ->
                val d = today.minusYears(off.toLong())
                val prefix = d.format(DateTimeFormatter.ofPattern("yyyy"))
                val total = repsCounts.entries.filter { it.key.startsWith(prefix) }.sumOf { it.value }
                AnalysisBar(prefix, total)
            }
        }
        _repsBars.value = rBars

        // ── Summary ───────────────────────────────────────────────────────────
        val total = bars.sumOf { it.value }
        val best  = bars.maxByOrNull { it.value }
        val totalReps = rBars.sumOf { it.value }
        val streak = repo.getCurrentStreak(today.format(dateFmt))
        _analysisSummary.value = AnalysisSummary(
            total, best?.label ?: "-", best?.value ?: 0,
            if (bars.isEmpty()) 0f else total.toFloat() / bars.size,
            totalReps, streak
        )

        // ── Muscle breakdown (pie) ────────────────────────────────────────────
        _muscleBreakdown.value = repo.getMuscleBreakdown(
            cutoffDate = when (range) {
                AnalysisRange.DAY   -> today.minusDays(6).format(dateFmt)
                AnalysisRange.MONTH -> today.minusMonths(5).withDayOfMonth(1).format(dateFmt)
                AnalysisRange.YEAR  -> today.minusYears(3).withDayOfYear(1).format(dateFmt)
            }
        )

        // ── Top exercises ─────────────────────────────────────────────────────
        _topExercises.value = repo.getTopExercises(
            cutoffDate = when (range) {
                AnalysisRange.DAY   -> today.minusDays(6).format(dateFmt)
                AnalysisRange.MONTH -> today.minusMonths(5).withDayOfMonth(1).format(dateFmt)
                AnalysisRange.YEAR  -> today.minusYears(3).withDayOfYear(1).format(dateFmt)
            },
            limit = 5
        )
    }

    private fun checkDailyReset() {
        val lastDate = repo.getSetting("last_active_date", "")
        val today = LocalDate.now().format(dateFmt)
        if (lastDate != today) {
            repo.clearAllCompletions()
            repo.setSetting("last_active_date", today)
        }
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    init {
        checkDailyReset()
        refreshDayLabels()
        refreshAllExercises()
        refreshToday()
        refreshHistory()
        refreshAnalysis()
    }
}

enum class AnalysisRange(val label: String) { DAY("Day"), MONTH("Month"), YEAR("Year") }
data class AnalysisBar(val label: String, val value: Int)
data class AnalysisSummary(
    val total: Int = 0,
    val bestLabel: String = "-",
    val bestValue: Int = 0,
    val average: Float = 0f,
    val totalReps: Int = 0,
    val streak: Int = 0
)
data class MuscleSlice(val muscle: String, val count: Int)
data class ExerciseStat(val name: String, val completions: Int, val totalReps: Int)
