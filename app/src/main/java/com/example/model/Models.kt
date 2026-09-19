package com.example.model

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis(),
    val subscriptionStatus: String = "free" // "free" or "pro"
)

data class TermItem(
    val term: String,
    val definition: String
)

data class FlashcardItem(
    val front: String,
    val back: String
)

data class QuestionItem(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val type: String = "mcq" // "mcq", "true_false", "short"
)

data class Lesson(
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

data class QuizAttempt(
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

data class DailyUsage(
    val userId: Long,
    val date: String, // YYYY-MM-DD
    val imageAnalyses: Int = 0,
    val aiMessages: Int = 0,
    val quizzes: Int = 0
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromLessonContent: Boolean = true
)

object SubscriptionLimits {
    const val FREE_DAILY_IMAGE_ANALYSES = 5
    const val FREE_DAILY_QUIZZES = 10
    const val FREE_DAILY_AI_MESSAGES = 25
    const val FREE_MAX_SAVED_LESSONS = 10

    const val PRO_DAILY_IMAGE_ANALYSES = 9999
    const val PRO_DAILY_QUIZZES = 9999
    const val PRO_DAILY_AI_MESSAGES = 9999
    const val PRO_MAX_SAVED_LESSONS = 9999
}
