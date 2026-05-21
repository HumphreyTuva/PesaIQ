package com.mpesa.tracker.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mpesa.tracker.MpesaTrackerApp
import com.mpesa.tracker.data.model.BudgetWithSpent
import com.mpesa.tracker.ui.components.GradientEdgeCard
import com.mpesa.tracker.ui.theme.MpesaTrackerTheme
import com.mpesa.tracker.utils.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {

    private val viewModel: BudgetViewModel by viewModels {
        BudgetViewModelFactory((requireActivity().application as MpesaTrackerApp).repository)
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
                    BudgetScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: BudgetViewModel) {
    val budgetsWithSpent by viewModel.budgetsWithSpent.observeAsState(emptyList())
    val monthName = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time) }

    var showAddSheet by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetWithSpent?>(null) }

    val totalBudget = budgetsWithSpent.sumOf { it.budget.limitAmount }
    val totalSpent = budgetsWithSpent.sumOf { it.spent }
    val remaining = (totalBudget - totalSpent).coerceAtLeast(0.0)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Text(
                        "Monthly Budget",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        monthName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    // Summary Cards
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GradientEdgeCard(
                            colors = listOf(ComposeColor(0xFF536D7A), ComposeColor(0xFF81A1C1)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column {
                                Text(
                                    "TOTAL BUDGET",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "%,.0f".format(totalBudget),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        GradientEdgeCard(
                            colors = listOf(ComposeColor(0xFF00E676), ComposeColor(0xFF7BED9F)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column {
                                Text(
                                    "REMAINING",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "%,.0f".format(remaining),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Budget List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 20.dp, end = 20.dp, top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (budgetsWithSpent.isEmpty()) {
                        item {
                            Box(Modifier.fillParentMaxHeight(0.6f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No budgets set for this month", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(budgetsWithSpent) { item ->
                            BudgetItem(
                                item = item,
                                onClick = { editingBudget = item }
                            )
                        }
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
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Add, "Add Budget")
                }
            }
        }

        if (showAddSheet) {
            val budgetsWithSpentValue = budgetsWithSpent
            val availableCategories by viewModel.availableCategories.observeAsState(emptyList())
            
            val existingCategories = budgetsWithSpentValue.map { it.budget.category }
            val filteredCategories = availableCategories.filter { it !in existingCategories }
            
            AddBudgetSheet(
                categories = filteredCategories,
                onSave = { cat, limit ->
                    viewModel.addBudget(cat, limit)
                    showAddSheet = false
                },
                onDismiss = { showAddSheet = false }
            )
        }

        editingBudget?.let { item ->
            AddBudgetSheet(
                categories = listOf(item.budget.category),
                initialAmount = item.budget.limitAmount.toString(),
                isEditing = true,
                onSave = { _, limit ->
                    viewModel.updateBudget(item.budget, limit)
                    editingBudget = null
                },
                onDelete = {
                    viewModel.deleteBudget(item.budget)
                    editingBudget = null
                },
                onDismiss = { editingBudget = null }
            )
        }
    }
}

@Composable
fun BudgetItem(
    item: BudgetWithSpent,
    onClick: () -> Unit
) {
    val progress = (item.spent / item.budget.limitAmount).toFloat().coerceIn(0f, 1f)
    val color = when {
        progress > 0.9f -> ComposeColor(0xFFFF5252)
        progress > 0.7f -> ComposeColor(0xFFFF9800)
        else -> ComposeColor(0xFF00E676)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.budget.category,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ksh %,.0f / %,.0f".format(item.spent, item.budget.limitAmount),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Custom Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color)
                )
            }

            Spacer(Modifier.height(8.dp))
            
            Text(
                text = if (progress >= 1f) "Over budget!" else "%,.0f%% spent".format(progress * 100),
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetSheet(
    categories: List<String>,
    initialAmount: String = "",
    isEditing: Boolean = false,
    onSave: (String, Double) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var amount by remember { mutableStateOf(initialAmount) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (isEditing) "Edit Budget" else "Set Category Budget",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))

            if (!isEditing) {
                Text("Category", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                if (categories.isEmpty()) {
                    Text("All categories already have budgets", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                } else {
                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    cat,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            } else {
                Text(
                    "Category: $selectedCategory",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Monthly Limit (Ksh)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isEditing && onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete")
                    }
                }

                Button(
                    onClick = {
                        val limit = amount.toDoubleOrNull()
                        if (limit != null && limit > 0 && selectedCategory.isNotEmpty()) {
                            onSave(selectedCategory, limit)
                        }
                    },
                    modifier = Modifier.weight(2f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedCategory.isNotEmpty()
                ) {
                    Text(
                        text = if (isEditing) "Update Budget" else "Save Budget",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
