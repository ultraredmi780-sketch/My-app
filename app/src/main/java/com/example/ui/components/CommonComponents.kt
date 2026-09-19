package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlashcardItem
import com.example.ui.i18n.Strings
import com.example.ui.theme.ProGold
import com.example.ui.theme.SuccessGreen

@Composable
fun ProBadge(
    isPro: Boolean,
    onUpgradeClick: () -> Unit,
    lang: String
) {
    if (isPro) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFB703), Color(0xFFFB8500))
                    )
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "PRO ⭐",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { onUpgradeClick() }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (lang == "ar") "ترقية إلى Pro" else "Upgrade to Pro",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProUpgradeDialog(
    message: String?,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
    lang: String
) {
    val isAr = lang == "ar"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ProGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isAr) "خطة StudyAI Pro" else "StudyAI Pro Plan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!message.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }

                Text(
                    text = if (isAr) "مزايا الاشتراك في Pro للطلاب:" else "Pro Student Benefits:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val features = listOf(
                    Strings.get("pro_feature_1", lang),
                    Strings.get("pro_feature_2", lang),
                    Strings.get("pro_feature_3", lang),
                    Strings.get("pro_feature_4", lang),
                    Strings.get("pro_feature_5", lang)
                )

                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isAr) "جاهز للربط عبر Google Play Billing دون رسوم خفية." else "Compatible with Google Play Billing.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFB8500)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = Strings.get("btn_upgrade_now", lang),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = Strings.get("btn_cancel", lang))
            }
        }
    )
}

@Composable
fun InteractiveFlashcard(
    item: FlashcardItem,
    index: Int,
    total: Int,
    lang: String
) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { isFlipped = !isFlipped },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rotation <= 90f)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Front Side
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (lang == "ar") "بطاقة ${index + 1} من $total • السؤال / المفهوم"
                        else "Card ${index + 1} of $total • Concept / Question",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = item.front,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.get("flip_card_hint", lang),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Back Side (flipped horizontally to remain readable)
                Column(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (lang == "ar") "الإجابة / الشرح المختصر ✓" else "Answer / Definition ✓",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = item.back,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PolicyDialog(
    isPrivacy: Boolean,
    onDismiss: () -> Unit,
    lang: String
) {
    val isAr = lang == "ar"
    val title = if (isPrivacy) Strings.get("privacy_policy", lang) else Strings.get("terms_of_service", lang)

    val content = if (isPrivacy) {
        if (isAr) {
            """
            سياسة الخصوصية لتطبيق StudyAI:
            1. حماية البيانات: نحن نحترم خصوصية الطلاب ولا نشارك أي صور أو نصوص ملتقطة مع أطراف ثالثة لأغراض إعلانية.
            2. تخزين الدروس: تُحفظ دروسك واختباراتك محلياً بشكل آمن على جهازك، ويمكنك حذف أي درس في أي وقت بنقرة واحدة.
            3. استخدام الذكاء الاصطناعي: تُعالج طلباتك التعليمية عبر واجهات برمجة آمنة مشفرة (HTTPS) لغرض توليد الشرح والاختبار فقط.
            4. أذونات الجهاز: يُطلب إذن الكاميرا فقط عند رغبتك في تصوير صفحات الكتب، ولا يُستخدم في الخلفية مطلقاً.
            5. خصوصية الحساب: لا يحق لأي طالب الوصول إلى محتوى أو نتائج طالب آخر.
            """.trimIndent()
        } else {
            """
            StudyAI Privacy Policy:
            1. Data Protection: We strictly respect student privacy. Images and study notes are never sold or shared with third parties for marketing.
            2. Local Storage: Your lessons, quiz records, and flashcards are safely kept on your device and can be deleted anytime.
            3. AI Processing: Educational queries are routed over encrypted HTTPS solely to generate explanations and quizzes.
            4. Permissions: Camera permission is requested only when taking a study picture and is never accessed in the background.
            5. User Isolation: Each student only accesses their own study materials.
            """.trimIndent()
        }
    } else {
        if (isAr) {
            """
            شروط الخدمة لتطبيق StudyAI:
            1. الغرض التعليمي: تم تصميم تطبيق StudyAI ليكون أداة مساعدة دراسية للطلاب للمراجعة والفهم.
            2. دقة المحتوى: يسعى الذكاء الاصطناعي لتقديم أدق إجابات ممكنة استناداً لنصوص دروسك، ومع ذلك يُنصح بمراجعة المراجع الرسمية للاختبارات النهائية.
            3. الاستخدام العادل: تخضع الخطط المجانية لحدود استخدام يومية للحفاظ على استقرار الخدمة للجميع.
            4. الملكية الفكرية: جميع المحتويات التي تدخلها تظل ملكاً لك.
            """.trimIndent()
        } else {
            """
            StudyAI Terms of Service:
            1. Educational Purpose: StudyAI is designed as an intelligent study companion to reinforce student learning.
            2. Content Accuracy: The AI strives for high accuracy based on provided materials, though students should consult textbooks for official syllabus exams.
            3. Fair Use: Free accounts have daily limits to ensure fair resources for all students.
            4. Ownership: Your study inputs and personal notes remain your property.
            """.trimIndent()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = content,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = Strings.get("btn_cancel", lang))
            }
        }
    )
}
