package com.example.ui.i18n

object Strings {
    fun get(key: String, lang: String = "ar"): String {
        val isAr = lang == "ar"
        return when (key) {
            // App
            "app_name" -> if (isAr) "StudyAI" else "StudyAI"
            "tagline" -> if (isAr) "مساعد دراستك الذكي" else "Your AI Study Companion"

            // Onboarding
            "onboard_title_1" -> if (isAr) "📸 صوّر أي درس أو كتاب" else "📸 Snap Any Book or Lesson"
            "onboard_desc_1" -> if (isAr) "التقط صورة لصفحة كتابك المدرسي أو دفترك أو ارفع صورة وسيقوم الذكاء الاصطناعي بتحليلها فوراً." else "Capture a photo of your textbook or notes, or upload an image for instant AI understanding."
            "onboard_title_2" -> if (isAr) "💡 شرح مبسط وملخصات ذكية" else "💡 Simplified Explanations & Summaries"
            "onboard_desc_2" -> if (isAr) "احصل على شرح سهل بأمثلة من واقع الحياة، ملخصات شاملة، أهم النقاط، وبطاقات مراجعة تفاعلية." else "Get easy explanations with everyday analogies, comprehensive summaries, and interactive flashcards."
            "onboard_title_3" -> if (isAr) "🧠 اختبر نفسك وتابع تقدمك" else "🧠 Test Yourself & Track Progress"
            "onboard_desc_3" -> if (isAr) "أنشئ اختبارات ذكية تتكيف مع مستواك لتقوية نقاط ضعفك ومتابعة تقدمك يوماً بعد يوم." else "Generate smart adaptive quizzes tailored to your level to master weak spots and track daily growth."
            "btn_start" -> if (isAr) "ابدأ الدراسة الآن" else "Start Studying Now"
            "btn_skip" -> if (isAr) "تخطي" else "Skip"
            "btn_next" -> if (isAr) "التالي" else "Next"

            // Auth
            "auth_title_login" -> if (isAr) "تسجيل الدخول إلى حسابك" else "Log In to StudyAI"
            "auth_title_register" -> if (isAr) "إنشاء حساب طالب جديد" else "Create New Student Account"
            "auth_name" -> if (isAr) "اسم الطالب" else "Student Name"
            "auth_email" -> if (isAr) "البريد الإلكتروني" else "Email Address"
            "auth_password" -> if (isAr) "كلمة المرور" else "Password"
            "btn_login" -> if (isAr) "دخول" else "Log In"
            "btn_register" -> if (isAr) "إنشاء حساب" else "Register"
            "auth_switch_to_register" -> if (isAr) "ليس لديك حساب؟ سجّل الآن" else "Don't have an account? Register"
            "auth_switch_to_login" -> if (isAr) "لديك حساب بالفعل؟ سجّل دخولك" else "Already have an account? Log in"
            "btn_guest" -> if (isAr) "دخول سريع كطالب ضيف" else "Quick Student Demo Sign-In"

            // Home
            "greeting" -> if (isAr) "أهلاً بك يا %s 👋" else "Welcome, %s 👋"
            "home_sub" -> if (isAr) "ما الذي تود دراسته ومراجعته اليوم؟" else "What would you like to study today?"
            "action_camera" -> if (isAr) "📸 صوّر درسًا" else "📸 Snap Lesson"
            "action_gallery" -> if (isAr) "📁 اختر صورة" else "📁 Choose Image"
            "action_text" -> if (isAr) "✍️ اكتب سؤالك" else "✍️ Enter Text"
            "action_quiz" -> if (isAr) "🧠 اختبرني" else "🧠 Quiz Me"
            "recent_lessons" -> if (isAr) "آخر دروسك" else "Recent Lessons"
            "no_recent_lessons" -> if (isAr) "لا توجد دروس بعد. صوّر صفحة كتابك أو اكتب موضوعاً لتبدأ!" else "No lessons yet. Snap a textbook page or enter a topic to begin!"
            "streak_card_title" -> if (isAr) "سلسلة أيام الدراسة" else "Study Streak"
            "streak_days" -> if (isAr) "%d أيام 🔥" else "%d Days 🔥"
            "stat_quizzes" -> if (isAr) "الاختبارات المنجزة" else "Quizzes Done"
            "stat_avg" -> if (isAr) "متوسط النتيجة" else "Average Score"
            "stat_lessons" -> if (isAr) "الدروس المدروسة" else "Lessons Studied"

            // Analysis
            "analyzing_title" -> if (isAr) "جاري تحليل الدرس..." else "Analyzing lesson..."
            "analyzing_subtitle" -> if (isAr) "يقوم الذكاء الاصطناعي باستخراج النص والمفاهيم وتوليد الملخص والأسئلة" else "AI is extracting content, synthesizing concepts, and crafting your quiz"
            "retake_photo" -> if (isAr) "إعادة التقاط الصورة" else "Retake Photo"
            "continue_analysis" -> if (isAr) "متابعة التحليل 🚀" else "Proceed with Analysis 🚀"
            "manual_input_title" -> if (isAr) "اكتب سؤالك أو موضوع الدرس" else "Enter your question or study topic"
            "manual_input_hint" -> if (isAr) "مثال: اشرح لي قانون نيوتن الأول، أو انسخ نصاً من كتابك هنا..." else "e.g., Explain photosynthesis, or paste text from your book..."
            "btn_analyze" -> if (isAr) "تحليل وشرح الآن" else "Analyze & Explain Now"

            // Lesson Result Tabs
            "tab_explanation" -> if (isAr) "📖 شرح مبسط" else "📖 Explanation"
            "tab_summary" -> if (isAr) "📝 الملخص" else "📝 Summary"
            "tab_keypoints" -> if (isAr) "📌 أهم النقاط" else "📌 Key Points"
            "tab_terms" -> if (isAr) "💡 مصطلحات" else "💡 Terms"
            "tab_qa" -> if (isAr) "❓ أسئلة وأجوبة" else "❓ Q & A"
            "tab_flashcards" -> if (isAr) "🗂️ بطاقات مراجعة" else "🗂️ Flashcards"
            "btn_explain_more" -> if (isAr) "💡 اشرح لي بطريقة أبسط / خيارات الشرح" else "💡 Explain More / Modes"
            "btn_chat_lesson" -> if (isAr) "💬 محادثة مع الدرس" else "💬 Chat With Lesson"
            "btn_quiz_lesson" -> if (isAr) "🧠 ابدأ اختباراً على هذا الدرس" else "🧠 Start Lesson Quiz"
            "save_lesson_success" -> if (isAr) "تم حفظ الدرس بنجاح في مراجعاتك!" else "Lesson saved successfully to reviews!"
            "delete_lesson" -> if (isAr) "حذف الدرس" else "Delete Lesson"

            // Explain More sheet
            "mode_simpler" -> if (isAr) "💡 اشرح لي بطريقة أبسط مع أمثلة واقعية" else "💡 Simpler explanation with real-life analogies"
            "mode_beginner" -> if (isAr) "🌱 شرح للمبتدئين خطوة بخطوة" else "🌱 Beginner guide step-by-step"
            "mode_concise" -> if (isAr) "⚡ شرح مختصر في 3 جمل" else "⚡ Ultra-concise in 3 sentences"
            "mode_detailed" -> if (isAr) "🔍 شرح بالتفصيل المعمّق" else "🔍 In-depth comprehensive breakdown"
            "mode_example" -> if (isAr) "🎯 أعطني أمثلة عملية من الحياة" else "🎯 Concrete everyday practical examples"
            "mode_memorize" -> if (isAr) "⭐ ما الذي يجب أن أحفظه للاختبار؟" else "⭐ What must I memorize for the test?"

            // Chat
            "chat_title" -> if (isAr) "محادثة حول: %s" else "Chatting: %s"
            "chat_hint" -> if (isAr) "اسأل أي سؤال حول الدرس (مثلاً: ما الفرق بين X و Y؟)" else "Ask anything about this lesson..."
            "chat_source_lesson" -> if (isAr) "مستند مباشرة لمحتوى الدرس 📘" else "Directly from lesson text 📘"
            "chat_source_general" -> if (isAr) "معرفة عامة مكملة للدرس 🌐" else "General contextual knowledge 🌐"
            "chat_starter_1" -> if (isAr) "لماذا حدث هذا؟" else "Why did this happen?"
            "chat_starter_2" -> if (isAr) "أعطني مثالاً تطبيقياً." else "Give me a practical example."
            "chat_starter_3" -> if (isAr) "اشرح لي أهم نقطة في هذا الدرس." else "Explain the most critical point."

            // Quizzes
            "quiz_title" -> if (isAr) "اختبار: %s" else "Quiz: %s"
            "quiz_setup_title" -> if (isAr) "إعداد الاختبار" else "Quiz Setup"
            "select_question_count" -> if (isAr) "عدد الأسئلة:" else "Question Count:"
            "count_5" -> if (isAr) "5 أسئلة" else "5 Questions"
            "count_10" -> if (isAr) "10 أسئلة" else "10 Questions"
            "count_20" -> if (isAr) "20 سؤالاً" else "20 Questions"
            "select_types" -> if (isAr) "أنواع الأسئلة:" else "Question Types:"
            "type_mcq" -> if (isAr) "اختيار من متعدد" else "Multiple Choice"
            "type_tf" -> if (isAr) "صح أو خطأ" else "True / False"
            "btn_start_quiz" -> if (isAr) "ابدأ الاختبار الآن 🏁" else "Start Quiz Now 🏁"
            "smart_quiz_btn" -> if (isAr) "🎯 اختبرني بذكاء (الوضع التكيفي)" else "🎯 Smart Adaptive Quiz"
            "smart_quiz_desc" -> if (isAr) "يتكيف الذكاء الاصطناعي مع مستواك: يزيد الصعوبة عند تفوقك ويسهل الأسئلة ويشرح فوراً عند الخطأ." else "AI adapts dynamically: raises difficulty on streaks, simplifies and explains on mistakes."
            "question_progress" -> if (isAr) "سؤال %d من %d" else "Question %d of %d"
            "confirm_answer" -> if (isAr) "تأكيد الإجابة" else "Submit Answer"
            "next_question" -> if (isAr) "السؤال التالي" else "Next Question"
            "finish_quiz" -> if (isAr) "إنهاء الاختبار وعرض النتيجة" else "Finish Quiz & View Score"

            // Quiz Result
            "quiz_result_title" -> if (isAr) "نتيجة الاختبار" else "Quiz Results"
            "score_label" -> if (isAr) "الدرجة المستحقة" else "Your Score"
            "correct_answers" -> if (isAr) "الإجابات الصحيحة" else "Correct Answers"
            "wrong_answers" -> if (isAr) "الإجابات الخاطئة" else "Wrong Answers"
            "time_spent" -> if (isAr) "الوقت المستغرق" else "Duration"
            "weak_points" -> if (isAr) "نقاط تحتاج إلى مراجعة" else "Topics Needing Review"
            "retry_weak" -> if (isAr) "🔄 إعادة اختبار المواضيع الضعيفة" else "🔄 Retest Weak Topics"
            "eval_excellent" -> if (isAr) "ممتاز! ⭐ أداء رائع وفهم متميز" else "Excellent! ⭐ Outstanding Mastery"
            "eval_very_good" -> if (isAr) "جيد جداً 👍 أنت على الطريق الصحيح" else "Very Good 👍 You are on the right track"
            "eval_needs_review" -> if (isAr) "يحتاج مراجعة 📚 راجع الملخص وأعد المحاولة" else "Needs Review 📚 Review summary & retry"

            // Review Screen
            "nav_home" -> if (isAr) "الرئيسية" else "Home"
            "nav_review" -> if (isAr) "مراجعاتي" else "Reviews"
            "nav_progress" -> if (isAr) "تقدمي" else "Progress"
            "nav_profile" -> if (isAr) "حسابي" else "Profile"
            "review_tab_lessons" -> if (isAr) "الدروس المحفوظة" else "Saved Lessons"
            "review_tab_quizzes" -> if (isAr) "الاختبارات السابقة" else "Past Quizzes"
            "review_tab_flashcards" -> if (isAr) "بطاقات المراجعة" else "Flashcards"
            "search_hint" -> if (isAr) "ابحث في دروسك ومفاهيمك..." else "Search your lessons & topics..."
            "flip_card_hint" -> if (isAr) "المس البطاقة لقلبها ومشاهدة الإجابة" else "Tap card to flip and view answer"

            // Progress Screen
            "progress_title" -> if (isAr) "📊 تقرير تقدمك الدراسي" else "📊 Your Study Progress"
            "total_quizzes" -> if (isAr) "إجمالي الاختبارات" else "Total Quizzes"
            "avg_score_bar" -> if (isAr) "متوسط درجاتك العامة" else "Overall Average Score"
            "frequent_mistakes" -> if (isAr) "المواضيع التي تحتاج تركيزاً أكثر" else "Topics Requiring Focus"
            "streak_banner" -> if (isAr) "لقد حافظت على دراستك المتواصلة! استمر في التعلم 🔥" else "You kept your study streak alive! Keep learning 🔥"

            // Profile & Settings
            "profile_title" -> if (isAr) "الملف الشخصي والإعدادات" else "Profile & Settings"
            "current_plan" -> if (isAr) "الخطة الحالية:" else "Current Plan:"
            "plan_free" -> if (isAr) "المجانية (Free)" else "Free Plan"
            "plan_pro" -> if (isAr) "الاحترافية (Pro ⭐)" else "Pro Plan ⭐"
            "upgrade_to_pro" -> if (isAr) "الترقية إلى StudyAI Pro 🚀" else "Upgrade to StudyAI Pro 🚀"
            "setting_lang" -> if (isAr) "اللغة (Language)" else "Language (اللغة)"
            "setting_theme" -> if (isAr) "المظهر (Theme)" else "Theme (المظهر)"
            "theme_system" -> if (isAr) "تلقائي حسب النظام" else "System Default"
            "theme_light" -> if (isAr) "الوضع الفاتح" else "Light Mode"
            "theme_dark" -> if (isAr) "الوضع الليلي" else "Dark Mode"
            "setting_notifications" -> if (isAr) "إشعارات التذكير اليومية" else "Daily Study Reminders"
            "privacy_policy" -> if (isAr) "سياسة الخصوصية" else "Privacy Policy"
            "terms_of_service" -> if (isAr) "شروط الخدمة" else "Terms of Service"
            "logout" -> if (isAr) "تسجيل الخروج" else "Log Out"

            // Usage Limits & Pro Modal
            "limit_modal_title" -> if (isAr) "بلغت الحد اليومي المجاني" else "Daily Free Limit Reached"
            "pro_feature_1" -> if (isAr) "تحليلات صور غير محدودة يومياً" else "Unlimited image analyses daily"
            "pro_feature_2" -> if (isAr) "اختبارات وتدريبات تكيفية غير محدودة" else "Unlimited adaptive smart quizzes"
            "pro_feature_3" -> if (isAr) "محادثات مطولة غير محدودة مع المدرس الذكي" else "Unlimited in-depth AI tutor chat"
            "pro_feature_4" -> if (isAr) "حفظ غير محدود للدروس وبطاقات المراجعة" else "Unlimited saved lessons & flashcards"
            "pro_feature_5" -> if (isAr) "أولوية وسرعة فائقة في معالجة طلبات AI" else "Priority ultra-fast AI processing"
            "btn_upgrade_now" -> if (isAr) "اشترك في Pro الآن (تجربة مجانية)" else "Upgrade to Pro (Free Trial)"
            "btn_cancel" -> if (isAr) "إغلاق" else "Close"

            // Offline & Errors
            "no_internet_msg" -> if (isAr) "لا يوجد اتصال بالإنترنت. تحقق من اتصالك وحاول مرة أخرى." else "No internet connection. Please check your network and try again."

            else -> key
        }
    }
}
