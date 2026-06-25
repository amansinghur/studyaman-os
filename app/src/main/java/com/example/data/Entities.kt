package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,        // Physics, Chemistry, Mathematics, English, GAT
    val category: String,       // JEE, UP_BOARD, BRIDGE_CLASS_11, NDA_MATHS, NDA_GAT
    val chapterName: String,
    val isCompleted: Boolean = false,
    val priority: String = "B", // A, B, C, D
    val pyqsSolved: Int = 0,
    val accuracy: Float = 0f,
    val isPracticalCompleted: Boolean = false, // UP Board specific
    val isWeak: Boolean = false // JEE specific
)

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val studyHours: Float,
    val attendance: Boolean = true,
    val dppDone: Boolean = false,
    val revisionDone: Boolean = false
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val habitName: String, // Wake Up Early, Swimming, Study Hours Completed, Revision, Current Affairs
    val isCompleted: Boolean = false
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val examType: String, // NDA, JEE, BOARD
    val testName: String,
    val score: Int,
    val maxScore: Int,
    val accuracy: Float,
    val remarks: String
)

@Entity(tableName = "pyq_entries")
data class PYQEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examType: String, // NDA, JEE, BOARD
    val subject: String,
    val chapterName: String,
    val attempted: Int,
    val remaining: Int,
    val accuracy: Float
)

@Entity(tableName = "error_logs")
data class ErrorLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val chapter: String,
    val errorType: String, // Concept, Formula, Calculation, Careless
    val description: String,
    val isSolved: Boolean = false
)

@Entity(tableName = "formulas")
data class FormulaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String, // Physics, Chemistry, Mathematics
    val formula: String,
    val explanation: String,
    val tags: String // Comma separated tags
)

@Entity(tableName = "current_affairs")
data class CurrentAffairsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val topic: String,
    val category: String, // Defense, National, International, Sports, Science
    val notes: String
)

@Entity(tableName = "revision_tasks")
data class RevisionTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chapterName: String,
    val subject: String,
    val category: String, // JEE, UP_BOARD, etc.
    val dueDate: String, // YYYY-MM-DD
    val stage: Int, // 1 (Day 1), 7 (Day 7), 21 (Day 21), 45 (Day 45)
    val isCompleted: Boolean = false
)

@Entity(tableName = "weekly_milestones")
data class WeeklyMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weekStartDate: String, // YYYY-MM-DD (Monday of the week)
    val physicsFocus: String = "",
    val chemistryFocus: String = "",
    val mathsFocus: String = "",
    val mockTests: String = "",
    val revision: String = "",
    val isCompleted: Boolean = false
)
