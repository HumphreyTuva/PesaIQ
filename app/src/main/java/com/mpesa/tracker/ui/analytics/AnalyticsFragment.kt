package com.mpesa.tracker.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.mpesa.tracker.MpesaTrackerApp
import com.mpesa.tracker.databinding.FragmentAnalyticsBinding
import com.mpesa.tracker.ui.dashboard.DashboardViewModel
import com.mpesa.tracker.ui.dashboard.DashboardViewModelFactory

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by activityViewModels {
        DashboardViewModelFactory((requireActivity().application as MpesaTrackerApp).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        setupPieChart()
        setupBarChart()
        observeViewModel()
    }

    private fun applyTheme() {
        val mode = com.mpesa.tracker.utils.ThemeManager.getUiMode(requireContext())
        val isDark = when (mode) {
            com.mpesa.tracker.utils.ThemeManager.UiMode.DARK -> true
            com.mpesa.tracker.utils.ThemeManager.UiMode.LIGHT -> false
            com.mpesa.tracker.utils.ThemeManager.UiMode.FOLLOW_SYSTEM -> 
                (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        val bgColor = if (isDark) Color.parseColor("#0C0D11") else Color.parseColor("#F5F5F7")
        val surfaceColor = if (isDark) Color.parseColor("#1A1C1E") else Color.WHITE
        val textColor = if (isDark) Color.WHITE else Color.BLACK
        val secondaryTextColor = if (isDark) Color.parseColor("#8890B0") else Color.parseColor("#6E6E73")

        binding.root.setBackgroundColor(bgColor)
        binding.cardDistribution.setCardBackgroundColor(surfaceColor)
        binding.cardTrends.setCardBackgroundColor(surfaceColor)
        
        binding.tvTitle.setTextColor(textColor)
        binding.tvTitleDistribution.setTextColor(secondaryTextColor)
        binding.tvTitleTrends.setTextColor(secondaryTextColor)
    }

    private fun setupPieChart() {
        val mode = com.mpesa.tracker.utils.ThemeManager.getUiMode(requireContext())
        val isDark = when (mode) {
            com.mpesa.tracker.utils.ThemeManager.UiMode.DARK -> true
            com.mpesa.tracker.utils.ThemeManager.UiMode.LIGHT -> false
            com.mpesa.tracker.utils.ThemeManager.UiMode.FOLLOW_SYSTEM -> 
                (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
        val secondaryTextColor = if (isDark) Color.parseColor("#8890B0") else Color.parseColor("#6E6E73")

        binding.pieChart.apply {
            description.isEnabled  = false
            setUsePercentValues(true)
            isDrawHoleEnabled      = true
            holeRadius             = 58f
            transparentCircleRadius = 62f
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.TRANSPARENT)
            legend.apply {
                isEnabled   = true
                textColor   = secondaryTextColor
                textSize    = 11f
                xEntrySpace = 12f
            }
            setNoDataText("No expenses this month")
            setNoDataTextColor(secondaryTextColor)
        }
    }

    private fun setupBarChart() {
        val mode = com.mpesa.tracker.utils.ThemeManager.getUiMode(requireContext())
        val isDark = when (mode) {
            com.mpesa.tracker.utils.ThemeManager.UiMode.DARK -> true
            com.mpesa.tracker.utils.ThemeManager.UiMode.LIGHT -> false
            com.mpesa.tracker.utils.ThemeManager.UiMode.FOLLOW_SYSTEM -> 
                (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
        val secondaryTextColor = if (isDark) Color.parseColor("#8890B0") else Color.parseColor("#6E6E73")
        val gridColor = if (isDark) Color.parseColor("#2A2C32") else Color.parseColor("#E5E5EA")

        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setTouchEnabled(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = secondaryTextColor
                textSize = 10f
                granularity = 1f
            }

            axisLeft.apply {
                textColor = secondaryTextColor
                textSize = 10f
                setDrawGridLines(true)
                this.gridColor = gridColor
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
            setNoDataText("No trend data available")
            setNoDataTextColor(secondaryTextColor)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            updatePieChart(state.categoryBreakdown.map { it.category to it.total.toFloat() })
            updateTrendChart(state.recentTransactions)
        }
    }

    private fun updateTrendChart(transactions: List<com.mpesa.tracker.data.model.Transaction>) {
        if (transactions.isEmpty()) {
            binding.barChart.clear()
            return
        }

        // Group by day for the last 7 days
        val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        val last7DaysExpenses = transactions
            .filter { it.isExpense }
            .groupBy { sdf.format(java.util.Date(it.timestamp)) }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }
            .toList()
            .takeLast(7)

        if (last7DaysExpenses.isEmpty()) {
            binding.barChart.clear()
            return
        }

        val entries = last7DaysExpenses.mapIndexed { index, pair ->
            BarEntry(index.toFloat(), pair.second)
        }

        val dataSet = BarDataSet(entries, "Daily Spending").apply {
            color = Color.parseColor("#00E676")
            val mode = com.mpesa.tracker.utils.ThemeManager.getUiMode(requireContext())
            val isDark = when (mode) {
                com.mpesa.tracker.utils.ThemeManager.UiMode.DARK -> true
                com.mpesa.tracker.utils.ThemeManager.UiMode.LIGHT -> false
                com.mpesa.tracker.utils.ThemeManager.UiMode.FOLLOW_SYSTEM -> 
                    (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            valueTextColor = if (isDark) Color.WHITE else Color.BLACK
            valueTextSize = 10f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return last7DaysExpenses.getOrNull(value.toInt())?.first ?: ""
                }
            }
            animateY(1000)
            invalidate()
        }
    }

    private fun updatePieChart(data: List<Pair<String, Float>>) {
        if (data.isEmpty()) { binding.pieChart.clear(); return }

        val chartColors = listOf(
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#00E676"), // Green
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#9D4EDD"), // Purple
            Color.parseColor("#FF5252"), // Red
            Color.parseColor("#FFD600"), // Yellow
            Color.parseColor("#E040FB"), // Pink
            Color.parseColor("#448AFF")  // Blue
        )

        val entries = data.take(8).map { (label, value) -> PieEntry(value, label) }
        val mode = com.mpesa.tracker.utils.ThemeManager.getUiMode(requireContext())
        val isDark = when (mode) {
            com.mpesa.tracker.utils.ThemeManager.UiMode.DARK -> true
            com.mpesa.tracker.utils.ThemeManager.UiMode.LIGHT -> false
            com.mpesa.tracker.utils.ThemeManager.UiMode.FOLLOW_SYSTEM -> 
                (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            colors       = chartColors
            valueTextSize  = 11f
            valueTextColor = if (isDark) Color.WHITE else Color.BLACK
            sliceSpace     = 3f
            valueFormatter = PercentFormatter(binding.pieChart)
        }

        binding.pieChart.apply {
            this.data = PieData(dataSet)
            animateY(900, Easing.EaseInOutCubic)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
