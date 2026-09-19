package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.LessonDao
import com.example.data.local.LessonEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.QuizAttemptDao
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyDatabase
import com.example.data.local.UsageDao
import com.example.data.local.UsageEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.remote.AiStudyService
import com.example.data.remote.AnalysisResponse
import com.example.data.remote.ChatResponse
import com.example.data.remote.ExplanationResponse
import com.example.model.DailyUsage
import com.example.model.Lesson
import com.example.model.QuestionItem
import com.example.model.QuizAttempt
import com.example.model.SubscriptionLimits
import com.example.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class LimitCheckResult {
    object Allowed : LimitCheckResult()
    data class LimitReached(val limitType: String, val message: String) : LimitCheckResult()
}

class StudyRepository(
    private val context: Context,
    private val database: StudyDatabase = StudyDatabase.getInstance(context),
    private val userDao: UserDao = database.userDao(),
    private val lessonDao: LessonDao = database.lessonDao(),
    private val quizDao: QuizAttemptDao = database.quizAttemptDao(),
    private val usageDao: UsageDao = database.usageDao(),
    val preferencesManager: PreferencesManager = PreferencesManager(context),
    val aiService: AiStudyService = AiStudyService()
) {

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    // --- User Session ---

    suspend fun getOrCreateDefaultUser(): User {
        val savedUserId = preferencesManager.getCurrentUserId()
        if (savedUserId > 0) {
            val user = userDao.getUserById(savedUserId).firstOrNull()
            if (user != null) return user.toDomain()
        }

        val first = userDao.getFirstUser()
        if (first != null) {
            preferencesManager.setCurrentUserId(first.id)
            return first.toDomain()
        }

        // Create initial default student user
        val defaultEntity = UserEntity(
            name = "طالب StudyAI",
            email = "student@studyai.app",
            subscriptionStatus = "free"
        )
        val id = userDao.insertUser(defaultEntity)
        preferencesManager.setCurrentUserId(id)
        return defaultEntity.copy(id = id).toDomain()
    }

    suspend fun registerUser(name: String, email: String): Result<User> {
        val existing = userDao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return Result.failure(Exception("البريد الإلكتروني مسجل مسبقاً"))
        }
        val entity = UserEntity(
            name = name.trim(),
            email = email.trim().lowercase(),
            subscriptionStatus = "free"
        )
        val id = userDao.insertUser(entity)
        preferencesManager.setCurrentUserId(id)
        return Result.success(entity.copy(id = id).toDomain())
    }

    suspend fun loginUser(email: String): Result<User> {
        val existing = userDao.getUserByEmail(email.trim().lowercase())
        return if (existing != null) {
            preferencesManager.setCurrentUserId(existing.id)
            Result.success(existing.toDomain())
        } else {
            // Auto register student seamlessly
            val entity = UserEntity(
                name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email.trim().lowercase(),
                subscriptionStatus = "free"
            )
            val id = userDao.insertUser(entity)
            preferencesManager.setCurrentUserId(id)
            Result.success(entity.copy(id = id).toDomain())
        }
    }

    suspend fun updateUserSubscription(userId: Long, isPro: Boolean) {
        userDao.updateSubscription(userId, if (isPro) "pro" else "free")
    }

    fun observeUser(userId: Long): Flow<User?> {
        return userDao.getUserById(userId).map { it?.toDomain() }
    }

    // --- Usage Limits ---

    suspend fun checkCanAnalyzeImage(user: User, isAr: Boolean): LimitCheckResult {
        if (user.subscriptionStatus == "pro") return LimitCheckResult.Allowed
        val usage = usageDao.getUsage(user.id, getTodayDateString())
        val count = usage?.imageAnalyses ?: 0
        return if (count >= SubscriptionLimits.FREE_DAILY_IMAGE_ANALYSES) {
            LimitCheckResult.LimitReached(
                "image",
                if (isAr) "لقد استنفدت حد تحليلات الصور اليومية المجانية (${SubscriptionLimits.FREE_DAILY_IMAGE_ANALYSES}/يوم). قم بالترقية إلى Pro لمتابعة الدراسة بلا حدود!"
                else "You have reached your daily free image analysis limit (${SubscriptionLimits.FREE_DAILY_IMAGE_ANALYSES}/day). Upgrade to Pro for unlimited study!"
            )
        } else {
            LimitCheckResult.Allowed
        }
    }

    suspend fun checkCanDoQuiz(user: User, isAr: Boolean): LimitCheckResult {
        if (user.subscriptionStatus == "pro") return LimitCheckResult.Allowed
        val usage = usageDao.getUsage(user.id, getTodayDateString())
        val count = usage?.quizzes ?: 0
        return if (count >= SubscriptionLimits.FREE_DAILY_QUIZZES) {
            LimitCheckResult.LimitReached(
                "quiz",
                if (isAr) "لقد أكملت الحد الأقصى للاختبارات اليومية المجانية (${SubscriptionLimits.FREE_DAILY_QUIZZES}/يوم). قم بالترقية إلى Pro لاختبارات غير محدودة!"
                else "Daily free quiz limit reached (${SubscriptionLimits.FREE_DAILY_QUIZZES}/day). Upgrade to Pro for unlimited quizzes!"
            )
        } else {
            LimitCheckResult.Allowed
        }
    }

    suspend fun checkCanSendAiMessage(user: User, isAr: Boolean): LimitCheckResult {
        if (user.subscriptionStatus == "pro") return LimitCheckResult.Allowed
        val usage = usageDao.getUsage(user.id, getTodayDateString())
        val count = usage?.aiMessages ?: 0
        return if (count >= SubscriptionLimits.FREE_DAILY_AI_MESSAGES) {
            LimitCheckResult.LimitReached(
                "message",
                if (isAr) "وصلت للحد الأقصى للرسائل اليومية المجانية (${SubscriptionLimits.FREE_DAILY_AI_MESSAGES}/يوم). احصل على اشتراك Pro للاستفادة الكاملة!"
                else "Daily free AI tutor message limit reached (${SubscriptionLimits.FREE_DAILY_AI_MESSAGES}/day). Upgrade to Pro to continue!"
            )
        } else {
            LimitCheckResult.Allowed
        }
    }

    suspend fun incrementImageUsage(userId: Long) {
        val today = getTodayDateString()
        val current = usageDao.getUsage(userId, today) ?: UsageEntity(userId = userId, date = today)
        usageDao.insertOrUpdate(current.copy(imageAnalyses = current.imageAnalyses + 1))
    }

    suspend fun incrementQuizUsage(userId: Long) {
        val today = getTodayDateString()
        val current = usageDao.getUsage(userId, today) ?: UsageEntity(userId = userId, date = today)
        usageDao.insertOrUpdate(current.copy(quizzes = current.quizzes + 1))
    }

    suspend fun incrementMessageUsage(userId: Long) {
        val today = getTodayDateString()
        val current = usageDao.getUsage(userId, today) ?: UsageEntity(userId = userId, date = today)
        usageDao.insertOrUpdate(current.copy(aiMessages = current.aiMessages + 1))
    }

    fun observeTodayUsage(userId: Long): Flow<DailyUsage?> {
        return usageDao.observeUsage(userId, getTodayDateString()).map {
            it?.let { DailyUsage(it.userId, it.date, it.imageAnalyses, it.aiMessages, it.quizzes) }
        }
    }

    // --- Lessons ---

    fun getLessons(userId: Long): Flow<List<Lesson>> {
        return lessonDao.getLessonsForUser(userId).map { list -> list.map { it.toDomain() } }
    }

    fun searchLessons(userId: Long, query: String): Flow<List<Lesson>> {
        return lessonDao.searchLessons(userId, query).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getLesson(lessonId: Long): Lesson? {
        return lessonDao.getLessonById(lessonId)?.toDomain()
    }

    suspend fun saveLesson(lesson: Lesson): Long {
        return lessonDao.insertLesson(lesson.toEntity())
    }

    suspend fun deleteLesson(lessonId: Long) {
        lessonDao.deleteLesson(lessonId)
    }

    // --- Quizzes ---

    fun getQuizAttempts(userId: Long): Flow<List<QuizAttempt>> {
        return quizDao.getAttemptsForUser(userId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun saveQuizAttempt(attempt: QuizAttempt): Long {
        return quizDao.insertAttempt(attempt.toEntity())
    }

    fun getQuizCount(userId: Long): Flow<Int> = quizDao.getQuizCount(userId)

    fun getAverageScore(userId: Long): Flow<Double?> = quizDao.getAverageScore(userId)

    fun getLessonCount(userId: Long): Flow<Int> = lessonDao.getLessonCount(userId)

    // --- AI Integration Wrapper ---

    suspend fun analyzeLesson(
        bitmap: Bitmap?,
        manualText: String?,
        language: String
    ): AnalysisResponse {
        return aiService.analyzeLesson(bitmap, manualText, language)
    }

    suspend fun chatWithLesson(
        lesson: Lesson,
        userQuestion: String,
        language: String
    ): ChatResponse {
        return aiService.chatWithLesson(lesson, userQuestion, language)
    }

    suspend fun explainMore(
        lesson: Lesson,
        mode: String,
        language: String
    ): ExplanationResponse {
        return aiService.explainMore(lesson, mode, language)
    }

    suspend fun generateCustomQuiz(
        lesson: Lesson,
        count: Int,
        types: List<String>,
        language: String
    ): List<QuestionItem> {
        return aiService.generateCustomQuiz(lesson, count, types, language)
    }
}

// --- Mappers ---

fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
    subscriptionStatus = subscriptionStatus
)

fun LessonEntity.toDomain() = Lesson(
    id = id,
    userId = userId,
    title = title,
    originalImagePath = originalImagePath,
    extractedText = extractedText,
    explanation = explanation,
    summary = summary,
    keyPoints = keyPoints,
    terms = terms,
    questions = questions,
    flashcards = flashcards,
    createdAt = createdAt
)

fun Lesson.toEntity() = LessonEntity(
    id = id,
    userId = userId,
    title = title,
    originalImagePath = originalImagePath,
    extractedText = extractedText,
    explanation = explanation,
    summary = summary,
    keyPoints = keyPoints,
    terms = terms,
    questions = questions,
    flashcards = flashcards,
    createdAt = createdAt
)

fun QuizAttemptEntity.toDomain() = QuizAttempt(
    id = id,
    userId = userId,
    lessonId = lessonId,
    lessonTitle = lessonTitle,
    score = score,
    correctAnswers = correctAnswers,
    totalQuestions = totalQuestions,
    durationSeconds = durationSeconds,
    createdAt = createdAt,
    weakTopics = weakTopics
)

fun QuizAttempt.toEntity() = QuizAttemptEntity(
    id = id,
    userId = userId,
    lessonId = lessonId,
    lessonTitle = lessonTitle,
    score = score,
    correctAnswers = correctAnswers,
    totalQuestions = totalQuestions,
    durationSeconds = durationSeconds,
    createdAt = createdAt,
    weakTopics = weakTopics
)
