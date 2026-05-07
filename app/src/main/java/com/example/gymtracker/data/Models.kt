package com.example.gymtracker.data

data class Exercise(
    val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val isCustom: Boolean = false
)

data class ScheduledExercise(
    val id: Long = 0,
    val dayId: Int,          // 1=Mon … 7=Sun
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val equipment: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0,
    val isWarmup: Boolean = false,
    val reps: Int = 0,
    val weight: Float = 0f
)

data class DayLabel(
    val dayId: Int,          // 1–7
    val label: String
)

data class HistoryEntry(
    val id: Long = 0,
    val dateKey: String,     // "yyyy-MM-dd"
    val dayId: Int,
    val exerciseId: Long,
    val exerciseName: String,
    val isCompleted: Boolean,
    val reps: Int = 0,
    val weight: Float = 0f
)

data class AppSetting(
    val key: String,
    val value: String
)

enum class ThemePreset(val label: String) {
    SLATE("Slate"),
    OCEAN("Ocean"),
    FOREST("Forest"),
    ROSE("Rose"),
    AMBER("Amber"),
    VIOLET("Violet")
}

enum class BgStyle(val label: String) {
    DARK("Dark"),
    BLUE("Blue"),
    WHITE("White")
}
