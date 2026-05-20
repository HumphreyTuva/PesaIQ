package com.mpesa.tracker.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mpesa.tracker.MpesaTrackerApp
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.ui.components.TransactionEditSheet
import com.mpesa.tracker.ui.components.TransactionItem
import com.mpesa.tracker.utils.ThemeManager

class TransactionsFragment : Fragment() {

    private val viewModel: TransactionsViewModel by viewModels {
        TransactionsViewModelFactory((requireActivity().application as MpesaTrackerApp).repository)
    }

    private val categories = listOf(
        "All", "Excluded", "Groceries", "Utilities", "Transport", "Food & Dining",
        "Airtime", "Entertainment", "Health", "Education",
        "Transfer", "Withdrawal", "Income", "Other"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
                    
                    TransactionsScreen(
                        viewModel = viewModel,
                        categories = categories,
                        onTransactionClick = { tx -> editingTransaction = tx }
                    )

                    editingTransaction?.let { tx ->
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val currentMode = remember { mutableStateOf(ThemeManager.getUiMode(context)) }
                        val isDark = when (currentMode.value) {
                            ThemeManager.UiMode.DARK -> true
                            ThemeManager.UiMode.LIGHT -> false
                            ThemeManager.UiMode.FOLLOW_SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                        }
                        
                        TransactionEditSheet(
                            tx = tx,
                            categories = categories,
                            isDark = isDark,
                            onSave = { updated ->
                                viewModel.updateTransaction(updated)
                                if (updated.category != tx.category && !updated.recipient.isNullOrEmpty()) {
                                    viewModel.updateTransactionWithRule(updated, updated.category)
                                }
                                editingTransaction = null
                            },
                            onDismiss = { editingTransaction = null }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    categories: List<String>,
    onTransactionClick: (Transaction) -> Unit
) {
    val transactions by viewModel.filteredTransactions.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val context = androidx.compose.ui.platform.LocalContext.current
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
    val borderColor = if (isDark) ComposeColor(0xFF2A2C32) else ComposeColor(0xFFE5E5EA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Header & Search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Transactions",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Skueomorphic Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.setSearchQuery(it)
                },
                placeholder = { Text("Search transactions...", color = secondaryTextColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ComposeColor(0xFFFF9800),
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = bgColor,
                    unfocusedContainerColor = bgColor,
                    cursorColor = ComposeColor(0xFFFF9800),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = secondaryTextColor) }
            )

            Spacer(Modifier.height(16.dp))

            // Category Filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) ComposeColor(0xFFFF9800) else (if (isDark) ComposeColor(0xFF232529) else ComposeColor(0xFFE5E5EA)),
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (isSelected) ComposeColor(0xFFFF9800) else borderColor,
                                CircleShape
                            )
                            .clickable {
                                selectedCategory = category
                                viewModel.setFilterCategory(if (category == "All") null else category)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) ComposeColor.Black else textColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Transactions List
        Box(modifier = Modifier.weight(1f)) {
            if (transactions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📭", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No transactions found",
                        color = secondaryTextColor,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(transactions) { tx ->
                        TransactionItem(
                            transaction = tx,
                            onClick = { onTransactionClick(tx) },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}
