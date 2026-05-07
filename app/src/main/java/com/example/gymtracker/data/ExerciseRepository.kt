package com.example.gymtracker.data

import android.content.Context

class ExerciseRepository(context: Context) {
    private val db = DatabaseHelper(context)

    // Day labels
    fun getDayLabels() = db.getDayLabels()
    fun updateDayLabel(dayId: Int, label: String) = db.updateDayLabel(dayId, label)

    // Exercises
    fun getAllExercises() = db.getAllExercises()
    fun insertExercise(name: String, muscleGroup: String, equipment: String) =
        db.insertExercise(name, muscleGroup, equipment)

    // Scheduled
    fun getScheduledForDay(dayId: Int) = db.getScheduledForDay(dayId)
    fun isExerciseScheduled(dayId: Int, exerciseId: Long) = db.isExerciseScheduled(dayId, exerciseId)
    fun addScheduledExercise(dayId: Int, exercise: Exercise) = db.addScheduledExercise(dayId, exercise)
    fun removeScheduledExercise(dayId: Int, exerciseId: Long) = db.removeScheduledExercise(dayId, exerciseId)
    fun setExerciseCompleted(scheduledId: Long, completed: Boolean) = db.setExerciseCompleted(scheduledId, completed)
    fun setExerciseCompletedWithReps(scheduledId: Long, completed: Boolean, reps: Int) =
        db.setExerciseCompletedWithReps(scheduledId, completed, reps)
    fun setExerciseCompletedWithRepsAndWeight(scheduledId: Long, completed: Boolean, reps: Int, weight: Float) =
        db.setExerciseCompletedWithRepsAndWeight(scheduledId, completed, reps, weight)
    fun setWarmup(scheduledId: Long, isWarmup: Boolean) = db.setWarmup(scheduledId, isWarmup)
    fun clearCompletionForDay(dayId: Int) = db.clearCompletionForDay(dayId)
    fun clearAllCompletions() = db.clearAllCompletions()

    // History
    fun snapshotDayToHistory(dateKey: String, dayId: Int) = db.snapshotDayToHistory(dateKey, dayId)
    fun getHistoryForDate(dateKey: String) = db.getHistoryForDate(dateKey)
    fun getDistinctHistoryDates() = db.getDistinctHistoryDates()
    fun getCompletedCountsByDate() = db.getCompletedCountsByDate()
    fun getTotalRepsByDate() = db.getTotalRepsByDate()
    fun getMuscleBreakdown(cutoffDate: String) = db.getMuscleBreakdown(cutoffDate)
    fun getTopExercises(cutoffDate: String, limit: Int) = db.getTopExercises(cutoffDate, limit)
    fun getCurrentStreak(todayKey: String) = db.getCurrentStreak(todayKey)

    // Settings
    fun getSetting(key: String, default: String = "") = db.getSetting(key, default)
    fun setSetting(key: String, value: String) = db.setSetting(key, value)
}
