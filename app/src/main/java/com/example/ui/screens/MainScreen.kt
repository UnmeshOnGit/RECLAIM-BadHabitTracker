package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Achievement
import com.example.data.Habit
import com.example.data.HabitEntry
import com.example.data.Note
import com.example.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

// Global state tracking dark mode
var isDarkThemeGlobal by mutableStateOf(false)

// Style Accent Constants - Adapted dynamically for "Professional Polish" Light/Mint Green or Slate-Dark Theme
val DeepCharcoal: Color
    get() = if (isDarkThemeGlobal) Color(0xFF101012) else Color(0xFFF7F9F2)       // App level background
val CardBackground: Color
    get() = if (isDarkThemeGlobal) Color(0xFF1A1A1E) else Color(0xFFFFFFFF)     // Modern card surfaces
val PrimaryTeal: Color
    get() = if (isDarkThemeGlobal) Color(0xFF00BFA5) else Color(0xFF386B20)        // Forest Sage / Primary Teal
val SoftOrange: Color
    get() = if (isDarkThemeGlobal) Color(0xFFFF8A65) else Color(0xFFA44B16)         // Cozy Rust Orange / Soft Orange
val WarningRed: Color
    get() = if (isDarkThemeGlobal) Color(0xFFF44336) else Color(0xFFB3261E)         // Crimson / red warning
val GoldenYellow: Color
    get() = if (isDarkThemeGlobal) Color(0xFFFFD54F) else Color(0xFF386B20)       // Yellow accent / primary
val SoftText: Color
    get() = if (isDarkThemeGlobal) Color(0xFFB0BEC5) else Color(0xFF74796D)           // Sage-gray accent text
val BorderColor: Color
    get() = if (isDarkThemeGlobal) Color(0xFF2C2C35) else Color(0xFFDDE5D9)        // Base divider and borders

// Helper colors for high fidelity text rendering
val TextColorPrimary: Color
    get() = if (isDarkThemeGlobal) Color(0xFFFFFFFF) else Color(0xFF1A1C18)
val TextColorSecondary: Color
    get() = if (isDarkThemeGlobal) Color(0xFFB0BEC5) else Color(0xFF43483E)

