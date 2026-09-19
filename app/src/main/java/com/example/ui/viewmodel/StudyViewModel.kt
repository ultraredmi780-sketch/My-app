package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.AnalysisResponse
import com.example.data.repository.LimitCheckResult
import com.example.data.repository.StudyRepository
import com.example.model.ChatMessage
import com.example.model.DailyUsage
import com.example.model.Lesson
import com.example.model.QuestionItem
import com.example.model.QuizAttempt
import com.example.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Onboarding : Screen()
    object Auth : Screen()
    object Home : Screen()
    object Review : Screen()
    object Progress : Screen()
    object Profile : Screen()
    object LessonDetail : Screen()
    object LessonChat : Screen()
    object QuizSetup : Screen()
    object ActiveQuiz : Screen()
    object QuizResult : Screen()
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    val repository = StudyRepository(application.applicationContext)

    val language: StateFlow<String> = repository.preferencesManager.language
    val theme: StateFlow<String> = repository.preferencesManager.theme
    val isOnboardingCompleted: StateFlow<Boolean> = repository.preferencesManager.isOnboardingCompleted

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _savedLessons = MutableStateFlow<List<Lesson>>(emptyList())
    val savedLessons: StateFlow<List<Lesson>> = _savedLessons.asStateFlow()

    private val _quizAttempts = MutableStateFlow<List<QuizAttempt>>(emptyList())
    val quizAttempts: StateFlow<List<QuizAttempt>> = _quizAttempts.asStateFlow()

    private val _todayUsage = MutableStateFlow<DailyUsage?>(null)
    val todayUsage: StateFlow<DailyUsage?> = _todayUsage.asStateFlow()

    private val _selectedLesson = MutableStateFlow<Lesson?>(null)
    val selectedLesson: StateFlow<Lesson?> = _selectedLesson.asStateFlow()

    // Analysis state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisStatusText = MutableStateFlow("")
    val analysisStatusText: StateFlow<String> = _analysisStatusText.asStateFlow()

    private val _analysisWarning = MutableStateFlow<String?>(null)
    val analysisWarning: StateFlow<String?> = _analysisWarning.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    // Explanation more mode result
    private val _customExplanation = MutableStateFlow<Pair<String, String>?>(null) // mode to text
    val customExplanation: StateFlow<Pair<String, String>?> = _customExplanation.asStateFlow()

    private val _isExplainingMore = MutableStateFlow(false)
    val isExplainingMore: StateFlow<Boolean> = _isExplainingMore.asStateFlow()

    // Lesson Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Active Quiz State
    private val _quizQuestions = MutableStateFlow<List<QuestionItem>>(emptyList())
    val quizQuestions: StateFlow<List<QuestionItem>> = _quizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _userSelectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val userSelectedAnswers: StateFlow<Map<Int, Int>> = _userSelectedAnswers.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _isSmartQuizMode = MutableStateFlow(false)
    val isSmartQuizMode: StateFlow<Boolean> = _isSmartQuizMode.asStateFlow()

    private val _smartQuizDifficulty = MutableStateFlow(1) // 1 to 3
    val smartQuizDifficulty: StateFlow<Int> = _smartQuizDifficulty.asStateFlow()

    private val _lastQuizResult = MutableStateFlow<QuizAttempt?>(null)
    val lastQuizResult: StateFlow<QuizAttempt?> = _lastQuizResult.asStateFlow()

    private val _quizStartTime = MutableStateFlow(0L)

    // Upgrade & Policy dialogs
    private val _showProUpgradeModal = MutableStateFlow(false)
    val showProUpgradeModal: StateFlow<Boolean> = _showProUpgradeModal.asStateFlow()

    private val _proUpgradeMessage = MutableStateFlow<String?>(null)
    val proUpgradeMessage: StateFlow<String?> = _proUpgradeMessage.asStateFlow()

    private val _showPrivacyPolicy = MutableStateFlow(false)
    val showPrivacyPolicy: StateFlow<Boolean> = _showPrivacyPolicy.asStateFlow()

    private val _showTermsOfService = MutableStateFlow(false)
    val showTermsOfService: StateFlow<Boolean> = _showTermsOfService.asStateFlow()

    // Search query for review
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadUserAndData()
    }

    private fun loadUserAndData() {
        viewModelScope.launch {
            val user = repository.getOrCreateDefaultUser()
            _currentUser.value = user

            // Route initial screen
            if (!repository.preferencesManager.isOnboardingCompleted()) {
                _currentScreen.value = Screen.Onboarding
            } else {
                _currentScreen.value = Screen.Home
            }

            // Observe lessons
            repository.getLessons(user.id).collect { list ->
                _savedLessons.value = list
            }
        }

        viewModelScope.launch {
            val user = repository.getOrCreateDefaultUser()
            repository.getQuizAttempts(user.id).collect { list ->
                _quizAttempts.value = list
            }
        }

        viewModelScope.launch {
            val user = repository.getOrCreateDefaultUser()
            repository.observeTodayUsage(user.id).collect { usage ->
                _todayUsage.value = usage
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun completeOnboarding() {
        repository.preferencesManager.setOnboardingCompleted(true)
        _currentScreen.value = Screen.Home
    }

    fun setLanguage(lang: String) {
        repository.preferencesManager.setLanguage(lang)
    }

    fun setTheme(theme: String) {
        repository.preferencesManager.setTheme(theme)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (query.isBlank()) {
                repository.getLessons(user.id).collect { _savedLessons.value = it }
            } else {
                repository.searchLessons(user.id, query).collect { _savedLessons.value = it }
            }
        }
    }

    // --- Image / Text Analysis ---

    fun setPreviewBitmap(bitmap: Bitmap?) {
        _previewBitmap.value = bitmap
    }

    fun analyzeInput(bitmap: Bitmap?, manualText: String?) {
        val user = _currentUser.value ?: return
        val isAr = language.value == "ar"

        viewModelScope.launch {
            // Check usage limit
            val limitCheck = repository.checkCanAnalyzeImage(user, isAr)
            if (limitCheck is LimitCheckResult.LimitReached) {
                _proUpgradeMessage.value = limitCheck.message
                _showProUpgradeModal.value = true
                return@launch
            }

            _isAnalyzing.value = true
            _analysisWarning.value = null
            _analysisStatusText.value = if (isAr) "جاري تحليل المحتوى بالذكاء الاصطناعي..." else "Analyzing content with AI..."

            val result = repository.analyzeLesson(bitmap, manualText, language.value)
            _isAnalyzing.value = false

            when (result) {
                is AnalysisResponse.Success -> {
                    repository.incrementImageUsage(user.id)
                    val newLesson = Lesson(
                        userId = user.id,
                        title = result.title,
                        originalImagePath = null,
                        extractedText = result.extractedText,
                        explanation = result.explanation,
                        summary = result.summary,
                        keyPoints = result.keyPoints,
                        terms = result.terms,
                        questions = result.questions,
                        flashcards = result.flashcards
                    )
                    val id = repository.saveLesson(newLesson)
                    val saved = newLesson.copy(id = id)
                    _selectedLesson.value = saved

                    // Reset chat
                    _chatMessages.value = listOf(
                        ChatMessage(
                            isUser = false,
                            text = if (isAr) "مرحباً! أنا مساعدك الذكي لدرس '${result.title}'. يمكنك سؤالي أي شيء حول هذا الدرس وسأجيبك بدقة."
                            else "Hello! I am your AI study assistant for '${result.title}'. Ask me any question about this lesson."
                        )
                    )

                    _currentScreen.value = Screen.LessonDetail
                }
                is AnalysisResponse.Warning -> {
                    _analysisWarning.value = result.message
                }
                is AnalysisResponse.Error -> {
                    _analysisWarning.value = result.message
                }
            }
        }
    }

    fun selectLesson(lesson: Lesson) {
        _selectedLesson.value = lesson
        val isAr = language.value == "ar"
        _chatMessages.value = listOf(
            ChatMessage(
                isUser = false,
                text = if (isAr) "مرحباً! أنا مساعدك الذكي لدرس '${lesson.title}'. يمكنك سؤالي أي شيء حول هذا الدرس وسأجيبك بدقة."
                else "Hello! I am your AI study assistant for '${lesson.title}'. Ask me any question about this lesson."
            )
        )
        _customExplanation.value = null
        _currentScreen.value = Screen.LessonDetail
    }

    fun deleteLesson(lessonId: Long) {
        viewModelScope.launch {
            repository.deleteLesson(lessonId)
            if (_selectedLesson.value?.id == lessonId) {
                _selectedLesson.value = null
                _currentScreen.value = Screen.Home
            }
        }
    }

    // --- Explain More Mode ---

    fun requestExplanationMode(mode: String) {
        val lesson = _selectedLesson.value ?: return
        viewModelScope.launch {
            _isExplainingMore.value = true
            val response = repository.explainMore(lesson, mode, language.value)
            _customExplanation.value = Pair(mode, response.explanation)
            _isExplainingMore.value = false
        }
    }

    fun clearCustomExplanation() {
        _customExplanation.value = null
    }

    // --- Chat With Lesson ---

    fun sendChatMessage(question: String) {
        if (question.isBlank()) return
        val lesson = _selectedLesson.value ?: return
        val user = _currentUser.value ?: return
        val isAr = language.value == "ar"

        viewModelScope.launch {
            val limitCheck = repository.checkCanSendAiMessage(user, isAr)
            if (limitCheck is LimitCheckResult.LimitReached) {
                _proUpgradeMessage.value = limitCheck.message
                _showProUpgradeModal.value = true
                return@launch
            }

            // Append user message
            val userMsg = ChatMessage(isUser = true, text = question.trim())
            _chatMessages.value = _chatMessages.value + userMsg
            _isChatLoading.value = true

            val response = repository.chatWithLesson(lesson, question, language.value)
            repository.incrementMessageUsage(user.id)
            _isChatLoading.value = false

            val aiMsg = ChatMessage(
                isUser = false,
                text = response.answer,
                isFromLessonContent = response.isFromLessonContent
            )
            _chatMessages.value = _chatMessages.value + aiMsg
        }
    }

    // --- Quizzes ---

    fun prepareQuiz(
        lesson: Lesson?,
        questionCount: Int = 5,
        types: List<String> = listOf("mcq", "true_false"),
        isSmartMode: Boolean = false
    ) {
        val targetLesson = lesson ?: _selectedLesson.value ?: _savedLessons.value.firstOrNull() ?: return
        val user = _currentUser.value ?: return
        val isAr = language.value == "ar"

        viewModelScope.launch {
            val limitCheck = repository.checkCanDoQuiz(user, isAr)
            if (limitCheck is LimitCheckResult.LimitReached) {
                _proUpgradeMessage.value = limitCheck.message
                _showProUpgradeModal.value = true
                return@launch
            }

            _selectedLesson.value = targetLesson
            _isSmartQuizMode.value = isSmartMode
            _smartQuizDifficulty.value = 1
            _currentQuestionIndex.value = 0
            _userSelectedAnswers.value = emptyMap()
            _isAnswerSubmitted.value = false
            _quizStartTime.value = System.currentTimeMillis()

            val questions = repository.generateCustomQuiz(targetLesson, questionCount, types, language.value)
            _quizQuestions.value = questions
            _currentScreen.value = Screen.ActiveQuiz
        }
    }

    fun selectAnswer(answerIndex: Int) {
        if (_isAnswerSubmitted.value) return
        val currentIdx = _currentQuestionIndex.value
        _userSelectedAnswers.value = _userSelectedAnswers.value + (currentIdx to answerIndex)
    }

    fun submitCurrentAnswer() {
        _isAnswerSubmitted.value = true

        // If smart quiz mode, adapt difficulty
        if (_isSmartQuizMode.value) {
            val currentIdx = _currentQuestionIndex.value
            val q = _quizQuestions.value.getOrNull(currentIdx)
            val selected = _userSelectedAnswers.value[currentIdx]
            if (q != null && selected != null) {
                if (selected == q.correctAnswerIndex) {
                    _smartQuizDifficulty.value = (_smartQuizDifficulty.value + 1).coerceAtMost(3)
                } else {
                    _smartQuizDifficulty.value = (_smartQuizDifficulty.value - 1).coerceAtLeast(1)
                }
            }
        }
    }

    fun nextQuestion() {
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < _quizQuestions.value.size) {
            _currentQuestionIndex.value = nextIdx
            _isAnswerSubmitted.value = false
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        val questions = _quizQuestions.value
        val answers = _userSelectedAnswers.value
        val lesson = _selectedLesson.value ?: return
        val user = _currentUser.value ?: return

        var correct = 0
        val weakList = mutableListOf<String>()

        questions.forEachIndexed { idx, q ->
            val userAns = answers[idx]
            if (userAns == q.correctAnswerIndex) {
                correct++
            } else {
                weakList.add(q.question.take(40))
            }
        }

        val total = questions.size.coerceAtLeast(1)
        val score = ((correct.toFloat() / total) * 100).toInt()
        val duration = (((System.currentTimeMillis() - _quizStartTime.value) / 1000)).toInt().coerceAtLeast(5)

        val attempt = QuizAttempt(
            userId = user.id,
            lessonId = lesson.id,
            lessonTitle = lesson.title,
            score = score,
            correctAnswers = correct,
            totalQuestions = total,
            durationSeconds = duration,
            weakTopics = weakList
        )

        viewModelScope.launch {
            repository.saveQuizAttempt(attempt)
            repository.incrementQuizUsage(user.id)
            _lastQuizResult.value = attempt
            _currentScreen.value = Screen.QuizResult
        }
    }

    fun retestWeakTopics() {
        val last = _lastQuizResult.value ?: return
        val lesson = _savedLessons.value.find { it.id == last.lessonId } ?: _selectedLesson.value
        prepareQuiz(lesson, questionCount = 5, isSmartMode = true)
    }

    // --- Subscription & Upgrade ---

    fun openProModal(message: String? = null) {
        _proUpgradeMessage.value = message
        _showProUpgradeModal.value = true
    }

    fun dismissProModal() {
        _showProUpgradeModal.value = false
        _proUpgradeMessage.value = null
    }

    fun upgradeToPro() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUserSubscription(user.id, isPro = true)
            _currentUser.value = user.copy(subscriptionStatus = "pro")
            _showProUpgradeModal.value = false
        }
    }

    fun downgradeToFree() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUserSubscription(user.id, isPro = false)
            _currentUser.value = user.copy(subscriptionStatus = "free")
        }
    }

    // --- Policy Dialogs ---

    fun setShowPrivacyPolicy(show: Boolean) {
        _showPrivacyPolicy.value = show
    }

    fun setShowTermsOfService(show: Boolean) {
        _showTermsOfService.value = show
    }

    // --- Auth ---

    fun login(email: String) {
        viewModelScope.launch {
            val result = repository.loginUser(email)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentScreen.value = Screen.Home
            }
        }
    }

    fun register(name: String, email: String) {
        viewModelScope.launch {
            val result = repository.registerUser(name, email)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentScreen.value = Screen.Home
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        repository.preferencesManager.setCurrentUserId(-1L)
        _currentScreen.value = Screen.Auth
    }
}
