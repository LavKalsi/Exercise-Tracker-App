package com.example.gymtracker.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "exercise_tracker.db"
        private const val DB_VERSION = 5

        // Tables
        const val T_EXERCISES = "exercises"
        const val T_DAY_LABELS = "day_labels"
        const val T_SCHEDULED = "scheduled_exercises"
        const val T_HISTORY = "history"
        const val T_SETTINGS = "app_settings"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $T_EXERCISES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                muscle_group TEXT NOT NULL,
                equipment TEXT NOT NULL,
                is_custom INTEGER NOT NULL DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE $T_DAY_LABELS (
                day_id INTEGER PRIMARY KEY,
                label TEXT NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE $T_SCHEDULED (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                day_id INTEGER NOT NULL,
                exercise_id INTEGER NOT NULL,
                exercise_name TEXT NOT NULL,
                muscle_group TEXT NOT NULL,
                equipment TEXT NOT NULL,
                is_completed INTEGER NOT NULL DEFAULT 0,
                sort_order INTEGER NOT NULL DEFAULT 0,
                is_warmup INTEGER NOT NULL DEFAULT 0,
                reps INTEGER NOT NULL DEFAULT 0,
                weight REAL NOT NULL DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE $T_HISTORY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date_key TEXT NOT NULL,
                day_id INTEGER NOT NULL,
                exercise_id INTEGER NOT NULL,
                exercise_name TEXT NOT NULL,
                is_completed INTEGER NOT NULL DEFAULT 0,
                reps INTEGER NOT NULL DEFAULT 0,
                weight REAL NOT NULL DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE $T_SETTINGS (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """)
        seedDayLabels(db)
        seedExercises(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS $T_EXERCISES")
            db.execSQL("""
                CREATE TABLE $T_EXERCISES (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    muscle_group TEXT NOT NULL,
                    equipment TEXT NOT NULL,
                    is_custom INTEGER NOT NULL DEFAULT 0
                )
            """)
            seedExercises(db)
        }
        if (oldVersion < 4) {
            // Add is_warmup to scheduled_exercises (preserves all existing rows)
            try {
                db.execSQL("ALTER TABLE $T_SCHEDULED ADD COLUMN is_warmup INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { /* column may already exist */ }
            // Add reps to scheduled_exercises (preserves all existing rows)
            try {
                db.execSQL("ALTER TABLE $T_SCHEDULED ADD COLUMN reps INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { /* column may already exist */ }
            // Add reps to history (preserves all existing rows)
            try {
                db.execSQL("ALTER TABLE $T_HISTORY ADD COLUMN reps INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { /* column may already exist */ }
        }
        if (oldVersion < 5) {
            // Add weight column to both tables — no data loss
            try {
                db.execSQL("ALTER TABLE $T_SCHEDULED ADD COLUMN weight REAL NOT NULL DEFAULT 0")
            } catch (_: Exception) { /* column may already exist */ }
            try {
                db.execSQL("ALTER TABLE $T_HISTORY ADD COLUMN weight REAL NOT NULL DEFAULT 0")
            } catch (_: Exception) { /* column may already exist */ }
        }
    }

    // ── Day Labels ────────────────────────────────────────────────────────────

    private fun seedDayLabels(db: SQLiteDatabase) {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        days.forEachIndexed { i, name ->
            db.insert(T_DAY_LABELS, null, ContentValues().apply {
                put("day_id", i + 1)
                put("label", name)
            })
        }
    }

    fun getDayLabels(): List<DayLabel> {
        val list = mutableListOf<DayLabel>()
        readableDatabase.query(T_DAY_LABELS, null, null, null, null, null, "day_id ASC").use { c ->
            while (c.moveToNext()) list.add(DayLabel(c.getInt(0), c.getString(1)))
        }
        return list
    }

    fun updateDayLabel(dayId: Int, label: String) {
        writableDatabase.update(T_DAY_LABELS, ContentValues().apply { put("label", label) },
            "day_id=?", arrayOf(dayId.toString()))
    }

    // ── Exercises ─────────────────────────────────────────────────────────────

    private fun seedExercises(db: SQLiteDatabase) {
        val muscles = listOf("Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Core", "Cardio")
        
        // Define base movements and their specific equipments/variations to create ~400 unique clean names
        val catalogData = mapOf(
            "Chest" to listOf(
                Pair("Bench Press", listOf("Barbell", "Dumbbell", "Smith Machine", "Incline Barbell", "Decline Barbell", "Incline Dumbbell", "Decline Dumbbell", "Close Grip Barbell", "Wide Grip Barbell", "Pause Rep Barbell")),
                Pair("Fly", listOf("Dumbbell Flat", "Dumbbell Incline", "Dumbbell Decline", "Cable Low to High", "Cable High to Low", "Machine Pec Deck", "Cable Standing", "Cable Seated", "Single Arm Cable", "Floor Dumbbell")),
                Pair("Push-Up", listOf("Standard", "Wide Grip", "Diamond", "Incline", "Decline", "Clapping", "Archer", "One-Arm", "Weighted", "Staggered Hand")),
                Pair("Press Machine", listOf("Seated Vertical", "Incline Lever", "Iso-Lateral", "Hammer Strength Flat", "Hammer Strength Incline", "Hammer Strength Decline", "Plate Loaded", "Narrow Seated", "Wide Seated", "Single Arm Seated")),
                Pair("Dips", listOf("Bodyweight Chest", "Weighted Chest", "Machine Assisted", "Ring", "Parallel Bar Wide", "Deep Chest Focus", "Pause Rep", "Eccentric Slow", "Straight Bar", "Kipping"))
            ),
            "Back" to listOf(
                Pair("Row", listOf("Barbell Bent Over", "One Arm Dumbbell", "Seated Cable", "T-Bar Chest Supported", "Meadows Row", "Incline Dumbbell", "Smith Machine Bent Over", "Seal Row Barbell", "Single Arm Cable Seated", "Renegade Row")),
                Pair("Pull-Up / Chin-Up", listOf("Standard Pull-Up", "Chin-Up Underhand", "Neutral Grip Pull-Up", "Wide Grip Pull-Up", "Weighted Pull-Up", "Weighted Chin-Up", "Behind the Neck Pull-Up", "L-Sit Pull-Up", "Towel Pull-Up", "Archer Pull-Up")),
                Pair("Lat Pulldown", listOf("Wide Grip", "Close Grip Neutral", "Reverse Grip V-Bar", "Single Arm Cable", "Behind the Neck", "Straight Arm Cable", "Underhand Grip", "Kneeling Single Arm", "Rope Attachment", "Lat Focus Lean Back")),
                Pair("Deadlift", listOf("Conventional Barbell", "Sumo Barbell", "Dumbbell", "Romanian Barbell", "Romanian Dumbbell", "Snatch Grip Barbell", "Deficit Barbell", "Rack Pull Barbell", "Trap Bar", "Single Leg Dumbbell")),
                Pair("Shrug", listOf("Dumbbell Standing", "Barbell Front", "Barbell Behind Back", "Smith Machine", "Cable Standing", "Dumbbell Seated", "Machine Seated", "Power Shrug", "Single Arm Dumbbell", "Kelso Row Shrug"))
            ),
            "Legs" to listOf(
                Pair("Squat", listOf("Barbell Back High Bar", "Barbell Low Bar", "Barbell Front", "Dumbbell Goblet", "Box Squat Barbell", "Zercher Barbell", "Hack Squat Machine", "Overhead Barbell", "Split Squat Dumbbell", "Pause Squat Barbell")),
                Pair("Press", listOf("Leg Press Horizontal", "Leg Press 45 Degree", "Leg Press Iso-Lateral", "Leg Press Single Leg", "Leg Press Wide Stance", "Leg Press Narrow Stance", "Leg Press High Foot Placement", "Leg Press Low Foot Placement", "Sled Press", "Vertical Leg Press")),
                Pair("Lunge", listOf("Walking Dumbbell", "Reverse Barbell", "Lateral Dumbbell", "Crossover Lunge", "Stationary Barbell", "Deficit Reverse Lunge", "Walking Barbell", "Pendulum Lunge", "Weighted Vest Walking", "Slider Lateral Lunge")),
                Pair("Extension/Curl", listOf("Leg Extension Machine", "Leg Extension Single Leg", "Seated Leg Curl Machine", "Lying Leg Curl Machine", "Standing Leg Curl", "Single Leg Extension", "Single Leg Curl", "Glute Ham Raise", "Nordic Hamstring Curl", "Slider Leg Curl")),
                Pair("Calf Raise", listOf("Standing Barbell", "Standing Machine", "Seated Machine", "Leg Press Calf Press", "Donkey Calf Raise", "Single Leg Bodyweight", "Single Leg Dumbbell", "Standing Dumbbell", "Smith Machine Standing", "Farmer Walk on Toes"))
            ),
            "Shoulders" to listOf(
                Pair("Overhead Press", listOf("Standing Barbell Military", "Seated Dumbbell", "Seated Barbell", "Arnold Press Dumbbell", "Push Press Barbell", "One Arm Dumbbell", "Smith Machine Seated", "Single Arm Kettlebell", "Z-Press Barbell", "Machine Shoulder Press")),
                Pair("Lateral Raise", listOf("Dumbbell Standing", "Dumbbell Seated", "Cable Single Arm", "Cable Cross Body", "Machine Lateral Raise", "Leaning Away Cable", "Dumbbell Partial Reps", "Plate Lateral Raise", "Around the World", "Front to Side Raise")),
                Pair("Front Raise", listOf("Dumbbell Alternating", "Barbell Standing", "Cable Rope", "Plate Front Raise", "Dumbbell Seated", "Single Arm Cable", "Hammer Grip Dumbbell", "Incline Bench Dumbbell", "EZ Bar Standing", "Two-Handed Dumbbell")),
                Pair("Rear Delt", listOf("Dumbbell Reverse Fly", "Face Pull Cable", "Rear Delt Peck Deck Machine", "Bent Over Lateral Raise", "Cable Crossover Rear Delt", "Lying Rear Delt Fly", "Incline Bench Reverse Fly", "Single Arm Rear Delt Cable", "High Cable Pull", "Band Face Pull")),
                Pair("Upright Row", listOf("Barbell Standard", "EZ Bar Close Grip", "Dumbbell Alternating", "Cable Straight Bar", "Smith Machine", "Single Arm Dumbbell", "Single Arm Cable", "Wide Grip Barbell", "Rope Upright Row", "Kettlebell Row"))
            ),
            "Biceps" to listOf(
                Pair("Curl", listOf("Barbell Standing", "Dumbbell Alternating", "Dumbbell Incline", "Concentration Dumbbell", "Preacher EZ Bar", "Hammer Dumbbell Standing", "Spider Curl EZ Bar", "Cable Straight Bar", "Cable Rope Hammer", "Reverse Grip Barbell")),
                Pair("Specialty Curl", listOf("Zottman Curl", "Waiter Curl Dumbbell", "Drag Curl Barbell", "Cheat Curl Barbell", "Cross Body Hammer", "Seated Incline Hammer", "Single Arm Preacher", "Bayesian Cable Curl", "High Cable Bicep Curl", "Machine Bicep Curl")),
                Pair("Hammer Curl", listOf("Dumbbell Neutral", "Cable Rope", "Dumbbell Seated", "Preacher Hammer", "Incline Bench Hammer", "Single Arm Cable Hammer", "Cross Body Dumbbell", "Kettlebell Hammer", "Reverse Grip EZ Bar", "Slow Eccentric Hammer")),
                Pair("Preacher", listOf("EZ Bar Standard", "Dumbbell Single Arm", "Barbell Close Grip", "Machine Preacher", "Cable Preacher", "Hammer Grip Dumbbell", "Reverse Grip Barbell", "Spider Curl Style", "Close Grip EZ Bar", "Wide Grip Barbell")),
                Pair("Cable Bicep", listOf("Standing Dual Handle", "Straight Bar Low Pulley", "Rope Low Pulley", "Single Arm Behind Back", "Overhead Cable Curl", "Lying Cable Curl", "Preacher Cable", "Concentration Cable", "Reverse Grip Cable", "EZ Bar Cable"))
            ),
            "Triceps" to listOf(
                Pair("Extension", listOf("Skull Crusher EZ Bar", "Overhead Dumbbell Seated", "Overhead Barbell Standing", "Lying Dumbbell", "Incline EZ Bar", "JM Press Barbell", "One Arm Overhead Dumbbell", "Cable Overhead Rope", "Machine Tricep Extension", "Cable Overhead Straight Bar")),
                Pair("Pushdown", listOf("Cable Rope", "Cable Straight Bar", "Cable V-Bar", "Cable Reverse Grip Single Arm", "Cable Single Arm D-Handle", "Machine Tricep Pushdown", "Cable Underhand Straight Bar", "Heavy Partial Pushdown", "Pause Rep Pushdown", "Slow Tempo Pushdown")),
                Pair("Dips", listOf("Parallel Bar Bodyweight", "Parallel Bar Weighted", "Bench Dip Feet Up", "Bench Dip Weighted", "Ring Dip", "Machine Dip", "Assisted Dip Machine", "Close Grip Push-Up", "Diamond Push-Up", "Medicine Ball Close Grip")),
                Pair("Kickback", listOf("Dumbbell Single Arm", "Dumbbell Both Arms", "Cable Single Arm", "Cable Constant Tension", "Bent Over Barbell", "Incline Bench Dumbbell", "Seated Kickback", "Kettlebell Kickback", "Slow Eccentric", "Isometric Hold")),
                Pair("Press", listOf("Close Grip Bench Press", "California Press Barbell", "Close Grip Floor Press", "Smith Machine Close Grip", "Close Grip Dumbbell Press", "Neutral Grip Dumbbell Press", "Tate Press Dumbbell", "Squeeze Press Dumbbell", "Floor Press Barbell", "Narrow Grip Incline Press"))
            ),
            "Core" to listOf(
                Pair("Crunch", listOf("Standard Floor", "Bicycle", "Reverse", "V-Up", "Vertical Leg", "Cable Kneeling", "Machine Seated", "Medicine Ball", "Weighted Floor", "Swiss Ball")),
                Pair("Plank", listOf("Standard Forearm", "Side Plank", "High Plank Hand", "Plank with Leg Lift", "Walking Plank", "Shoulder Tap Plank", "Saw Plank", "Star Plank", "Mountain Climber Plank", "Spiderman Plank")),
                Pair("Leg Raise", listOf("Lying Flat", "Hanging Straight Leg", "Hanging Knee Raise", "Captain's Chair", "Dragon Flag", "Flutter Kicks", "Scissor Kicks", "Single Leg Lowering", "Reverse Leg Lift", "Windshield Wipers")),
                Pair("Rotation", listOf("Russian Twist Dumbbell", "Woodchopper Cable High", "Woodchopper Cable Low", "Landmine Rotation Barbell", "Pallof Press Cable", "Side Bend Dumbbell", "Windmill Kettlebell", "Cable Twist Seated", "Rotational Throw Med Ball", "Stability Ball Twist")),
                Pair("Stability", listOf("Bird Dog", "Dead Bug", "Hollow Body Hold", "Superman Hold", "Ab Wheel Rollout", "Bear Crawl", "Renegade Row Core Focus", "L-Sit Hold", "Turkish Get-Up", "Farmer Walk Heavy"))
            ),
            "Cardio" to listOf(
                Pair("Running", listOf("Treadmill Standard", "Outdoor Sprint", "Hill Sprints", "Incline Treadmill", "High Knees", "Jogging In Place", "Interval Training", "Fartlek Run", "Long Distance", "Tempo Run")),
                Pair("Cycling", listOf("Stationary Bike", "Spin Class", "Road Cycling", "Mountain Biking", "Assault Bike", "Recumbent Bike", "Standing Sprint", "Tabata Cycling", "Hill Climb Bike", "Low Intensity Cruise")),
                Pair("Jumping", listOf("Jump Rope Basic", "Double Unders", "Box Jumps", "Burpees", "Jumping Jacks", "Star Jumps", "Skaters", "Tuck Jumps", "Mountain Climbers", "Broad Jumps")),
                Pair("Bodyweight Cardio", listOf("Bear Crawl", "Frog Jump", "Shadow Boxing", "Step Up", "Stair Climbing", "Swimming", "Rowing Machine", "Elliptical Machine", "Vertical Climber", "Battle Ropes")),
                Pair("HIIT", listOf("Thrusters", "Wall Ball", "Kettlebell Swings", "Man-makers", "Clean and Press", "Medicine Ball Slams", "Snatch Kettlebell", "Goblet Lunge Cardio", "Shadow Kickboxing", "Jump Squat"))
            )
        )

        db.beginTransaction()
        try {
            catalogData.forEach { (muscle, movements) ->
                movements.forEach { (core, variations) ->
                    variations.forEach { variation ->
                        val name = if (variation.contains(core, ignoreCase = true)) variation else "$variation $core"
                        // Determine equipment based on name
                        val equip = when {
                            name.contains("Barbell", true) -> "Barbell"
                            name.contains("Dumbbell", true) -> "Dumbbell"
                            name.contains("Cable", true) -> "Cable"
                            name.contains("Machine", true) -> "Machine"
                            name.contains("Plate", true) -> "Equipment"
                            name.contains("Kettlebell", true) -> "Equipment"
                            name.contains("Med Ball", true) -> "Equipment"
                            name.contains("Band", true) -> "Equipment"
                            else -> "Bodyweight"
                        }
                        db.insert(T_EXERCISES, null, ContentValues().apply {
                            put("name", name); put("muscle_group", muscle)
                            put("equipment", equip); put("is_custom", 0)
                        })
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllExercises(): List<Exercise> {
        val list = mutableListOf<Exercise>()
        readableDatabase.query(T_EXERCISES, null, null, null, null, null, "name ASC").use { c ->
            while (c.moveToNext()) list.add(
                Exercise(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4) == 1)
            )
        }
        return list
    }

    fun insertExercise(name: String, muscleGroup: String, equipment: String): Long {
        return writableDatabase.insert(T_EXERCISES, null, ContentValues().apply {
            put("name", name); put("muscle_group", muscleGroup)
            put("equipment", equipment); put("is_custom", 1)
        })
    }

    // ── Scheduled Exercises ───────────────────────────────────────────────────

    fun getScheduledForDay(dayId: Int): List<ScheduledExercise> {
        val list = mutableListOf<ScheduledExercise>()
        readableDatabase.query(T_SCHEDULED, null, "day_id=?", arrayOf(dayId.toString()),
            null, null, "is_warmup DESC, sort_order ASC, id ASC").use { c ->
            while (c.moveToNext()) list.add(
                ScheduledExercise(c.getLong(0), c.getInt(1), c.getLong(2),
                    c.getString(3), c.getString(4), c.getString(5), c.getInt(6) == 1, c.getInt(7),
                    c.getInt(8) == 1, c.getInt(9), c.getFloat(10))
            )
        }
        return list
    }

    fun isExerciseScheduled(dayId: Int, exerciseId: Long): Boolean {
        readableDatabase.query(T_SCHEDULED, arrayOf("id"), "day_id=? AND exercise_id=?",
            arrayOf(dayId.toString(), exerciseId.toString()), null, null, null).use { c ->
            return c.count > 0
        }
    }

    fun addScheduledExercise(dayId: Int, exercise: Exercise): Long {
        val order = getScheduledForDay(dayId).size
        return writableDatabase.insert(T_SCHEDULED, null, ContentValues().apply {
            put("day_id", dayId); put("exercise_id", exercise.id)
            put("exercise_name", exercise.name); put("muscle_group", exercise.muscleGroup)
            put("equipment", exercise.equipment); put("is_completed", 0)
            put("sort_order", order); put("is_warmup", 0)
        })
    }

    fun removeScheduledExercise(dayId: Int, exerciseId: Long) {
        writableDatabase.delete(T_SCHEDULED, "day_id=? AND exercise_id=?",
            arrayOf(dayId.toString(), exerciseId.toString()))
    }

    fun setExerciseCompleted(scheduledId: Long, completed: Boolean) {
        writableDatabase.update(T_SCHEDULED, ContentValues().apply { put("is_completed", if (completed) 1 else 0) },
            "id=?", arrayOf(scheduledId.toString()))
    }

    fun setExerciseCompletedWithReps(scheduledId: Long, completed: Boolean, reps: Int) {
        writableDatabase.update(T_SCHEDULED, ContentValues().apply {
            put("is_completed", if (completed) 1 else 0)
            put("reps", reps)
        }, "id=?", arrayOf(scheduledId.toString()))
    }

    fun setExerciseCompletedWithRepsAndWeight(scheduledId: Long, completed: Boolean, reps: Int, weight: Float) {
        writableDatabase.update(T_SCHEDULED, ContentValues().apply {
            put("is_completed", if (completed) 1 else 0)
            put("reps", reps)
            put("weight", weight)
        }, "id=?", arrayOf(scheduledId.toString()))
    }

    fun setWarmup(scheduledId: Long, isWarmup: Boolean) {
        writableDatabase.update(T_SCHEDULED, ContentValues().apply {
            put("is_warmup", if (isWarmup) 1 else 0)
        }, "id=?", arrayOf(scheduledId.toString()))
    }

    fun clearCompletionForDay(dayId: Int) {
        writableDatabase.update(T_SCHEDULED, ContentValues().apply { put("is_completed", 0) },
            "day_id=?", arrayOf(dayId.toString()))
    }

    fun clearAllCompletions() {
        writableDatabase.update(T_SCHEDULED, ContentValues().apply { put("is_completed", 0) }, null, null)
    }

    // ── History ───────────────────────────────────────────────────────────────

    fun snapshotDayToHistory(dateKey: String, dayId: Int) {
        val scheduled = getScheduledForDay(dayId)
        // Remove existing snapshot for this date
        writableDatabase.delete(T_HISTORY, "date_key=?", arrayOf(dateKey))
        scheduled.forEach { s ->
            writableDatabase.insert(T_HISTORY, null, ContentValues().apply {
                put("date_key", dateKey); put("day_id", dayId)
                put("exercise_id", s.exerciseId); put("exercise_name", s.exerciseName)
                put("is_completed", if (s.isCompleted) 1 else 0)
                put("reps", s.reps)
                put("weight", s.weight)
            })
        }
    }

    fun getHistoryForDate(dateKey: String): List<HistoryEntry> {
        val list = mutableListOf<HistoryEntry>()
        readableDatabase.query(T_HISTORY, null, "date_key=?", arrayOf(dateKey),
            null, null, "id ASC").use { c ->
            while (c.moveToNext()) list.add(
                HistoryEntry(c.getLong(0), c.getString(1), c.getInt(2),
                    c.getLong(3), c.getString(4), c.getInt(5) == 1, c.getInt(6), c.getFloat(7))
            )
        }
        return list
    }

    fun getDistinctHistoryDates(): List<String> {
        val list = mutableListOf<String>()
        readableDatabase.rawQuery("SELECT DISTINCT date_key FROM $T_HISTORY ORDER BY date_key ASC", null).use { c ->
            while (c.moveToNext()) list.add(c.getString(0))
        }
        return list
    }

    /** Returns map of dateKey -> completedCount for analysis */
    fun getCompletedCountsByDate(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        readableDatabase.rawQuery(
            "SELECT date_key, SUM(is_completed) FROM $T_HISTORY GROUP BY date_key", null).use { c ->
            while (c.moveToNext()) map[c.getString(0)] = c.getInt(1)
        }
        return map
    }

    /** Returns map of dateKey -> total reps for analysis */
    fun getTotalRepsByDate(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        readableDatabase.rawQuery(
            "SELECT date_key, SUM(reps) FROM $T_HISTORY WHERE is_completed=1 GROUP BY date_key", null).use { c ->
            while (c.moveToNext()) map[c.getString(0)] = c.getInt(1)
        }
        return map
    }

    /** Returns muscle group -> completed exercise count since cutoffDate */
    fun getMuscleBreakdown(cutoffDate: String): List<com.example.gymtracker.viewmodel.MuscleSlice> {
        val list = mutableListOf<com.example.gymtracker.viewmodel.MuscleSlice>()
        readableDatabase.rawQuery(
            """SELECT h.exercise_id, e.muscle_group, COUNT(*) as cnt
               FROM $T_HISTORY h
               LEFT JOIN $T_EXERCISES e ON h.exercise_id = e.id
               WHERE h.is_completed = 1 AND h.date_key >= ?
               GROUP BY e.muscle_group
               ORDER BY cnt DESC""",
            arrayOf(cutoffDate)
        ).use { c ->
            while (c.moveToNext()) {
                val muscle = c.getString(1) ?: "Other"
                list.add(com.example.gymtracker.viewmodel.MuscleSlice(muscle, c.getInt(2)))
            }
        }
        return list
    }

    /** Returns top N exercises by completions since cutoffDate */
    fun getTopExercises(cutoffDate: String, limit: Int): List<com.example.gymtracker.viewmodel.ExerciseStat> {
        val list = mutableListOf<com.example.gymtracker.viewmodel.ExerciseStat>()
        readableDatabase.rawQuery(
            """SELECT exercise_name, COUNT(*) as completions, SUM(reps) as total_reps
               FROM $T_HISTORY
               WHERE is_completed = 1 AND date_key >= ?
               GROUP BY exercise_name
               ORDER BY completions DESC
               LIMIT ?""",
            arrayOf(cutoffDate, limit.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(com.example.gymtracker.viewmodel.ExerciseStat(
                    c.getString(0), c.getInt(1), c.getInt(2)
                ))
            }
        }
        return list
    }

    /** Returns current consecutive workout streak (days with at least 1 completed exercise) */
    fun getCurrentStreak(todayKey: String): Int {
        val dates = getDistinctHistoryDates().toSet()
        val completedDates = mutableSetOf<String>()
        readableDatabase.rawQuery(
            "SELECT DISTINCT date_key FROM $T_HISTORY WHERE is_completed=1", null).use { c ->
            while (c.moveToNext()) completedDates.add(c.getString(0))
        }
        var streak = 0
        var d = java.time.LocalDate.parse(todayKey)
        val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        while (completedDates.contains(d.format(fmt))) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    fun getSetting(key: String, default: String = ""): String {
        readableDatabase.query(T_SETTINGS, arrayOf("value"), "key=?", arrayOf(key),
            null, null, null).use { c ->
            return if (c.moveToFirst()) c.getString(0) else default
        }
    }

    fun setSetting(key: String, value: String) {
        writableDatabase.insertWithOnConflict(T_SETTINGS, null, ContentValues().apply {
            put("key", key); put("value", value)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
