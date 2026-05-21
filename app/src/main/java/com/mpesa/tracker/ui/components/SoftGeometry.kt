package com.mpesa.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.data.model.TransactionType
val SafaricomGreen = Color(0xFF25D366) // Vibrant neon green
val CyanEdge = Color(0xFF00BFFF) // Cyan/Blue for the bottom of the gradient

// Global Theme Colors from the design screenshot
private val ColorAccent = Color(0xFFFF9800)
private val ColorTextSecondaryDark = Color(0xFF8890B0)
private val ColorTextSecondaryLight = Color(0xFF6E6E73)
private val ColorMetallicLight = Color(0xFFB0B4BC)
private val ColorMetallicDark = Color(0xFF40444B)

/**
 * 2. Gradient Side-Border Modifier
 */
fun Modifier.gradientSideAccent(
    colors: List<Color>,
    strokeWidth: Dp = 3.dp,
    cornerRadius: Dp = 16.dp
) = this.drawBehind {
    val sw = strokeWidth.toPx()
    val cr = cornerRadius.toPx()

    val path = Path().apply {
        moveTo(cr, 0f)
        arcTo(Rect(0f, 0f, cr * 2, cr * 2), 270f, -90f, false)
        lineTo(0f, size.height - cr)
        arcTo(Rect(0f, size.height - cr * 2, cr * 2, size.height), 180f, -90f, false)
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(colors),
        style = Stroke(width = sw, cap = StrokeCap.Round)
    )
}

/**
 * 3. Soft-Shadow Pill Button
 */
