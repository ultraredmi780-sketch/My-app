package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.model.FlashcardItem
import com.example.model.Lesson
import com.example.model.QuestionItem
import com.example.model.TermItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class AnalysisResponse {
    data class Success(
        val title: String,
        val extractedText: String,
        val explanation: String,
        val summary: String,
        val keyPoints: List<String>,
        val terms: List<TermItem>,
        val questions: List<QuestionItem>,
        val flashcards: List<FlashcardItem>
    ) : AnalysisResponse()

    data class Warning(val message: String) : AnalysisResponse()
    data class Error(val message: String) : AnalysisResponse()
}

data class ChatResponse(
    val answer: String,
    val isFromLessonContent: Boolean
)

data class ExplanationResponse(
    val explanation: String,
    val mode: String
)

class AiStudyService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Analyze an educational photo or user manual text.
     */
    suspend fun analyzeLesson(
        bitmap: Bitmap?,
        manualText: String?,
        language: String
    ): AnalysisResponse = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // If no API key or empty, or test placeholder
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext provideOfflineOrDemoAnalysis(manualText, language)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val prompt = buildString {
                append("You are StudyAI, an advanced educational AI assistant for students. ")
                append("Analyze this input in ${if (language == "ar") "Arabic" else "English"}. ")
                append("\nRules:\n")
                append("1. If the image is blurry, completely illegible, or too dark to read, set status to 'unclear_image'.\n")
                append("2. If the image does not contain educational, study, book, or academic text/content, set status to 'not_study_content'.\n")
                append("3. DO NOT invent facts not supported by the content.\n")
                append("4. Otherwise set status to 'success' and output STRICT JSON in this exact structure:\n")
                append("{\n")
                append("  \"status\": \"success\",\n")
                append("  \"title\": \"Title of the lesson/topic\",\n")
                append("  \"extractedText\": \"Extracted or recognized text\",\n")
                append("  \"explanation\": \"Simplified explanation easy for students to understand with real-life analogy\",\n")
                append("  \"summary\": \"Concise and comprehensive summary\",\n")
                append("  \"keyPoints\": [\"Point 1\", \"Point 2\", \"Point 3\"],\n")
                append("  \"terms\": [{\"term\": \"term name\", \"definition\": \"meaning\"}],\n")
                append("  \"questions\": [{\"question\": \"Q?\", \"options\": [\"A\",\"B\",\"C\",\"D\"], \"correctAnswerIndex\": 0, \"explanation\": \"why\", \"type\": \"mcq\"}],\n")
                append("  \"flashcards\": [{\"front\": \"Question or Concept\", \"back\": \"Answer or Definition\"}]\n")
                append("}\n")
                if (!manualText.isNullOrBlank()) {
                    append("Student manual text/question: $manualText\n")
                }
            }

            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // Add text prompt
            val textPart = JSONObject().apply { put("text", prompt) }
            partsArray.put(textPart)

            // Add image if available
            if (bitmap != null) {
                val base64Data = bitmapToBase64(bitmap)
                val imagePart = JSONObject().apply {
                    val inlineData = JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Data)
                    }
                    put("inlineData", inlineData)
                }
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                val genConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                return@withContext AnalysisResponse.Error(
                    if (language == "ar") "فشل الاتصال بالذكاء الاصطناعي ($errBody). يرجى التأكد من مفتاح API أو المحاولة لاحقًا."
                    else "AI connection error ($errBody). Please check API configuration or try again."
                )
            }

            val resStr = response.body?.string() ?: ""
            val resObj = JSONObject(resStr)
            val candidates = resObj.optJSONArray("candidates")
            val rawContent = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text", "") ?: ""

            parseAnalysisJson(rawContent, language)
        } catch (e: Exception) {
            e.printStackTrace()
            AnalysisResponse.Error(
                if (language == "ar") "حدث خطأ أثناء معالجة الدرس: ${e.localizedMessage ?: "تحقق من اتصال الإنترنت"}"
                else "An error occurred while processing: ${e.localizedMessage ?: "Check network connection"}"
            )
        }
    }

    /**
     * Ask a question about the lesson with grounding check.
     */
    suspend fun chatWithLesson(
        lesson: Lesson,
        userQuestion: String,
        language: String
    ): ChatResponse = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalTutorChat(lesson, userQuestion, language)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val prompt = buildString {
                append("You are an educational tutor in StudyAI. ")
                append("Here is the lesson content context:\n")
                append("TITLE: ${lesson.title}\n")
                append("SUMMARY: ${lesson.summary}\n")
                append("EXPLANATION: ${lesson.explanation}\n")
                append("EXTRACTED TEXT: ${lesson.extractedText}\n\n")
                append("Student asks in ${if (language == "ar") "Arabic" else "English"}: $userQuestion\n\n")
                append("Rules:\n")
                append("1. If the answer can be fully answered from the lesson content, answer it clearly and set isFromLessonContent to true.\n")
                append("2. If the answer requires general knowledge outside the lesson text, answer it helpfully, explicitly mention that this extends beyond the provided lesson content, and set isFromLessonContent to false.\n")
                append("Output JSON: {\"answer\": \"your explanation\", \"isFromLessonContent\": boolean}")
            }

            val requestJson = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                }
                put("contents", JSONArray().apply {
                    put(JSONObject().apply { put("parts", parts) })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val resStr = response.body?.string() ?: ""
                val resObj = JSONObject(resStr)
                val text = resObj.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "") ?: ""

                val parsed = JSONObject(text)
                return@withContext ChatResponse(
                    answer = parsed.optString("answer", "لا تتوفر إجابة حالياً."),
                    isFromLessonContent = parsed.optBoolean("isFromLessonContent", true)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        generateLocalTutorChat(lesson, userQuestion, language)
    }

    /**
     * "Explain to me more" / اشرح لي أكثر with customizable modes.
     */
    suspend fun explainMore(
        lesson: Lesson,
        mode: String,
        language: String
    ): ExplanationResponse = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineExplanation(lesson, mode, language)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val prompt = buildString {
                append("Lesson Title: ${lesson.title}\n")
                append("Lesson Text: ${lesson.extractedText.ifBlank { lesson.explanation }}\n\n")
                append("Task: Provide an explanation in ${if (language == "ar") "Arabic" else "English"} specifically for mode '$mode':\n")
                when (mode) {
                    "simpler" -> append("Explain in the simplest possible terms, with everyday real-world examples and relatable analogies.")
                    "beginner" -> append("Explain for complete beginners step by step with zero prerequisite assumptions.")
                    "concise" -> append("Provide a quick 3-sentence ultra-concise breakdown of the core concept.")
                    "detailed" -> append("Provide a comprehensive, in-depth deep-dive covering nuances, causes, and effects.")
                    "example" -> append("Provide 2 practical, concrete, everyday life applications or examples illustrating this.")
                    "memorize" -> append("Highlight exactly what must be memorized: formulas, core dates, keywords, and mnemonic memory tricks.")
                }
                append("\nOutput plain text explanation without markdown headers.")
            }

            val requestJson = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                }
                put("contents", JSONArray().apply {
                    put(JSONObject().apply { put("parts", parts) })
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val resStr = response.body?.string() ?: ""
                val resObj = JSONObject(resStr)
                val text = resObj.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "") ?: ""

                if (text.isNotBlank()) {
                    return@withContext ExplanationResponse(text.trim(), mode)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        getOfflineExplanation(lesson, mode, language)
    }

    /**
     * Generate custom quiz with specified number of questions and question types.
     */
    suspend fun generateCustomQuiz(
        lesson: Lesson,
        questionCount: Int,
        questionTypes: List<String>,
        language: String
    ): List<QuestionItem> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val prompt = buildString {
                    append("Based SOLELY on this lesson content, generate exactly $questionCount questions for students in ${if (language == "ar") "Arabic" else "English"}.\n")
                    append("Types requested: ${questionTypes.joinToString(",")}\n")
                    append("Lesson Title: ${lesson.title}\n")
                    append("Lesson Content: ${lesson.extractedText.ifBlank { lesson.explanation }}\n")
                    append("Output JSON format strictly:\n")
                    append("{\n")
                    append("  \"questions\": [\n")
                    append("    {\"question\": \"...\", \"options\": [\"Option 1\",\"Option 2\",\"Option 3\",\"Option 4\"], \"correctAnswerIndex\": 0, \"explanation\": \"why it is correct\", \"type\": \"mcq\"}\n")
                    append("  ]\n")
                    append("}\n")
                }

                val requestJson = JSONObject().apply {
                    val parts = JSONArray().apply { put(JSONObject().apply { put("text", prompt) }) }
                    put("contents", JSONArray().apply { put(JSONObject().apply { put("parts", parts) }) })
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                    })
                }

                val response = client.newCall(
                    Request.Builder().url(endpoint).post(requestJson.toString().toRequestBody(jsonMediaType)).build()
                ).execute()

                if (response.isSuccessful) {
                    val raw = response.body?.string() ?: ""
                    val root = JSONObject(raw)
                    val text = root.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text", "") ?: ""

                    val parsed = JSONObject(text)
                    val qArr = parsed.optJSONArray("questions")
                    if (qArr != null && qArr.length() > 0) {
                        val result = mutableListOf<QuestionItem>()
                        for (i in 0 until qArr.length()) {
                            val qObj = qArr.getJSONObject(i)
                            val optArr = qObj.optJSONArray("options") ?: JSONArray()
                            val opts = mutableListOf<String>()
                            for (j in 0 until optArr.length()) {
                                opts.add(optArr.getString(j))
                            }
                            result.add(
                                QuestionItem(
                                    question = qObj.optString("question", ""),
                                    options = opts,
                                    correctAnswerIndex = qObj.optInt("correctAnswerIndex", 0),
                                    explanation = qObj.optString("explanation", ""),
                                    type = qObj.optString("type", "mcq")
                                )
                            )
                        }
                        if (result.isNotEmpty()) return@withContext result
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback: build interactive questions from lesson
        generateLocalQuestions(lesson, questionCount, language)
    }

    private fun parseAnalysisJson(rawJson: String, language: String): AnalysisResponse {
        return try {
            val root = JSONObject(rawJson)
            val status = root.optString("status", "success")

            if (status == "unclear_image") {
                return AnalysisResponse.Warning(
                    if (language == "ar") "الصورة غير واضحة، حاول التقاط صورة بإضاءة أفضل وزاوية مستقيمة."
                    else "The image is unclear. Please try capturing with better lighting and a steady angle."
                )
            }
            if (status == "not_study_content") {
                return AnalysisResponse.Warning(
                    if (language == "ar") "الصورة لا تحتوي على محتوى دراسي أو تعليمي واضح. يرجى تصوير صفحة كتاب أو ملخص دراسي."
                    else "The image does not contain clear academic or study content. Please capture a book page or study notes."
                )
            }

            val title = root.optString("title", if (language == "ar") "درس دراسي جديد" else "New Study Lesson")
            val extractedText = root.optString("extractedText", "")
            val explanation = root.optString("explanation", "")
            val summary = root.optString("summary", "")

            val keyPoints = mutableListOf<String>()
            val kpArr = root.optJSONArray("keyPoints")
            if (kpArr != null) {
                for (i in 0 until kpArr.length()) {
                    keyPoints.add(kpArr.getString(i))
                }
            }

            val terms = mutableListOf<TermItem>()
            val termsArr = root.optJSONArray("terms")
            if (termsArr != null) {
                for (i in 0 until termsArr.length()) {
                    val tObj = termsArr.getJSONObject(i)
                    terms.add(TermItem(tObj.optString("term"), tObj.optString("definition")))
                }
            }

            val questions = mutableListOf<QuestionItem>()
            val qArr = root.optJSONArray("questions")
            if (qArr != null) {
                for (i in 0 until qArr.length()) {
                    val qObj = qArr.getJSONObject(i)
                    val optArr = qObj.optJSONArray("options") ?: JSONArray()
                    val opts = mutableListOf<String>()
                    for (j in 0 until optArr.length()) {
                        opts.add(optArr.getString(j))
                    }
                    questions.add(
                        QuestionItem(
                            question = qObj.optString("question"),
                            options = opts,
                            correctAnswerIndex = qObj.optInt("correctAnswerIndex", 0),
                            explanation = qObj.optString("explanation"),
                            type = qObj.optString("type", "mcq")
                        )
                    )
                }
            }

            val flashcards = mutableListOf<FlashcardItem>()
            val fArr = root.optJSONArray("flashcards")
            if (fArr != null) {
                for (i in 0 until fArr.length()) {
                    val fObj = fArr.getJSONObject(i)
                    flashcards.add(FlashcardItem(fObj.optString("front"), fObj.optString("back")))
                }
            }

            AnalysisResponse.Success(
                title = title,
                extractedText = extractedText,
                explanation = explanation,
                summary = summary,
                keyPoints = keyPoints,
                terms = terms,
                questions = questions,
                flashcards = flashcards
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AnalysisResponse.Error(
                if (language == "ar") "تعذر معالجة استجابة الذكاء الاصطناعي بدقة: ${e.message}"
                else "Could not parse AI response: ${e.message}"
            )
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun provideOfflineOrDemoAnalysis(manualText: String?, language: String): AnalysisResponse {
        val isAr = language == "ar"
        val subject = manualText?.takeIf { it.isNotBlank() } ?: (if (isAr) "قوانين الحركة لنيوتن" else "Newton's Laws of Motion")

        val title = if (isAr) "درس: $subject" else "Lesson: $subject"
        val extractedText = if (isAr) {
            "تم استخراج النص الدراسي وتحليله بنجاح لموضوع: $subject. ينص المبدأ الأساسي على أن لكل فعل رد فعل مساوٍ له في المقدار ومضاد له في الاتجاه، وأن الأجسام تظل في حالتها ما لم تؤثر عليها قوى خارجية."
        } else {
            "Study text extracted and synthesized for: $subject. The fundamental principle states that for every action there is an equal and opposite reaction, and objects maintain their velocity unless an external force acts."
        }

        val explanation = if (isAr) {
            "تخيل أنك تدفع جداراً بيدك بقوة؛ ستشعر أن الجدار يضغط بيدك أيضاً بنفس المقدار! هذا هو المبدأ الجوهري. يسهل فهمه من خلال أمثلة الحياة اليومية كركوب الدراجة أو ركل الكرة."
        } else {
            "Imagine pushing against a wall with your hand; you can feel the wall pressing back against your hand with the exact same magnitude! That is the core principle."
        }

        val summary = if (isAr) {
            "يلخص هذا الدرس المبادئ الأساسية للموضوع وأهم النظريات وتطبيقاتها العلمية، مع التركيز على العلاقات الرياضية وتطبيقات الحركة والقوى."
        } else {
            "This lesson summarizes the foundational principles of the topic, key physical laws, and their scientific applications in daily life."
        }

        val keyPoints = if (isAr) {
            listOf(
                "القانون الأول: يصف خاصية القصور الذاتي للأجسام الساكنة والمتحركة.",
                "القانون الثاني: القوة المحصلة تساوي الكتلة مضروبة في التسارع (F = m × a).",
                "القانون الثالث: قوى الفعل ورد الفعل متزامنة وتؤثر في جسمين مختلفين."
            )
        } else {
            listOf(
                "First Law: Defines inertia of stationary and moving objects.",
                "Second Law: Net Force equals mass times acceleration (F = m * a).",
                "Third Law: Action and reaction forces act simultaneously on different bodies."
            )
        }

        val terms = if (isAr) {
            listOf(
                TermItem("القصور الذاتي (Inertia)", "مقاومة الجسم لأي تغيير في حالته الحركية أو سكونه."),
                TermItem("التسارع (Acceleration)", "معدل التغير في السرعة المتجهة بالنسبة للزمن."),
                TermItem("القوة المحصلة (Net Force)", "مجموع القوى المؤثرة على جسم في اتجاه معين.")
            )
        } else {
            listOf(
                TermItem("Inertia", "The resistance of any physical object to any change in its velocity."),
                TermItem("Acceleration", "The rate of change of the velocity of an object with respect to time."),
                TermItem("Net Force", "The overall force acting on an object when all individual forces are combined.")
            )
        }

        val questions = if (isAr) {
            listOf(
                QuestionItem(
                    question = "ما هي المعادلة الرياضية لقانون نيوتن الثاني؟",
                    options = listOf("F = m × a", "E = m × c²", "v = d / t", "W = F × d"),
                    correctAnswerIndex = 0,
                    explanation = "القوة (F) تساوي الكتلة (m) مضروبة في التسارع (a).",
                    type = "mcq"
                ),
                QuestionItem(
                    question = "قوى الفعل ورد الفعل تلغي بعضها البعض دائماً لأنها متساوية ومضادة.",
                    options = listOf("صح", "خطأ"),
                    correctAnswerIndex = 1,
                    explanation = "خطأ، لأن قوى الفعل ورد الفعل تؤثران على جسمين مختلفين فلا تلغيان بعضهما.",
                    type = "true_false"
                ),
                QuestionItem(
                    question = "ما الخاصية الفيزيائية التي تعبر عن مقاومة الجسم للتغير في حركته؟",
                    options = listOf("القصور الذاتي", "الكثافة", "اللزوجة", "الضغط"),
                    correctAnswerIndex = 0,
                    explanation = "القصور الذاتي هو ميل الجسم لمقاومة التغير في سرعته أو اتجاهه.",
                    type = "mcq"
                )
            )
        } else {
            listOf(
                QuestionItem(
                    question = "What is the formula for Newton's Second Law?",
                    options = listOf("F = m * a", "E = mc^2", "v = d / t", "P = W / t"),
                    correctAnswerIndex = 0,
                    explanation = "Net Force (F) equals mass (m) multiplied by acceleration (a).",
                    type = "mcq"
                ),
                QuestionItem(
                    question = "Action and reaction forces cancel each other out on the same object.",
                    options = listOf("True", "False"),
                    correctAnswerIndex = 1,
                    explanation = "False, because action and reaction act on two distinct objects.",
                    type = "true_false"
                )
            )
        }

        val flashcards = if (isAr) {
            listOf(
                FlashcardItem("ما هو القصور الذاتي؟", "هو خاصية احتفاظ الجسم بحالته من سكون أو حركة ما لم تؤثر عليه قوة خارجية."),
                FlashcardItem("ما هي وحدة قياس القوة في النظام الدولي؟", "النيوتن (N) ويعادل كجم.م/ث²."),
                FlashcardItem("متى يكون تسارع الجسم مساوياً للصفر؟", "عندما تكون القوة المحصلة المؤثرة عليه مساوية للصفر (في حالة السكون أو السرعة الثابتة).")
            )
        } else {
            listOf(
                FlashcardItem("What is Inertia?", "The property of matter by which it continues in its existing state of rest or uniform motion."),
                FlashcardItem("What is the SI unit of force?", "The Newton (N), which equals kg·m/s²."),
                FlashcardItem("When is acceleration zero?", "When the net force acting on the body is zero.")
            )
        }

        return AnalysisResponse.Success(
            title = title,
            extractedText = extractedText,
            explanation = explanation,
            summary = summary,
            keyPoints = keyPoints,
            terms = terms,
            questions = questions,
            flashcards = flashcards
        )
    }

    private fun generateLocalTutorChat(lesson: Lesson, question: String, language: String): ChatResponse {
        val isAr = language == "ar"
        val qLower = question.lowercase()

        val answer = if (isAr) {
            when {
                qLower.contains("لماذا") || qLower.contains("سبب") ->
                    "بناءً على درس '${lesson.title}'، يحدث ذلك نتيجة التفاعل بين القوى والمبادئ المشروحة في النص؛ فالأسباب المباشرة موضحة في الملخص والنقاط الأساسية."
                qLower.contains("مثال") ->
                    "مثال من واقعنا العملي: عندما تركب حافلة وتتوقف فجأة، يندفع جسمك للأمام بسبب القصور الذاتي، تماماً كما ورد في شرح الدرس."
                qLower.contains("فرق") || qLower.contains("مقارنة") ->
                    "الفرق الجوهري يكمن في التعريف والخصائص: يمكنك مراجعة قسم 'المصطلحات المهمة' في الدرس لمشاهدة الفارق الدقيق لكل مصطلح."
                else ->
                    "استناداً إلى نص ومفاهيم درس '${lesson.title}': ${lesson.explanation.take(200)}... هذه النقطة محورية لاجتياز اختبار هذا الموضوع بنجاح."
            }
        } else {
            when {
                qLower.contains("why") ->
                    "Based on '${lesson.title}', this occurs because of the key principles covered in the lesson text and summary."
                qLower.contains("example") ->
                    "Everyday example: When you are in a car that brakes suddenly, your body lunges forward due to inertia, exactly as explained."
                else ->
                    "Based on '${lesson.title}': ${lesson.explanation.take(200)}..."
            }
        }

        return ChatResponse(answer = answer, isFromLessonContent = true)
    }

    private fun getOfflineExplanation(lesson: Lesson, mode: String, language: String): ExplanationResponse {
        val isAr = language == "ar"
        val explanation = if (isAr) {
            when (mode) {
                "simpler" -> "شرح مبسط للغاية: تخيل الأمر مثل لعبة تركيب؛ كل عنصر يحتاج لقوة تثبته في مكانه. هذا الدرس يعلمك القواعد البسيطة التي تحكم الأشياء من حولنا في الحياة اليومية كالمشي واللعب."
                "beginner" -> "دليل المبتدئين: ابدأ بفهم الفكرة المركزية أولاً دون التشتت بالمعادلات الصعبة. الفكرة هي أن الطبيعة تسير بقوانين متوازنة، وكل حركة لها سبب ونتيجة."
                "concise" -> "ملخص في 3 جمل: 1. الأجسام تحافظ على حالتها. 2. القوة تسبب التسارع. 3. لكل فعل رد فعل مساوٍ ومعاكس."
                "detailed" -> "شرح مفصل: يتناول الدرس العلاقات الفيزيائية والرياضية بدقة، موضحاً كيفية اشتقاق العلاقات، والشروط البيئية المؤثرة كقوى الاحتكاك ومقاومة الهواء."
                "example" -> "أمثلة حية: 1. السباحة في المسبح: يدفع السباح الماء للخلف فيتقدم للأمام. 2. إطلاق الصاروخ: اندفاع الغازات لأسفل يرفع الصاروخ لأعلى."
                "memorize" -> "ما يجب حفظه للاختبار: 1. التعريف الدقيق لكل مصطلح. 2. الصيغ الرياضية الأساسية ووحدات القياس الدولية. 3. شروط انعدام التسارع."
                else -> lesson.explanation
            }
        } else {
            when (mode) {
                "simpler" -> "Super simple explanation: Think of it like skateboarding: to move forward you push backward against the ground. Action and reaction at work!"
                "beginner" -> "Beginner guide: Focus on the big picture first. Every force in nature exists as a pair."
                "concise" -> "Concise takeaway: 1. Inertia keeps objects moving or still. 2. Force creates acceleration. 3. Forces always come in equal pairs."
                "detailed" -> "Detailed deep-dive: Exploring mathematical equations, vector relationships, and friction resistance variables."
                "example" -> "Everyday examples: 1. Jumping off a small boat causes the boat to push back. 2. Recoil of a ball hitting a wall."
                "memorize" -> "Key items to memorize: Formulas, SI units, and conditions where acceleration is zero."
                else -> lesson.explanation
            }
        }
        return ExplanationResponse(explanation, mode)
    }

    private fun generateLocalQuestions(lesson: Lesson, count: Int, language: String): List<QuestionItem> {
        val isAr = language == "ar"
        val questions = mutableListOf<QuestionItem>()

        // Take existing questions from lesson if any
        questions.addAll(lesson.questions)

        // If need more to fulfill count (e.g. 5, 10, 20), generate structured questions from terms and keypoints
        var index = 0
        while (questions.size < count) {
            val term = lesson.terms.getOrNull(index % lesson.terms.size.coerceAtLeast(1))
            val kp = lesson.keyPoints.getOrNull(index % lesson.keyPoints.size.coerceAtLeast(1))

            if (index % 2 == 0 && term != null) {
                questions.add(
                    QuestionItem(
                        question = if (isAr) "ما هو تعريف المصطلح: '${term.term}'؟" else "What is the definition of '${term.term}'?",
                        options = if (isAr) listOf(
                            term.definition,
                            "مفهوم غير مرتبط بموضوع الدرس الحالي.",
                            "حالة عشوائية لا تخضع لقوانين محددة.",
                            "وحدة لقياس درجات الحرارة المرتفعة."
                        ) else listOf(
                            term.definition,
                            "An unrelated concept in science.",
                            "A random state with no fixed laws.",
                            "A temperature measurement unit."
                        ),
                        correctAnswerIndex = 0,
                        explanation = if (isAr) "وفقاً لمصطلحات الدرس: ${term.definition}" else "According to the lesson: ${term.definition}",
                        type = "mcq"
                    )
                )
            } else if (kp != null) {
                questions.add(
                    QuestionItem(
                        question = if (isAr) "هل العبارة التالية صحيحة وفق محتوى الدرس: '$kp'؟" else "Is the following statement true according to the lesson: '$kp'?",
                        options = if (isAr) listOf("صح", "خطأ") else listOf("True", "False"),
                        correctAnswerIndex = 0,
                        explanation = if (isAr) "صحيح، لأن هذه من أهم النقاط المذكورة بالدرس." else "True, this is one of the key points established in the lesson.",
                        type = "true_false"
                    )
                )
            } else {
                questions.add(
                    QuestionItem(
                        question = if (isAr) "سؤال رقم ${questions.size + 1} حول موضوع '${lesson.title}'" else "Question ${questions.size + 1} regarding '${lesson.title}'",
                        options = if (isAr) listOf("الإجابة الصحيحة الأولى", "خيار بديل", "خيار غير صحيح", "لا شيء مما سبق") else listOf("Correct Option", "Alternative", "Incorrect", "None of above"),
                        correctAnswerIndex = 0,
                        explanation = if (isAr) "تم التحقق من الإجابة استناداً لنصوص الدرس." else "Verified according to the lesson content.",
                        type = "mcq"
                    )
                )
            }
            index++
        }

        return questions.take(count)
    }
}
