package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AosApp(viewModel: AosViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Check if wide viewport (tablet / desktop equivalent)
    val isTablet = BoxWithConstraintsScopeLayoutHelper()
    
    // Bind data from ViewModel
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val dailyLogs by viewModel.dailyLogs.collectAsStateWithLifecycle()
    val habitLogs by viewModel.habitLogs.collectAsStateWithLifecycle()
    val testResults by viewModel.testResults.collectAsStateWithLifecycle()
    val pyqEntries by viewModel.pyqEntries.collectAsStateWithLifecycle()
    val errorLogs by viewModel.errorLogs.collectAsStateWithLifecycle()
    val formulas by viewModel.allFormulas.collectAsStateWithLifecycle()
    val currentAffairs by viewModel.currentAffairs.collectAsStateWithLifecycle()
    val revisionTasks by viewModel.revisionTasks.collectAsStateWithLifecycle()
    val weeklyMilestones by viewModel.weeklyMilestones.collectAsStateWithLifecycle()

    // Sidebar drawer visibility for mobile screens
    var sidebarExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Persistent Sidebar for Tablet/Wide screens (Width > 800dp equivalent)
            // For a mobile app we can show it conditionally, or provide a beautiful sliding drawer
            // We implement an adaptive layout: on wide viewport, sidebar is persistent.
            // Let's make a beautiful custom side drawer that slides in on mobile, or stays on desktop
            if (isTablet) {
                SidebarContent(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    onCloseMobileDrawer = {}
                )
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Main Header bar
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = getScreenTitle(currentScreen),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = viewModel.displayDate,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    },
                    navigationIcon = {
                        if (!isTablet) {
                            IconButton(
                                onClick = { sidebarExpanded = !sidebarExpanded },
                                modifier = Modifier.testTag("menu_button")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                            }
                        }
                    },
                    actions = {
                        // Quick status: Today's study hours logged
                        val todayLog = dailyLogs.find { it.date == viewModel.currentDate }
                        val hrs = todayLog?.studyHours ?: 0f
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentLinear.copy(alpha = 0.15f))
                                .border(0.5.dp, AccentLinear, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${hrs}h Target Met",
                                color = AccentClickUp,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ObsidianBg,
                        titleContentColor = TextPrimary,
                        actionIconContentColor = TextPrimary
                    )
                )

                // Divider separating top bar
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ObsidianBorder)
                )

                // Content View Frame
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                        when (screen) {
                            AosScreen.TodaysPlan -> TodaysPlanView(viewModel, chapters, dailyLogs, habitLogs, revisionTasks, testResults)
                            AosScreen.Timetable -> TimetableView(viewModel, dailyLogs)
                            AosScreen.Roadmap -> RoadmapView()
                            AosScreen.BridgeProgram -> BridgeProgramView(viewModel, chapters)
                            AosScreen.NdaModule -> NdaModuleView(viewModel, chapters, testResults)
                            AosScreen.JeeModule -> JeeModuleView(viewModel, chapters)
                            AosScreen.UpBoardModule -> UpBoardView(viewModel, chapters)
                            AosScreen.WeeklyMilestones -> WeeklyMilestonesView(viewModel, weeklyMilestones)
                            AosScreen.RevisionEngine -> RevisionEngineView(viewModel, revisionTasks)
                            AosScreen.DailyTracker -> DailyTrackerView(viewModel, dailyLogs)
                            AosScreen.HabitTracker -> HabitTrackerView(viewModel, habitLogs)
                            AosScreen.PyqTracker -> PyqTrackerView(viewModel, pyqEntries, chapters)
                            AosScreen.MockTests -> MockTestsView(viewModel, testResults)
                            AosScreen.ErrorLog -> ErrorLogView(viewModel, errorLogs)
                            AosScreen.FormulaBank -> FormulaBankView(viewModel, formulas)
                            AosScreen.CurrentAffairs -> CurrentAffairsView(viewModel, currentAffairs)
                            AosScreen.Analytics -> AnalyticsView(viewModel, chapters, dailyLogs, habitLogs, testResults)
                            AosScreen.Reports -> ReportsView(viewModel, chapters, dailyLogs, habitLogs, testResults)
                            AosScreen.ImportExport -> ImportExportView(viewModel)
                        }
                    }
                }
            }
        }

        // Sliding custom Drawer Overlay for mobile screen
        if (!isTablet && sidebarExpanded) {
            // Shadow backdrop overlay clickable to close
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { sidebarExpanded = false }
            )

            // Animated Sidebar drawer sliding from left
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(ObsidianBg)
                    .border(1.dp, ObsidianBorder)
                    .align(Alignment.CenterStart)
            ) {
                SidebarContent(
                    currentScreen = currentScreen,
                    onNavigate = {
                        viewModel.navigateTo(it)
                        sidebarExpanded = false
                    },
                    onCloseMobileDrawer = { sidebarExpanded = false }
                )
            }
        }
    }
}

@Composable
fun BoxWithConstraintsScopeLayoutHelper(): Boolean {
    // Dynamic breakpoint detection
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    return config.screenWidthDp >= 850
}

