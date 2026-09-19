package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ProUpgradeDialog
import com.example.ui.i18n.Strings
import com.example.ui.screens.ActiveQuizScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LessonChatScreen
import com.example.ui.screens.LessonDetailScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.QuizResultScreen
import com.example.ui.screens.QuizSetupScreen
import com.example.ui.screens.ReviewScreen
import com.example.ui.theme.StudyAITheme
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StudyViewModel = viewModel()
            val theme by viewModel.theme.collectAsState()
            val lang by viewModel.language.collectAsState()

            val layoutDirection = if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                StudyAITheme(themePreference = theme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        StudyApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun StudyApp(viewModel: StudyViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val lang by viewModel.language.collectAsState()
    val showProModal by viewModel.showProUpgradeModal.collectAsState()
    val proModalMessage by viewModel.proUpgradeMessage.collectAsState()

    val showBottomBar = currentScreen in listOf(
        Screen.Home,
        Screen.Review,
        Screen.Progress,
        Screen.Profile
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                StudyBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    lang = lang
                )
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_crossfade"
        ) { screen ->
            when (screen) {
                is Screen.Onboarding -> {
                    OnboardingScreen(
                        onFinish = { viewModel.completeOnboarding() },
                        lang = lang
                    )
                }
                is Screen.Auth -> {
                    AuthScreen(
                        onLogin = { email -> viewModel.login(email) },
                        onRegister = { name, email -> viewModel.register(name, email) },
                        onGuestLogin = { viewModel.login("student@studyai.app") },
                        lang = lang
                    )
                }
                is Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                is Screen.Review -> {
                    ReviewScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                is Screen.Progress -> {
                    ProgressScreen(viewModel = viewModel)
                }
                is Screen.Profile -> {
                    ProfileScreen(viewModel = viewModel)
                }
                is Screen.LessonDetail -> {
                    LessonDetailScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Home) },
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                is Screen.LessonChat -> {
                    LessonChatScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.LessonDetail) }
                    )
                }
                is Screen.QuizSetup -> {
                    QuizSetupScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                is Screen.ActiveQuiz -> {
                    ActiveQuizScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                is Screen.QuizResult -> {
                    QuizResultScreen(
                        viewModel = viewModel,
                        onHome = { viewModel.navigateTo(Screen.Home) }
                    )
                }
            }
        }
    }

    // Pro Upgrade Modal
    if (showProModal) {
        ProUpgradeDialog(
            message = proModalMessage,
            onDismiss = { viewModel.dismissProModal() },
            onUpgrade = { viewModel.upgradeToPro() },
            lang = lang
        )
    }
}

@Composable
fun StudyBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    lang: String
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Home,
            onClick = { onNavigate(Screen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(Strings.get("nav_home", lang)) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Review,
            onClick = { onNavigate(Screen.Review) },
            icon = { Icon(Icons.Default.Book, contentDescription = null) },
            label = { Text(Strings.get("nav_review", lang)) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Progress,
            onClick = { onNavigate(Screen.Progress) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text(Strings.get("nav_progress", lang)) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Profile,
            onClick = { onNavigate(Screen.Profile) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(Strings.get("nav_profile", lang)) }
        )
    }
}
