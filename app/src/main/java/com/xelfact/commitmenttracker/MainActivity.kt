package com.xelfact.commitmenttracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

// Navy & Gold Minimalist Color Palette
val NavyPrimary = Color(0xFF0F2342)
val NavyLight = Color(0xFF1D3B6B)
val GoldAccent = Color(0xFFD4AF37)
val GoldSoft = Color(0xFFF9F3DF)
val SurfaceBg = Color(0xFFF5F7FA)
val GreenIncome = Color(0xFF1B8A5A)
val RedExpense = Color(0xFFC62828)

enum class EntryType(val label: String) {
    INCOME("Income (Salary / Profit)"),
    EXPENSE("Commitment / Bill")
}

data class FinancialItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val category: String,
    val type: EntryType,
    val dueDay: Int = 1,
    val isPaid: Boolean = false
)

val DefaultCategories = listOf(
    "Salary",
    "Business Profit",
    "Loans & Instalments",
    "Utilities & Bills",
    "Subscriptions",
    "Insurance / Takaful",
    "Other"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = NavyPrimary,
                    secondary = GoldAccent,
                    background = SurfaceBg,
                    surface = Color.White
                )
            ) {
                CommitmentTrackerApp()
            }
        }
    }
}

fun formatRM(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("en", "MY"))
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    return "RM ${format.format(amount)}"
}

fun loadItems(context: Context): List<FinancialItem> {
    val prefs = context.getSharedPreferences("commitment_tracker_prefs", Context.MODE_PRIVATE)
    val json = prefs.getString("items_json", null)
    return if (json != null) {
        val type = object : TypeToken<List<FinancialItem>>() {}.type
        Gson().fromJson(json, type)
    } else {
        // Starter sample data
        listOf(
            FinancialItem(name = "Monthly Salary", amount = 5500.0, category = "Salary", type = EntryType.INCOME, dueDay = 28, isPaid = true),
            FinancialItem(name = "Side Business Profit", amount = 2000.0, category = "Business Profit", type = EntryType.INCOME, dueDay = 30, isPaid = true),
            FinancialItem(name = "Housing Loan / Rent", amount = 1500.0, category = "Loans & Instalments", type = EntryType.EXPENSE, dueDay = 1, isPaid = true),
            FinancialItem(name = "Car Instalment", amount = 750.0, category = "Loans & Instalments", type = EntryType.EXPENSE, dueDay = 5, isPaid = false),
            FinancialItem(name = "TNB & Water Bill", amount = 180.0, category = "Utilities & Bills", type = EntryType.EXPENSE, dueDay = 15, isPaid = false),
            FinancialItem(name = "Unifi & Postpaid", amount = 160.0, category = "Utilities & Bills", type = EntryType.EXPENSE, dueDay = 18, isPaid = false),
            FinancialItem(name = "Cloud & Streaming", amount = 85.0, category = "Subscriptions", type = EntryType.EXPENSE, dueDay = 10, isPaid = true)
        )
    }
}

