package com.example.gymtracker.ui.fragment

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.gymtracker.databinding.DialogCreateExerciseBinding

class CreateExerciseDialog(
    private val onSubmit: (name: String, muscle: String, equip: String) -> Unit
) : DialogFragment() {

    private val muscles = listOf("Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Core", "Cardio", "Other")
    private val equips  = listOf("Barbell", "Dumbbell", "Cable", "Machine", "Bodyweight", "Equipment", "Other")

    private var selectedMuscle = ""
    private var selectedEquip  = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val b = DialogCreateExerciseBinding.inflate(LayoutInflater.from(requireContext()))

        buildChips(b.chipsMuscle, muscles) { selectedMuscle = it }
        buildChips(b.chipsEquip, equips)   { selectedEquip  = it }

        b.btnCreate.setOnClickListener {
            val name = b.etName.text?.toString()?.trim() ?: ""
            if (name.isNotBlank()) {
                onSubmit(name, selectedMuscle.ifBlank { "Other" }, selectedEquip.ifBlank { "Other" })
                dismiss()
            } else {
                b.etName.error = "Required"
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(b.root)
            .create()
            .also {
                it.window?.setBackgroundDrawableResource(android.R.color.transparent)
                it.window?.setWindowAnimations(com.example.gymtracker.R.style.DialogAnimation)
            }
    }

    private fun buildChips(container: LinearLayout, options: List<String>, onSelect: (String) -> Unit) {
        val ctx = requireContext()
        val dp8 = (8 * ctx.resources.displayMetrics.density).toInt()
        val dp5 = (5 * ctx.resources.displayMetrics.density).toInt()
        val dp6 = (6 * ctx.resources.displayMetrics.density).toInt()

        val outValue = TypedValue()
        ctx.theme.resolveAttribute(com.example.gymtracker.R.attr.appOnSurfaceVariantColor, outValue, true)
        val variantColor = outValue.data

        options.forEach { opt ->
            val chip = TextView(ctx).apply {
                text = opt
                textSize = 12f
                setTextColor(variantColor)
                setPadding(dp8, dp5, dp8, dp5)
                setBackgroundResource(com.example.gymtracker.R.drawable.bg_chip_unselected)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = dp6
                layoutParams = lp
            }
            chip.setOnClickListener {
                // Reset all chips in container
                for (i in 0 until container.childCount) {
                    val c = container.getChildAt(i) as TextView
                    c.setBackgroundResource(com.example.gymtracker.R.drawable.bg_chip_unselected)
                    c.setTextColor(variantColor)
                }
                chip.setBackgroundResource(com.example.gymtracker.R.drawable.bg_chip_selected)
                chip.setTextColor(Color.WHITE)
                onSelect(opt)
            }
            container.addView(chip)
        }
    }
}
