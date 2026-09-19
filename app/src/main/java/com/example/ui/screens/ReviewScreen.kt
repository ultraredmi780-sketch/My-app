package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuizAttempt
import com.example.ui.components.InteractiveFlashcard
import com.example.ui.i18n.Strings
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun ReviewScreen(
    viewModel: StudyViewModel,
    onNavigate: (Screen) -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val isAr = lang == "ar"
    val lessons by viewModel.savedLessons.collectAsState()
    val quizzes by viewModel.quizAttempts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Collect all flashcards from all lessons
    val allFlashcards = remember(lessons) {
        lessons.flatMap { it.flashcards }
    }

    // Collect all weak points from quizzes
    val allWeakTopics = remember(quizzes) {
        quizzes.flatMap { it.weakTopics }.distinct()
    }

    val tabs = listOf(
        Strings.get("review_tab_lessons", lang),
        Strings.get("review_tab_quizzes", lang),
        Strings.get("review_tab_flashcards", lang),
        if (isAr) "نقاط للتحسين (${allWeakTopics.size})" else "Weak Points (${allWeakTopics.size})"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Title
        Text(
            text = Strings.get("nav_review", lang),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
        )

        // Live Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text(Strings.get("search_hint", lang), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 20.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // List Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Saved Lessons
                    if (lessons.isEmpty()) {
                        item {
                            EmptyState(
                                message = if (isAr) "لا توجد دروس محفوظة مطابقة." else "No matching saved lessons found."
                            )
                        }
                    } else {
                        items(lessons) { lesson ->
                            LessonCard(
                                lesson = lesson,
                                onClick = { viewModel.selectLesson(lesson) },
                                lang = lang
                            )
                        }
                    }
                }
                1 -> {
                    // Past Quizzes
                    if (quizzes.isEmpty()) {
                        item {
                            EmptyState(
                                message = if (isAr) "لم تقم بإجراء أي اختبار بعد. ابدأ أول اختبار الآن!" else "No past quizzes yet. Take your first quiz now!"
                            )
                        }
                    } else {
                        items(quizzes) { attempt ->
                            QuizHistoryCard(attempt = attempt, lang = lang)
                        }
                    }
                }
                2 -> {
                    // Flashcards
                    if (allFlashcards.isEmpty()) {
                        item {
                            EmptyState(
                                message = if (isAr) "لا توجد بطاقات مراجعة بعد. قم بتحليل درس لتوليد بطاقات تفاعلية." else "No flashcards yet. Analyze a lesson to generate cards."
                            )
                        }
                    } else {
                        itemsIndexed(allFlashcards) { idx, card ->
                            Box(modifier = Modifier.padding(vertical = 6.dp)) {
                                InteractiveFlashcard(
                                    item = card,
                                    index = idx,
                                    total = allFlashcards.size,
                                    lang = lang
                                )
                            }
                        }
                    }
                }
                3 -> {
                    // Weak Topics
                    if (allWeakTopics.isEmpty()) {
                        item {
                            EmptyState(
                                message = if (isAr) "أحسنت! لا توجد نقاط ضعف مسجلة حالياً." else "Great job! No weak points recorded."
                            )
                        }
                    } else {
                        items(allWeakTopics) { topic ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ErrorRed)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = topic,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizHistoryCard(attempt: QuizAttempt, lang: String) {
    val isAr = lang == "ar"
    val scoreColor = when {
        attempt.score >= 85 -> SuccessGreen
        attempt.score >= 60 -> MaterialTheme.colorScheme.primary
        else -> ErrorRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.lessonTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isAr) "${attempt.correctAnswers}/${attempt.totalQuestions} إجابات صحيحة • ${attempt.durationSeconds} ثانية"
                    else "${attempt.correctAnswers}/${attempt.totalQuestions} correct • ${attempt.durationSeconds}s",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(scoreColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${attempt.score}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}
