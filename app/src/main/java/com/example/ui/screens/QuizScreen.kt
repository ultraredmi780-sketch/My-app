package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lesson
import com.example.ui.i18n.Strings
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ProGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun QuizSetupScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val isAr = lang == "ar"
    val savedLessons by viewModel.savedLessons.collectAsState()
    val selectedLesson by viewModel.selectedLesson.collectAsState()

    var questionCount by remember { mutableStateOf(5) }
    var allowMcq by remember { mutableStateOf(true) }
    var allowTf by remember { mutableStateOf(true) }
    var isSmartMode by remember { mutableStateOf(false) }
    var targetLesson by remember { mutableStateOf(selectedLesson ?: savedLessons.firstOrNull()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = Strings.get("quiz_setup_title", lang),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Lesson Picker if multiple lessons exist
        if (savedLessons.isNotEmpty()) {
            Text(
                text = if (isAr) "اختر الدرس المراد اختباره:" else "Select Lesson to Test:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            savedLessons.forEach { lesson ->
                val isChosen = targetLesson?.id == lesson.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { targetLesson = lesson },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChosen) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isChosen) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Text(
                        text = lesson.title,
                        fontSize = 14.sp,
                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                        color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Question Count
        Text(
            text = Strings.get("select_question_count", lang),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5, 10, 20).forEach { count ->
                val isSelected = questionCount == count
                OutlinedButton(
                    onClick = { questionCount = count },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$count",
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Question Types
        Text(
            text = Strings.get("select_types", lang),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = allowMcq,
                onClick = { allowMcq = !allowMcq },
                label = { Text(Strings.get("type_mcq", lang)) }
            )
            FilterChip(
                selected = allowTf,
                onClick = { allowTf = !allowTf },
                label = { Text(Strings.get("type_tf", lang)) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Smart Adaptive Quiz Mode Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("smart_quiz_btn", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Switch(
                        checked = isSmartMode,
                        onCheckedChange = { isSmartMode = it }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Strings.get("smart_quiz_desc", lang),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Start Quiz Button
        Button(
            onClick = {
                val types = mutableListOf<String>()
                if (allowMcq) types.add("mcq")
                if (allowTf) types.add("true_false")
                if (types.isEmpty()) types.add("mcq")

                viewModel.prepareQuiz(
                    lesson = targetLesson,
                    questionCount = questionCount,
                    types = types,
                    isSmartMode = isSmartMode
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = Strings.get("btn_start_quiz", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActiveQuizScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val isAr = lang == "ar"
    val questions by viewModel.quizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userSelectedAnswers.collectAsState()
    val isAnswerSubmitted by viewModel.isAnswerSubmitted.collectAsState()
    val isSmartMode by viewModel.isSmartQuizMode.collectAsState()
    val smartDifficulty by viewModel.smartQuizDifficulty.collectAsState()

    if (questions.isEmpty()) {
        onBack()
        return
    }

    val currentQ = questions[currentIndex]
    val totalCount = questions.size
    val selectedOptionIndex = userAnswers[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Header: Progress and Close
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Strings.get("question_progress", lang).format(currentIndex + 1, totalCount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Smart difficulty indicator
            if (isSmartMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAr) "مستوى الصعوبة: $smartDifficulty/3" else "Level: $smartDifficulty/3",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Exit Quiz")
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress Bar
        val progress = ((currentIndex + 1).toFloat() / totalCount).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Question Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = currentQ.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(currentQ.options.size) { optIdx ->
                val optionText = currentQ.options[optIdx]
                val isSelected = selectedOptionIndex == optIdx
                val isCorrect = currentQ.correctAnswerIndex == optIdx

                // Card background and border color based on state
                val (bgColor, borderColor) = when {
                    !isAnswerSubmitted && isSelected ->
                        Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                    isAnswerSubmitted && isCorrect ->
                        Pair(SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                    isAnswerSubmitted && isSelected && !isCorrect ->
                        Pair(ErrorRed.copy(alpha = 0.15f), ErrorRed)
                    else ->
                        Pair(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outlineVariant)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isAnswerSubmitted) {
                            viewModel.selectAnswer(optIdx)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(borderColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${('A'.code + optIdx).toChar()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = borderColor
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optionText,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected || (isAnswerSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isAnswerSubmitted) {
                            if (isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Explanation shown AFTER answer submission
            if (isAnswerSubmitted && currentQ.explanation.isNotBlank()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isAr) "💡 توضيح الذكاء الاصطناعي:" else "💡 AI Explanation:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQ.explanation,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons
        if (!isAnswerSubmitted) {
            Button(
                onClick = { viewModel.submitCurrentAnswer() },
                enabled = selectedOptionIndex != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = Strings.get("confirm_answer", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (currentIndex < totalCount - 1) Strings.get("next_question", lang)
                    else Strings.get("finish_quiz", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuizResultScreen(
    viewModel: StudyViewModel,
    onHome: () -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val isAr = lang == "ar"
    val result by viewModel.lastQuizResult.collectAsState()

    if (result == null) {
        onHome()
        return
    }
    val attempt = result!!

    val evaluation = when {
        attempt.score >= 85 -> Strings.get("eval_excellent", lang)
        attempt.score >= 60 -> Strings.get("eval_very_good", lang)
        else -> Strings.get("eval_needs_review", lang)
    }

    val scoreColor = when {
        attempt.score >= 85 -> SuccessGreen
        attempt.score >= 60 -> MaterialTheme.colorScheme.primary
        else -> ErrorRed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Strings.get("quiz_result_title", lang),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Circular Score Display
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(scoreColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${attempt.score}%",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Text(
                    text = Strings.get("score_label", lang),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Evaluation Badge
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = scoreColor.copy(alpha = 0.15f)
            )
        ) {
            Text(
                text = evaluation,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Summary Statistics Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${attempt.correctAnswers}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Text(
                        text = Strings.get("correct_answers", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val wrong = attempt.totalQuestions - attempt.correctAnswers
                    Text(
                        text = "$wrong",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                    Text(
                        text = Strings.get("wrong_answers", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${attempt.durationSeconds}s",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = Strings.get("time_spent", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Weak Points Section if any
        if (attempt.weakTopics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Strings.get("weak_points", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    attempt.weakTopics.forEach { topic ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = topic,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Retest Weak Topics Button
            OutlinedButton(
                onClick = { viewModel.retestWeakTopics() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.get("retry_weak", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Home
        Button(
            onClick = onHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (isAr) "العودة إلى شاشة الرئيسية 🏠" else "Back to Home 🏠",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