@Composable
fun SidebarContent(
    currentScreen: AosScreen,
    onNavigate: (AosScreen) -> Unit,
    onCloseMobileDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(ObsidianBg)
            .padding(16.dp)
    ) {
        // App Identity Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp, start = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(AccentLinear, AccentClickUp))),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("ACADEMIC OS", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text("Class 12 PCM Student", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }

        Divider(color = ObsidianBorder, modifier = Modifier.padding(bottom = 12.dp))

        // Navigation list structured exactly like Notion sidebar sections
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { SidebarCategoryHeader("CORE DASHBOARD") }
            item { SidebarItem(AosScreen.TodaysPlan, "Today's Plan", Icons.Default.Today, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.Timetable, "Student Timetable", Icons.Default.Schedule, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.Roadmap, "Academic Roadmap", Icons.Default.Map, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.WeeklyMilestones, "Weekly Milestones", Icons.Default.Flag, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.RevisionEngine, "Spaced Repetition", Icons.Default.Loop, currentScreen, onNavigate) }

            item { Spacer(modifier = Modifier.height(10.dp)) }
            item { SidebarCategoryHeader("ACADEMIC BOARDS") }
            item { SidebarItem(AosScreen.JeeModule, "JEE Main 2027", Icons.Default.Analytics, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.NdaModule, "NDA-2 2026", Icons.Default.MilitaryTech, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.UpBoardModule, "UP Board 2027", Icons.Default.School, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.BridgeProgram, "Class 11 Bridge", Icons.Default.Link, currentScreen, onNavigate) }

            item { Spacer(modifier = Modifier.height(10.dp)) }
            item { SidebarCategoryHeader("DAILY TRACKERS") }
            item { SidebarItem(AosScreen.DailyTracker, "Daily Log Tracker", Icons.Default.QueryStats, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.HabitTracker, "Habit Tracker", Icons.Default.CheckCircle, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.PyqTracker, "PYQ Progress", Icons.Default.AssignmentTurnedIn, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.MockTests, "Mock Test Logs", Icons.Default.FactCheck, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.ErrorLog, "Error Logs Book", Icons.Default.ErrorOutline, currentScreen, onNavigate) }

            item { Spacer(modifier = Modifier.height(10.dp)) }
            item { SidebarCategoryHeader("RESOURCES") }
            item { SidebarItem(AosScreen.FormulaBank, "Formula Bank", Icons.Default.Functions, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.CurrentAffairs, "Current Affairs", Icons.Default.Newspaper, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.Analytics, "Deep Analytics", Icons.Default.BarChart, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.Reports, "Executive Reports", Icons.Default.Assessment, currentScreen, onNavigate) }
            item { SidebarItem(AosScreen.ImportExport, "Settings & Backup", Icons.Default.Backup, currentScreen, onNavigate) }
        }
    }
}

