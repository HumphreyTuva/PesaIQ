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
                MaterialTheme {
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

    var showAddSheet by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetWithSpent?>(null) }

    val totalBudget = budgetsWithSpent.sumOf { it.budget.limitAmount }
    val totalSpent = budgetsWithSpent.sumOf { it.spent }
    val remaining = (totalBudget - totalSpent).coerceAtLeast(0.0)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = bgColor
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
                        .background(surfaceColor)
                        .padding(20.dp)
                ) {
                    Text("Monthly Budget", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text(monthName, color = secondaryTextColor, fontSize = 14.sp)

                    Spacer(Modifier.height(24.dp))

                    // Summary Cards
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GradientEdgeCard(
                            colors = listOf(ComposeColor(0xFF536D7A), ComposeColor(0xFF81A1C1)),
                            modifier = Modifier.weight(1f),
                            isDark = isDark
                        ) {
                            Column {
                                Text("TOTAL BUDGET", color = secondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("%,.0f".format(totalBudget), color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        GradientEdgeCard(
                            colors = listOf(ComposeColor(0xFF00E676), ComposeColor(0xFF7BED9F)),
                            modifier = Modifier.weight(1f),
                            isDark = isDark
                        ) {
                            Column {
                                Text("REMAINING", color = secondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("%,.0f".format(remaining), color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
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
                                Text("No budgets set for this month", color = ComposeColor.Gray)
                            }
                        }
                    } else {
                        items(budgetsWithSpent) { item ->
                            BudgetItem(
                                item = item,
                                isDark = isDark,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                surfaceColor = surfaceColor,
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
                    containerColor = ComposeColor(0xFFFF9800),
                    contentColor = ComposeColor.White,
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
                isDark = isDark,
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
                isDark = isDark,
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
    isDark: Boolean,
    textColor: ComposeColor,
    secondaryTextColor: ComposeColor,
    surfaceColor: ComposeColor,
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
        color = surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) ComposeColor(0xFF2A2D32) else ComposeColor(0xFFE5E5EA)),
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
                Text(item.budget.category, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Ksh %,.0f / %,.0f".format(item.spent, item.budget.limitAmount),
                    color = secondaryTextColor,
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
                    .background(if (isDark) ComposeColor(0xFF232529) else ComposeColor(0xFFE5E5EA))
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
    isDark: Boolean,
    initialAmount: String = "",
    isEditing: Boolean = false,
    onSave: (String, Double) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var amount by remember { mutableStateOf(initialAmount) }
    
    val surfaceColor = if (isDark) ComposeColor(0xFF1A1C1E) else ComposeColor(0xFFFFFFFF)
    val textColor = if (isDark) ComposeColor.White else ComposeColor.Black
    val secondaryTextColor = if (isDark) ComposeColor(0xFF8890B0) else ComposeColor(0xFF6E6E73)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = if (isDark) ComposeColor(0xFFB0B4BC) else ComposeColor(0xFF8E8E93)) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (isEditing) "Edit Budget" else "Set Category Budget",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))

            if (!isEditing) {
                Text("Category", color = secondaryTextColor, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                if (categories.isEmpty()) {
                    Text("All categories already have budgets", color = ComposeColor.Red, fontSize = 12.sp)
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
                                    colors = RadioButtonDefaults.colors(selectedColor = ComposeColor(0xFFFF9800))
                                )
                                Text(cat, color = textColor, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            } else {
                Text("Category: $selectedCategory", color = textColor, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Monthly Limit (Ksh)", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = ComposeColor(0xFFFF9800),
                    unfocusedBorderColor = if (isDark) ComposeColor(0xFF2A2C32) else ComposeColor(0xFFE5E5EA)
                )
            )

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isEditing && onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ComposeColor.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ComposeColor.Red),
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
                    colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedCategory.isNotEmpty()
                ) {
                    Text(
                        text = if (isEditing) "Update Budget" else "Save Budget",
                        color = ComposeColor.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
