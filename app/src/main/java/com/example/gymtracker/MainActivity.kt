package com.example.gymtracker

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.gymtracker.data.BgStyle
import com.example.gymtracker.data.ThemePreset
import com.example.gymtracker.databinding.ActivityMainBinding
import com.example.gymtracker.ui.fragment.AnalysisFragment
import com.example.gymtracker.ui.fragment.DayListFragment
import com.example.gymtracker.ui.fragment.HistoryFragment
import com.example.gymtracker.ui.fragment.HomeFragment
import com.example.gymtracker.ui.fragment.ThemePickerDialog
import com.example.gymtracker.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    private val homeFragment     = HomeFragment()
    private val historyFragment  = HistoryFragment()
    private val analysisFragment = AnalysisFragment()

    private var currentNavId = R.id.navHome

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Edge-to-edge: draw behind status bar and nav bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Push top bar below status bar
        ViewCompat.setOnApplyWindowInsetsListener(b.topBar) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(
                resources.getDimensionPixelSize(R.dimen.topbar_padding_start),
                statusBar + resources.getDimensionPixelSize(R.dimen.topbar_padding_top),
                resources.getDimensionPixelSize(R.dimen.topbar_padding_end),
                resources.getDimensionPixelSize(R.dimen.topbar_padding_bottom)
            )
            insets
        }

        // Push bottom nav above navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(b.bottomNav) { view, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(0, 0, 0, navBar)
            insets
        }

        b.tvTopTitle.text = vm.todayName

        if (savedInstanceState == null) {
            showFragment(homeFragment)
        }

        // BottomNavigationView listener
        b.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentNavId) return@setOnItemSelectedListener false
            
            val direction = getNavigationDirection(currentNavId, item.itemId)
            currentNavId = item.itemId
            
            when (item.itemId) {
                R.id.navHome     -> { showFragment(homeFragment, direction);     true }
                R.id.navHistory  -> { showFragment(historyFragment, direction);  true }
                R.id.navAnalysis -> { showFragment(analysisFragment, direction); true }
                else -> false
            }
        }

        b.btnExercises.setOnClickListener {
            b.bottomNav.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragmentContainer, DayListFragment())
                .addToBackStack("daylist")
                .commit()
        }

        b.btnTheme.setOnClickListener {
            ThemePickerDialog().show(supportFragmentManager, "theme")
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                b.bottomNav.visibility = View.VISIBLE
                b.tvTopTitle.text = vm.todayName
            }
        }
    }

    private fun showFragment(fragment: Fragment, direction: Int = 0) {
        val transaction = supportFragmentManager.beginTransaction()
        
        if (direction > 0) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (direction < 0) {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        
        transaction.replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun getNavigationDirection(oldId: Int, newId: Int): Int {
        val order = listOf(R.id.navHome, R.id.navHistory, R.id.navAnalysis)
        return order.indexOf(newId) - order.indexOf(oldId)
    }

    private fun applyTheme() {
        val repo = com.example.gymtracker.data.ExerciseRepository(applicationContext)
        val preset = try {
            ThemePreset.valueOf(repo.getSetting("theme", ThemePreset.SLATE.name))
        } catch (e: Exception) { ThemePreset.SLATE }
        val bg = try {
            BgStyle.valueOf(repo.getSetting("bg_style", BgStyle.DARK.name))
        } catch (e: Exception) { BgStyle.DARK }

        setTheme(resolveTheme(preset, bg))
    }

    companion object {
        fun resolveTheme(preset: ThemePreset, bg: BgStyle): Int = when (bg) {
            BgStyle.DARK -> when (preset) {
                ThemePreset.SLATE  -> R.style.Theme_GymTracker
                ThemePreset.OCEAN  -> R.style.Theme_GymTracker_Dark_Ocean
                ThemePreset.FOREST -> R.style.Theme_GymTracker_Dark_Forest
                ThemePreset.ROSE   -> R.style.Theme_GymTracker_Dark_Rose
                ThemePreset.AMBER  -> R.style.Theme_GymTracker_Dark_Amber
                ThemePreset.VIOLET -> R.style.Theme_GymTracker_Dark_Violet
            }
            BgStyle.BLUE -> when (preset) {
                ThemePreset.SLATE  -> R.style.Theme_GymTracker_Blue_Slate
                ThemePreset.OCEAN  -> R.style.Theme_GymTracker_Blue_Ocean
                ThemePreset.FOREST -> R.style.Theme_GymTracker_Blue_Forest
                ThemePreset.ROSE   -> R.style.Theme_GymTracker_Blue_Rose
                ThemePreset.AMBER  -> R.style.Theme_GymTracker_Blue_Amber
                ThemePreset.VIOLET -> R.style.Theme_GymTracker_Blue_Violet
            }
            BgStyle.WHITE -> when (preset) {
                ThemePreset.SLATE  -> R.style.Theme_GymTracker_White_Slate
                ThemePreset.OCEAN  -> R.style.Theme_GymTracker_White_Ocean
                ThemePreset.FOREST -> R.style.Theme_GymTracker_White_Forest
                ThemePreset.ROSE   -> R.style.Theme_GymTracker_White_Rose
                ThemePreset.AMBER  -> R.style.Theme_GymTracker_White_Amber
                ThemePreset.VIOLET -> R.style.Theme_GymTracker_White_Violet
            }
        }
    }
}
