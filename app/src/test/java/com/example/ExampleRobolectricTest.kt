package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.Converters
import com.example.model.FlashcardItem
import com.example.model.QuestionItem
import com.example.model.SubscriptionLimits
import com.example.model.TermItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StudyAI", appName)
    }

    @Test
    fun `test converters serialization`() {
        val converters = Converters()

        // Terms
        val terms = listOf(TermItem("Inertia", "Resistance to change in motion"))
        val serializedTerms = converters.fromTermList(terms)
        val deserializedTerms = converters.toTermList(serializedTerms)
        assertEquals(1, deserializedTerms.size)
        assertEquals("Inertia", deserializedTerms[0].term)

        // Flashcards
        val flashcards = listOf(FlashcardItem("Front question?", "Back answer!"))
        val serializedCards = converters.fromFlashcardList(flashcards)
        val deserializedCards = converters.toFlashcardList(serializedCards)
        assertEquals(1, deserializedCards.size)
        assertEquals("Front question?", deserializedCards[0].front)

        // Questions
        val questions = listOf(
            QuestionItem(
                question = "What is F=ma?",
                options = listOf("Newton 2nd", "Newton 1st"),
                correctAnswerIndex = 0,
                explanation = "Force equation",
                type = "mcq"
            )
        )
        val serializedQuestions = converters.fromQuestionList(questions)
        val deserializedQuestions = converters.toQuestionList(serializedQuestions)
        assertEquals(1, deserializedQuestions.size)
        assertEquals("Newton 2nd", deserializedQuestions[0].options[0])
    }

    @Test
    fun `test subscription limits constants`() {
        assertTrue(SubscriptionLimits.FREE_DAILY_IMAGE_ANALYSES in 1..10)
        assertTrue(SubscriptionLimits.FREE_DAILY_QUIZZES in 5..20)
        assertTrue(SubscriptionLimits.PRO_DAILY_IMAGE_ANALYSES > 100)
    }
}
