package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class AosScreen {
    object TodaysPlan : AosScreen()
    object Timetable : AosScreen()
    object Roadmap : AosScreen()
    object BridgeProgram : AosScreen()
    object NdaModule : AosScreen()
    object JeeModule : AosScreen()
    object UpBoardModule : AosScreen()
    object WeeklyMilestones : AosScreen()
    object RevisionEngine : AosScreen()
    object DailyTracker : AosScreen()
    object HabitTracker : AosScreen()
    object PyqTracker : AosScreen()
    object MockTests : AosScreen()
    object ErrorLog : AosScreen()
    object FormulaBank : AosScreen()
    object CurrentAffairs : AosScreen()
    object Analytics : AosScreen()
    object Reports : AosScreen()
    object ImportExport : AosScreen()
}

data class BackupData(
    val chapters: List<ChapterEntity> = emptyList(),
    val dailyLogs: List<DailyLogEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
    val testResults: List<TestResultEntity> = emptyList(),
    val pyqEntries: List<PYQEntryEntity> = emptyList(),
    val errorLogs: List<ErrorLogEntity> = emptyList(),
    val formulas: List<FormulaEntity> = emptyList(),
    val currentAffairs: List<CurrentAffairsEntity> = emptyList(),
    val revisionTasks: List<RevisionTaskEntity> = emptyList(),
    val weeklyMilestones: List<WeeklyMilestoneEntity> = emptyList()
)

class AosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AosRepository(db.aosDao())

    // --- Core Navigation State ---
    private val _currentScreen = MutableStateFlow<AosScreen>(AosScreen.TodaysPlan)
    val currentScreen: StateFlow<AosScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AosScreen) {
        _currentScreen.value = screen
    }

    // --- Form/Interaction States ---
    val formulaSearchQuery = MutableStateFlow("")
    val currentAffairsReviewFilter = MutableStateFlow("Daily") // Daily, Weekly, Monthly

    // --- Reactive Data Flows ---
    val chapters: StateFlow<List<ChapterEntity>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyLogs: StateFlow<List<DailyLogEntity>> = repository.allDailyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitLogs: StateFlow<List<HabitLogEntity>> = repository.allHabitLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val testResults: StateFlow<List<TestResultEntity>> = repository.allTestResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pyqEntries: StateFlow<List<PYQEntryEntity>> = repository.allPYQEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val errorLogs: StateFlow<List<ErrorLogEntity>> = repository.allErrorLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFormulas: StateFlow<List<FormulaEntity>> = formulaSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allFormulas
            } else {
                repository.searchFormulas(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentAffairs: StateFlow<List<CurrentAffairsEntity>> = repository.allCurrentAffairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val revisionTasks: StateFlow<List<RevisionTaskEntity>> = repository.allRevisionTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyMilestones: StateFlow<List<WeeklyMilestoneEntity>> = repository.allWeeklyMilestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Derived State: Today's Plan Tasks ---
    val currentDate: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val displayDate: String
        get() = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())

    init {
        viewModelScope.launch {
            // Seed database with amazing dummy & template data
            repository.seedDefaultDataIfNeeded()
            ensureTodayHabitLogsAndDailyLog()
        }
    }

    private suspend fun ensureTodayHabitLogsAndDailyLog() {
        val todayStr = currentDate
        // Ensure daily log exists
        val existingLog = repository.getDailyLogForDate(todayStr)
        if (existingLog == null) {
            repository.insertDailyLog(DailyLogEntity(
                date = todayStr,
                studyHours = 0f,
                attendance = true,
                dppDone = false,
                revisionDone = false
            ))
        }

        // Ensure default habits exist for today
        val existingHabitLogs = repository.getHabitsForDate(todayStr).first()
        if (existingHabitLogs.isEmpty()) {
            val habits = listOf("Wake Up Early", "Swimming", "Study Hours Completed", "Revision", "Current Affairs")
            val list = habits.map { HabitLogEntity(date = todayStr, habitName = it, isCompleted = false) }
            db.aosDao().insertHabitLogs(list)
        }
    }

    // --- Mutations ---

    // Chapter completion toggles
    fun toggleChapterCompleted(chapter: ChapterEntity) {
        viewModelScope.launch {
            val updated = chapter.copy(isCompleted = !chapter.isCompleted)
            repository.updateChapter(updated)

            // Trigger Revision tasks automatically if chapter is marked complete!
            if (updated.isCompleted) {
                scheduleSpacedRepetition(updated)
            }
        }
    }

    fun updateChapterPriority(chapter: ChapterEntity, priority: String) {
        viewModelScope.launch {
            repository.updateChapter(chapter.copy(priority = priority))
        }
    }

    fun updateChapterPractical(chapter: ChapterEntity, practical: Boolean) {
        viewModelScope.launch {
            repository.updateChapter(chapter.copy(isPracticalCompleted = practical))
        }
    }

    fun updateChapterWeak(chapter: ChapterEntity, weak: Boolean) {
        viewModelScope.launch {
            repository.updateChapter(chapter.copy(isWeak = weak))
        }
    }

    fun updateChapterPyqsAndAccuracy(chapter: ChapterEntity, pyqs: Int, accuracy: Float) {
        viewModelScope.launch {
            repository.updateChapter(chapter.copy(pyqsSolved = pyqs, accuracy = accuracy))
        }
    }

    // Spaced Repetition Logic (Automatic Day 1, 7, 21, 45 Scheduling)
    private suspend fun scheduleSpacedRepetition(chapter: ChapterEntity) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val stages = listOf(1, 7, 21, 45)
        for (days in stages) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, days)
            val dueDateStr = sdf.format(cal.time)
            repository.insertRevisionTask(RevisionTaskEntity(
                chapterName = chapter.chapterName,
                subject = chapter.subject,
                category = chapter.category,
                dueDate = dueDateStr,
                stage = days,
                isCompleted = false
            ))
        }
    }

    fun toggleRevisionTaskCompleted(task: RevisionTaskEntity) {
        viewModelScope.launch {
            repository.updateRevisionTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    // Daily Logs
    fun updateDailyLog(hours: Float, dppDone: Boolean, revisionDone: Boolean) {
        viewModelScope.launch {
            val log = DailyLogEntity(
                date = currentDate,
                studyHours = hours,
                attendance = true,
                dppDone = dppDone,
                revisionDone = revisionDone
            )
            repository.insertDailyLog(log)
        }
    }

    // Habits
    fun toggleHabitCompleted(habit: HabitLogEntity) {
        viewModelScope.launch {
            repository.updateHabitLog(habit.copy(isCompleted = !habit.isCompleted))
        }
    }

    // Mock Tests
    fun addMockTest(examType: String, testName: String, score: Int, maxScore: Int, accuracy: Float, remarks: String) {
        viewModelScope.launch {
            repository.insertTestResult(TestResultEntity(
                date = currentDate,
                examType = examType,
                testName = testName,
                score = score,
                maxScore = maxScore,
                accuracy = accuracy,
                remarks = remarks
            ))
        }
    }

    fun deleteMockTest(test: TestResultEntity) {
        viewModelScope.launch {
            repository.deleteTestResult(test)
        }
    }

    // Error Log
    fun addErrorLog(subject: String, chapter: String, errorType: String, description: String) {
        viewModelScope.launch {
            repository.insertErrorLog(ErrorLogEntity(
                subject = subject,
                chapter = chapter,
                errorType = errorType,
                description = description,
                isSolved = false
            ))
        }
    }

    fun toggleErrorSolved(log: ErrorLogEntity) {
        viewModelScope.launch {
            repository.updateErrorLog(log.copy(isSolved = !log.isSolved))
        }
    }

    fun deleteErrorLog(log: ErrorLogEntity) {
        viewModelScope.launch {
            repository.deleteErrorLog(log)
        }
    }

    // Formulas
    fun addFormula(subject: String, formulaText: String, explanation: String, tags: String) {
        viewModelScope.launch {
            repository.insertFormula(FormulaEntity(
                subject = subject,
                formula = formulaText,
                explanation = explanation,
                tags = tags
            ))
        }
    }

    fun deleteFormula(formula: FormulaEntity) {
        viewModelScope.launch {
            repository.deleteFormula(formula)
        }
    }

    // Current Affairs
    fun addCurrentAffairs(topic: String, category: String, notes: String) {
        viewModelScope.launch {
            repository.insertCurrentAffairs(CurrentAffairsEntity(
                date = currentDate,
                topic = topic,
                category = category,
                notes = notes
            ))
        }
    }

    fun deleteCurrentAffairs(entry: CurrentAffairsEntity) {
        viewModelScope.launch {
            repository.deleteCurrentAffairs(entry)
        }
    }

    // Weekly Milestones
    fun saveWeeklyMilestone(
        weekStart: String,
        phy: String,
        chem: String,
        math: String,
        mock: String,
        rev: String,
        completed: Boolean
    ) {
        viewModelScope.launch {
            // Find existing if possible or insert new
            val existing = weeklyMilestones.value.find { it.weekStartDate == weekStart }
            val mile = WeeklyMilestoneEntity(
                id = existing?.id ?: 0,
                weekStartDate = weekStart,
                physicsFocus = phy,
                chemistryFocus = chem,
                mathsFocus = math,
                mockTests = mock,
                revision = rev,
                isCompleted = completed
            )
            repository.insertWeeklyMilestone(mile)
        }
    }

    fun carryForwardBacklog(backlogMilestone: WeeklyMilestoneEntity, targetWeekStartDate: String) {
        viewModelScope.launch {
            // Automatically carry forward details into a new target week
            val existingTarget = weeklyMilestones.value.find { it.weekStartDate == targetWeekStartDate }
            val newPhy = if (backlogMilestone.isCompleted) "" else backlogMilestone.physicsFocus
            val newChem = if (backlogMilestone.isCompleted) "" else backlogMilestone.chemistryFocus
            val newMath = if (backlogMilestone.isCompleted) "" else backlogMilestone.mathsFocus
            
            val targetMile = WeeklyMilestoneEntity(
                id = existingTarget?.id ?: 0,
                weekStartDate = targetWeekStartDate,
                physicsFocus = (existingTarget?.physicsFocus ?: "") + (if (newPhy.isNotEmpty()) "\n[CARRYOVER] $newPhy" else ""),
                chemistryFocus = (existingTarget?.chemistryFocus ?: "") + (if (newChem.isNotEmpty()) "\n[CARRYOVER] $newChem" else ""),
                mathsFocus = (existingTarget?.mathsFocus ?: "") + (if (newMath.isNotEmpty()) "\n[CARRYOVER] $newMath" else ""),
                mockTests = existingTarget?.mockTests ?: "",
                revision = existingTarget?.revision ?: "",
                isCompleted = false
            )
            repository.insertWeeklyMilestone(targetMile)
        }
    }

    // --- Import / Export Implementation ---
    fun exportBackupJson(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val backup = BackupData(
                    chapters = repository.getAllChaptersDirect(),
                    dailyLogs = repository.getAllDailyLogsDirect(),
                    habitLogs = repository.getAllHabitLogsDirect(),
                    testResults = repository.getAllTestResultsDirect(),
                    pyqEntries = repository.getAllPYQEntriesDirect(),
                    errorLogs = repository.getAllErrorLogsDirect(),
                    formulas = repository.getAllFormulasDirect(),
                    currentAffairs = repository.getAllCurrentAffairsDirect(),
                    revisionTasks = repository.getAllRevisionTasksDirect(),
                    weeklyMilestones = repository.getAllWeeklyMilestonesDirect()
                )
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter(BackupData::class.java).indent("  ")
                val jsonStr = adapter.toJson(backup)
                onSuccess(jsonStr)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown serialization error")
            }
        }
    }

    fun importBackupJson(jsonStr: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter(BackupData::class.java)
                val backup = adapter.fromJson(jsonStr)
                if (backup != null) {
                    repository.restoreBackup(
                        chapters = backup.chapters,
                        dailyLogs = backup.dailyLogs,
                        habitLogs = backup.habitLogs,
                        testResults = backup.testResults,
                        pyqEntries = backup.pyqEntries,
                        errorLogs = backup.errorLogs,
                        formulas = backup.formulas,
                        currentAffairs = backup.currentAffairs,
                        revisionTasks = backup.revisionTasks,
                        weeklyMilestones = backup.weeklyMilestones
                    )
                    onSuccess()
                } else {
                    onError("Parsed backup data is null.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to parse import JSON")
            }
        }
    }
}
