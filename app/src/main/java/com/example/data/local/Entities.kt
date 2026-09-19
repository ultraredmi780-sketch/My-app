package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.FlashcardItem
import com.example.model.QuestionItem
import com.example.model.TermItem

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val subscriptionStatus: String = "free" // "free" or "pro"
)

@Entity(
    tableName = "lessons",
    indices = [Index(value = ["userId"])]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val originalImagePath: String? = null,
    val extractedText: String,
    val explanation: String,
    val summary: String,
    val keyPoints: List<String>,
    val terms: List<TermItem>,
    val questions: List<QuestionItem>,
    val flashcards: List<FlashcardItem>,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quiz_attempts",
    indices = [Index(value = ["userId"]), Index(value = ["lessonId"])]
)
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val lessonId: Long,
    val lessonTitle: String,
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val durationSeconds: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val weakTopics: List<String> = emptyList()
)

@Entity(
    tableName = "usage",
    primaryKeys = ["userId", "date"]
)
data class UsageEntity(
    val userId: Long,
    val date: String, // Format: YYYY-MM-DD
    val imageAnalyses: Int = 0,
    val aiMessages: Int = 0,
    val quizzes: Int = 0
)
