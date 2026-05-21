package com.mpesa.tracker.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
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
import com.mpesa.tracker.ui.theme.MpesaTrackerTheme
import com.mpesa.tracker.utils.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TransactionsFragment : Fragment() {

    private val viewModel: TransactionsViewModel by viewModels {
        TransactionsViewModelFactory((requireActivity().application as MpesaTrackerApp).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val context = LocalContext.current
                val currentMode = remember { mutableStateOf(ThemeManager.getUiMode(context)) }

                val isDark = when (currentMode.value) {
                    ThemeManager.UiMode.DARK -> true
                    ThemeManager.UiMode.LIGHT -> false
                    ThemeManager.UiMode.FOLLOW_SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                }

                MpesaTrackerTheme(darkTheme = isDark) {
                    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
                    var showAddManual by remember { mutableStateOf(false) }

                    val dbCategories by viewModel.categories.observeAsState(emptyList())
                    val categoryNames = remember(dbCategories) {
                        listOf("All", "Excluded") + dbCategories.map { it.name }
                    }
                    
                    TransactionsScreen(
                        viewModel = viewModel,
                        categories = categoryNames,
                        onTransactionClick = { tx -> editingTransaction = tx },
                        onAddManualClick = { showAddManual = true }
                    )

                    if (showAddManual) {
                        TransactionEditSheet(
                            tx = null,
                            categories = dbCategories.map { it.name },
                            isManualEntry = true,
                            onSave = { manualTx ->
                                viewModel.addManualTransaction(manualTx)
                                showAddManual = false
                            },
                            onDismiss = { showAddManual = false }
                        )
                    }

                    editingTransaction?.let { tx ->
                        TransactionEditSheet(
                            tx = tx,
                            categories = dbCategories.map { it.name },
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
    onTransactionClick: (Transaction) -> Unit,
    onAddManualClick: () -> Unit
) {
    val transactions by viewModel.filteredTransactions.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header & Search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Transactions",
                color = MaterialTheme.colorScheme.onSurface,
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
                placeholder = { Text("Search transactions...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
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
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(
                        items = transactions,
                        key = { it.id }
                    ) { tx ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                    val wasExcluded = tx.isExcluded
                                    if (wasExcluded) {
                                        viewModel.includeTransaction(tx)
                                    } else {
                                        viewModel.excludeTransaction(tx)
                                    }
                                    
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = if (wasExcluded) "Transaction restored" else "Transaction excluded",
                                            actionLabel = "UNDO",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            if (wasExcluded) viewModel.excludeTransaction(tx)
                                            else viewModel.includeTransaction(tx)
                                        }
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val direction = dismissState.dismissDirection
                                if (direction == SwipeToDismissBoxValue.Settled) return@SwipeToDismissBox

                                val color by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.Settled -> ComposeColor.Transparent
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    }, label = "background"
                                )
                                
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) 
                                        Alignment.CenterStart else Alignment.CenterEnd
                                ) {
                                    Icon(
                                        if (tx.isExcluded) Icons.Default.Add else Icons.Default.Block,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            content = {
                                TransactionItem(
                                    transaction = tx,
                                    onClick = { onTransactionClick(tx) }
                                )
                            }
                        )
                    }
                }
            }

            // Circular Add Button (Aligned Bottom End/Right)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 32.dp)
            ) {
                FloatingActionButton(
                    onClick = onAddManualClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Add, "Add Cash Transaction")
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
                snackbar = { data ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            TextButton(onClick = { data.performAction() }) {
                                Text(data.visuals.actionLabel ?: "", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(data.visuals.message)
                    }
                }
            )
        }
    }
}
