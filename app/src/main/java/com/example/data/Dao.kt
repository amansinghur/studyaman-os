package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AosDao {

    // --- Chapters ---
    @Query("SELECT * FROM chapters ORDER BY id ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    // --- Daily Logs ---
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllDailyLogs(): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getDailyLogForDate(date: String): DailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(log: DailyLogEntity)

    // --- Habit Logs ---
    @Query("SELECT * FROM habit_logs ORDER BY date DESC, habitName ASC")
    fun getAllHabitLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getHabitsForDateFlow(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getHabitsForDate(date: String): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLogs(logs: List<HabitLogEntity>)

    @Update
    suspend fun updateHabitLog(log: HabitLogEntity)

    // --- Test Results ---
    @Query("SELECT * FROM test_results ORDER BY date DESC")
    fun getAllTestResults(): Flow<List<TestResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(result: TestResultEntity)

    @Delete
    suspend fun deleteTestResult(result: TestResultEntity)

    // --- PYQ Entries ---
    @Query("SELECT * FROM pyq_entries ORDER BY id DESC")
    fun getAllPYQEntries(): Flow<List<PYQEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPYQEntry(entry: PYQEntryEntity)

    @Update
    suspend fun updatePYQEntry(entry: PYQEntryEntity)

    // --- Error Logs ---
    @Query("SELECT * FROM error_logs ORDER BY id DESC")
    fun getAllErrorLogs(): Flow<List<ErrorLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertErrorLog(log: ErrorLogEntity)

    @Update
    suspend fun updateErrorLog(log: ErrorLogEntity)

    @Delete
    suspend fun deleteErrorLog(log: ErrorLogEntity)

    // --- Formulas ---
    @Query("SELECT * FROM formulas ORDER BY subject ASC, formula ASC")
    fun getAllFormulas(): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas WHERE formula LIKE '%' || :query || '%' OR explanation LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchFormulas(query: String): Flow<List<FormulaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: FormulaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormulas(formulas: List<FormulaEntity>)

    @Delete
    suspend fun deleteFormula(formula: FormulaEntity)

    // --- Current Affairs ---
    @Query("SELECT * FROM current_affairs ORDER BY date DESC")
    fun getAllCurrentAffairs(): Flow<List<CurrentAffairsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentAffairs(entry: CurrentAffairsEntity)

    @Delete
    suspend fun deleteCurrentAffairs(entry: CurrentAffairsEntity)

    // --- Revision Tasks ---
    @Query("SELECT * FROM revision_tasks ORDER BY dueDate ASC")
    fun getAllRevisionTasks(): Flow<List<RevisionTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisionTask(task: RevisionTaskEntity)

    @Update
    suspend fun updateRevisionTask(task: RevisionTaskEntity)

    @Delete
    suspend fun deleteRevisionTask(task: RevisionTaskEntity)

    // --- Weekly Milestones ---
    @Query("SELECT * FROM weekly_milestones ORDER BY weekStartDate DESC")
    fun getAllWeeklyMilestones(): Flow<List<WeeklyMilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyMilestone(milestone: WeeklyMilestoneEntity)

    @Update
    suspend fun updateWeeklyMilestone(milestone: WeeklyMilestoneEntity)
}