@Composable
fun SoftActionButton(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val containerColor = if (isDark) Color(0xFF232529).copy(alpha = 0.8f) else Color.White
    val textColor = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f)
    
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = modifier
            .height(44.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = accentColor.copy(alpha = 0.3f)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Box(modifier = Modifier.size(16.dp)) { icon() }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Premium Spending Overview Card with Metallic Dial
 */
@Composable
fun SpendingOverviewCard(
    remainingDays: Int,
    balance: Double,
    monthlyLimit: Double,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val progress = (kotlin.math.abs(balance) / monthlyLimit.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    val surfaceColor = if (isDark) Color(0xFF1A1C1E) else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val secondaryTextColor = if (isDark) ColorTextSecondaryDark else ColorTextSecondaryLight
    val borderColor = if (isDark) Color(0xFF2A2D32) else Color(0xFFE5E5EA)
    val trackColor = if (isDark) Color(0xFF222428) else Color(0xFFF2F2F7)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main Card Surface
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Skueomorphic Dial
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Outer Ring Glow
                        drawCircle(
                            color = Color.Black.copy(alpha = if (isDark) 0.6f else 0.1f),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Metallic Outer Ring
                        drawCircle(
                            brush = Brush.sweepGradient(
                                0.0f to ColorMetallicLight,
                                0.5f to ColorMetallicDark,
                                1.0f to ColorMetallicLight
                            ),
                            radius = size.minDimension / 2 - 2.dp.toPx(),
                            style = Stroke(width = 5.dp.toPx())
                        )
                    }

                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(60.dp),
                        color = ColorAccent,
                        trackColor = trackColor,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )

                    // Brushed Metal Center
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.size(44.dp),
                        shadowElevation = 8.dp
                    ) {
                        Box(
                            modifier = Modifier.background(
                                brush = Brush.radialGradient(
                                    colors = listOf(ColorMetallicLight, ColorMetallicDark)
                                )
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp)) // Breathing room between circle and column

                // Right Column (The Fix): Vertical arrangement for text elements
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Top: Days Remaining
                    Text(
                        text = "$remainingDays days remaining",
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(4.dp))

                    // Middle: Balance with BuildAnnotatedString
                    val balanceText = buildAnnotatedString {
                        val sign = if (balance >= 0) "+" else "-"
                        withStyle(style = SpanStyle(
                            color = secondaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )) {
                            append(sign)
                            append("Ksh ")
                        }
                        withStyle(style = SpanStyle(
                            color = textColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )) {
                            append("%,.0f".format(kotlin.math.abs(balance)))
                        }
                    }

                    Text(
                        text = balanceText,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible
                    )

                    Spacer(Modifier.height(4.dp))

                    // Bottom: Monthly Limit
                    Text(
                        text = "Monthly Limit: %,.0f".format(monthlyLimit),
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Metallic Clamps
        MetallicClamp(modifier = Modifier.align(Alignment.TopCenter), isTop = true)
        MetallicClamp(modifier = Modifier.align(Alignment.BottomCenter), isTop = false)
    }
}

@Composable
private fun MetallicClamp(modifier: Modifier, isTop: Boolean) {
    Box(
        modifier = modifier
            .width(180.dp)
            .height(8.dp)
            .background(
                brush = Brush.verticalGradient(
                    if (isTop) listOf(ColorMetallicLight, ColorMetallicDark)
                    else listOf(ColorMetallicDark, ColorMetallicLight)
                ),
                shape = RoundedCornerShape(
                    bottomStart = if (isTop) 12.dp else 0.dp,
                    bottomEnd = if (isTop) 12.dp else 0.dp,
                    topStart = if (isTop) 0.dp else 12.dp,
                    topEnd = if (isTop) 0.dp else 12.dp
                )
            )
            .border(
                0.5.dp,
                Color.White.copy(alpha = 0.3f),
                RoundedCornerShape(
                    bottomStart = if (isTop) 12.dp else 0.dp,
                    bottomEnd = if (isTop) 12.dp else 0.dp,
                    topStart = if (isTop) 0.dp else 12.dp,
                    topEnd = if (isTop) 0.dp else 12.dp
                )
            )
    )
}

/**
 * 4. Gradient Edge Card for Summary
 */

// --- Theme Colors ---

@Composable
fun GradientEdgeCard(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val surfaceColor = if (isDark) Color(0xFF1A1C1E) else Color.White

    // 1. Outer Box: Holds the gradient and defines the overall 20dp shape
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(colors = colors),
                shape = RoundedCornerShape(20.dp)
            )
            // 2. Padding pushes the inner Surface right, revealing the 4dp neon edge
            .padding(start = 4.dp)
    ) {
        // 3. Inner Surface: The actual dark card containing your layout
        Surface(
            // Inner radii adjusted slightly so corners nest perfectly inside the outer box
            shape = RoundedCornerShape(
                topStart = 18.dp,
                bottomStart = 18.dp,
                topEnd = 20.dp,
                bottomEnd = 20.dp
            ),
            color = surfaceColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Optional: You can add a subtle background pattern here for the "SoftGeometry" lines

            Box(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun MPesaBalanceCard() {
    GradientEdgeCard(
        colors = listOf(SafaricomGreen, CyanEdge),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Title
            Text(
                text = "M-PESA Balance",
                color = SafaricomGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Balance Area & Eye Icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hidden balance pill
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(140.dp)
                        .background(
                            color = Color(0xFF2A2D32), // Lighter grey for the input area
                            shape = RoundedCornerShape(8.dp)
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Visibility Icon
                IconButton(
                    onClick = { /* Toggle visibility logic */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "Show Balance",
                        tint = Color(0xFFAAAAAA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle placeholder (e.g., hidden phone number)
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(200.dp)
                    .background(
                        color = Color(0xFF2A2D32).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // View Statements Button
            OutlinedButton(
                onClick = { /* Handle click */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SafaricomGreen),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = SafaricomGreen
                )
            ) {
                Text(
                    text = "View statements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditSheet(
    tx: Transaction?,
    categories: List<String>,
    isDark: Boolean = true,
    isManualEntry: Boolean = false,
    onSave: (Transaction) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(tx?.amount?.toString() ?: "") }
    var recipient by remember { mutableStateOf(tx?.recipient ?: tx?.phone ?: "") }
    var note by remember { mutableStateOf(tx?.note ?: "") }
    var selectedCategory by remember { mutableStateOf(tx?.category ?: "Other") }
    var isExcluded by remember { mutableStateOf(tx?.isExcluded ?: false) }
    var transactionType by remember { mutableStateOf(tx?.type ?: TransactionType.SEND) }

    val surfaceColor = if (isDark) Color(0xFF1A1C1E) else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val secondaryTextColor = if (isDark) ColorTextSecondaryDark else ColorTextSecondaryLight
    val borderColor = if (isDark) Color(0xFF2A2C32) else Color(0xFFE5E5EA)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ColorMetallicLight) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (isManualEntry) "Add Cash Transaction" else "Edit Transaction",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(24.dp))

            if (isManualEntry) {
                Text("Type", color = secondaryTextColor, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val types = listOf(TransactionType.SEND, TransactionType.RECEIVE)
                    types.forEach { type ->
                        val isSelected = transactionType == type
                        val label = if (type == TransactionType.SEND) "Expense" else "Income"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) ColorAccent else (if (isDark) Color(0xFF232529) else Color(0xFFF2F2F7)), RoundedCornerShape(12.dp))
                                .clickable { transactionType = type }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSelected) Color.Black else textColor,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Amount Field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (Ksh)", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = ColorAccent,
                    unfocusedBorderColor = borderColor
                )
            )

            Spacer(Modifier.height(16.dp))

            // Recipient Field
            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                label = { Text(if (transactionType.isExpense) "Recipient / Business" else "Sender / Source", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = ColorAccent,
                    unfocusedBorderColor = borderColor
                )
            )

            Spacer(Modifier.height(16.dp))

            // Category Selector
            Text("Category", color = secondaryTextColor, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories.filter { it != "All" && it != "Excluded" }) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) ColorAccent else (if (isDark) Color(0xFF232529) else Color(0xFFF2F2F7)), CircleShape)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            cat,
                            color = if (isSelected) Color.Black else textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (!isManualEntry) {
                // Exclude Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Exclude from calculations", color = textColor, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isExcluded,
                        onCheckedChange = { isExcluded = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ColorAccent)
                    )
                }
                Spacer(Modifier.height(32.dp))
            } else {
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt <= 0) return@Button
                    
                    if (isManualEntry) {
                        onSave(Transaction(
                            transactionId = "CASH-${System.currentTimeMillis()}",
                            type = transactionType,
                            amount = amt,
                            recipient = recipient.ifBlank { "Cash Transaction" },
                            category = selectedCategory,
                            note = note,
                            rawSms = "Manual entry",
                            timestamp = System.currentTimeMillis(),
                            isManual = true
                        ))
                    } else {
                        onSave(tx!!.copy(
                            amount = amt,
                            recipient = recipient,
                            category = selectedCategory,
                            note = note,
                            isExcluded = isExcluded
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isManualEntry) "Add Transaction" else "Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
