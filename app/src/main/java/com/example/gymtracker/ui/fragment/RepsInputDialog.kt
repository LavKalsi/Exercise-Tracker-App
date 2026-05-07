package com.example.gymtracker.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import com.example.gymtracker.databinding.DialogRepsInputBinding

class RepsInputDialog(
    private val exerciseName: String,
    private val onConfirm: (reps: Int, weight: Float) -> Unit,
    private val onSkip: () -> Unit
) : DialogFragment() {

    private var _b: DialogRepsInputBinding? = null
    private val b get() = _b!!

    private var reps = 10
    private var weight = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = DialogRepsInputBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.tvExerciseName.text = exerciseName

        // ── Reps input ──────────────────────────────────────────────────────
        b.etReps.setText(reps.toString())

        b.btnMinus.setOnClickListener {
            if (reps > 1) {
                reps--
                b.etReps.setText(reps.toString())
                b.etReps.setSelection(b.etReps.text?.length ?: 0)
            }
        }
        b.btnPlus.setOnClickListener {
            reps++
            b.etReps.setText(reps.toString())
            b.etReps.setSelection(b.etReps.text?.length ?: 0)
        }

        b.etReps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toIntOrNull()
                if (v != null && v > 0) {
                    reps = v
                }
            }
        })

        // ── Weight input ──────────────────────────────────────────────────────
        b.etWeight.setText(formatWeight(weight))

        b.btnWeightMinus.setOnClickListener {
            weight = (weight - 2.5f).coerceAtLeast(0f)
            b.etWeight.setText(formatWeight(weight))
            b.etWeight.setSelection(b.etWeight.text?.length ?: 0)
        }
        b.btnWeightPlus.setOnClickListener {
            weight += 2.5f
            b.etWeight.setText(formatWeight(weight))
            b.etWeight.setSelection(b.etWeight.text?.length ?: 0)
        }

        b.etWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toFloatOrNull()
                if (v != null && v >= 0f) {
                    weight = v
                }
            }
        })

        // ── Actions ───────────────────────────────────────────────────────────
        b.btnConfirm.setOnClickListener {
            onConfirm(reps, weight)
            dismiss()
        }

        b.btnSkip.setOnClickListener {
            onSkip()
            dismiss()
        }
    }

    private fun formatWeight(w: Float): String =
        if (w == w.toLong().toFloat()) w.toLong().toString() else String.format("%.1f", w)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