fun saveItems(context: Context, items: List<FinancialItem>) {
    val prefs = context.getSharedPreferences("commitment_tracker_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("items_json", Gson().toJson(items)).apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentTrackerApp() {
    val context = LocalContext.current
    var items by remember { mutableStateOf(loadItems(context)) }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Category") } // Category, AmountHigh, DueDate
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<FinancialItem?>(null) }

    fun updateItems(newList: List<FinancialItem>) {
        items = newList
        saveItems(context, newList)
    }

    val totalIncome = items.filter { it.type == EntryType.INCOME }.sumOf { it.amount }
    val totalCommitments = items.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount }
    val unpaidCommitments = items.filter { it.type == EntryType.EXPENSE && !it.isPaid }.sumOf { it.amount }
    val balanceLeft = totalIncome - totalCommitments

    val categories = remember(items) {
        listOf("All") + (DefaultCategories + items.map { it.category }).distinct()
    }

    val filteredAndSortedItems = remember(items, selectedCategory, sortBy) {
        val filtered = if (selectedCategory == "All") {
            items
        } else {
            items.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        when (sortBy) {
            "AmountHigh" -> filtered.sortedByDescending { it.amount }
            "DueDate" -> filtered.sortedBy { it.dueDay }
            else -> filtered.sortedWith(compareBy({ it.type }, { it.category }, { it.name }))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Monthly Cash & Commitments", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Track Salary, Profit & Monthly Bills", fontSize = 12.sp, color = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        // Reset all monthly paid statuses for new month
                        updateItems(items.map { if (it.type == EntryType.EXPENSE) it.copy(isPaid = false) else it })
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Month", tint = GoldAccent)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingItem = null
                    showAddEditDialog = true
                },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add Entry") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceBg)
                .padding(padding)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "BALANCE LEFT (AFTER COMMITMENTS)",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRM(balanceLeft),
                        color = if (balanceLeft >= 0) Color.White else Color(0xFFFF8A80),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = NavyLight)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Salary & Profit", color = Color(0xFFB0C4DE), fontSize = 12.sp)
                            Text(formatRM(totalIncome), color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Commitments", color = Color(0xFFB0C4DE), fontSize = 12.sp)
                            Text(formatRM(totalCommitments), color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    if (unpaidCommitments > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Remaining unpaid bills this month: ${formatRM(unpaidCommitments)}",
                            color = GoldSoft,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Sort Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filteredAndSortedItems.size} items",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SortPill("Category", sortBy == "Category") { sortBy = "Category" }
                    SortPill("Highest RM", sortBy == "AmountHigh") { sortBy = "AmountHigh" }
                    SortPill("Due Day", sortBy == "DueDate") { sortBy = "DueDate" }
                }
            }

            // List of Entries
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredAndSortedItems, key = { it.id }) { item ->
                    FinancialItemCard(
                        item = item,
                        onTogglePaid = {
                            updateItems(items.map {
                                if (it.id == item.id) it.copy(isPaid = !it.isPaid) else it
                            })
                        },
                        onEdit = {
                            editingItem = item
                            showAddEditDialog = true
                        },
                        onDelete = {
                            updateItems(items.filter { it.id != item.id })
                        }
                    )
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditEntryDialog(
            existingItem = editingItem,
            availableCategories = DefaultCategories,
            onDismiss = { showAddEditDialog = false },
            onSave = { savedItem ->
                if (editingItem != null) {
                    updateItems(items.map { if (it.id == savedItem.id) savedItem else it })
                } else {
                    updateItems(items + savedItem)
                }
                showAddEditDialog = false
            }
        )
    }
}

@Composable
fun SortPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) GoldAccent else Color.White
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = NavyPrimary
        )
    }
}

@Composable
fun FinancialItemCard(
    item: FinancialItem,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = item.type == EntryType.INCOME
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isIncome) {
                Checkbox(
                    checked = item.isPaid,
                    onCheckedChange = { onTogglePaid() },
                    colors = CheckboxDefaults.colors(checkedColor = GreenIncome)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GreenIncome, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (!isIncome && item.isPaid) Color.Gray else NavyPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isIncome) Color(0xFFE8F5E9) else GoldSoft
                    ) {
                        Text(
                            text = item.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = if (isIncome) GreenIncome else NavyPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isIncome) "Payday: ${item.dueDay}th" else "Due: ${item.dueDay}th",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncome) "+ " else "- ") + formatRM(item.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isIncome) GreenIncome else RedExpense
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename / Edit", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryDialog(
    existingItem: FinancialItem?,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (FinancialItem) -> Unit
) {
    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var amountText by remember { mutableStateOf(existingItem?.amount?.toString() ?: "") }
    var selectedType by remember { mutableStateOf(existingItem?.type ?: EntryType.EXPENSE) }
    var category by remember { mutableStateOf(existingItem?.category ?: "Loans & Instalments") }
    var dueDayText by remember { mutableStateOf(existingItem?.dueDay?.toString() ?: "1") }
    var expandedCat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingItem == null) "Add Income or Commitment" else "Rename & Edit Entry",
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type Selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EntryType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                if (type == EntryType.INCOME && category == "Loans & Instalments") {
                                    category = "Salary"
                                } else if (type == EntryType.EXPENSE && category == "Salary") {
                                    category = "Loans & Instalments"
                                }
                            },
                            label = { Text(if (type == EntryType.INCOME) "Salary / Profit" else "Bill / Loan", fontSize = 12.sp) }
                        )
                    }
                }

                // Rename / Title Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Payment Name (Rename anytime)") },
                    placeholder = { Text("e.g. Maybank Car Loan, Salary, TNB") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (RM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown / Custom Input
                ExposedDropdownMenuBox(
                    expanded = expandedCat,
                    onExpandedChange = { expandedCat = !expandedCat }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (Select or type custom)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                // Due Day Input
                OutlinedTextField(
                    value = dueDayText,
                    onValueChange = { dueDayText = it },
                    label = { Text("Day of Month (1 - 31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val day = (dueDayText.toIntOrNull() ?: 1).coerceIn(1, 31)
                    if (name.isNotBlank() && amt > 0) {
                        onSave(
                            FinancialItem(
                                id = existingItem?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                amount = amt,
                                category = category.trim().ifBlank { "Other" },
                                type = selectedType,
                                dueDay = day,
                                isPaid = existingItem?.isPaid ?: false
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