enum class BottomTab(val route: String, val title: String) {
    DASHBOARD("dashboard", "Dashboard"),
    HABITS("habits", "Habits"),
    CALENDAR("calendar", "Calendar"),
    INSIGHTS("insights", "Insights"),
    PROFILE("profile", "Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(BottomTab.DASHBOARD) }
    
    // Core database flows
    val habitsList by viewModel.habits.collectAsStateWithLifecycle()
    val entriesList by viewModel.entries.collectAsStateWithLifecycle()
    val notesList by viewModel.notes.collectAsStateWithLifecycle()
    val achievementsList by viewModel.achievements.collectAsStateWithLifecycle()
    val preferencesList by viewModel.preferences.collectAsStateWithLifecycle()

    val themePref = preferencesList.find { it.key == "theme_preference" }?.value ?: "light"

    // Preferences mapping
    val username = preferencesList.find { it.key == "username" }?.value ?: "Guest Warrior"
    val motivationQuote = preferencesList.find { it.key == "motivation_quote" }?.value ?: "The secret of change is to focus all your energy on building the new."
    val recoveryGoals = preferencesList.find { it.key == "recovery_goals" }?.value ?: "Sustain clarity and reclaim self-control."
    val recoveryScoreStr = preferencesList.find { it.key == "recovery_score" }?.value ?: "50"
    val recoveryScore = recoveryScoreStr.toIntOrNull() ?: 50
    val avatar = preferencesList.find { it.key == "avatar" }?.value ?: "shield"

    // Dialog trigger flags
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showJournalWriterDialog by remember { mutableStateOf(false) }

        // Edge to edge Scaffold with theme settings
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = DeepCharcoal,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "App Logo",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECLAIM",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontSize = 20.sp,
                            color = TextColorPrimary
                        )
                    }
                },
                actions = {
                    // Quick Action hooks for Journal & XP points Display
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(BorderColor, shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "XP",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        // XP Formula: Streaks + resisting urges
                        val totalUrgesControlled = entriesList.count { it.status == "UrgeControlled" }
                        val xp = (recoveryScore * 5) + (totalUrgesControlled * 15)
                        Text(
                            text = "$xp XP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextColorPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepCharcoal,
                    titleContentColor = TextColorPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 8.dp,
                modifier = Modifier.border(width = 1.dp, color = BorderColor).shadow(12.dp)
            ) {
                BottomTab.values().forEach { tab ->
                    val selected = activeTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { activeTab = tab },
                        icon = {
                            val icon = when (tab) {
                                BottomTab.DASHBOARD -> Icons.Default.Dashboard
                                BottomTab.HABITS -> Icons.Default.CheckCircle
                                BottomTab.CALENDAR -> Icons.Default.DateRange
                                BottomTab.INSIGHTS -> Icons.Default.BarChart
                                BottomTab.PROFILE -> Icons.Default.Person
                            }
                            Icon(icon, contentDescription = tab.title, modifier = Modifier.testTag("nav_icon_${tab.route}"))
                        },
                        label = { Text(tab.title, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDarkThemeGlobal) Color(0xFFFFFFFF) else Color(0xFF111F0E),
                            selectedTextColor = if (isDarkThemeGlobal) Color(0xFFFFFFFF) else Color(0xFF111F0E),
                            unselectedIconColor = SoftText,
                            unselectedTextColor = SoftText,
                            indicatorColor = if (isDarkThemeGlobal) Color(0xFF1A3D2F) else Color(0xFFD7E8CD)
                        ),
                        modifier = Modifier.testTag("nav_${tab.route}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (activeTab == BottomTab.HABITS) {
                FloatingActionButton(
                    onClick = { showAddHabitDialog = true },
                    containerColor = PrimaryTeal,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_habit_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Bad Habit")
                }
            } else if (activeTab == BottomTab.DASHBOARD) {
                FloatingActionButton(
                    onClick = { showJournalWriterDialog = true },
                    containerColor = SoftOrange,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("log_entry_fab")
                ) {
                    Icon(Icons.Default.Book, contentDescription = "Quick Reflection Journal")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepCharcoal)
        ) {
            when (activeTab) {
                BottomTab.DASHBOARD -> DashboardTab(viewModel, username, motivationQuote, recoveryScore, habitsList, entriesList)
                BottomTab.HABITS -> HabitsTab(viewModel, habitsList, entriesList)
                BottomTab.CALENDAR -> CalendarTab(viewModel, habitsList, entriesList)
                BottomTab.INSIGHTS -> InsightsTab(viewModel, habitsList, entriesList)
                BottomTab.PROFILE -> ProfileTab(viewModel, username, motivationQuote, recoveryGoals, avatar, achievementsList, notesList, themePref)
            }
        }
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onConfirm = { name, category, severity, goal, tags, target, icon ->
                viewModel.addHabit(name, category, severity, goal, tags, target, icon)
                showAddHabitDialog = false
            }
        )
    }

    if (showJournalWriterDialog) {
        JournalWriterDialog(
            habits = habitsList,
            onDismiss = { showJournalWriterDialog = false },
            onSave = { text, mood, habitId ->
                viewModel.addJournalNote(text, mood, habitId)
                showJournalWriterDialog = false
            }
        )
    }
}

// ---------------- DASHBOARD TAB SCREEN ----------------
@Composable
fun DashboardTab(
    viewModel: HabitViewModel,
    username: String,
    motivationQuote: String,
    recoveryScore: Int,
    habits: List<Habit>,
    entries: List<HabitEntry>
) {
    val aiInsight by viewModel.aiInsight.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val timerSecondsLeft by viewModel.urgeTimerSecondsLeft.collectAsStateWithLifecycle()
    val timerRunning by viewModel.isUrgeTimerRunning.collectAsStateWithLifecycle()

    var showQuickLogDialogForHabit by remember { mutableStateOf<Habit?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Card in "Professional Polish" Header style
        item {
            val dateStr = remember { 
                SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()) 
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dateStr.uppercase(Locale.getDefault()),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColorSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hi, $username",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextColorPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val activeHabits = habits.filter { !it.isArchived }
                        if (activeHabits.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            ) {
                                activeHabits.take(3).forEach { habit ->
                                    val habitEntries = entries.filter { it.habitId == habit.id }
                                    val streaks = viewModel.repository.calculateStreaksForHabit(habitEntries)
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BorderColor.copy(alpha = 0.25f))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = habit.name.uppercase(Locale.getDefault()),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextColorPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "🔥 ${streaks.currentStreak} Days Streak",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SoftOrange
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        // Mini Progress Graph (Sparkline showing clean/sober trend over last 7 days)
                                        Box(
                                            modifier = Modifier
                                                .width(80.dp)
                                                .height(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val last7DaysPoints = remember(entries, habit.id) {
                                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                val cal = Calendar.getInstance()
                                                val points = mutableListOf<Float>()
                                                
                                                for (i in (0..6).reversed()) {
                                                    cal.time = Date()
                                                    cal.add(Calendar.DAY_OF_YEAR, -i)
                                                    val dStr = sdf.format(cal.time)
                                                    val dayEntries = habitEntries.filter { it.date == dStr }
                                                    if (dayEntries.isEmpty()) {
                                                        points.add(1f) // baseline
                                                    } else {
                                                        if (dayEntries.any { it.status == "Relapsed" }) {
                                                            points.add(0f)
                                                        } else if (dayEntries.any { it.status == "UrgeControlled" }) {
                                                            points.add(1.5f)
                                                        } else {
                                                            points.add(2f)
                                                        }
                                                    }
                                                }
                                                points
                                            }
                                            
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                if (last7DaysPoints.size >= 2) {
                                                    val numPts = last7DaysPoints.size
                                                    val dx = size.width / (numPts - 1)
                                                    val maxVal = 2f
                                                    val scaleY = size.height / maxVal
                                                    
                                                    val coords = last7DaysPoints.mapIndexed { index, value ->
                                                        val cx = index * dx
                                                        val cy = size.height - (value * scaleY)
                                                        Offset(cx, cy.coerceIn(0f, size.height))
                                                    }
                                                    
                                                    val pathFill = androidx.compose.ui.graphics.Path().apply {
                                                        moveTo(0f, size.height)
                                                        coords.forEach { pt -> lineTo(pt.x, pt.y) }
                                                        lineTo(size.width, size.height)
                                                        close()
                                                    }
                                                    
                                                    drawPath(
                                                        path = pathFill,
                                                        brush = Brush.verticalGradient(
                                                            colors = listOf(PrimaryTeal.copy(alpha = 0.3f), Color.Transparent)
                                                        )
                                                    )
                                                    
                                                    for (idx in 0 until numPts - 1) {
                                                        drawLine(
                                                            color = PrimaryTeal,
                                                            start = coords[idx],
                                                            end = coords[idx + 1],
                                                            strokeWidth = 1.8.dp.toPx(),
                                                            cap = StrokeCap.Round
                                                        )
                                                    }
                                                    
                                                    drawCircle(
                                                        color = PrimaryTeal,
                                                        radius = 2.dp.toPx(),
                                                        center = coords.last()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Create habit trackers below to initialize live progress sparklines.",
                                fontSize = 11.sp,
                                color = TextColorSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"$motivationQuote\"",
                            fontSize = 12.sp,
                            color = TextColorSecondary,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Avatar circle
                    val shortInitials = if (username.length >= 2) username.substring(0, 2).uppercase(Locale.getDefault()) else " Alex".substring(0, 2).uppercase(Locale.getDefault())
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFDDE5D9), CircleShape)
                            .border(1.dp, Color(0xFFC1C9BE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortInitials,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColorPrimary
                        )
                    }
                }
            }
        }

        // Recovery Score Algorithm Meter Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD7E8CD)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Recovery Score",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF111F0E)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$recoveryScore",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF111F0E)
                                )
                                Text(
                                    text = "%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111F0E),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Shield",
                                tint = Color(0xFF111F0E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(recoveryScore.toFloat() / 100f)
                                    .background(Color(0xFF386B20), RoundedCornerShape(4.dp))
                            )
                        }
                        Text(
                            text = if (recoveryScore >= 75) "+4%" else "+1%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF386B20)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val recoveryTrendText = when {
                        recoveryScore >= 85 -> "Your consistency is outstanding this week. Keep protecting your sanity."
                        recoveryScore >= 65 -> "Your consistency is higher this week than last. Stay disciplined!"
                        recoveryScore >= 40 -> "Streaks are forming. Maintain vigilance of potential trigger events."
                        else -> "High temptation risk detected. Utilize CBT cravings timers if urges surface."
                    }
                    Text(
                        text = recoveryTrendText,
                        fontSize = 12.sp,
                        color = Color(0xFF43483E),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Urgent Countdown Challenge (Crave Delay System)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (timerRunning) {
                        if (isDarkThemeGlobal) Color(0xFF2D1B14) else Color(0xFFFDECE3)
                    } else CardBackground
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (timerRunning) SoftOrange else BorderColor),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Urge Alert",
                                tint = if (timerRunning) SoftOrange else TextColorSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (timerRunning) "CRITICAL WAVE CONTROL" else "FEELING A STRONG URGE?",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (timerRunning) SoftOrange else TextColorPrimary
                                )
                                Text(
                                    text = if (timerRunning) "SURVIVE THIS 15-MINUTE WAVE NOW" else "CBT countdown to beat cravings.",
                                    fontSize = 11.sp,
                                    color = TextColorSecondary
                                )
                            }
                        }
                        
                        if (timerRunning) {
                            val minutes = timerSecondsLeft / 60
                            val seconds = timerSecondsLeft % 60
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftOrange
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!timerRunning) {
                        Button(
                            onClick = { viewModel.startUrgeCountdown(900) }, // 15 mins
                            colors = ButtonDefaults.buttonColors(containerColor = SoftOrange, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("I FEEL THE CRAVING (START 15m DELAY)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { 
                                    viewModel.stopUrgeCountdown()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColorSecondary),
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text("CANCEL", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { 
                                    viewModel.stopUrgeCountdown()
                                    // Log success for first active habit as controlled urge!
                                    if (habits.isNotEmpty()) {
                                        viewModel.logEntry(
                                            habitId = habits.first().id,
                                            status = "UrgeControlled",
                                            count = 0,
                                            timeOfDay = "Current",
                                            trigger = "Wave Challenge Completed",
                                            moodBefore = "Craving",
                                            moodAfter = "Liberated",
                                            notes = "Defeated urge by surviving 15 minutes delay cycle!"
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal, contentColor = Color.White),
                                modifier = Modifier.weight(1.5f).height(48.dp).testTag("survived_urge_btn")
                            ) {
                                Text("I SURVIVED IT! 🎉", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Milestone & Notification schedule
        item {
            val context = LocalContext.current
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminders",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "BI-DAILY PREVENTATIVE REMINDERS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryTeal,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Scheduled twice daily: 9:00 AM & 9:00 PM",
                                    fontSize = 11.sp,
                                    color = TextColorSecondary
                                )
                            }
                        }
                        
                        // Small glowing active pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryTeal.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF81C784), CircleShape)
                            )
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Milestone Progress Area
                    val activeHabits = habits.filter { !it.isArchived }
                    var maxCurrentStreak = 0
                    var streakHabitName = ""

                    for (habit in activeHabits) {
                        val habitEntries = entries.filter { it.habitId == habit.id }
                        var currentStreak = 0
                        val sortedNewest = habitEntries.sortedByDescending { it.date }
                        for (entry in sortedNewest) {
                            if (entry.status == "Relapsed") {
                                break
                            } else {
                                currentStreak++
                            }
                        }
                        if (currentStreak >= maxCurrentStreak) {
                            maxCurrentStreak = currentStreak
                            streakHabitName = habit.name
                        }
                    }

                    val milestoneTitle: String
                    val milestoneBody: String
                    val progressFraction: Float
                    val daysLeft: Int

                    if (activeHabits.isEmpty()) {
                        milestoneTitle = "Define Your First Habit"
                        milestoneBody = "Reclaim control by creating a bad habit target to initialize metrics tracking!"
                        progressFraction = 0f
                        daysLeft = 0
                    } else if (maxCurrentStreak == 0) {
                        milestoneTitle = "First Logged Day"
                        milestoneBody = "Log your first clean day for '$streakHabitName' to trigger a 3-day milestone countdown!"
                        progressFraction = 0.1f
                        daysLeft = 3
                    } else if (maxCurrentStreak < 3) {
                        daysLeft = 3 - maxCurrentStreak
                        milestoneTitle = "First Clean Steps (3 Days)"
                        milestoneBody = "Only $daysLeft more ${if (daysLeft == 1) "day" else "days"} of control for '$streakHabitName' to unlock this achievement!"
                        progressFraction = (maxCurrentStreak.toFloat() / 3f).coerceIn(0f, 1f)
                    } else if (maxCurrentStreak < 7) {
                        daysLeft = 7 - maxCurrentStreak
                        milestoneTitle = "Iron Will (7 Days)"
                        milestoneBody = "Only $daysLeft more ${if (daysLeft == 1) "day" else "days"} of control for '$streakHabitName' to reach this milestone!"
                        progressFraction = (maxCurrentStreak.toFloat() / 7f).coerceIn(0f, 1f)
                    } else if (maxCurrentStreak < 30) {
                        daysLeft = 30 - maxCurrentStreak
                        milestoneTitle = "Habit Breaker (30 Days)"
                        milestoneBody = "Maintain diligence! $daysLeft more ${if (daysLeft == 1) "day" else "days"} of control for '$streakHabitName' to reach legendary status!"
                        progressFraction = (maxCurrentStreak.toFloat() / 30f).coerceIn(0f, 1f)
                    } else {
                        val nextTarget = 90
                        daysLeft = nextTarget - maxCurrentStreak
                        milestoneTitle = "90-Day Victory Legend ($nextTarget Days)"
                        milestoneBody = "Incredible streak of $maxCurrentStreak days! Just $daysLeft more days to conquer this habit!"
                        progressFraction = (maxCurrentStreak.toFloat() / nextTarget.toFloat()).coerceIn(0f, 1f)
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BorderColor.copy(alpha = 0.15f))
                            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆 NEXT MILESTONE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColorSecondary,
                                letterSpacing = 1.sp
                            )
                            if (activeHabits.isNotEmpty()) {
                                Text(
                                    text = "Current Streak: $maxCurrentStreak d",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftOrange
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = milestoneTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = TextColorPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = milestoneBody,
                            fontSize = 12.sp,
                            color = TextColorSecondary,
                            lineHeight = 16.sp
                        )
                        
                        if (activeHabits.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    color = PrimaryTeal,
                                    trackColor = BorderColor.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Text(
                                    text = "${(progressFraction * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                val (title, body) = com.example.notification.NotificationHelper.getMilestoneMessage(context)
                                com.example.notification.NotificationHelper.showNotification(context, title, body)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("test_push_reminder_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TEST PUSH REMINDER NOW", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Habits Dashboard Tracker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE FIGHTS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${habits.filter { !it.isArchived }.size} Targets",
                    fontSize = 12.sp,
                    color = TextColorSecondary
                )
            }
        }

        if (habits.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Empty",
                            tint = TextColorSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No bad habits active. Tap on the 'Habits' tab below and click the '+' button to start fighting your bad habits!",
                            fontSize = 13.sp,
                            color = TextColorSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val activeHabitsList = habits.filter { !it.isArchived }
            items(activeHabitsList) { habit ->
                val habitEntries = entries.filter { it.habitId == habit.id }
                val streaks = viewModel.repository.calculateStreaksForHabit(habitEntries)
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFE8F3E0), CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "clean",
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = habit.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextColorPrimary
                                    )
                                    Text(
                                        text = "${habit.category} • Severity: ${habit.severity}",
                                        fontSize = 11.sp,
                                        color = TextColorSecondary
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${streaks.currentStreak} DAYS",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryTeal
                                )
                                Text(
                                    text = "Longest: ${streaks.longestStreak}d",
                                    fontSize = 10.sp,
                                    color = TextColorSecondary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Assess Today Status",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColorSecondary
                            )
                            Button(
                                onClick = { showQuickLogDialogForHabit = habit },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp).testTag("quick_log_${habit.id}")
                            ) {
                                Text("LOG QUICK STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Behavioral AI Coach Assessment
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "AI Coach",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BEHAVIORAL THERAPY COACH",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = TextColorPrimary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.generateCoachReport() },
                            modifier = Modifier.testTag("ai_coach_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Generate CBT assessment", tint = PrimaryTeal)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isAiLoading) {
                        LinearProgressIndicator(
                            color = PrimaryTeal,
                            trackColor = BorderColor,
                            modifier = Modifier.fillMaxWidth().height(4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = aiInsight,
                        fontSize = 13.sp,
                        color = TextColorSecondary,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }

    if (showQuickLogDialogForHabit != null) {
        QuickLogDialog(
            habit = showQuickLogDialogForHabit!!,
            onDismiss = { showQuickLogDialogForHabit = null },
            onLog = { status, count, time, trigger, moodBefore, moodAfter, notes ->
                viewModel.logEntry(
                    showQuickLogDialogForHabit!!.id,
                    status,
                    count,
                    time,
                    trigger,
                    moodBefore,
                    moodAfter,
                    notes
                )
                showQuickLogDialogForHabit = null
            }
        )
    }
}

// ---------------- HABITS SECTION TAB ----------------
@Composable
fun HabitsTab(
    viewModel: HabitViewModel,
    habits: List<Habit>,
    entries: List<HabitEntry>
) {
    var showArchived by remember { mutableStateOf(false) }

    val displayedHabits = habits.filter { it.isArchived == showArchived }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BorderColor, shape = RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!showArchived) CardBackground else Color.Transparent)
                    .clickable { showArchived = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ACTIVE",
                    color = if (!showArchived) PrimaryTeal else SoftText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (showArchived) CardBackground else Color.Transparent)
                    .clickable { showArchived = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ARCHIVED",
                    color = if (showArchived) PrimaryTeal else SoftText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (displayedHabits.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No habits to show here. Create new ones to begin tracking!",
                            color = TextColorSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(displayedHabits) { habit ->
                    val habitEntries = entries.filter { it.habitId == habit.id }
                    val streaks = viewModel.repository.calculateStreaksForHabit(habitEntries)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth().shadow(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = habit.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextColorPrimary
                                        )
                                        if (habit.isPaused) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(BorderColor, shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("PAUSED", fontSize = 9.sp, color = SoftOrange, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Category: ${habit.category} • Goal: ${habit.recoveryGoal}",
                                        fontSize = 12.sp,
                                        color = TextColorSecondary
                                      )
                                      Spacer(modifier = Modifier.height(4.dp))
                                      Text(
                                          text = "Triggers: ${habit.triggerTags}",
                                          fontSize = 11.sp,
                                          color = SoftOrange,
                                          fontWeight = FontWeight.Bold
                                      )
                                  }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (habit.severity.lowercase()) {
                                                "high" -> WarningRed.copy(alpha = 0.15f)
                                                "medium" -> SoftOrange.copy(alpha = 0.15f)
                                                else -> PrimaryTeal.copy(alpha = 0.15f)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = habit.severity.uppercase(),
                                        color = when (habit.severity.lowercase()) {
                                            "high" -> WarningRed
                                            "medium" -> SoftOrange
                                            else -> PrimaryTeal
                                        },
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = BorderColor)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Metrics row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("CURRENT STREAK", fontSize = 9.sp, color = TextColorSecondary, fontWeight = FontWeight.Bold)
                                    Text("${streaks.currentStreak} DAYS", fontSize = 16.sp, fontWeight = FontWeight.Black, color = PrimaryTeal)
                                }
                                Column {
                                    Text("LONGEST STREAK", fontSize = 9.sp, color = TextColorSecondary, fontWeight = FontWeight.Bold)
                                    Text("${streaks.longestStreak} DAYS", fontSize = 16.sp, fontWeight = FontWeight.Black, color = PrimaryTeal)
                                }
                                Column {
                                    Text("RELAPSES", fontSize = 9.sp, color = TextColorSecondary, fontWeight = FontWeight.Bold)
                                    Text("${habitEntries.count { it.status == "Relapsed" }} TIMES", fontSize = 16.sp, fontWeight = FontWeight.Black, color = WarningRed)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            // Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.togglePauseHabit(habit) },
                                    modifier = Modifier.weight(1f).testTag("pause_habit_${habit.id}"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColorPrimary),
                                    border = BorderStroke(1.dp, BorderColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (habit.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = "pause toggle",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (habit.isPaused) "Resume" else "Pause", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.archiveHabit(habit) },
                                    modifier = Modifier.weight(1f).testTag("archive_habit_${habit.id}"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColorPrimary),
                                    border = BorderStroke(1.dp, BorderColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Archive, contentDescription = "Archive", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Archive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.deleteHabit(habit.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarningRed, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("delete_habit_${habit.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CALENDAR HEATMAP TAB ----------------
@Composable
fun CalendarTab(
    viewModel: HabitViewModel,
    habits: List<Habit>,
    entries: List<HabitEntry>
) {
    var selectedHabit by remember { mutableStateOf<Habit?>(null) }
    
    // Automatically select first active habit as default
    LaunchedEffect(habits) {
        if (selectedHabit == null && habits.isNotEmpty()) {
            selectedHabit = habits.firstOrNull { !it.isArchived } ?: habits.first()
        }
    }

    val todayCal = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(todayCal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(todayCal.get(Calendar.MONTH) + 1) } // 1-based Month
    var showMonthYearPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown habits chooser
        Text(
            text = "SELECT HABIT TARGET",
            fontSize = 11.sp,
            color = PrimaryTeal,
            fontWeight = FontWeight.Bold
        )
        if (habits.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BorderColor, shape = RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.weight(1f).clickable { expanded = true }.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        selectedHabit?.name ?: "Choose Target",
                        color = TextColorPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "drop", tint = TextColorPrimary)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(CardBackground)
                ) {
                    habits.forEach { bHabit ->
                        DropdownMenuItem(
                            text = { Text(bHabit.name, color = TextColorPrimary) },
                            onClick = {
                                selectedHabit = bHabit
                                expanded = false
                            }
                        )
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create a bad habit target first to view calendar history.", modifier = Modifier.padding(16.dp), color = TextColorSecondary)
            }
        }

        selectedHabit?.let { curHabit ->
            val habitEntries = entries.filter { it.habitId == curHabit.id }
            
            // Month navigation & dialog picker trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentMonth == 1) {
                        currentMonth = 12
                        currentYear -= 1
                    } else {
                        currentMonth -= 1
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "last month", tint = TextColorPrimary)
                }
                
                val monthName = when(currentMonth) {
                    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
                    5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
                    9 -> "September"; 10 -> "October"; 11 -> "November"; else -> "December"
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showMonthYearPicker = true }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .background(BorderColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$monthName $currentYear".uppercase(),
                        color = TextColorPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.CalendarToday, 
                        contentDescription = "Pick Month/Year", 
                        tint = PrimaryTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = {
                    if (currentMonth == 12) {
                        currentMonth = 1
                        currentYear += 1
                    } else {
                        currentMonth += 1
                    }
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "next month", tint = TextColorPrimary)
                }
            }

            // Month/Year Picker Dialog
            if (showMonthYearPicker) {
                Dialog(onDismissRequest = { showMonthYearPicker = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "CHOOSE CALENDAR PERIOD",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Year", fontWeight = FontWeight.Bold, color = TextColorPrimary, fontSize = 14.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(onClick = { currentYear -= 1 }) {
                                        Icon(Icons.Default.Remove, contentDescription = "prev year", tint = TextColorPrimary)
                                    }
                                    Text(
                                        "$currentYear", 
                                        fontWeight = FontWeight.Black, 
                                        color = TextColorPrimary, 
                                        fontSize = 18.sp
                                    )
                                    IconButton(onClick = { currentYear += 1 }) {
                                        Icon(Icons.Default.Add, contentDescription = "next year", tint = TextColorPrimary)
                                    }
                                }
                            }
                            
                            HorizontalDivider(color = BorderColor)
                            
                            Text("Month", fontWeight = FontWeight.Bold, color = TextColorSecondary, fontSize = 12.sp)
                            
                            val months = listOf(
                                "Jan", "Feb", "Mar", "Apr",
                                "May", "Jun", "Jul", "Aug",
                                "Sep", "Oct", "Nov", "Dec"
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (r in 0 until 4) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (c in 0 until 3) {
                                            val mIdx = r * 3 + c
                                            val mName = months[mIdx]
                                            val isSelected = currentMonth == (mIdx + 1)
                                            val btnBg = if (isSelected) PrimaryTeal else BorderColor
                                            val btnTxt = if (isSelected) Color.White else TextColorPrimary
                                            
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(btnBg, RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        currentMonth = mIdx + 1
                                                        showMonthYearPicker = false
                                                    }
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(mName, color = btnTxt, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Button(
                                onClick = { showMonthYearPicker = false },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("CLOSE", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Grid header
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = TextColorSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // 35 days monthly overview
            val daysInMonth = when(currentMonth) {
                4,6,9,11 -> 30
                2 -> if ((currentYear % 4 == 0 && currentYear % 100 != 0) || currentYear % 400 == 0) 29 else 28
                else -> 31
            }

            val calendar = Calendar.getInstance()
            calendar.set(currentYear, currentMonth - 1, 1)
            val firstDayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-based

            val totalBlocks = firstDayIndex + daysInMonth
            val rowsCount = (totalBlocks + 6) / 7

            var selectedDateStr by remember { mutableStateOf<String?>(null) }
            
            // Clear selected date when selected habit changes
            LaunchedEffect(curHabit.id) {
                selectedDateStr = null
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until rowsCount) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val blockIndex = row * 7 + col
                            val dayNum = blockIndex - firstDayIndex + 1
                            val isValidDay = dayNum in 1..daysInMonth

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isValidDay) {
                                    val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth, dayNum)
                                    val entryForDay = habitEntries.find { it.date == dateStr }
                                    
                                    val bgColor = when (entryForDay?.status) {
                                        "Clean" -> Color(0xFFD7E8CD) // Soft Success Green
                                        "Relapsed" -> Color(0xFFFFDAD1) // Warm Peach Red
                                        "UrgeControlled" -> Color(0xFFFDECE3) // Soft Peach/Orange
                                        "FeltUrge", "Urge" -> Color(0xFFF9F0D5) // Soft Yellow
                                        "Neutral" -> BorderColor
                                        else -> BorderColor.copy(alpha = 0.4f)
                                    }

                                    val isSelected = selectedDateStr == dateStr
                                    val borderStroke = if (isSelected) {
                                        BorderStroke(2.dp, PrimaryTeal)
                                    } else if (entryForDay == null) {
                                        BorderStroke(1.dp, BorderColor)
                                    } else {
                                        BorderStroke(0.dp, Color.Transparent)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(bgColor, RoundedCornerShape(8.dp))
                                            .border(borderStroke, RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedDateStr = dateStr
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val textCol = when (entryForDay?.status) {
                                            "Clean" -> Color(0xFF111F0E)
                                            "Relapsed" -> Color(0xFF410002)
                                            "UrgeControlled" -> Color(0xFFA44B16)
                                            "FeltUrge", "Urge" -> Color(0xFF6B5808)
                                            else -> TextColorPrimary
                                        }
                                        Text(
                                            text = "$dayNum",
                                            color = textCol,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendIndicator("Sober", Color(0xFFD7E8CD))
                Spacer(modifier = Modifier.width(12.dp))
                LegendIndicator("Resisted Cravings", Color(0xFFFDECE3))
                Spacer(modifier = Modifier.width(12.dp))
                LegendIndicator("Relapsed", Color(0xFFFFDAD1))
            }

            selectedDateStr?.let { dateStr ->
                val currentDetail = habitEntries.find { it.date == dateStr } ?: HabitEntry(
                    habitId = curHabit.id,
                    date = dateStr,
                    status = "Clean"
                )

                CalendarLogDialog(
                    dateStr = dateStr,
                    currentDetail = currentDetail,
                    onDismiss = { selectedDateStr = null },
                    onSave = { status, count, timeOfDay, trigger, notes ->
                        viewModel.logEntryForDate(
                            curHabit.id,
                            dateStr,
                            status,
                            count = count,
                            timeOfDay = timeOfDay,
                            trigger = trigger,
                            moodBefore = "Neutral",
                            moodAfter = if (status == "Relapsed") "Guilty" else "Calm",
                            notes = notes
                        )
                        selectedDateStr = null
                    }
                )
            }
        }
    }
}

@Composable
fun LegendIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = TextColorSecondary, fontSize = 10.sp)
    }
}

// ---------------- INSIGHTS & ANALYTICS TAB ----------------
@Composable
fun RecoveryFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(if (selected) PrimaryTeal else BorderColor.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextColorPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SuccessRatePieChart(
    clean: Float,
    resisted: Float,
    relapsed: Float,
    modifier: Modifier = Modifier
) {
    val total = clean + resisted + relapsed
    if (total == 0f) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Logs Available", color = TextColorSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        return
    }

    val cleanAngle = (clean / total) * 360f
    val resistedAngle = (resisted / total) * 360f
    val relapsedAngle = (relapsed / total) * 360f

    val cleanColor = Color(0xFF81C784)      // Soft green
    val resistedColor = Color(0xFFFFB74D)   // Orange
    val relapsedColor = Color(0xFFE57373)   // Soft red

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            val strokeWidthPx = 14.dp.toPx()
            
            if (cleanAngle > 0f) {
                drawArc(
                    color = cleanColor,
                    startAngle = startAngle,
                    sweepAngle = cleanAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
                startAngle += cleanAngle
            }
            if (resistedAngle > 0f) {
                drawArc(
                    color = resistedColor,
                    startAngle = startAngle,
                    sweepAngle = resistedAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
                startAngle += resistedAngle
            }
            if (relapsedAngle > 0f) {
                drawArc(
                    color = relapsedColor,
                    startAngle = startAngle,
                    sweepAngle = relapsedAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val percentage = (((clean + resisted) / total) * 100).toInt()
            Text(
                text = "$percentage%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextColorPrimary
            )
            Text(
                text = "Compliance",
                fontSize = 9.sp,
                color = TextColorSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeeklyProgressLineGraph(
    scores: List<Float>,
    modifier: Modifier = Modifier
) {
    val cal = java.util.Calendar.getInstance()
    val sdfDay = java.text.SimpleDateFormat("E", java.util.Locale.getDefault())
    val dayNames = remember {
        val names = mutableListOf<String>()
        val c = java.util.Calendar.getInstance()
        for (i in (0..6).reversed()) {
            c.time = java.util.Date()
            c.add(java.util.Calendar.DAY_OF_YEAR, -i)
            names.add(sdfDay.format(c.time))
        }
        names
    }

    val pointColor = PrimaryTeal
    val lineColor = PrimaryTeal
    val fillColor = PrimaryTeal.copy(alpha = 0.2f)

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().shadow(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WEEKLY SUCCESS PROFILE (LINE CHART)",
                color = PrimaryTeal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Trend of daily clean compliance rates",
                color = TextColorSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (scores.isEmpty()) return@Canvas
                val numPoints = scores.size
                val spaceBetween = size.width / (numPoints - 1)
                val strokeWidthPx = 3.dp.toPx()

                // Draw Y-axis guide lines
                val line50Y = size.height * 0.5f
                drawLine(
                    color = BorderColor,
                    start = Offset(0f, line50Y),
                    end = Offset(size.width, line50Y),
                    strokeWidth = 1.dp.toPx()
                )

                val line100Y = 0f
                drawLine(
                    color = BorderColor.copy(alpha = 0.5f),
                    start = Offset(0f, line100Y),
                    end = Offset(size.width, line100Y),
                    strokeWidth = 1.dp.toPx()
                )

                val coordinates = scores.mapIndexed { idx, score ->
                    val x = idx * spaceBetween
                    val ratio = score / 100f
                    val y = size.height - (ratio * size.height)
                    Offset(x, y.coerceIn(0f, size.height))
                }

                // Fill area under line
                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height)
                    coordinates.forEach { pt ->
                        lineTo(pt.x, pt.y)
                    }
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor, Color.Transparent)
                    )
                )

                // Draw curve connection line
                for (i in 0 until numPoints - 1) {
                    drawLine(
                        color = lineColor,
                        start = coordinates[i],
                        end = coordinates[i + 1],
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }

                // Draw points
                coordinates.forEach { pt ->
                    drawCircle(
                        color = pointColor,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        color = TextColorSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendHeatmap(
    statuses: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().shadow(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MONTHLY COGNITIVE HEATMAP (HEATMAP)",
                color = PrimaryTeal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Behavior compliance tracking across the last 4 weeks",
                color = TextColorSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = modifier.align(Alignment.CenterHorizontally)
            ) {
                for (week in 0 until 4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (day in 0 until 7) {
                            val idx = week * 7 + day
                            val status = if (idx < statuses.size) statuses[idx] else "Unlogged"
                            
                            val boxColor = when (status) {
                                "Clean" -> Color(0xFF81C784)
                                "UrgeControlled" -> Color(0xFFFFB74D)
                                "Relapsed" -> Color(0xFFE57373)
                                else -> BorderColor.copy(alpha = 0.4f)
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(boxColor)
                                    .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicator("Sober", Color(0xFF81C784))
                LegendIndicator("Resisted", Color(0xFFFFB74D))
                LegendIndicator("Slipped", Color(0xFFE57373))
                LegendIndicator("Empty", BorderColor.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun RecoveryGrowthCurve(
    cumulativeValues: List<Float>,
    modifier: Modifier = Modifier
) {
    val curveColor = SoftOrange
    val areaColor = SoftOrange.copy(alpha = 0.15f)

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().shadow(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RECOVERY GROWTH CURVE (LINE CHART)",
                color = SoftOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Visualizing cumulative safe and sober logged milestones",
                color = TextColorSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                if (cumulativeValues.isEmpty()) return@Canvas
                val numPoints = cumulativeValues.size
                val spaceBetween = size.width / (numPoints - 1)
                val maxVal = cumulativeValues.maxOfOrNull { it } ?: 30f
                val heightScale = if (maxVal > 0f) size.height / maxVal else size.height / 30f

                val coordinates = cumulativeValues.mapIndexed { idx, value ->
                    val x = idx * spaceBetween
                    val y = size.height - (value * heightScale)
                    Offset(x, y.coerceIn(0f, size.height))
                }

                val areaPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height)
                    coordinates.forEach { pt ->
                        lineTo(pt.x, pt.y)
                    }
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(areaColor, Color.Transparent)
                    )
                )

                for (i in 0 until numPoints - 1) {
                    drawLine(
                        color = curveColor,
                        start = coordinates[i],
                        end = coordinates[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                if (coordinates.isNotEmpty()) {
                    drawCircle(
                        color = curveColor,
                        radius = 4.dp.toPx(),
                        center = coordinates.first()
                    )
                    drawCircle(
                        color = curveColor,
                        radius = 6.dp.toPx(),
                        center = coordinates.last()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("30 Days Ago", color = TextColorSecondary, fontSize = 9.sp)
                Text("Today", color = TextColorSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TriggerBarChart(
    sortedTriggers: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (sortedTriggers.isEmpty()) {
        Text("No emotional triggers logged yet.", color = TextColorSecondary, fontSize = 11.sp)
        return
    }

    val maxVal = sortedTriggers.maxOfOrNull { it.second } ?: 1
    val baseColor = PrimaryTeal

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sortedTriggers.take(4).forEach { (name, count) ->
            val ratio = count.toFloat() / maxVal.toFloat()
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name.uppercase(java.util.Locale.getDefault()),
                        color = TextColorPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$count instances",
                        color = SoftOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BorderColor.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(ratio.coerceIn(0.05f, 1f))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(baseColor, baseColor.copy(alpha = 0.6f))
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun RelapseTimePieChart(
    morning: Float,
    afternoon: Float,
    evening: Float,
    night: Float,
    modifier: Modifier = Modifier
) {
    val total = morning + afternoon + evening + night
    if (total == 0f) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No logged triggers during periods", color = TextColorSecondary, fontSize = 11.sp)
        }
        return
    }

    val morningAngle = (morning / total) * 360f
    val afternoonAngle = (afternoon / total) * 360f
    val eveningAngle = (evening / total) * 360f
    val nightAngle = (night / total) * 360f

    val colors = listOf(
        Color(0xFFFFB74D), // Morning Yellow-Orange
        Color(0xFF81C784), // Afternoon Green
        Color(0xFF4FC3F7), // Evening Blue
        Color(0xFF9575CD)  // Night Purple
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val angles = listOf(morningAngle, afternoonAngle, eveningAngle, nightAngle)
                
                angles.forEachIndexed { i, angle ->
                    if (angle > 0f) {
                        drawArc(
                            color = colors[i],
                            startAngle = startAngle,
                            sweepAngle = angle,
                            useCenter = true
                        )
                        startAngle += angle
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LegendIndicator("Morning (${(morning/total*100).toInt()}%)", colors[0])
            LegendIndicator("Afternoon (${(afternoon/total*100).toInt()}%)", colors[1])
            LegendIndicator("Evening (${(evening/total*100).toInt()}%)", colors[2])
            LegendIndicator("Night (${(night/total*100).toInt()}%)", colors[3])
        }
    }
}

@Composable
fun InsightMetricTile(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BorderColor.copy(alpha = 0.4f))
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 9.sp,
                color = TextColorSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                fontSize = 11.sp,
                color = TextColorPrimary,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InsightsTab(
    viewModel: HabitViewModel,
    habits: List<Habit>,
    entries: List<HabitEntry>
) {
    var selectedHabitFilterId by remember { mutableStateOf<Int?>(null) }

    val filteredEntries = remember(entries, selectedHabitFilterId) {
        if (selectedHabitFilterId == null) {
            entries
        } else {
            entries.filter { it.habitId == selectedHabitFilterId }
        }
    }

    // Calculations
    val totalCount = filteredEntries.size
    val cleanCount = filteredEntries.count { it.status == "Clean" }.toFloat()
    val resistedCount = filteredEntries.count { it.status == "UrgeControlled" }.toFloat()
    val relapsedCount = filteredEntries.count { it.status == "Relapsed" }.toFloat()
    val successRate = if (totalCount > 0) (((cleanCount + resistedCount) / totalCount) * 100).toInt() else 100

    // Relapse Time group, split to support multiple logged times per day
    val relapses = filteredEntries.filter { it.status == "Relapsed" }
    val allSlipTimes = relapses.flatMap {
        it.timeOfDay.split(",")
            .map { part -> part.trim() }
            .filter { part -> part.isNotEmpty() && part != "Anytime" }
    }
    val slipTimeCounts = allSlipTimes.groupBy { it }.mapValues { it.value.size }
    val mostCommonRelapseTime = if (slipTimeCounts.isNotEmpty()) {
        slipTimeCounts.maxByOrNull { it.value }?.key ?: "N/A"
    } else {
        "No Relapses logged"
    }

    val totalSlipsMetric = relapses.sumOf { it.count.coerceAtLeast(1) }

    // Trigger distribution (all incidents)
    val triggerGroups = filteredEntries.filter { it.trigger.isNotEmpty() }.groupBy { it.trigger }.mapValues { it.value.size }
    val sortedTriggers = triggerGroups.toList().sortedByDescending { it.second }
    val topTrigger = if (sortedTriggers.isNotEmpty()) sortedTriggers.first().first else "No Triggers"

    // Worst performing situations
    val situationGroups = relapses.filter { it.trigger.isNotEmpty() && it.timeOfDay.isNotEmpty() }
        .groupBy { "${it.timeOfDay} with ${it.trigger}" }
    val worstSituation = if (situationGroups.isNotEmpty()) {
        situationGroups.maxByOrNull { it.value.size }?.key ?: "N/A"
    } else {
        "None recorded"
    }

    // Weekday Rate calculation
    val sdfStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val weekdaySales = mutableMapOf<Int, Int>() 
    val weekdayTotals = mutableMapOf<Int, Int>() 
    
    filteredEntries.forEach { entry ->
        try {
            val d = sdfStr.parse(entry.date)
            if (d != null) {
                val c = java.util.Calendar.getInstance()
                c.time = d
                val dayOfWeek = c.get(java.util.Calendar.DAY_OF_WEEK)
                weekdayTotals[dayOfWeek] = (weekdayTotals[dayOfWeek] ?: 0) + 1
                if (entry.status != "Relapsed") {
                    weekdaySales[dayOfWeek] = (weekdaySales[dayOfWeek] ?: 0) + 1
                }
            }
        } catch (e: Exception) {}
    }

    val daysMap = mapOf(
        java.util.Calendar.MONDAY to "Monday",
        java.util.Calendar.TUESDAY to "Tuesday",
        java.util.Calendar.WEDNESDAY to "Wednesday",
        java.util.Calendar.THURSDAY to "Thursday",
        java.util.Calendar.FRIDAY to "Friday",
        java.util.Calendar.SATURDAY to "Saturday",
        java.util.Calendar.SUNDAY to "Sunday"
    )

    val bestWeekdayRate = daysMap.keys.map { dayId ->
        val tot = weekdayTotals[dayId] ?: 0
        val succ = weekdaySales[dayId] ?: 0
        val rate = if (tot > 0) (succ.toFloat() / tot * 100).toInt() else 100
        dayId to rate
    }
    val bestDayEntry = bestWeekdayRate.maxByOrNull { it.second }
    val bestWeekdayStr = if (bestDayEntry != null && weekdayTotals.values.any { it > 0 }) {
        "${daysMap[bestDayEntry.first] ?: "N/A"} (${bestDayEntry.second}% Sbr)"
    } else {
        "Not enough data"
    }

    // Weekly trend scores over last 7 days
    val last7DaysScores = remember(filteredEntries) {
        val scores = mutableListOf<Float>()
        val cal = java.util.Calendar.getInstance()
        
        for (i in (0..6).reversed()) {
            cal.time = java.util.Date()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val dStr = sdfStr.format(cal.time)
            
            val dayEntries = filteredEntries.filter { it.date == dStr }
            if (dayEntries.isEmpty()) {
                scores.add(100f) 
            } else {
                val cleanOrResisted = dayEntries.count { it.status == "Clean" || it.status == "UrgeControlled" }
                val score = (cleanOrResisted.toFloat() / dayEntries.size) * 100f
                scores.add(score)
            }
        }
        scores
    }

    // Monthly Heatmap last 28 days statuses
    val last28DaysStatuses = remember(filteredEntries) {
        val statuses = mutableListOf<String>()
        val cal = java.util.Calendar.getInstance()
        
        for (i in (0..27).reversed()) {
            cal.time = java.util.Date()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val dStr = sdfStr.format(cal.time)
            
            val dayEntries = filteredEntries.filter { it.date == dStr }
            val status = if (dayEntries.isEmpty()) {
                "Unlogged"
            } else {
                if (dayEntries.any { it.status == "Relapsed" }) {
                    "Relapsed"
                } else if (dayEntries.any { it.status == "UrgeControlled" }) {
                    "UrgeControlled"
                } else {
                    "Clean"
                }
            }
            statuses.add(status)
        }
        statuses
    }

    // Recovery Growth values over last 30 days
    val last30DaysCumulativeSober = remember(filteredEntries) {
        val cal = java.util.Calendar.getInstance()
        val totals = mutableListOf<Float>()
        var runningClean = 0f
        
        for (i in (0..29).reversed()) {
            cal.time = java.util.Date()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val dStr = sdfStr.format(cal.time)
            
            val dayEntries = filteredEntries.filter { it.date == dStr }
            val daySlipped = dayEntries.any { it.status == "Relapsed" }
            if (!daySlipped && dayEntries.isNotEmpty()) {
                runningClean += 1f
            }
            totals.add(runningClean)
        }
        totals
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "TRIGGERS & COGNITIVE ANALYTICS",
                fontSize = 11.sp,
                color = PrimaryTeal,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "SITUATIONAL INSIGHTS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextColorPrimary
            )
        }

        // Habit Filter Line
        if (habits.isNotEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)
                ) {
                    RecoveryFilterChip(
                        text = "All Habits",
                        selected = selectedHabitFilterId == null,
                        onClick = { selectedHabitFilterId = null }
                    )
                    habits.forEach { habit ->
                        RecoveryFilterChip(
                            text = habit.name,
                            selected = selectedHabitFilterId == habit.id,
                            onClick = { selectedHabitFilterId = habit.id }
                        )
                    }
                }
            }
        }

        // Demo Data Loader Panel
        if (filteredEntries.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().shadow(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = "Sober Info", tint = PrimaryTeal)
                        }
                        Text(
                            text = "NO RECOVERY DATA FOUND",
                            fontWeight = FontWeight.Bold,
                            color = TextColorPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Generate 30 days of realistic behavioral records across your habits to test-drive your metrics dashboard instantly, or log daily on the calendar tab.",
                            color = TextColorSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Button(
                            onClick = {
                                viewModel.seedSampleAnalyticsData()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("seed_analytics_btn")
                        ) {
                            Text("⚡ GENERATE SAMPLE RECOVERY DATA", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            // Display Compliance success pie chart ring
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "OVERALL COMPLIANCE SCORE (PIE CHART)",
                                color = PrimaryTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total clean days over logged time period, including resisted urges.",
                                color = TextColorSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        SuccessRatePieChart(
                            clean = cleanCount,
                            resisted = resistedCount,
                            relapsed = relapsedCount,
                            modifier = Modifier.size(110.dp)
                        )
                    }
                }
            }

            // High Fidelity Summary values
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "COMPUTED BEHAVIORAL HIGHLIGHTS",
                            color = PrimaryTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                InsightMetricTile(
                                    title = "Success Rate",
                                    value = "$successRate%",
                                    icon = Icons.Default.CheckCircle,
                                    iconColor = Color(0xFF81C784)
                                )
                                InsightMetricTile(
                                    title = "Common Slip Time",
                                    value = mostCommonRelapseTime,
                                    icon = Icons.Default.Warning,
                                    iconColor = SoftOrange
                                )
                                InsightMetricTile(
                                    title = "Best Weekday",
                                    value = bestWeekdayStr,
                                    icon = Icons.Default.CalendarToday,
                                    iconColor = PrimaryTeal
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                InsightMetricTile(
                                    title = "Primary Trigger",
                                    value = topTrigger,
                                    icon = Icons.Default.Warning,
                                    iconColor = Color(0xFFE57373)
                                )
                                InsightMetricTile(
                                    title = "Worst Situation",
                                    value = worstSituation,
                                    icon = Icons.Default.Lock,
                                    iconColor = Color(0xFFE57373)
                                )
                                InsightMetricTile(
                                    title = "Total Slips",
                                    value = "$totalSlipsMetric times",
                                    icon = Icons.Default.LocalFireDepartment,
                                    iconColor = Color(0xFFEF5350)
                                )
                            }
                        }
                    }
                }
            }

            // Weekly Progress Line Graph
            item {
                WeeklyProgressLineGraph(scores = last7DaysScores)
            }

            // Monthly Trend Heatmap Grid
            item {
                MonthlyTrendHeatmap(statuses = last28DaysStatuses)
            }

            // Recovery Growth curve
            item {
                RecoveryGrowthCurve(cumulativeValues = last30DaysCumulativeSober)
            }

            // SITUATIONAL TRIGGER INSTANCES BAR GRAPH
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EMOTIONAL & SITUATIONAL TRIGGERS (BAR GRAPH)",
                            color = PrimaryTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TriggerBarChart(sortedTriggers = sortedTriggers)
                    }
                }
            }

            // Relapse time pie chart circle slices
            val morningCount = relapses.filter { it.timeOfDay.contains("Morning", ignoreCase = true) }.sumOf { it.count.coerceAtLeast(1) }.toFloat()
            val afternoonCount = relapses.filter { it.timeOfDay.contains("Afternoon", ignoreCase = true) }.sumOf { it.count.coerceAtLeast(1) }.toFloat()
            val eveningCount = relapses.filter { it.timeOfDay.contains("Evening", ignoreCase = true) }.sumOf { it.count.coerceAtLeast(1) }.toFloat()
            val nightCount = relapses.filter { it.timeOfDay.contains("Night", ignoreCase = true) }.sumOf { it.count.coerceAtLeast(1) }.toFloat()

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SLIP TIME DISTRIBUTION (PIE CHART)",
                            color = PrimaryTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Time of day analysis for relapse events",
                            color = TextColorSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RelapseTimePieChart(
                            morning = morningCount,
                            afternoon = afternoonCount,
                            evening = eveningCount,
                            night = nightCount,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------- PROFILE & SETTINGS TAB ----------------
@Composable
fun ProfileTab(
    viewModel: HabitViewModel,
    username: String,
    motivationQuote: String,
    recoveryGoals: String,
    avatar: String,
    achievements: List<Achievement>,
    notes: List<Note>,
    theme: String
) {
    var editMode by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf(username) }
    var inputQuote by remember { mutableStateOf(motivationQuote) }
    var inputGoals by remember { mutableStateOf(recoveryGoals) }
    var inputAvatar by remember { mutableStateOf(avatar) }

    var journalQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROFILE SECTOR",
                    fontSize = 11.sp,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (editMode) "Save Changes" else "Edit Details",
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable {
                            if (editMode) {
                                viewModel.updateProfile(inputName, inputQuote, inputGoals, inputAvatar)
                            }
                            editMode = !editMode
                        }
                        .testTag("edit_profile_toggle")
                )
            }
        }

        // Avatar & Identification Row
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE8F3E0), CircleShape)
                        .border(1.dp, PrimaryTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarIcon = when (inputAvatar) {
                        "shield" -> Icons.Default.Lock
                        "sword" -> Icons.Default.LocalFireDepartment
                        "star" -> Icons.Default.Star
                        else -> Icons.Default.Person
                    }
                    Icon(avatarIcon, contentDescription = "profile avatar", tint = PrimaryTeal, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                if (!editMode) {
                    Column {
                        Text(username, color = TextColorPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(recoveryGoals, color = TextColorSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            placeholder = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextColorPrimary,
                                unfocusedTextColor = TextColorPrimary,
                                focusedBorderColor = PrimaryTeal,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("shield", "sword", "star").forEach { av ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (inputAvatar == av) PrimaryTeal else BorderColor, RoundedCornerShape(8.dp))
                                        .clickable { inputAvatar = av }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(av.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Theme Selection Sector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "APPEARANCE SETTINGS",
                        color = PrimaryTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "App Interface Mode",
                                fontWeight = FontWeight.Bold,
                                color = TextColorPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Toggle between high contrast Light or Dark styles",
                                color = TextColorSecondary,
                                fontSize = 11.sp
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .background(BorderColor, RoundedCornerShape(24.dp))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (theme == "light") PrimaryTeal else Color.Transparent)
                                    .clickable {
                                        viewModel.setThemePreference("light")
                                    }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "LIGHT",
                                    color = if (theme == "light") Color.White else TextColorSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (theme == "dark") PrimaryTeal else Color.Transparent)
                                    .clickable {
                                        viewModel.setThemePreference("dark")
                                    }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "DARK",
                                    color = if (theme == "dark") Color.White else TextColorSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (editMode) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Motivational Quote", color = TextColorSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = inputQuote,
                        onValueChange = { inputQuote = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextColorPrimary,
                            unfocusedTextColor = TextColorPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Recovery Goals", color = TextColorSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = inputGoals,
                        onValueChange = { inputGoals = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextColorPrimary,
                            unfocusedTextColor = TextColorPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            }
        }

        // Achievements Section
        item {
            Text(
                text = "ACHIEVED MILESTONES",
                color = PrimaryTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }

        if (achievements.isEmpty()) {
            item {
                Text("Lock milestones will unlock once requirements are satisfied.", color = TextColorSecondary, fontSize = 12.sp)
            }
        } else {
            items(achievements) { ach ->
                val isUnlocked = ach.unlockedAt != null
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color(0xFFE8F3E0) else CardBackground
                    ),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.Star else Icons.Default.Lock,
                            contentDescription = "milestone",
                            tint = if (isUnlocked) PrimaryTeal else TextColorSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = ach.title,
                                fontWeight = FontWeight.Bold,
                                color = TextColorPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = ach.description,
                                fontSize = 12.sp,
                                color = TextColorSecondary
                            )
                        }
                    }
                }
            }
        }

        // Searchable Daily journaling Reflections Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY REFLECTIONS DIARY",
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${notes.size} notes",
                    color = TextColorSecondary,
                    fontSize = 11.sp
                )
            }
        }

        item {
            OutlinedTextField(
                value = journalQuery,
                onValueChange = { journalQuery = it },
                placeholder = { Text("Search reflection memory...", color = TextColorSecondary.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth().testTag("reflection_search"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextColorPrimary,
                    unfocusedTextColor = TextColorPrimary,
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                leadingIcon = { Icon(Icons.Default.Book, contentDescription = "search", tint = TextColorSecondary) }
            )
        }

        val filteredNotes = if (journalQuery.isEmpty()) {
            notes
        } else {
            notes.filter { it.text.contains(journalQuery, ignoreCase = true) }
        }

        if (filteredNotes.isEmpty()) {
            item {
                Text("No matching journals found.", color = TextColorSecondary, fontSize = 12.sp)
            }
        } else {
            items(filteredNotes) { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFE8F3E0), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = note.mood.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PrimaryTeal
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(note.date, color = TextColorPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { viewModel.deleteJournalNote(note) },
                                modifier = Modifier.size(24.dp).testTag("delete_note_${note.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "delete reflection", tint = WarningRed, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(note.text, color = TextColorPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// ---------------- CALENDAR LOG INTERACTION DIALOG ----------------
@Composable
fun CalendarLogDialog(
    dateStr: String,
    currentDetail: HabitEntry,
    onDismiss: () -> Unit,
    onSave: (status: String, count: Int, timeOfDay: String, trigger: String, notes: String) -> Unit
) {
    var logStatus by remember { mutableStateOf(currentDetail.status) }
    var logTrigger by remember { mutableStateOf(currentDetail.trigger) }
    var logNotes by remember { mutableStateOf(currentDetail.notes) }
    var logCount by remember { mutableStateOf(if (currentDetail.status == "Relapsed" && currentDetail.count > 0) currentDetail.count else 1) }
    var logTimeOfDay by remember { mutableStateOf(if (currentDetail.status == "Relapsed" && currentDetail.timeOfDay.isNotEmpty()) currentDetail.timeOfDay else "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LOG DATE: $dateStr",
                            fontWeight = FontWeight.Bold,
                            color = TextColorPrimary,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "close detail", tint = TextColorPrimary)
                    }
                }
                
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(4.dp))
                
                Text("Tap status to log instantly:", color = TextColorSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Clean", "Relapsed", "UrgeControlled").forEach { stat ->
                        val selected = logStatus == stat
                        val itemBg = if (selected) {
                            when (stat) {
                                "Clean" -> Color(0xFFD7E8CD)
                                "Relapsed" -> Color(0xFFFFDAD1)
                                else -> Color(0xFFFDECE3)
                            }
                        } else BorderColor
                        val itemText = if (selected) {
                            when (stat) {
                                "Clean" -> Color(0xFF111F0E)
                                "Relapsed" -> Color(0xFF410002)
                                else -> Color(0xFFA44B16)
                            }
                        } else TextColorPrimary
                        
                        val label = when (stat) {
                            "Clean" -> "🟢 Sober"
                            "Relapsed" -> "🔴 Slip"
                            else -> "🟡 Resisted"
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, if (selected) itemText.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .background(itemBg, RoundedCornerShape(8.dp))
                                .clickable { 
                                    logStatus = stat
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = itemText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (logStatus == "Relapsed") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarningRed.copy(alpha = 0.08f))
                            .border(1.dp, WarningRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️ SLIP / RELAPSE DETAILS",
                            color = WarningRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        
                        Text(
                            text = "How many times did you slip on this day?",
                            color = TextColorPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { if (logCount > 1) logCount-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BorderColor.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextColorPrimary)
                            }
                            
                            Text(
                                text = logCount.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextColorPrimary,
                                modifier = Modifier.testTag("slip_count_text")
                            )
                            
                            IconButton(
                                onClick = { logCount++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BorderColor.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextColorPrimary)
                            }
                        }
                        
                        Text(
                            text = "Time of slip(s):",
                            color = TextColorPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        
                        // Select general time sectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Morning", "Afternoon", "Evening", "Night").forEach { sector ->
                                val isChecked = logTimeOfDay.contains(sector, ignoreCase = true)
                                val resolvedColor = if (isChecked) WarningRed else BorderColor
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChecked) WarningRed.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(1.dp, resolvedColor, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val parts = logTimeOfDay.split(",")
                                                .map { it.trim() }
                                                .filter { it.isNotEmpty() }
                                                .toMutableSet()
                                            if (parts.contains(sector)) {
                                                parts.remove(sector)
                                            } else {
                                                parts.add(sector)
                                            }
                                            logTimeOfDay = parts.sorted().joinToString(", ")
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sector,
                                        fontSize = 10.sp,
                                        color = if (isChecked) WarningRed else TextColorSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        // Custom text field for specific times
                        OutlinedTextField(
                            value = logTimeOfDay,
                            onValueChange = { logTimeOfDay = it },
                            placeholder = { Text("e.g. 10:30 AM, 4:00 PM or custom notes", fontSize = 11.sp, color = TextColorSecondary.copy(alpha = 0.5f)) },
                            label = { Text("Slip Times", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("slip_time_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextColorPrimary,
                                unfocusedTextColor = TextColorPrimary,
                                unfocusedBorderColor = BorderColor,
                                focusedBorderColor = WarningRed,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedLabelColor = WarningRed,
                                unfocusedLabelColor = SoftText
                            ),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                OutlinedTextField(
                    value = logTrigger,
                    onValueChange = { 
                        logTrigger = it 
                    },
                    placeholder = { Text("Trigger e.g. stress, fatigue", color = TextColorSecondary.copy(alpha = 0.6f)) },
                    label = { Text("Trigger Tag") },
                    modifier = Modifier.fillMaxWidth().testTag("calendar_trigger_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextColorPrimary,
                        unfocusedTextColor = TextColorPrimary,
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = PrimaryTeal,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = PrimaryTeal,
                        unfocusedLabelColor = SoftText
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = logNotes,
                    onValueChange = { 
                        logNotes = it 
                    },
                    placeholder = { Text("What led to this outcome? Reflection notes...", color = TextColorSecondary.copy(alpha = 0.6f)) },
                    label = { Text("CBT Quick Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextColorPrimary,
                        unfocusedTextColor = TextColorPrimary,
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = PrimaryTeal,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = PrimaryTeal,
                        unfocusedLabelColor = SoftText
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColorSecondary),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = {
                            onSave(
                                logStatus,
                                if (logStatus == "Relapsed") logCount else 0,
                                if (logStatus == "Relapsed") logTimeOfDay else "Anytime",
                                logTrigger,
                                logNotes
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).height(48.dp).testTag("save_calendar_log_btn")
                    ) {
                        Text("SAVE LOG", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------- QUICK LOG ENTRY FORM DIALOG ----------------
@Composable
fun QuickLogDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onLog: (status: String, count: Int, time: String, trigger: String, moodBefore: String, moodAfter: String, notes: String) -> Unit
) {
    var status by remember { mutableStateOf("Clean") } // "Clean", "Relapsed", "UrgeControlled"
    var trigger by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var moodBefore by remember { mutableStateOf("Neutral") }
    var moodAfter by remember { mutableStateOf("Neutral") }
    var timeOfDay by remember { mutableStateOf("Evening") }
    var logCount by remember { mutableStateOf(1) }
    var logTimeOfDay by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBackground,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "LOG TODAY: ${habit.name.uppercase()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TextColorPrimary
                )

                // Status tabs
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Daily Result", color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Clean", "Relapsed", "UrgeControlled").forEach { st ->
                            val selected = status == st
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) PrimaryTeal else BorderColor)
                                    .clickable { status = st }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = st,
                                    color = if (selected) Color.White else TextColorSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                if (status == "Relapsed") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarningRed.copy(alpha = 0.08f))
                            .border(1.dp, WarningRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️ SLIP / RELAPSE DETAILS",
                            color = WarningRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        
                        Text(
                            text = "How many times did you slip today?",
                            color = TextColorPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { if (logCount > 1) logCount-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BorderColor.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextColorPrimary)
                            }
                            
                            Text(
                                text = logCount.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextColorPrimary,
                                modifier = Modifier.testTag("quick_slip_count_text")
                            )
                            
                            IconButton(
                                onClick = { logCount++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BorderColor.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextColorPrimary)
                            }
                        }
                        
                        Text(
                            text = "Time of slip(s):",
                            color = TextColorPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        
                        // Select general time sectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Morning", "Afternoon", "Evening", "Night").forEach { sector ->
                                val isChecked = logTimeOfDay.contains(sector, ignoreCase = true)
                                val resolvedColor = if (isChecked) WarningRed else BorderColor
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChecked) WarningRed.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(1.dp, resolvedColor, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val parts = logTimeOfDay.split(",")
                                                .map { it.trim() }
                                                .filter { it.isNotEmpty() }
                                                .toMutableSet()
                                            if (parts.contains(sector)) {
                                                parts.remove(sector)
                                            } else {
                                                parts.add(sector)
                                            }
                                            logTimeOfDay = parts.sorted().joinToString(", ")
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sector,
                                        fontSize = 10.sp,
                                        color = if (isChecked) WarningRed else TextColorSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        // Custom text field for specific times
                        OutlinedTextField(
                            value = logTimeOfDay,
                            onValueChange = { logTimeOfDay = it },
                            placeholder = { Text("e.g. 10:30 AM, 4:00 PM or custom notes", fontSize = 11.sp, color = TextColorSecondary.copy(alpha = 0.5f)) },
                            label = { Text("Slip Times", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("quick_slip_time_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextColorPrimary,
                                unfocusedTextColor = TextColorPrimary,
                                unfocusedBorderColor = BorderColor,
                                focusedBorderColor = WarningRed,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedLabelColor = WarningRed,
                                unfocusedLabelColor = SoftText
                            ),
                            singleLine = true
                        )
                    }
                }

                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    placeholder = { Text("Main trigger reason (e.g. stress, fatigue)") },
                    label = { Text("Trigger Tag") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextColorPrimary,
                        unfocusedTextColor = TextColorPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = PrimaryTeal,
                        unfocusedLabelColor = SoftText
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("log_trigger_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("What led to this outcome? Any lessons learned?") },
                    label = { Text("CBT Quick Notes") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextColorPrimary,
                        unfocusedTextColor = TextColorPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = PrimaryTeal,
                        unfocusedLabelColor = SoftText
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("log_notes_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColorPrimary)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = {
                            onLog(
                                status,
                                if (status == "Relapsed") logCount else 0,
                                if (status == "Relapsed") logTimeOfDay else timeOfDay,
                                trigger,
                                if (status == "Relapsed") "Neutral" else moodBefore,
                                if (status == "Relapsed") "Guilty" else moodAfter,
                                notes
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier.weight(1.5f).testTag("dialog_log_confirm_btn")
                    ) {
                        Text("LOG RECOVERY", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------- JOURNAL REFLECTIONS WRITER DIALOG ----------------
@Composable
fun JournalWriterDialog(
    habits: List<Habit>,
    onDismiss: () -> Unit,
    onSave: (text: String, mood: String, habitId: Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("Calm") }
    var selectedHabitId by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBackground,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "DAILY RECOVERY DIARY",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TextColorPrimary
                )

                // Mood selector row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Select Reflection Mood", color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Calm", "Anxious", "Stressed", "Happy").forEach { md ->
                            val selected = mood == md
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) SoftOrange else BorderColor)
                                    .clickable { mood = md }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(md, color = if (selected) Color.White else TextColorSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Text body input
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Write down your emotional reflections, trigger counters, or daily sobriety triumphs...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("journal_body_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextColorPrimary,
                        unfocusedTextColor = TextColorPrimary,
                        focusedBorderColor = SoftOrange,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = SoftOrange,
                        unfocusedLabelColor = SoftText
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColorPrimary)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = { 
                            if (text.isNotEmpty()) {
                                onSave(text, mood, selectedHabitId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftOrange),
                        modifier = Modifier.weight(1.5f).testTag("journal_save_btn")
                    ) {
                        Text("SAVE REFLECTION", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------- ADD BAD HABIT CREATION DIALOG ----------------
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, severity: String, goal: String, tags: String, target: Int, icon: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Smoking") }
    var severity by remember { mutableStateOf("High") }
    var recoveryGoal by remember { mutableStateOf("0 per day") }
    var triggerTags by remember { mutableStateOf("") }
    var customIcon by remember { mutableStateOf("smoking") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBackground,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "DECLARE WAR ON BAD HABIT",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TextColorPrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Habit name e.g. Smoking, Screen time") },
                    label = { Text("Bad Habit Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextColorPrimary,
                        unfocusedTextColor = TextColorPrimary,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = PrimaryTeal,
                        unfocusedLabelColor = SoftText
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_habit_name_input")
                )

                // Category selection chooser
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Category", color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var expanded by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BorderColor, shape = RoundedCornerShape(8.dp))
                                .clickable { expanded = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, color = TextColorPrimary)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "arrow", tint = TextColorPrimary)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(CardBackground)
                        ) {
                            listOf("Smoking", "Porn", "Screen Time", "Junk Food", "Alcohol", "Gaming", "Procrastination", "Custom").forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = TextColorPrimary) },
                                    onClick = {
                                        category = cat
                                        customIcon = cat.lowercase()
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Severity Selection
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("High", "Medium", "Low").forEach { sev ->
                        val selected = severity == sev
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) {
                                        when (sev) {
                                            "High" -> WarningRed
                                            "Medium" -> SoftOrange
                                            else -> PrimaryTeal
                                        }
                                    } else BorderColor
                                )
                                .clickable { severity = sev }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sev, color = if (selected) Color.White else TextColorSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = recoveryGoal,
                        onValueChange = { recoveryGoal = it },
                        placeholder = { Text("e.g. 0 per day") },
                        label = { Text("Recovery Goal") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextColorPrimary,
                            unfocusedTextColor = TextColorPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = PrimaryTeal,
                            unfocusedLabelColor = SoftText
                        ),
                        modifier = Modifier.weight(1f).testTag("add_habit_goal_input")
                    )

                    OutlinedTextField(
                        value = triggerTags,
                        onValueChange = { triggerTags = it },
                        placeholder = { Text("stress, fatigue") },
                        label = { Text("Triggers (comma-spaced)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextColorPrimary,
                            unfocusedTextColor = TextColorPrimary,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = PrimaryTeal,
                            unfocusedLabelColor = SoftText
                        ),
                        modifier = Modifier.weight(1f).testTag("add_habit_tags_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColorPrimary)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                onConfirm(name, category, severity, recoveryGoal, triggerTags, 1, customIcon)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier.weight(1.5f).testTag("dialog_add_habit_confirm_btn")
                    ) {
                        Text("START SOVEREIGN ROAD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Simple Helper Extension for CircleShape
fun circleShape() = CircleShape
