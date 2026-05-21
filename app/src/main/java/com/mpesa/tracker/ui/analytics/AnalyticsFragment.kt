package com.mpesa.tracker.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.color.MaterialColors
import com.mpesa.tracker.MpesaTrackerApp
import com.mpesa.tracker.R
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.databinding.FragmentAnalyticsBinding
import com.mpesa.tracker.databinding.ItemMomComparisonBinding
import com.mpesa.tracker.ui.dashboard.DashboardViewModel
import com.mpesa.tracker.ui.dashboard.DashboardViewModelFactory
import com.mpesa.tracker.ui.transactions.TransactionAdapter
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by activityViewModels {
        DashboardViewModelFactory((requireActivity().application as MpesaTrackerApp).repository)
    }

    private lateinit var transactionAdapter: TransactionAdapter
    private var isGraphView = true
    
    private var month1Index = 1
    private var month2Index = 0
    private var allTransactions: List<Transaction> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGreeting()
        setupTransactionList()
        setupLineCharts()
        setupBarChart()
        setupListeners()
        observeViewModel()
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            else -> "Good evening,"
        }
        binding.tvGreeting.text = greeting
    }

    private fun setupTransactionList() {
        transactionAdapter = TransactionAdapter { /* Handle click if needed */ }
        binding.rvAnalyticsTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupLineCharts() {
        val onSurfaceVariant = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
        val outlineVariant = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOutlineVariant, Color.LTGRAY)

        val charts = listOf(binding.lineChart, binding.lineChartBalance)
        charts.forEach { chart ->
            chart.apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = onSurfaceVariant
                    axisLineColor = Color.TRANSPARENT
                    yOffset = 10f
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = outlineVariant
                    textColor = onSurfaceVariant
                    axisLineColor = Color.TRANSPARENT
                    setLabelCount(5, true)
                }

                axisRight.isEnabled = false
                legend.isEnabled = false

                setNoDataText("No trend data available")
                setNoDataTextColor(onSurfaceVariant)
            }
        }
    }

    private fun setupBarChart() {
        val onSurfaceVariant = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
        val outlineVariant = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOutlineVariant, Color.LTGRAY)

        binding.barChartIncomeExpenses.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = onSurfaceVariant
                axisLineColor = Color.TRANSPARENT
                granularity = 1f
                setCenterAxisLabels(true)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = outlineVariant
                textColor = onSurfaceVariant
                axisLineColor = Color.TRANSPARENT
                setLabelCount(5, true)
            }

            axisRight.isEnabled = false
            
            legend.apply {
                textColor = onSurfaceVariant
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }
    }

    private fun setupListeners() {
        binding.tvToggleView.setOnClickListener {
            isGraphView = !isGraphView
            updateViewToggle()
        }

        binding.btnViewAll.setOnClickListener {
            findNavController().navigate(R.id.transactionsFragment)
        }

        binding.btnMonth1Prev.setOnClickListener { changeMonth(1, 1) }
        binding.btnMonth1Next.setOnClickListener { changeMonth(1, -1) }
        binding.btnMonth2Prev.setOnClickListener { changeMonth(2, 1) }
        binding.btnMonth2Next.setOnClickListener { changeMonth(2, -1) }
    }

    private fun changeMonth(selector: Int, delta: Int) {
        val monthFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val grouped = allTransactions.groupBy { monthFmt.format(Date(it.timestamp)) }
            .toList()
            .sortedByDescending { runCatching { monthFmt.parse(it.first)?.time }.getOrDefault(0L) }

        if (grouped.size < 2) return

        if (selector == 1) {
            month1Index = (month1Index + delta).coerceIn(0, grouped.size - 1)
        } else {
            month2Index = (month2Index + delta).coerceIn(0, grouped.size - 1)
        }
        updateMoMComparison(allTransactions)
    }

    private fun updateViewToggle() {
        if (isGraphView) {
            binding.tvToggleView.text = "Switch to table ∨"
            binding.lineChart.visibility = View.VISIBLE
            binding.layoutTable.visibility = View.GONE
            binding.scrollChips.visibility = View.GONE
        } else {
            binding.tvToggleView.text = "Switch to graph ∨"
            binding.lineChart.visibility = View.GONE
            binding.layoutTable.visibility = View.VISIBLE
            binding.scrollChips.visibility = View.VISIBLE
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            allTransactions = state.recentTransactions
            transactionAdapter.submitList(state.recentTransactions.take(5))
            updateTrendData(state.recentTransactions)
            updateBarChartData(state.recentTransactions)
            updateNetBalanceTrend(state.recentTransactions)
            updateMoMComparison(state.recentTransactions)
            populateTable(state.recentTransactions)
        }
    }

    private fun updateTrendData(transactions: List<Transaction>) {
        if (transactions.isEmpty()) return

        val expenses = transactions.filter { it.isExpense }
        if (expenses.isEmpty()) {
            binding.lineChart.clear()
            return
        }

        val groupFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
        
        val last14Days = expenses
            .groupBy { groupFmt.format(Date(it.timestamp)) }
            .mapValues { entry -> 
                val date = groupFmt.parse(entry.key)
                val label = if (date != null) displayFmt.format(date) else ""
                label to entry.value.sumOf { it.amount }.toFloat()
            }
            .toList()
            .sortedBy { it.first } 
            .takeLast(10)

        val entries = last14Days.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second.second)
        }

        val accentGreen = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, Color.GREEN)

        val dataSet = LineDataSet(entries, "Daily Spending").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            color = accentGreen
            setCircleColor(accentGreen)
            lineWidth = 2.5f
            circleRadius = 3.5f
            setDrawCircleHole(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chart_gradient)
            highLightColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface, Color.WHITE)
            setDrawHorizontalHighlightIndicator(false)
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return last14Days.getOrNull(value.toInt())?.second?.first ?: ""
                }
            }
            xAxis.labelCount = 5
            animateX(800)
            invalidate()
        }
    }

    private fun updateBarChartData(transactions: List<Transaction>) {
        if (transactions.isEmpty()) return

        val monthFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val groupedByMonth = transactions.groupBy { monthFmt.format(Date(it.timestamp)) }
            .toList()
            .sortedBy { runCatching { monthFmt.parse(it.first)?.time }.getOrDefault(0L) }
            .takeLast(3)

        if (groupedByMonth.isEmpty()) return

        val incomeEntries = ArrayList<BarEntry>()
        val expenseEntries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        groupedByMonth.forEachIndexed { index, (month, txs) ->
            val income = txs.filter { !it.isExpense }.sumOf { it.amount }.toFloat()
            val expense = txs.filter { it.isExpense }.sumOf { it.amount }.toFloat()
            incomeEntries.add(BarEntry(index.toFloat(), income))
            expenseEntries.add(BarEntry(index.toFloat(), expense))
            labels.add(month.split(" ")[0]) // Just Jan, Feb, etc.
        }

        val accentGreen = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, Color.GREEN)
        val accentRed = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorError, Color.RED)

        val incomeSet = BarDataSet(incomeEntries, "Income").apply {
            color = accentGreen
            setDrawValues(false)
            barBorderWidth = 0f
        }
        val expenseSet = BarDataSet(expenseEntries, "Expenses").apply {
            color = accentRed
            setDrawValues(false)
            barBorderWidth = 0f
        }

        val data = BarData(incomeSet, expenseSet)
        val groupSpace = 0.3f
        val barSpace = 0.05f
        val barWidth = 0.3f 

        data.barWidth = barWidth
        binding.barChartIncomeExpenses.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = groupedByMonth.size.toFloat()
            xAxis.granularity = 1f
            xAxis.setCenterAxisLabels(true)
            
            groupBars(0f, groupSpace, barSpace)
            animateY(1000)
            invalidate()
        }
    }

    private fun updateNetBalanceTrend(transactions: List<Transaction>) {
        if (transactions.isEmpty()) return

        val monthFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val groupedByMonth = transactions.groupBy { monthFmt.format(Date(it.timestamp)) }
            .toList()
            .sortedBy { runCatching { monthFmt.parse(it.first)?.time }.getOrDefault(0L) }
            .takeLast(6)

        if (groupedByMonth.isEmpty()) return

        val entries = groupedByMonth.mapIndexed { index, pair ->
            val net = pair.second.sumOf { if (it.isExpense) -it.amount else it.amount }.toFloat()
            Entry(index.toFloat(), net)
        }

        val primaryColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, Color.BLUE)
        val surfaceColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSurface, Color.BLACK)
        val onSurface = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface, Color.WHITE)

        val dataSet = LineDataSet(entries, "Net Balance").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            color = primaryColor
            setCircleColor(primaryColor)
            circleHoleColor = surfaceColor
            lineWidth = 2.5f
            circleRadius = 5f
            setDrawCircleHole(true)
            setDrawValues(true)
            valueTextColor = onSurface
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value >= 1000 || value <= -1000) "KSh %.0fk".format(value/1000) else "KSh %.0f".format(value)
                }
            }
            setDrawFilled(true)
            fillColor = primaryColor
            fillAlpha = 50
        }

        binding.lineChartBalance.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return groupedByMonth.getOrNull(value.toInt())?.first?.split(" ")?.get(0) ?: ""
                }
            }
            xAxis.labelCount = groupedByMonth.size
            animateX(800)
            invalidate()
        }
    }

    private fun updateMoMComparison(transactions: List<Transaction>) {
        val monthFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val groupedByMonth = transactions.groupBy { monthFmt.format(Date(it.timestamp)) }
            .toList()
            .sortedByDescending { runCatching { monthFmt.parse(it.first)?.time }.getOrDefault(0L) }

        if (groupedByMonth.size < 2) {
            binding.momComparisonContainer.removeAllViews()
            return
        }

        // Ensure indices are within bounds
        month1Index = month1Index.coerceIn(0, groupedByMonth.size - 1)
        month2Index = month2Index.coerceIn(0, groupedByMonth.size - 1)
        
        // If they are the same, pick different ones if possible
        if (month1Index == month2Index) {
            if (month1Index < groupedByMonth.size - 1) month2Index = month1Index + 1
            else if (month1Index > 0) month2Index = month1Index - 1
        }

        val month1Data = groupedByMonth[month1Index]
        val month2Data = groupedByMonth[month2Index]

        binding.tvMonth1Name.text = month1Data.first
        binding.tvMonth2Name.text = month2Data.first

        val m1Short = month1Data.first.split(" ")[0]
        val m2Short = month2Data.first.split(" ")[0]

        val m1Cats = month1Data.second.filter { it.isExpense }.groupBy { it.category }
        val m2Cats = month2Data.second.filter { it.isExpense }.groupBy { it.category }

        val allCategories = (m1Cats.keys + m2Cats.keys).distinct()
        
        binding.momComparisonContainer.removeAllViews()

        allCategories.sortedByDescending { m1Cats[it]?.sumOf { tx -> tx.amount } ?: 0.0 }.forEach { category ->
            val m1Amount = m1Cats[category]?.sumOf { it.amount } ?: 0.0
            val m2Amount = m2Cats[category]?.sumOf { it.amount } ?: 0.0
            
            if (m1Amount == 0.0 && m2Amount == 0.0) return@forEach

            val diff = m2Amount - m1Amount
            val percent = if (m1Amount > 0) (diff / m1Amount) * 100 else if (m2Amount > 0) 100.0 else 0.0

            val itemBinding = ItemMomComparisonBinding.inflate(layoutInflater, binding.momComparisonContainer, false)
            itemBinding.tvCategoryName.text = category
            
            itemBinding.tvMonth1Info.text = "$m1Short: KSh%,.2f".format(m1Amount)
            itemBinding.tvMonth2Info.text = if (m2Amount > 0) "$m2Short: KSh%,.2f".format(m2Amount) else "$m2Short: —"
            
            itemBinding.tvPercentage.text = "%s%.1f%%".format(if (diff >= 0 && percent != 0.0) "+" else "", percent)
            itemBinding.tvDiffAmount.text = "KSh%,.2f".format(Math.abs(diff))

            val isImprovement = diff <= 0 // Lower spending is good
            val color = if (isImprovement) {
                ContextCompat.getColor(requireContext(), R.color.accent_green)
            } else {
                ContextCompat.getColor(requireContext(), R.color.accent_red)
            }
            val icon = if (isImprovement) R.drawable.ic_trending_down else R.drawable.ic_trending_up

            itemBinding.tvPercentage.setTextColor(color)
            itemBinding.tvDiffAmount.setTextColor(color)
            itemBinding.ivTrend.setImageResource(icon)
            itemBinding.ivTrend.setColorFilter(color)

            binding.momComparisonContainer.addView(itemBinding.root)
        }
    }

    private fun populateTable(transactions: List<com.mpesa.tracker.data.model.Transaction>) {
        binding.tableRowsContainer.removeAllViews()
        
        val monthFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val monthData = transactions
            .filter { it.isExpense }
            .groupBy { monthFmt.format(Date(it.timestamp)).uppercase() }
            .mapValues { entry ->
                val total = entry.value.sumOf { it.amount }
                val cal = Calendar.getInstance()
                val monthStr = entry.key
                val date = monthFmt.parse(monthStr)
                if (date != null) cal.time = date
                val days = if (cal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && 
                               cal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                } else {
                    cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                }
                val avg = if (days > 0) total / days else total
                total to avg
            }
            .toList()
            .sortedByDescending { 
                runCatching { monthFmt.parse(it.first)?.time }.getOrDefault(0L)
            }

        var grandTotal = 0.0
        var totalAvgCount = 0
        var totalAvgSum = 0.0

        monthData.forEach { (month, data) ->
            val row = layoutInflater.inflate(R.layout.item_analytics_table_row, binding.tableRowsContainer, false)
            row.findViewById<TextView>(R.id.tv_month).text = month.split(" ")[0] // Just the month name
            row.findViewById<TextView>(R.id.tv_spent).text = "KSH %,.0f".format(data.first)
            row.findViewById<TextView>(R.id.tv_avg).text = "KSH %,.0f".format(data.second)
            binding.tableRowsContainer.addView(row)
            
            grandTotal += data.first
            totalAvgSum += data.second
            totalAvgCount++
        }

        binding.tvTotalSpent.text = "KSH %,.0f".format(grandTotal)
        binding.tvTotalAvg.text = "KSH %,.0f".format(if (totalAvgCount > 0) totalAvgSum / totalAvgCount else 0.0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