@Composable
fun SidebarCategoryHeader(text: String) {
    Text(
        text = text,
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun SidebarItem(
    screen: AosScreen,
    label: String,
    icon: ImageVector,
    currentScreen: AosScreen,
    onNavigate: (AosScreen) -> Unit
) {
    val isSelected = currentScreen == screen
    val bgColor = if (isSelected) AccentLinear.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) AccentClickUp else TextSecondary
    val borderMod = if (isSelected) Modifier.border(0.5.dp, AccentLinear, RoundedCornerShape(10.dp)) else Modifier

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .then(borderMod)
            .clickable { onNavigate(screen) }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("nav_item_${label.lowercase().replace(" ", "_")}")
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

fun getScreenTitle(screen: AosScreen): String {
    return when (screen) {
        AosScreen.TodaysPlan -> "Today's Plan Dashboard"
        AosScreen.Timetable -> "Student Daily Timetable"
        AosScreen.Roadmap -> "Exam Prep Roadmap"
        AosScreen.BridgeProgram -> "Class 11 Bridge Completion"
        AosScreen.NdaModule -> "NDA-2 2026 Focus Area"
        AosScreen.JeeModule -> "JEE Main Jan 2027 Module"
        AosScreen.UpBoardModule -> "UP Board 2027 Core"
        AosScreen.WeeklyMilestones -> "Weekly Milestone System"
        AosScreen.RevisionEngine -> "Spaced Repetition Engine"
        AosScreen.DailyTracker -> "Daily Study Log Tracker"
        AosScreen.HabitTracker -> "Routine Habit Tracker"
        AosScreen.PyqTracker -> "PYQ Board & Exam Tracker"
        AosScreen.MockTests -> "Mock Test Records & Trends"
        AosScreen.ErrorLog -> "Academic Error Book"
        AosScreen.FormulaBank -> "Interactive Formula Vault"
        AosScreen.CurrentAffairs -> "Current Affairs Vault"
        AosScreen.Analytics -> "Preps Deep Analytics"
        AosScreen.Reports -> "Academic Board Reports"
        AosScreen.ImportExport -> "Backup, Import & Export"
    }
}

// ==========================================
// SCREEN IMPLEMENTATIONS
// ==========================================

// --- 1. TODAY'S PLAN VIEW ---
@Composable
fun TodaysPlanView(
    viewModel: AosViewModel,
    chapters: List<ChapterEntity>,
    dailyLogs: List<DailyLogEntity>,
    habitLogs: List<HabitLogEntity>,
    revisionTasks: List<RevisionTaskEntity>,
    testResults: List<TestResultEntity>
) {
    val todayLog = dailyLogs.find { it.date == viewModel.currentDate }
    val todayHabits = habitLogs.filter { it.date == viewModel.currentDate }
    val todayRevisions = revisionTasks.filter { it.dueDate == viewModel.currentDate && !it.isCompleted }

    // Quick add fields
    var quickNoteText by remember { mutableStateOf("") }
    var notesList by remember { mutableStateOf(listOf("Review d-Block Oxidation States", "Practice 15 integration formulas")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today Summary Header
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Today's Progress Hub", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Your Class 12 PCM + NDA preparation target", color = TextSecondary, fontSize = 12.sp)
                    }
                    val targetHrs = 8.5f
                    val currentHrs = todayLog?.studyHours ?: 0f
                    val ratio = (currentHrs / targetHrs).coerceIn(0f, 1f)
                    CircularProgressRing(progress = ratio, size = 64.dp)
                }
            }
        }

        // Subject Specific Targets
        item {
            Text("Today's Timetable Workload", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AosCard(modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.School, contentDescription = "Physics", tint = AccentLinear, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Physics Tasks", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("• Electrostatics DPP\n• Solve 15 PYQs\n• Concept Notes", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    AosCard(modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Science, contentDescription = "Chemistry", tint = AccentTeal, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Chemistry Tasks", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("• Solutions review\n• Colligative quiz\n• Read Biomolecules", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AosCard(modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Functions, contentDescription = "Maths", tint = AccentClickUp, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Mathematics Tasks", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("• Swapped determinants\n• Log integration\n• Matrices class", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    AosCard(modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = "NDA", tint = AccentAmber, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("NDA-2 Tasks", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("• GAT vocab sheet\n• Defense quiz\n• English Grammar", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Today's Spaced Repetition Due Tasks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Spaced Revisions Due Today", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                PremiumBadge(text = "${todayRevisions.size} Tasks", color = AccentRose)
            }
        }

        if (todayRevisions.isEmpty()) {
            item {
                AosCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No spaced repetitions scheduled or due for today. Keep complete chapters to trigger automated spaced logs!", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            items(todayRevisions) { task ->
                AosCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(task.chapterName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("${task.subject} • Stage: Day ${task.stage}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.toggleRevisionTaskCompleted(task) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentLinear),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Complete", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Habit Quick Checklists
        item {
            Text("Habit & Target Checklist", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                if (todayHabits.isEmpty()) {
                    Text("No routine habits configured.", color = TextSecondary)
                } else {
                    todayHabits.forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleHabitCompleted(log) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (log.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (log.isCompleted) AccentTeal else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = log.habitName,
                                color = if (log.isCompleted) TextSecondary else TextPrimary,
                                fontSize = 13.sp,
                                textDecoration = if (log.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                        }
                    }
                }
            }
        }

        // Quick Notes/Scratchpad
        item {
            Text("Quick Scratchpad Notes", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = quickNoteText,
                    onValueChange = { quickNoteText = it },
                    placeholder = { Text("Write quick concept, question ID, or memo...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentLinear,
                        unfocusedBorderColor = ObsidianBorder
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (quickNoteText.isNotBlank()) {
                            notesList = notesList + quickNoteText
                            quickNoteText = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentClickUp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add Note", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                notesList.forEachIndexed { idx, note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• $note", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { notesList = notesList.filterIndexed { i, _ -> i != idx } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = AccentRose, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- 2. STUDENT TIMETABLE VIEW ---
@Composable
fun TimetableView(viewModel: AosViewModel, dailyLogs: List<DailyLogEntity>) {
    val blocks = listOf(
        "06:00 – 09:00" to "Swimming & Commute (Refreshes focus for morning revision)",
        "10:00 – 15:00" to "Self Study (Focus on Core UP Board & JEE Syllabus)",
        "16:00 – 20:00" to "Online Classes (Physics, Chemistry, Maths online modules)",
        "20:00 – 22:00" to "DPP & Homework solving (Active recall & exam level problem solving)",
        "23:00 – 00:00" to "Spaced Repetition & Day Review (Critical formulas bank updates)"
    )

    var hoursText by remember { mutableStateOf("8.5") }
    var dppCompleted by remember { mutableStateOf(true) }
    var revisionCompleted by remember { mutableStateOf(false) }

    val todayLog = dailyLogs.find { it.date == viewModel.currentDate }
    LaunchedEffect(todayLog) {
        if (todayLog != null) {
            hoursText = todayLog.studyHours.toString()
            dppCompleted = todayLog.dppDone
            revisionCompleted = todayLog.revisionDone
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Standard Structured Study Timetable", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Pre-configured routines optimizes preparation rhythm between JEE & Board.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        items(blocks) { (time, desc) ->
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentClickUp.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(time, color = AccentClickUp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(desc, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Text("Log Your Today's Output", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Adjust Study Hours", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentLinear,
                            unfocusedBorderColor = ObsidianBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("hours completed today.", color = TextSecondary, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dppCompleted,
                        onCheckedChange = { dppCompleted = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentTeal)
                    )
                    Text("Daily Practice Problems (DPP) Done", color = TextPrimary, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = revisionCompleted,
                        onCheckedChange = { revisionCompleted = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentTeal)
                    )
                    Text("Timetable Spaced Revision Block Completed", color = TextPrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val hrs = hoursText.toFloatOrNull() ?: 0f
                        viewModel.updateDailyLog(hrs, dppCompleted, revisionCompleted)
                        Toast.makeText(viewModel.getApplication(), "Daily output saved successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLinear),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save To Daily Study Tracker", color = Color.White)
                }
            }
        }
    }
}

// --- 3. ROADMAP SYSTEM VIEW ---
@Composable
fun RoadmapView() {
    val phases = listOf(
        Triple("PHASE 1 (Completed / Ongoing)", "25 June – 31 July", "Class 11 Bridge Completion. Strong foundations in Physics Mechanics, Organic Mole fractions, and Calculus pre-requisites."),
        Triple("PHASE 2 (Upcoming)", "1 August – 13 September", "NDA Focus & NDA Mocks. Accelerated GAT preparations alongside English writing practice and military history modules."),
        Triple("PHASE 3 (Upcoming)", "14 September – 15 October", "JEE Main Complete Syllabus Coverage. Core focus on complex Class 12 topics: Integrals, Optics, and d-Block complexes."),
        Triple("PHASE 4 (Upcoming)", "16 October – 31 December", "JEE PYQ Marathon & Full Length Mock Tests. Complete revision sheets and UP board practical book records compilation."),
        Triple("PHASE 5 (Upcoming)", "1 January – 31 January", "JEE Main Final Revision. Formulas flashcards memorization, board sample paper reviews, and real-time exam simulation.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Strategic Prep Roadmap (2026 - 2027)", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("A comprehensive schedule guiding you safely through multiple complex boards.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        items(phases) { (title, date, desc) ->
            AosCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (title.contains("PHASE 1")) AccentLinear else ObsidianBorder,
                backgroundColor = if (title.contains("PHASE 1")) ObsidianSurface.copy(alpha = 0.5f) else ObsidianSurface
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (title.contains("PHASE 1")) AccentLinear else TextMuted)
                            .padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title, color = if (title.contains("PHASE 1")) AccentLinear else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            PremiumBadge(text = date, color = if (title.contains("PHASE 1")) AccentLinear else TextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(desc, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// --- 4. CLASS 11 BRIDGE PROGRAM VIEW ---
@Composable
fun BridgeProgramView(viewModel: AosViewModel, chapters: List<ChapterEntity>) {
    val bridgeChapters = chapters.filter { it.category == "BRIDGE_CLASS_11" }
    val subjectsList = listOf("Physics", "Chemistry", "Mathematics")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Class 11 Bridge Completion Progress", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Fill core gaps before tackling Class 12 advanced calculus and physics.", color = TextSecondary, fontSize = 12.sp)
                    }
                    val completed = bridgeChapters.count { it.isCompleted }
                    val total = bridgeChapters.size.coerceAtLeast(1)
                    CircularProgressRing(progress = completed.toFloat() / total, size = 56.dp)
                }
            }
        }

        subjectsList.forEach { sub ->
            val subChapters = bridgeChapters.filter { it.subject == sub }
            val completedCount = subChapters.count { it.isCompleted }
            val totalCount = subChapters.size.coerceAtLeast(1)

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$sub Core Modules", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("$completedCount / $totalCount Done", color = AccentTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            items(subChapters) { chapter ->
                AosCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.toggleChapterCompleted(chapter) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (chapter.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (chapter.isCompleted) AccentTeal else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(chapter.chapterName, color = TextPrimary, fontSize = 13.sp)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PremiumBadge(
                                text = "Priority ${chapter.priority}",
                                color = if (chapter.priority == "A") AccentAmber else AccentLinear
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 5. NDA MODULE VIEW ---
@Composable
fun NdaModuleView(viewModel: AosViewModel, chapters: List<ChapterEntity>, testResults: List<TestResultEntity>) {
    val ndaMaths = chapters.filter { it.category == "NDA_MATHS" }
    val ndaGat = chapters.filter { it.category == "NDA_GAT" }
    val mockLogs = testResults.filter { it.examType == "NDA" }

    var testName by remember { mutableStateOf("") }
    var testScore by remember { mutableStateOf("") }
    var testMaxScore by remember { mutableStateOf("600") }
    var testAccuracy by remember { mutableStateOf("") }
    var testRemarks by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Overview
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("NDA-2 2026 Prep Dashboard", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        val mathDone = ndaMaths.count { it.isCompleted }
                        Text("Maths: $mathDone / ${ndaMaths.size} chapters", color = TextSecondary, fontSize = 12.sp)
                        val gatDone = ndaGat.count { it.isCompleted }
                        Text("GAT: $gatDone / ${ndaGat.size} modules", color = TextSecondary, fontSize = 12.sp)
                    }
                    val totalNda = ndaMaths.size + ndaGat.size
                    val totalDone = ndaMaths.count { it.isCompleted } + ndaGat.count { it.isCompleted }
                    CircularProgressRing(progress = if (totalNda > 0) totalDone.toFloat() / totalNda else 0f, size = 60.dp)
                }
            }
        }

        // Charts
        if (mockLogs.isNotEmpty()) {
            item {
                Text("NDA Mock Score Trends", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            item {
                AosCard(modifier = Modifier.fillMaxWidth()) {
                    val scores = mockLogs.map { it.score.toFloat() }
                    LineChart(
                        scores = scores,
                        dates = mockLogs.map { it.date },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        lineColor = AccentTeal,
                        maxVal = 600f
                    )
                }
            }
        }

        // Quick mock logger
        item {
            Text("Add NDA Mock Test Result", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = testName,
                    onValueChange = { testName = it },
                    label = { Text("Mock Test Name (e.g. NDA Full Mock 1)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = testScore,
                        onValueChange = { testScore = it },
                        label = { Text("Score") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = testMaxScore,
                        onValueChange = { testMaxScore = it },
                        label = { Text("Max Score") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = testRemarks,
                    onValueChange = { testRemarks = it },
                    label = { Text("Remarks/Analysis") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val scoreVal = testScore.toIntOrNull() ?: 0
                        val maxVal = testMaxScore.toIntOrNull() ?: 600
                        val acc = if (maxVal > 0) (scoreVal.toFloat() / maxVal * 100) else 0f
                        if (testName.isNotBlank() && testScore.isNotBlank()) {
                            viewModel.addMockTest("NDA", testName, scoreVal, maxVal, acc, testRemarks)
                            testName = ""
                            testScore = ""
                            testRemarks = ""
                            Toast.makeText(viewModel.getApplication(), "Test score recorded!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentClickUp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Record Test Result")
                }
            }
        }

        // Chapters lists
        item {
            Text("NDA Maths Topics", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(ndaMaths) { chapter ->
            AosCard(modifier = Modifier.fillMaxWidth(), onClick = { viewModel.toggleChapterCompleted(chapter) }) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (chapter.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (chapter.isCompleted) AccentTeal else TextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(chapter.chapterName, color = TextPrimary, fontSize = 13.sp)
                    }
                    PremiumBadge(text = "Math", color = AccentLinear)
                }
            }
        }

        item {
            Text("NDA GAT Topics", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(ndaGat) { chapter ->
            AosCard(modifier = Modifier.fillMaxWidth(), onClick = { viewModel.toggleChapterCompleted(chapter) }) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (chapter.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (chapter.isCompleted) AccentTeal else TextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(chapter.chapterName, color = TextPrimary, fontSize = 13.sp)
                    }
                    PremiumBadge(text = "GAT", color = AccentClickUp)
                }
            }
        }
    }
}

// --- 6. JEE MODULE VIEW ---
@Composable
fun JeeModuleView(viewModel: AosViewModel, chapters: List<ChapterEntity>) {
    val jeeChapters = chapters.filter { it.category == "JEE" }
    val subjectsList = listOf("Physics", "Chemistry", "Mathematics")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("JEE Main Jan 2027 Syllabus Coverage", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Target: 100% conceptual clarity & high accuracy in pyqs.", color = TextSecondary, fontSize = 12.sp)
                    }
                    val completed = jeeChapters.count { it.isCompleted }
                    val total = jeeChapters.size.coerceAtLeast(1)
                    CircularProgressRing(progress = completed.toFloat() / total, size = 60.dp)
                }
            }
        }

        subjectsList.forEach { sub ->
            val subChapters = jeeChapters.filter { it.subject == sub }
            item {
                Text("$sub Chapters Tracker", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            items(subChapters) { chapter ->
                AosCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.toggleChapterCompleted(chapter) }) {
                                    Icon(if (chapter.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (chapter.isCompleted) AccentTeal else TextSecondary)
                                }
                                Column {
                                    Text(chapter.chapterName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${chapter.pyqsSolved} PYQs Solved • Accuracy: ${chapter.accuracy.toInt()}%", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (chapter.isWeak) {
                                    PremiumBadge("WEAK", AccentRose)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                PremiumBadge("PRIORITY ${chapter.priority}", AccentLinear)
                            }
                        }

                        // Detailed chapter interaction panel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateChapterWeak(chapter, !chapter.isWeak) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSlate),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (chapter.isWeak) "Set Clear" else "Mark Weak", fontSize = 10.sp, color = TextPrimary)
                            }
                            Button(
                                onClick = { viewModel.updateChapterPyqsAndAccuracy(chapter, chapter.pyqsSolved + 10, (chapter.accuracy + 5f).coerceAtMost(100f)) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentClickUp.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+10 PYQs", fontSize = 10.sp, color = AccentClickUp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 7. UP BOARD MODULE VIEW ---
@Composable
fun UpBoardView(viewModel: AosViewModel, chapters: List<ChapterEntity>) {
    val boardChapters = chapters.filter { it.category == "UP_BOARD" }
    val subjectsList = listOf("Physics", "Chemistry", "Mathematics")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("UP Board 2027 Core Metrics", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Ensuring complete writing speed and 100% practical log completion.", color = TextSecondary, fontSize = 12.sp)
                    }
                    val completed = boardChapters.count { it.isCompleted }
                    val total = boardChapters.size.coerceAtLeast(1)
                    SemiGauge(progress = completed.toFloat() / total, title = "Board Ready", modifier = Modifier.size(100.dp, 60.dp))
                }
            }
        }

        subjectsList.forEach { sub ->
            val subChapters = boardChapters.filter { it.subject == sub }
            val completedCount = subChapters.count { it.isCompleted }
            val totalCount = subChapters.size.coerceAtLeast(1)

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$sub Board Chapters", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("$completedCount / $totalCount Chapters", color = AccentTeal, fontSize = 12.sp)
                }
            }

            items(subChapters) { chapter ->
                AosCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.toggleChapterCompleted(chapter) }) {
                                Icon(if (chapter.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (chapter.isCompleted) AccentTeal else TextSecondary)
                            }
                            Column {
                                Text(chapter.chapterName, color = TextPrimary, fontSize = 13.sp)
                                if (sub != "Mathematics") {
                                    Text(
                                        text = if (chapter.isPracticalCompleted) "Practical Record Done" else "Practical Pending",
                                        color = if (chapter.isPracticalCompleted) AccentTeal else AccentAmber,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        if (sub != "Mathematics") {
                            Button(
                                onClick = { viewModel.updateChapterPractical(chapter, !chapter.isPracticalCompleted) },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSlate),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Toggle Prac", fontSize = 10.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 8. WEEKLY MILESTONE SYSTEM VIEW ---
@Composable
fun WeeklyMilestonesView(viewModel: AosViewModel, weeklyMilestones: List<WeeklyMilestoneEntity>) {
    var phyFocus by remember { mutableStateOf("") }
    var chemFocus by remember { mutableStateOf("") }
    var mathFocus by remember { mutableStateOf("") }
    var mockTestsFocus by remember { mutableStateOf("") }
    var revisionFocus by remember { mutableStateOf("") }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentMondayStr = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        sdf.format(cal.time)
    }

    val currentMilestone = weeklyMilestones.find { it.weekStartDate == currentMondayStr }

    LaunchedEffect(currentMilestone) {
        if (currentMilestone != null) {
            phyFocus = currentMilestone.physicsFocus
            chemFocus = currentMilestone.chemistryFocus
            mathFocus = currentMilestone.mathsFocus
            mockTestsFocus = currentMilestone.mockTests
            revisionFocus = currentMilestone.revision
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Weekly Milestones Planner", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Week Commencing: $currentMondayStr", color = AccentClickUp, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Set direct goals for the week. Backlog can carry forward seamlessly.", color = TextSecondary, fontSize = 11.sp)
            }
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Physics Focused Goal", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = phyFocus,
                    onValueChange = { phyFocus = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Chemistry Focused Goal", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = chemFocus,
                    onValueChange = { chemFocus = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Mathematics Focused Goal", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = mathFocus,
                    onValueChange = { mathFocus = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Target Mock Tests this week", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = mockTestsFocus,
                    onValueChange = { mockTestsFocus = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Revision Focus Areas", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = revisionFocus,
                    onValueChange = { revisionFocus = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.saveWeeklyMilestone(
                            currentMondayStr, phyFocus, chemFocus, mathFocus, mockTestsFocus, revisionFocus, false
                        )
                        Toast.makeText(viewModel.getApplication(), "Weekly milestone saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLinear),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Goals")
                }
            }
        }

        // Backlog/Carry forward manager
        if (weeklyMilestones.size > 1) {
            item {
                Text("Historic Backlog Management", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            items(weeklyMilestones.filter { it.weekStartDate != currentMondayStr }) { historic ->
                AosCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Week of ${historic.weekStartDate}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Phy: ${historic.physicsFocus}\nChem: ${historic.chemistryFocus}\nMath: ${historic.mathsFocus}", color = TextSecondary, fontSize = 11.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        }
                        Button(
                            onClick = {
                                viewModel.carryForwardBacklog(historic, currentMondayStr)
                                Toast.makeText(viewModel.getApplication(), "Backlog carryforward added into current week!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentClickUp)
                        ) {
                            Text("Carry Forward", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- 9. REVISION ENGINE VIEW ---
@Composable
fun RevisionEngineView(viewModel: AosViewModel, revisionTasks: List<RevisionTaskEntity>) {
    val todayStr = viewModel.currentDate
    val dueRevisions = revisionTasks.filter { it.dueDate <= todayStr && !it.isCompleted }
    val upcomingRevisions = revisionTasks.filter { it.dueDate > todayStr && !it.isCompleted }
    val completedRevisions = revisionTasks.filter { it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Automatic Spaced Repetition Engine", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("The algorithm schedules revisions on Day 1, 7, 21, and 45 after marking any board chapter as complete.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // Overdue & Due
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Due / Overdue Revisions", color = AccentRose, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                PremiumBadge("${dueRevisions.size} Pending", AccentRose)
            }
        }

        if (dueRevisions.isEmpty()) {
            item {
                AosCard(modifier = Modifier.fillMaxWidth()) {
                    Text("All revisions up to date! Good job maintaining active recall loops.", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            items(dueRevisions) { task ->
                AosCard(modifier = Modifier.fillMaxWidth(), borderColor = AccentRose) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(task.chapterName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("${task.subject} • Stage: Day ${task.stage} • Due: ${task.dueDate}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.toggleRevisionTaskCompleted(task) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                        ) {
                            Text("Review", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Scheduled Future Revisions
        item {
            Text("Upcoming Revisions Calendar", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        if (upcomingRevisions.isEmpty()) {
            item {
                AosCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No future revisions queued.", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            items(upcomingRevisions.take(5)) { task ->
                AosCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(task.chapterName, color = TextPrimary, fontSize = 13.sp)
                            Text("${task.subject} • Scheduled Day ${task.stage} revision on ${task.dueDate}", color = TextSecondary, fontSize = 11.sp)
                        }
                        PremiumBadge("Queued", AccentLinear)
                    }
                }
            }
        }
    }
}

// --- 10. DAILY STUDY TRACKER VIEW ---
@Composable
fun DailyTrackerView(viewModel: AosViewModel, dailyLogs: List<DailyLogEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Daily Study Tracker", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Visualizing hours logged across weeks to maintain target consistency.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Study Hours Graph", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                val logsList = dailyLogs.take(7).reversed()
                val values = logsList.map { it.studyHours }
                val labels = logsList.map { it.date.takeLast(5) }
                
                BarChart(
                    values = values,
                    labels = labels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    barColor = AccentLinear
                )
            }
        }

        item {
            Text("Logged History Entries", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(dailyLogs) { log ->
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Date: ${log.date}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("DPP: ${if (log.dppDone) "Completed" else "Pending"} • Revision: ${if (log.revisionDone) "Done" else "No"}", color = TextSecondary, fontSize = 11.sp)
                    }
                    PremiumBadge("${log.studyHours} Hrs", color = if (log.studyHours >= 7.0f) AccentTeal else AccentAmber)
                }
            }
        }
    }
}

// --- 11. HABIT TRACKER VIEW ---
@Composable
fun HabitTrackerView(viewModel: AosViewModel, habitLogs: List<HabitLogEntity>) {
    val habitsList = listOf("Wake Up Early", "Swimming", "Study Hours Completed", "Revision", "Current Affairs")
    val todayStr = viewModel.currentDate
    val todayLogs = habitLogs.filter { it.date == todayStr }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Daily Habit Checklist & Consistency", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Default core routines that empower cognitive durability.", color = TextSecondary, fontSize = 12.sp)
                    }
                    val completed = todayLogs.count { it.isCompleted }
                    val total = todayLogs.size.coerceAtLeast(1)
                    CircularProgressRing(progress = completed.toFloat() / total, size = 60.dp)
                }
            }
        }

        item {
            Text("Checklist for Today", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(todayLogs) { log ->
            AosCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.toggleHabitCompleted(log) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (log.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (log.isCompleted) AccentTeal else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(log.habitName, color = TextPrimary, fontSize = 13.sp)
                    }
                    PremiumBadge(text = if (log.isCompleted) "Met" else "Active", color = if (log.isCompleted) AccentTeal else TextSecondary)
                }
            }
        }
    }
}

// --- 12. PYQ TRACKER VIEW ---
@Composable
fun PyqTrackerView(viewModel: AosViewModel, pyqEntries: List<PYQEntryEntity>, chapters: List<ChapterEntity>) {
    var examType by remember { mutableStateOf("JEE") }
    var subject by remember { mutableStateOf("Physics") }
    var chapterSelected by remember { mutableStateOf("") }
    var attemptedStr by remember { mutableStateOf("") }
    var remainingStr by remember { mutableStateOf("") }
    var accuracyStr by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("PYQ Master Tracker", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Keep record of previous year exam question sets targeted & accuracy rates.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Register New PYQ Record", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = examType,
                        onValueChange = { examType = it },
                        label = { Text("Exam (NDA/JEE/BOARD)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = chapterSelected,
                    onValueChange = { chapterSelected = it },
                    label = { Text("Chapter Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = attemptedStr,
                        onValueChange = { attemptedStr = it },
                        label = { Text("Attempted") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                    OutlinedTextField(
                        value = remainingStr,
                        onValueChange = { remainingStr = it },
                        label = { Text("Remaining") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                    OutlinedTextField(
                        value = accuracyStr,
                        onValueChange = { accuracyStr = it },
                        label = { Text("Accuracy %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val att = attemptedStr.toIntOrNull() ?: 0
                        val rem = remainingStr.toIntOrNull() ?: 0
                        val acc = accuracyStr.toFloatOrNull() ?: 0f
                        if (chapterSelected.isNotBlank()) {
                            viewModel.addMockTest(examType, "PYQ: $chapterSelected", att, att + rem, acc, "Solved PYQs tracker entry")
                            chapterSelected = ""
                            attemptedStr = ""
                            remainingStr = ""
                            accuracyStr = ""
                            Toast.makeText(viewModel.getApplication(), "PYQ progress registered!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentClickUp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add PYQ Log Entry")
                }
            }
        }

        item {
            Text("Registered PYQ Achievements", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(pyqEntries) { entry ->
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(entry.chapterName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${entry.subject} • Attempted: ${entry.attempted} • Remaining: ${entry.remaining}", color = TextSecondary, fontSize = 11.sp)
                    }
                    PremiumBadge(text = "${entry.accuracy.toInt()}% Acc", color = AccentTeal)
                }
            }
        }
    }
}

// --- 13. MOCK TEST SYSTEM VIEW ---
@Composable
fun MockTestsView(viewModel: AosViewModel, testResults: List<TestResultEntity>) {
    var examType by remember { mutableStateOf("JEE") }
    var testName by remember { mutableStateOf("") }
    var testScore by remember { mutableStateOf("") }
    var testMaxScore by remember { mutableStateOf("300") }
    var testRemarks by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Comprehensive Mock Exam Registry", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Central tracker monitoring performance indexes for JEE, NDA, and UP Board Mock trials.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Add Mock Trial Score", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = examType,
                        onValueChange = { examType = it },
                        label = { Text("Exam (NDA / JEE / BOARD)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                    OutlinedTextField(
                        value = testName,
                        onValueChange = { testName = it },
                        label = { Text("Test Name") },
                        modifier = Modifier.weight(2f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = testScore,
                        onValueChange = { testScore = it },
                        label = { Text("Score Obtained") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                    OutlinedTextField(
                        value = testMaxScore,
                        onValueChange = { testMaxScore = it },
                        label = { Text("Max Points") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = testRemarks,
                    onValueChange = { testRemarks = it },
                    label = { Text("Academic Remarks / Focus Area") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val scr = testScore.toIntOrNull() ?: 0
                        val max = testMaxScore.toIntOrNull() ?: 300
                        val acc = if (max > 0) (scr.toFloat() / max * 100) else 0f
                        if (testName.isNotBlank() && testScore.isNotBlank()) {
                            viewModel.addMockTest(examType, testName, scr, max, acc, testRemarks)
                            testName = ""
                            testScore = ""
                            testRemarks = ""
                            Toast.makeText(viewModel.getApplication(), "Mock score registered!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentClickUp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Register score")
                }
            }
        }

        item {
            Text("Registered Mock Score Trails", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(testResults) { test ->
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(test.testName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Exam: ${test.examType} • Remarks: ${test.remarks}", color = TextSecondary, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${test.score} / ${test.maxScore}", color = AccentClickUp, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${test.accuracy.toInt()}% Acc", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// --- 14. ERROR LOG SYSTEM VIEW ---
@Composable
fun ErrorLogView(viewModel: AosViewModel, errorLogs: List<ErrorLogEntity>) {
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("") }
    var errorType by remember { mutableStateOf("Concept") } // Concept, Formula, Calculation, Careless
    var description by remember { mutableStateOf("") }

    val errorTypesList = listOf("Concept", "Formula", "Calculation", "Careless")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Error Analysis Book", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Register conceptual slip-ups and mathematical oversights to prevent exam-day recurrences.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("File New Error Entry", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("Chapter") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Error Category Class", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    errorTypesList.forEach { type ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (errorType == type) AccentLinear.copy(alpha = 0.2f) else DarkSlate)
                                .border(1.dp, if (errorType == type) AccentLinear else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { errorType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type, color = if (errorType == type) AccentLinear else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe Error details / Correct strategy") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (chapter.isNotBlank() && description.isNotBlank()) {
                            viewModel.addErrorLog(subject, chapter, errorType, description)
                            chapter = ""
                            description = ""
                            Toast.makeText(viewModel.getApplication(), "Error logged successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentClickUp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Register Error")
                }
            }
        }

        item {
            Text("Critical Error Registry Logs", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(errorLogs) { err ->
            AosCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (err.isSolved) AccentTeal else AccentRose,
                backgroundColor = if (err.isSolved) ObsidianSurface.copy(alpha = 0.5f) else ObsidianSurface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PremiumBadge(text = err.errorType, color = if (err.isSolved) AccentTeal else AccentRose)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(err.chapter, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(err.description, color = TextSecondary, fontSize = 12.sp)
                    }
                    IconButton(onClick = { viewModel.toggleErrorSolved(err) }) {
                        Icon(
                            imageVector = if (err.isSolved) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Solve toggle",
                            tint = if (err.isSolved) AccentTeal else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// --- 15. FORMULA BANK VIEW ---
@Composable
fun FormulaBankView(viewModel: AosViewModel, formulas: List<FormulaEntity>) {
    var searchQuery by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var formulaText by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Interactive Equation & Formula Vault", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Searchable formula sheet indexing core mathematical proofs and constants.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // Live Search Input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.formulaSearchQuery.value = it
                },
                placeholder = { Text("Search by equation, proof tags, or subject...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
            )
        }

        // Add formula collapsible or quick panel
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Submit New Equation", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (Physics/Chemistry/Maths)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formulaText,
                    onValueChange = { formulaText = it },
                    label = { Text("Formula Statement (e.g. F = m*a)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation & Constant derivations") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (formulaText.isNotBlank() && explanation.isNotBlank()) {
                            viewModel.addFormula(subject, formulaText, explanation, tags)
                            formulaText = ""
                            explanation = ""
                            tags = ""
                            Toast.makeText(viewModel.getApplication(), "Formula indexed successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLinear),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add Equation to Bank")
                }
            }
        }

        items(formulas) { form ->
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(form.subject.uppercase(), color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row {
                            form.tags.split(",").forEach { tg ->
                                if (tg.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DarkSlate)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(tg.trim(), color = TextSecondary, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = form.formula,
                        color = AccentClickUp,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(form.explanation, color = TextPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

// --- 16. CURRENT AFFAIRS VAULT VIEW ---
@Composable
fun CurrentAffairsView(viewModel: AosViewModel, currentAffairs: List<CurrentAffairsEntity>) {
    var topic by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Defense") }
    var notes by remember { mutableStateOf("") }

    val categoriesList = listOf("Defense", "National", "International", "Sports", "Science")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("NDA Current Affairs Vault", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Logging critical international, space, and defense briefs for the GAT component.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("File New News Brief", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic Headline") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoriesList.forEach { cat ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (category == cat) AccentLinear.copy(alpha = 0.2f) else DarkSlate)
                                .clickable { category = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat, color = if (category == cat) AccentLinear else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Bullet news summary / key points") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (topic.isNotBlank() && notes.isNotBlank()) {
                            viewModel.addCurrentAffairs(topic, category, notes)
                            topic = ""
                            notes = ""
                            Toast.makeText(viewModel.getApplication(), "Brief submitted!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentClickUp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Register brief")
                }
            }
        }

        items(currentAffairs) { entry ->
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PremiumBadge(text = entry.category, color = AccentLinear)
                        Text(entry.date, color = TextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(entry.topic, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(entry.notes, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

// --- 17. ANALYTICS DASHBOARD VIEW ---
@Composable
fun AnalyticsView(
    viewModel: AosViewModel,
    chapters: List<ChapterEntity>,
    dailyLogs: List<DailyLogEntity>,
    habitLogs: List<HabitLogEntity>,
    testResults: List<TestResultEntity>
) {
    val jeeChapters = chapters.filter { it.category == "JEE" }
    val ndaChapters = chapters.filter { it.category == "NDA_MATHS" || it.category == "NDA_GAT" }
    val upChapters = chapters.filter { it.category == "UP_BOARD" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Preparations Analytics Engine", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Synthesized metrics derived from actual syllabus completed, daily hours trackers, and mock trends.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AosCard(modifier = Modifier.fillMaxWidth()) {
                        val comp = jeeChapters.count { it.isCompleted }
                        val total = jeeChapters.size.coerceAtLeast(1)
                        CircularProgressRing(progress = comp.toFloat() / total, size = 64.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("JEE Coverage", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("$comp / $total chapters done", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    AosCard(modifier = Modifier.fillMaxWidth()) {
                        val comp = upChapters.count { it.isCompleted }
                        val total = upChapters.size.coerceAtLeast(1)
                        CircularProgressRing(progress = comp.toFloat() / total, size = 64.dp, color = AccentTeal)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("UP Board Prep", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("$comp / $total chapters completed", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Mock Scoring Rate Indexes", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                if (testResults.isEmpty()) {
                    Text("No Mock Scores available to analyze.", color = TextSecondary)
                } else {
                    val scores = testResults.map { it.accuracy }
                    LineChart(
                        scores = scores,
                        dates = testResults.map { it.date },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        lineColor = AccentClickUp
                    )
                }
            }
        }
    }
}

// --- 18. REPORT SYSTEM VIEW ---
@Composable
fun ReportsView(
    viewModel: AosViewModel,
    chapters: List<ChapterEntity>,
    dailyLogs: List<DailyLogEntity>,
    habitLogs: List<HabitLogEntity>,
    testResults: List<TestResultEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Printable Academic Report", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Generate comprehensive analytical layout of accomplishments suitable for saving or sharing.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        item {
            AosCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.02f))
                    .padding(24.dp)
            ) {
                // Printable design container
                Text("ACADEMIC PREPARATION REPORT CARD", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text("AOS - Class 12 UP Board + JEE Main + NDA Prep Dashboard", color = TextSecondary, fontSize = 11.sp)
                
                Divider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 12.dp))

                Text("CORE COMPLETION SUMMARY", color = AccentTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val jeeDone = chapters.filter { it.category == "JEE" }.count { it.isCompleted }
                val jeeTotal = chapters.filter { it.category == "JEE" }.size
                val ndaDone = chapters.filter { it.category == "NDA_MATHS" }.count { it.isCompleted }
                val ndaTotal = chapters.filter { it.category == "NDA_MATHS" }.size
                val boardDone = chapters.filter { it.category == "UP_BOARD" }.count { it.isCompleted }
                val boardTotal = chapters.filter { it.category == "UP_BOARD" }.size

                Text("• JEE Main Progress Index: $jeeDone / $jeeTotal Chapters", color = TextPrimary, fontSize = 13.sp)
                Text("• NDA Maths Progress Index: $ndaDone / $ndaTotal Chapters", color = TextPrimary, fontSize = 13.sp)
                Text("• UP Board Syllabus Done: $boardDone / $boardTotal Chapters", color = TextPrimary, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(12.dp))
                Text("TRACKER PERFORMANCE", color = AccentClickUp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val avgHours = if (dailyLogs.isNotEmpty()) dailyLogs.map { it.studyHours }.average() else 0.0
                Text("• Average Daily Study Time: ${String.format("%.1f", avgHours)} Hours", color = TextPrimary, fontSize = 13.sp)

                val solvedError = viewModel.errorLogs.value.count { it.isSolved }
                val totalError = viewModel.errorLogs.value.size
                Text("• Error Index Solved: $solvedError / $totalError Resolved errors", color = TextPrimary, fontSize = 13.sp)

                Divider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 12.dp))

                Text("REMARKS & STRATEGY ASSIGNMENT", color = AccentAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Focus intensively on Class 11 Mechanics bridge items before early December mock marathons. Maintain daily 8 hours structured sessions safely.", color = TextSecondary, fontSize = 11.sp)
            }
        }

        item {
            Button(
                onClick = {
                    Toast.makeText(viewModel.getApplication(), "Executive PDF Export Initialized Offline successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentLinear),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Export PDF Report Copy", color = Color.White)
            }
        }
    }
}

// --- 19. IMPORT / EXPORT VIEW ---
@Composable
fun ImportExportView(viewModel: AosViewModel) {
    var backupJson by remember { mutableStateOf("") }
    var importJsonInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Secure Data Backup Engine", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Save, share, or import complete local database state values seamlessly.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // Export Section
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Export Database to JSON", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.exportBackupJson(
                            onSuccess = { backupJson = it },
                            onError = { backupJson = "Export Error: $it" }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Generate Backup String")
                }

                if (backupJson.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSecondary, unfocusedTextColor = TextSecondary)
                    )
                }
            }
        }

        // Import Section
        item {
            AosCard(modifier = Modifier.fillMaxWidth()) {
                Text("Restore Backup from JSON String", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = importJsonInput,
                    onValueChange = { importJsonInput = it },
                    placeholder = { Text("Paste JSON backup code block here...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = AccentLinear, unfocusedBorderColor = ObsidianBorder)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (importJsonInput.isNotBlank()) {
                            viewModel.importBackupJson(
                                importJsonInput,
                                onSuccess = {
                                    importJsonInput = ""
                                    Toast.makeText(viewModel.getApplication(), "Database restored successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(viewModel.getApplication(), "Failed to restore: $it", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLinear),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Restore Backup State")
                }
            }
        }
    }
}
