package com.mpesa.tracker.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.mpesa.tracker.R

/**
 * Manages user-chosen accent color, font, and dark/light theme.
 */
object ThemeManager {

    private const val PREFS_NAME   = "theme_prefs"
    private const val KEY_COLOR    = "accent_color"
    private const val KEY_FONT     = "font_style"
    private const val KEY_MODE     = "ui_mode"

    enum class UiMode(val displayName: String, val value: Int) {
        LIGHT("Light", AppCompatDelegate.MODE_NIGHT_NO),
        DARK("Dark", AppCompatDelegate.MODE_NIGHT_YES),
        FOLLOW_SYSTEM("System", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    // ── Preset palette ────────────────────────────────────────────────────────

    data class AccentColor(val name: String, val hex: String) {
        val color: Int get() = Color.parseColor(hex)
    }

    val PRESETS = listOf(
        AccentColor("M-Pesa Green", "#00A651"),
        AccentColor("Safaricom",    "#4CAF50"),
        AccentColor("Ocean Blue",   "#2196F3"),
        AccentColor("Violet",       "#9D4EDD"),
        AccentColor("Coral",        "#FF6348"),
        AccentColor("Amber",        "#FFB800"),
        AccentColor("Rose",         "#FF4081"),
        AccentColor("Teal",         "#00BCD4"),
        AccentColor("Indigo",       "#3F51B5"),
        AccentColor("Crimson",      "#F44336")
    )

    enum class FontStyle(val displayName: String) {
        DEFAULT("Default"),
        ROUNDED("Rounded"),
        SERIF("Serif"),
        MONOSPACE("Monospace")
    }

    // ── Read/Write ────────────────────────────────────────────────────────────

    fun getAccentColor(context: Context): Int {
        val hex = prefs(context).getString(KEY_COLOR, "#00A651") ?: "#00A651"
        return runCatching { Color.parseColor(hex) }.getOrDefault(Color.parseColor("#00A651"))
    }

    fun setAccentColor(context: Context, hex: String) {
        prefs(context).edit().putString(KEY_COLOR, hex).apply()
    }

    fun getFontStyle(context: Context): FontStyle {
        val name = prefs(context).getString(KEY_FONT, FontStyle.DEFAULT.name) ?: FontStyle.DEFAULT.name
        return runCatching { FontStyle.valueOf(name) }.getOrDefault(FontStyle.DEFAULT)
    }

    fun setFontStyle(context: Context, style: FontStyle) {
        prefs(context).edit().putString(KEY_FONT, style.name).apply()
    }

    fun getUiMode(context: Context): UiMode {
        val name = prefs(context).getString(KEY_MODE, UiMode.FOLLOW_SYSTEM.name) ?: UiMode.FOLLOW_SYSTEM.name
        return runCatching { UiMode.valueOf(name) }.getOrDefault(UiMode.FOLLOW_SYSTEM)
    }

    fun setUiMode(context: Context, mode: UiMode) {
        prefs(context).edit().putString(KEY_MODE, mode.name).apply()
        AppCompatDelegate.setDefaultNightMode(mode.value)
    }

    /** Call this in Activity.onCreate BEFORE super.onCreate or setContentView */
    fun applyUiMode(context: Context) {
        AppCompatDelegate.setDefaultNightMode(getUiMode(context).value)
    }

    // ── Apply to a live Activity ──────────────────────────────────────────────

    /**
     * Call this from Activity.onStart() or Fragment.onViewCreated()
     * to tint all accent-colored views in the given root.
     */
    fun applyToRoot(context: Context, root: View) {
        val color = getAccentColor(context)
        val font  = getFont(context)
        applyColorToView(root, color)
        if (font != null) applyFontToView(root, font)
    }

    /**
     * Apply accent color + font to BottomNavigationView specifically.
     */
    fun applyToBottomNav(context: Context, nav: BottomNavigationView) {
        val color = getAccentColor(context)
        val csl = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(color, Color.parseColor("#8890B0"))
        )
        nav.itemIconTintList = csl
        nav.itemTextColor    = csl
        // Optional: dynamic indicator color
        runCatching {
            nav.itemActiveIndicatorColor = ColorStateList.valueOf(lighten(color, 0.9f))
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun getFont(context: Context): Typeface? {
        return when (getFontStyle(context)) {
            FontStyle.DEFAULT   -> null
            FontStyle.ROUNDED   -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
            FontStyle.SERIF     -> Typeface.SERIF
            FontStyle.MONOSPACE -> Typeface.MONOSPACE
        }
    }

    private fun applyColorToView(view: View, color: Int) {
        val csl = ColorStateList.valueOf(color)

        when (view) {
            is ExtendedFloatingActionButton -> view.backgroundTintList = csl
            is FloatingActionButton         -> view.backgroundTintList = csl
            is LinearProgressIndicator      -> view.setIndicatorColor(color)
            is BottomNavigationView         -> { /* handled separately */ }
            is com.google.android.material.button.MaterialButton -> {
                if (view.backgroundTintList?.defaultColor != Color.TRANSPARENT && 
                    view.backgroundTintList?.defaultColor != Color.WHITE) {
                    view.backgroundTintList = csl
                }
                view.strokeColor = csl
            }
            is com.google.android.material.chip.Chip -> {
                // Simplified tinting for chips
                view.chipIconTint = csl
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyColorToView(view.getChildAt(i), color)
        }
    }

    private fun applyFontToView(view: View, font: Typeface) {
        if (view is TextView) view.typeface = font
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyFontToView(view.getChildAt(i), font)
        }
    }

    /** Blend a color toward black by the given factor (0 = original, 1 = black). */
    fun blendWithBlack(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * (1 - factor)).toInt()
        val g = (Color.green(color) * (1 - factor)).toInt()
        val b = (Color.blue(color) * (1 - factor)).toInt()
        return Color.argb(a, r, g, b)
    }

    /** Lighten a color toward white. */
    fun lighten(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) + (255 - Color.red(color)) * factor).toInt().coerceAtMost(255)
        val g = (Color.green(color) + (255 - Color.green(color)) * factor).toInt().coerceAtMost(255)
        val b = (Color.blue(color) + (255 - Color.blue(color)) * factor).toInt().coerceAtMost(255)
        return Color.argb(a, r, g, b)
    }
}
