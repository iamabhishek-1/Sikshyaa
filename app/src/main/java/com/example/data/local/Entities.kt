package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Abhishek Kushwaha",
    val studentClass: String = "+2 Science",
    val schoolCollege: String = "Uniglobe College",
    val preferredLanguage: String = "English + Nepali",
    val targetExams: String = "NEB Exams, MBBS Entrance",
    val streak: Int = 3,
    val totalXp: Int = 450,
    val studyTimeMinutes: Int = 120,
    val completedLessons: Int = 5,
    val completedQuestions: Int = 24,
    val accuracyRate: Float = 0.82f,
    val rankEstimation: String = "Topper Tier"
)

@Entity(tableName = "library_items")
data class LibraryItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // Notes, Books, Old Questions, Formula Sheets
    val subject: String,
    val numberOfPages: Int = 15,
    val author: String = "Sikshyaa Academic Board",
    val isDownloaded: Boolean = false,
    val isBookmarked: Boolean = false,
    val lastReadPage: Int = 0,
    val localNotes: String = "",
    val highlightsJson: String = ""
)

@Entity(tableName = "course_progress")
data class CourseProgressEntity(
    @PrimaryKey val courseId: String,
    val category: String, // Class 8, SEE, +2 Science, Loksewa, MBBS, etc.
    val subject: String,
    val title: String,
    val videosCompleted: Int = 0,
    val totalVideos: Int = 8,
    val notesRead: Int = 0,
    val totalNotes: Int = 4,
    val completedPercent: Int = 0
)

@Entity(tableName = "mock_exam_results")
data class MockExamResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examName: String,
    val category: String,
    val score: Int,
    val totalQuestions: Int,
    val accuracy: Float,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "Regular" // Regular, Exam, Viva, Revision
)
