# 💪 GymTracker

A clean, offline Android gym tracker — schedule workouts by day, log sets with reps & weight, and analyze your performance over time.

<br/>

## Screenshots

| Home | Analysis | History |
|------|----------|---------|
| ![Home](screenshots/Home.jpeg) | ![Analysis](screenshots/Analysis.jpeg) | ![History](screenshots/History.jpeg) |

<br/>

## Features

- **📅 Weekly Schedule** — Build a workout plan for each day of the week from a library of 400+ exercises across 8 muscle groups
- **🔍 Exercise Search** — Instantly search and filter by muscle group; create your own custom exercises
- **🔥 Warmup Flag** — Mark any exercise as a warmup and it pins to the top of your list with a badge
- **✅ Set Logging** — When you complete an exercise, log your reps and weight (kg) using a stepper or by typing directly
- **📊 Performance Dashboard** — Animated bar charts for workout volume and reps, a muscle group pie chart, reps-by-muscle horizontal bars, streak counter, and top 5 exercises
- **🕓 Workout History** — Browse every past session by date with reps and weight logged per exercise
- **🎨 Theming** — Six color presets (Slate, Ocean, Forest, Rose, Amber, Violet) with Dark, Blue, and White background styles
- **💾 Safe Migrations** — SQLite database upgrades with `ALTER TABLE` — your data is never wiped on app update

<br/>

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | XML Views, ViewBinding, Material 3 |
| Architecture | MVVM — ViewModel + LiveData |
| Database | SQLite via `SQLiteOpenHelper` |
| Charts | [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) |
| Navigation | Fragment back stack |
| Min SDK | 33 (Android 13) |

<br/>

## Getting Started

1. Clone the repo
   ```bash
   git clone https://github.com/your-username/GymTracker.git
   ```
2. Open in **Android Studio**
3. Let Gradle sync
4. Run on a device or emulator (API 33+)

No API keys, no backend, no account needed — fully offline.

<br/>

## Project Structure

```
app/src/main/java/com/example/gymtracker/
├── data/
│   ├── DatabaseHelper.kt       # SQLite schema, queries, migrations
│   ├── ExerciseRepository.kt   # Data access layer
│   └── Models.kt               # Data classes
├── ui/
│   ├── adapter/                # RecyclerView adapters
│   ├── fragment/               # All screens + dialogs
│   └── (theme, screens)
└── viewmodel/
    └── MainViewModel.kt        # Single shared ViewModel
```

<br/>

## Topics

`android` `kotlin` `sqlite` `gym` `workout-tracker` `fitness` `mpandroidchart` `offline` `mvvm` `material-design`

<br/>

## License

```
MIT License — feel free to use, modify, and distribute.
```
