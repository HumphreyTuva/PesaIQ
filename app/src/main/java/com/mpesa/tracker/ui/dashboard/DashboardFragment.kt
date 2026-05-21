package com.mpesa.tracker.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mpesa.tracker.MpesaTrackerApp
import com.mpesa.tracker.R
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.ui.components.GradientEdgeCard
import com.mpesa.tracker.ui.components.SoftActionButton
import com.mpesa.tracker.ui.components.SpendingOverviewCard
import com.mpesa.tracker.ui.components.TransactionItem
import com.mpesa.tracker.utils.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by activityViewModels {
        DashboardViewModelFactory((requireActivity().application as MpesaTrackerApp).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    DashboardScreen(
                        viewModel = viewModel,
                        onTransactionClick = { /* Handle transaction click if needed */ },
                        onSeeAllClick = { findNavController().navigate(R.id.transactionsFragment) },
                        onSettingsClick = { findNavController().navigate(R.id.settingsFragment) },
                        onScanClick = {
                            Toast.makeText(requireContext(), "Scanning for new messages...", Toast.LENGTH_SHORT).show()
                            (activity as? com.mpesa.tracker.ui.MainActivity)?.scanHistoricalSms()
                        },
                        onInsightsClick = { findNavController().navigate(R.id.analyticsFragment) },
                        onExportClick = { findNavController().navigate(R.id.exportFragment) }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onTransactionClick: (Transaction) -> Unit,
    onSeeAllClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onScanClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onExportClick: () -> Unit
) {
    val state by viewModel.state.observeAsState()
    val selectedMonth by viewModel.selectedMonth.observeAsState()
    val selectedYear by viewModel.selectedYear.observeAsState()
    val context = LocalContext.current
    val currentMode = remember { mutableStateOf(ThemeManager.getUiMode(context)) }
    
    val isDark = when (currentMode.value) {
        ThemeManager.UiMode.DARK -> true
        ThemeManager.UiMode.LIGHT -> false
        ThemeManager.UiMode.FOLLOW_SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val bgColor = if (isDark) ComposeColor(0xFF0C0D11) else ComposeColor(0xFFF5F5F7)
    val surfaceColor = if (isDark) ComposeColor(0xFF1A1C1E) else ComposeColor(0xFFFFFFFF)
    val textColor = if (isDark) ComposeColor.White else ComposeColor.Black
    val secondaryTextColor = if (isDark) ComposeColor(0xFF8890B0) else ComposeColor(0xFF6E6E73)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // --- FIXED TOP PART ---
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "M-Tracker",
                    color = textColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val newMode = if (currentMode.value == ThemeManager.UiMode.DARK) {
                                ThemeManager.UiMode.LIGHT
                            } else {
                                ThemeManager.UiMode.DARK
                            }
                            ThemeManager.setUiMode(context, newMode)
                            currentMode.value = newMode
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (currentMode.value == ThemeManager.UiMode.DARK) {
                                Icons.Default.LightMode
                            } else {
                                Icons.Default.DarkMode
                            },
                            contentDescription = "Toggle Theme",
                            tint = secondaryTextColor
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    IconButton(onClick = onSettingsClick, modifier = Modifier.size(26.dp)) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = secondaryTextColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SoftActionButton(
                    text = "Scan",
                    accentColor = ComposeColor(0xFF536D7A),
                    modifier = Modifier.weight(1f),
                    isDark = isDark,
                    icon = { Icon(Icons.Default.Refresh, null, tint = ComposeColor(0xFF81A1C1), modifier = Modifier.size(16.dp)) },
                    onClick = onScanClick
                )
                Spacer(Modifier.width(8.dp))
                SoftActionButton(
                    text = "Insights",
                    accentColor = ComposeColor(0xFF8B6E5A),
                    modifier = Modifier.weight(1f),
                    isDark = isDark,
                    icon = { Icon(Icons.Default.PieChart, null, tint = ComposeColor(0xFFD08770), modifier = Modifier.size(16.dp)) },
                    onClick = onInsightsClick
                )
                Spacer(Modifier.width(8.dp))
                SoftActionButton(
                    text = "Export",
                    accentColor = ComposeColor(0xFF8F5E5E),
                    modifier = Modifier.weight(1f),
                    isDark = isDark,
                    icon = { Icon(Icons.Default.Share, null, tint = ComposeColor(0xFFBF616A), modifier = Modifier.size(16.dp)) },
                    onClick = onExportClick
                )
            }

            Spacer(Modifier.height(24.dp))

            // Month Navigation & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Overview",
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .width(40.dp)
                            .height(3.dp)
                            .background(ComposeColor(0xFFFF9800))
                    )
                }

                Row(
                    modifier = Modifier
                        .background(surfaceColor, CircleShape)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = textColor)
                    }
                    val monthLabel = remember(selectedMonth, selectedYear) {
                        if (selectedMonth != null && selectedYear != null) {
                            val cal = Calendar.getInstance().apply { set(selectedYear!!, selectedMonth!!, 1) }
                            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
                        } else ""
                    }
                    Text(
                        text = monthLabel,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { viewModel.nextMonth() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = textColor)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Spending Overview Card
            state?.let {
                val cal = Calendar.getInstance()
                val today = cal.get(Calendar.DAY_OF_MONTH)
                val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val remainingDays = totalDays - today

                SpendingOverviewCard(
                    remainingDays = remainingDays,
                    balance = it.balance,
                    monthlyLimit = it.monthlyLimit,
                    isDark = isDark
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientEdgeCard(
                        colors = listOf(ComposeColor(0xFF4C8D9B), ComposeColor(0xFF88C0D0)),
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    ) {
                        Column {
                            Text("INCOME", color = secondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("%,.0f".format(it.totalIncome), color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    GradientEdgeCard(
                        colors = listOf(ComposeColor(0xFFBF616A), ComposeColor(0xFFD08770)),
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    ) {
                        Column {
                            Text("EXPENSES", color = secondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("%,.0f".format(it.totalExpenses), color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp)) // Increased spacing to prevent overlap

        // --- SCROLLABLE TRANSACTIONS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See all →",
                color = ComposeColor(0xFFFF9800),
                fontSize = 13.sp,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Transaction List Container (Scrollable part)
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = surfaceColor,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                val transactions = state?.recentTransactions ?: emptyList()
                items(transactions) { tx ->
                    TransactionItem(
                        transaction = tx,
                        onClick = { onTransactionClick(tx) },
                        isDark = isDark
                    )
                }
                
                if (transactions.isEmpty() && state?.isLoading == false) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No transactions found", color = ComposeColor.Gray)
                        }
                    }
                }
            }

            if (state?.isLoading == true) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ComposeColor(0xFFFF9800))
                }
            }
        }
    }
}
