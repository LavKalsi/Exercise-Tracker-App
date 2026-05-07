package com.example.gymtracker.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.gymtracker.MainActivity
import com.example.gymtracker.data.BgStyle
import com.example.gymtracker.data.ThemePreset
import com.example.gymtracker.databinding.DialogThemePickerBinding
import com.example.gymtracker.viewmodel.MainViewModel

class ThemePickerDialog : DialogFragment() {

    private val vm: MainViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val b = DialogThemePickerBinding.inflate(LayoutInflater.from(requireContext()))

        val currentPreset = vm.theme.value ?: ThemePreset.SLATE
        val currentBg     = vm.bgStyle.value ?: BgStyle.DARK

        // Highlight current selections on open
        highlightBg(b, currentBg)
        highlightAccent(b, currentPreset)

        // Background pickers
        b.bgDark.setOnClickListener  { pickBg(b, BgStyle.DARK) }
        b.bgBlue.setOnClickListener  { pickBg(b, BgStyle.BLUE) }
        b.bgWhite.setOnClickListener { pickBg(b, BgStyle.WHITE) }

        // Accent pickers
        b.themeSlate.setOnClickListener  { pickAccent(b, ThemePreset.SLATE) }
        b.themeOcean.setOnClickListener  { pickAccent(b, ThemePreset.OCEAN) }
        b.themeForest.setOnClickListener { pickAccent(b, ThemePreset.FOREST) }
        b.themeRose.setOnClickListener   { pickAccent(b, ThemePreset.ROSE) }
        b.themeAmber.setOnClickListener  { pickAccent(b, ThemePreset.AMBER) }
        b.themeViolet.setOnClickListener { pickAccent(b, ThemePreset.VIOLET) }

        b.btnClose.setOnClickListener { dismiss() }

        return AlertDialog.Builder(requireContext())
            .setView(b.root)
            .create()
            .also {
                it.window?.setBackgroundDrawableResource(android.R.color.transparent)
                it.window?.setWindowAnimations(com.example.gymtracker.R.style.DialogAnimation)
            }
    }

    private fun pickBg(b: DialogThemePickerBinding, style: BgStyle) {
        vm.setBgStyle(style)
        highlightBg(b, style)
        applyAndRecreate()
    }

    private fun pickAccent(b: DialogThemePickerBinding, preset: ThemePreset) {
        vm.setTheme(preset)
        highlightAccent(b, preset)
        applyAndRecreate()
    }

    private fun applyAndRecreate() {
        dismiss()
        activity?.recreate()
    }

    private fun highlightBg(b: DialogThemePickerBinding, selected: BgStyle) {
        val all = listOf(b.bgDark to BgStyle.DARK, b.bgBlue to BgStyle.BLUE, b.bgWhite to BgStyle.WHITE)
        all.forEach { (view, style) -> applySelectionBorder(view, style == selected) }
    }

    private fun highlightAccent(b: DialogThemePickerBinding, selected: ThemePreset) {
        val all = listOf(
            b.themeSlate  to ThemePreset.SLATE,
            b.themeOcean  to ThemePreset.OCEAN,
            b.themeForest to ThemePreset.FOREST,
            b.themeRose   to ThemePreset.ROSE,
            b.themeAmber  to ThemePreset.AMBER,
            b.themeViolet to ThemePreset.VIOLET
        )
        all.forEach { (view, preset) -> applySelectionBorder(view, preset == selected) }
    }

    private fun applySelectionBorder(view: LinearLayout, selected: Boolean) {
        view.setBackgroundResource(
            if (selected) com.example.gymtracker.R.drawable.bg_selected_card
            else com.example.gymtracker.R.drawable.bg_glass_card
        )
    }
}
