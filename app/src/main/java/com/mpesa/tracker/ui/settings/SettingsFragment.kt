package com.mpesa.tracker.ui.settings

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mpesa.tracker.MpesaTrackerApp
import com.mpesa.tracker.R
import com.mpesa.tracker.data.model.ExclusionRule
import com.mpesa.tracker.databinding.FragmentSettingsBinding
import com.mpesa.tracker.databinding.SheetAddExclusionBinding
import com.mpesa.tracker.ui.MainActivity
import com.mpesa.tracker.utils.ThemeManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory((requireActivity().application as MpesaTrackerApp).repository)
    }

    private lateinit var exclusionAdapter: ExclusionRuleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupColorPicker()
        setupFontPicker()
        setupThemePicker()
        setupExclusionList()
        observeExclusionRules()
    }

    private fun setupThemePicker() {
        val current = ThemeManager.getUiMode(requireContext())

        ThemeManager.UiMode.values().forEach { mode ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = mode.displayName
                isCheckable = true
                isChecked = mode == current
                // Consistent styling with font chips
                chipBackgroundColor = resources.getColorStateList(R.color.chip_bg_selector, null)
                setTextColor(resources.getColorStateList(R.color.chip_text_selector, null))
                chipStrokeWidth = 1f
                chipStrokeColor = resources.getColorStateList(R.color.chip_stroke_selector, null)
            }
            chip.setOnClickListener {
                ThemeManager.setUiMode(requireContext(), mode)
                Toast.makeText(requireContext(), "${mode.displayName} theme applied ✓", Toast.LENGTH_SHORT).show()
            }
            binding.themeChipGroup.addView(chip)
        }
    }

    // ── Color picker ──────────────────────────────────────────────────────────

    private fun setupColorPicker() {
        val currentColor = ThemeManager.getAccentColor(requireContext())

        ThemeManager.PRESETS.forEach { preset ->
            val circle = layoutInflater.inflate(
                R.layout.item_color_circle, binding.colorPickerRow, false
            )
            val colorView  = circle.findViewById<View>(R.id.view_color)
            val checkmark  = circle.findViewById<View>(R.id.view_check)

            colorView.backgroundTintList = ColorStateList.valueOf(preset.color)
            checkmark.visibility = if (preset.color == currentColor) View.VISIBLE else View.GONE

            circle.setOnClickListener {
                // Deselect all
                for (i in 0 until binding.colorPickerRow.childCount) {
                    binding.colorPickerRow.getChildAt(i)
                        .findViewById<View>(R.id.view_check)?.visibility = View.GONE
                }
                checkmark.visibility = View.VISIBLE

                // Save + apply immediately
                ThemeManager.setAccentColor(requireContext(), preset.hex)
                ThemeManager.applyToRoot(requireContext(), requireActivity().window.decorView)
                (activity as? MainActivity)?.refreshTheme()

                Toast.makeText(requireContext(), "${preset.name} applied ✓", Toast.LENGTH_SHORT).show()
            }

            binding.colorPickerRow.addView(circle)
        }

        // Custom color hex input
        binding.btnCustomColor.setOnClickListener { showCustomColorDialog() }
    }

    private fun showCustomColorDialog() {
        val sheet = BottomSheetDialog(requireContext(), R.style.Theme_MpesaTracker_BottomSheet)
        val v = layoutInflater.inflate(R.layout.sheet_custom_color, null)
        sheet.setContentView(v)

        val etHex    = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_hex)
        val btnApply = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_apply_color)
        val preview  = v.findViewById<View>(R.id.view_preview)

        etHex.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val hex = s.toString().let { if (it.startsWith("#")) it else "#$it" }
                runCatching {
                    preview.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hex))
                }
            }
        })

        btnApply.setOnClickListener {
            val raw = etHex.text.toString()
            val hex = if (raw.startsWith("#")) raw else "#$raw"
            runCatching { Color.parseColor(hex) }.onSuccess {
                ThemeManager.setAccentColor(requireContext(), hex)
                ThemeManager.applyToRoot(requireContext(), requireActivity().window.decorView)
                (activity as? MainActivity)?.refreshTheme()
                Toast.makeText(requireContext(), "Custom color applied ✓", Toast.LENGTH_SHORT).show()
                sheet.dismiss()
            }.onFailure {
                Toast.makeText(requireContext(), "Invalid hex color", Toast.LENGTH_SHORT).show()
            }
        }
        sheet.show()
    }

    // ── Font picker ───────────────────────────────────────────────────────────

    private fun setupFontPicker() {
        val current = ThemeManager.getFontStyle(requireContext())

        ThemeManager.FontStyle.values().forEach { style ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text       = style.displayName
                isCheckable = true
                isChecked   = style == current
                chipBackgroundColor = resources.getColorStateList(R.color.chip_bg_selector, null)
                setTextColor(resources.getColorStateList(R.color.chip_text_selector, null))
                chipStrokeWidth = 1f
                chipStrokeColor = resources.getColorStateList(R.color.chip_stroke_selector, null)
            }
            chip.setOnClickListener {
                for (i in 0 until binding.fontChipGroup.childCount)
                    (binding.fontChipGroup.getChildAt(i) as? com.google.android.material.chip.Chip)
                        ?.isChecked = false
                chip.isChecked = true

                ThemeManager.setFontStyle(requireContext(), style)
                ThemeManager.applyToRoot(requireContext(), requireActivity().window.decorView)
                Toast.makeText(requireContext(), "${style.displayName} font applied ✓", Toast.LENGTH_SHORT).show()
            }
            binding.fontChipGroup.addView(chip)
        }
    }

    // ── Exclusion rules ───────────────────────────────────────────────────────

    private fun setupExclusionList() {
        exclusionAdapter = ExclusionRuleAdapter(
            onToggle = { rule, enabled -> viewModel.toggleRule(rule, enabled) },
            onDelete = { rule ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remove rule?")
                    .setMessage("\"${rule.keyword}\" will no longer be excluded.")
                    .setPositiveButton("Remove") { _, _ -> viewModel.deleteRule(rule) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvExclusions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exclusionAdapter
            isNestedScrollingEnabled = false
        }

        binding.fabAddExclusion.setOnClickListener { showAddExclusionSheet() }
    }

    private fun observeExclusionRules() {
        viewModel.exclusionRules.observe(viewLifecycleOwner) { rules ->
            exclusionAdapter.submitList(rules)
            binding.tvNoExclusions.visibility = if (rules.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddExclusionSheet() {
        val sheet = BottomSheetDialog(requireContext(), R.style.Theme_MpesaTracker_BottomSheet)
        val sheetBinding = SheetAddExclusionBinding.inflate(layoutInflater)
        sheet.setContentView(sheetBinding.root)

        var selectedMatchType = ExclusionRule.MatchType.CONTAINS

        // Match type chip group
        val chips = mapOf(
            sheetBinding.chipContains   to ExclusionRule.MatchType.CONTAINS,
            sheetBinding.chipExact      to ExclusionRule.MatchType.EXACT,
            sheetBinding.chipStartsWith to ExclusionRule.MatchType.STARTS_WITH
        )
        chips.forEach { (chip, type) ->
            chip.setOnClickListener {
                chips.keys.forEach { it.isChecked = false }
                chip.isChecked   = true
                selectedMatchType = type
            }
        }

        sheetBinding.btnCancelExclusion.setOnClickListener { sheet.dismiss() }

        sheetBinding.btnSaveExclusion.setOnClickListener {
            val keyword = sheetBinding.etKeyword.text?.toString()?.trim() ?: ""
            if (keyword.isBlank()) {
                sheetBinding.etKeyword.error = "Enter a keyword"
                return@setOnClickListener
            }
            viewModel.addRule(keyword, selectedMatchType)
            Toast.makeText(requireContext(), "\"$keyword\" will now be excluded ✓", Toast.LENGTH_SHORT).show()
            sheet.dismiss()
        }

        sheet.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
