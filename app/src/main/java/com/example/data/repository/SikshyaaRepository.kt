package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.GeminiRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import android.util.Log

// Representation of an MCQ question for the practice state
data class McqQuestion(
    val id: Int,
    val subject: String,
    val difficulty: String, // Beginner, Intermediate, Advanced, Expert
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val solutionExplanation: String,
    val isMedicalCase: Boolean = false,
    val imagePlaceholder: String? = null
)

// Representative Mock Exam Structure
data class MockExam(
    val id: String,
    val title: String,
    val category: String, // SEE, NEB, MBBS, Loksewa, Banking
    val durationMinutes: Int,
    val totalQuestions: Int,
    val passMarks: Int,
    val questions: List<McqQuestion>
)

class SikshyaaRepository(private val db: AppDatabase) {

    private val progressDao = db.userProgressDao()
    private val libraryDao = db.libraryItemDao()
    private val courseProgressDao = db.courseProgressDao()
    private val examResultDao = db.mockExamResultDao()
    private val chatDao = db.chatMessageDao()
    private val geminiSource = GeminiRemoteDataSource()

    // --- State Exposal ---
    val userProgressRef: Flow<UserProgressEntity?> = progressDao.getUserProgress()
    val allLibraryItems: Flow<List<LibraryItemEntity>> = libraryDao.getAllLibraryItems()
    val bookmarkedLibraryItems: Flow<List<LibraryItemEntity>> = libraryDao.getBookmarkedLibraryItems()
    val allCoursesProgress: Flow<List<CourseProgressEntity>> = courseProgressDao.getAllCourseProgress()
    val allExamResults: Flow<List<MockExamResultEntity>> = examResultDao.getAllExamResults()
    val allChatMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllChatMessages()

    // --- Local Operations ---
    suspend fun saveProgress(progress: UserProgressEntity) {
        progressDao.insertUserProgress(progress)
    }

    suspend fun updateLibraryItem(item: LibraryItemEntity) {
        libraryDao.updateLibraryItem(item)
    }

    suspend fun updateCourseProgress(course: CourseProgressEntity) {
        courseProgressDao.updateCourseProgress(course)
    }

    suspend fun insertExamResult(result: MockExamResultEntity) {
        examResultDao.insertExamResult(result)
    }

    suspend fun saveChatMessage(sender: String, message: String, mode: String): ChatMessageEntity {
        val entity = ChatMessageEntity(sender = sender, message = message, mode = mode)
        chatDao.insertChatMessage(entity)
        return entity
    }

    suspend fun clearChatHistory() {
        chatDao.clearHistory()
    }

    // --- AI Operations ---
    suspend fun askAiTutor(prompt: String, mode: String): String {
        val instruction = when (mode) {
            "Exam Mode" -> "You are in EXAM COACH MODE. Respond with highly focused study tips, key questions likely to be asked, and short checklist answers for quick memorization."
            "Quick Revision Mode" -> "You are in FLASH REVISION MODE. Summarize the user's questions into concise 3-bullet summaries, 1 formula, and 1 memory trick."
            "Viva Practice Mode" -> "You are in VIVA VOCE EXAMINER MODE. Ask the user 1 hard conceptual or clinical question next based on their topic of interest. Evaluate their response strictly and provide corrective pointers."
            "Interview Preparation Mode" -> "You are in JOB INTERVIEW COACH. Guide the user with professional communication standards, banking acts, administrative concepts, mock feedback, and state governance structures."
            else -> "You are the primary Sikshyaa Educational AI Tutor. Assist students preparing for Class 8-10, NEB +2, Loksewa (Civil Service), MBBS, and Nepalese Universities. Incorporate local resources, Nepalese legal acts, or syllabus references when useful. Keep answers highly structured with clear titles, equations, bullet points, or clinical notes."
        }
        val aiResponseText = geminiSource.getAiResponse(prompt, instruction)
        saveChatMessage("ai", aiResponseText, mode)
        return aiResponseText
    }

    // --- Database Seed Seeders ---
    suspend fun seedDatabaseIfEmpty() {
        try {
            // Seed User Progress if empty
            val currentUser = progressDao.getUserProgress().firstOrNull()
            if (currentUser == null) {
                progressDao.insertUserProgress(UserProgressEntity())
            }

            // Seed Library Items
            libraryDao.insertLibraryItems(getDefaultLibraryItems())

            // Seed Course Progress
            courseProgressDao.insertCourseProgressList(getDefaultCourseProgress())

            Log.d("SikshyaaRepository", "Database preloaded successfully with Mock PDF data and Syllabi tracker.")
        } catch (e: Exception) {
            Log.e("SikshyaaRepository", "Error during seeding database: ", e)
        }
    }

    // --- Memory / In-App Data Sets ---
    fun getCoursesByLevel(): Map<String, List<String>> {
        return mapOf(
            "SEE (Class 10)" to listOf("Compulsory Mathematics", "Science & Technology", "English Literature", "Nepali Byakaran", "Social Studies", "Computer Science", "Optional Mathematics"),
            "+2 Science" to listOf("Physics Part I & II", "Chemistry (Inorganic & Physical)", "Biology (Botany & Zoology)", "Calculus & Geometry"),
            "+2 Management" to listOf("Accountancy & Ledgering", "Micro/Macro Economics", "Business Studies"),
            "Bachelor Stream" to listOf("BBS Business Finance", "BSc General Science", "BIT Network Ethics", "BCA Software Engineering", "Engineering Basics"),
            "MBBS Preparation" to listOf("High Yield Gross Anatomy", "Physiology Systems", "Clinical Pharmacology", "Biochemistry Cycles", "General Pathology"),
            "Loksewa & Civil" to listOf("Nepali Constitution & Acts", "General Knowledge (GK)", "Quantitative Aptitude / IQ", "National Banking Regulation")
        )
    }

    private fun getDefaultLibraryItems(): List<LibraryItemEntity> {
        return listOf(
            LibraryItemEntity("pdf_neb_phys", "NEB +2 Physics Formula Companion", "Formula Sheets", "Physics", 22, "Dr. S. K. Dev", isDownloaded = true),
            LibraryItemEntity("pdf_see_math", "SEE Compulsory Math Past 5 Yr Solutions", "Solutions", "Mathematics", 120, "Academic Support Team Nepal", isDownloaded = true),
            LibraryItemEntity("pdf_lok_const", "Constitution of Nepal Fundamental Rights Guide", "Loksewa Materials", "Constitution", 18, "L. R. Bhandari", isDownloaded = false),
            LibraryItemEntity("pdf_mbbs_anat", "Gross Anatomy: Upper Limb High Value Cards", "Medical Notes", "Anatomy", 45, "Prof. Dr. J. P. Agrawal", isDownloaded = false),
            LibraryItemEntity("pdf_bbs_econ", "Microeconomics Fundamentals for BBS 1st Year", "Books", "Economics", 240, "TU Eco Dept", isDownloaded = false),
            LibraryItemEntity("pdf_see_sci", "SEE Chemistry Quick Formula Flashbook", "Notes", "Science", 15, "Sikshyaa Scientific Team", isDownloaded = true, isBookmarked = true)
        )
    }

    private fun getDefaultCourseProgress(): List<CourseProgressEntity> {
        return listOf(
            CourseProgressEntity("neb_phys_1", "+2 Science", "Physics", "Mechanics & Sound", 4, 8, 2, 4, 50),
            CourseProgressEntity("neb_chem_1", "+2 Science", "Chemistry", "Atomic Theory & Bonding", 1, 6, 1, 3, 20),
            CourseProgressEntity("see_math_1", "SEE (Class 10)", "Compulsory Mathematics", "Sets, Arithmetic & Algebra", 8, 8, 4, 4, 100),
            CourseProgressEntity("see_sci_1", "SEE (Class 10)", "Science & Technology", "Force & Gravity", 5, 10, 2, 5, 45),
            CourseProgressEntity("mbb_anat_1", "MBBS Preparation", "Anatomy", "Osteology of Thorax", 0, 12, 0, 6, 0),
            CourseProgressEntity("lok_gk_1", "Loksewa & Civil", "General Knowledge (GK)", "Geography of Nepal & Districts", 6, 12, 3, 5, 52)
        )
    }

    // Mock MCQs dataset
    val practiceQuestions: List<McqQuestion> = listOf(
        // Physics
        McqQuestion(101, "Physics", "Beginner", "According to Newton's Second Law of Motion, force is directly proportional to what?", listOf("Velocity", "Rate of change of momentum", "Displacement", "Inertia"), 1, "Force equals rate of change of momentum (F = dp/dt). If mass is constant, F = ma."),
        McqQuestion(102, "Physics", "Intermediate", "The value of acceleration due to gravity (g) at the center of the Earth is:", listOf("9.8 m/s²", "Infinite", "Zero", "11.2 m/s²"), 2, "At the center of the earth, gravity components pull in all directions equally, resulting in a net gravitational force (and thus g) of zero."),
        McqQuestion(103, "Physics", "Advanced", "What is the escape velocity from the surface of Earth?", listOf("11.2 km/s", "8.0 km/s", "9.8 km/s", "1.12 km/s"), 0, "Escape velocity on Earth is approximately 11.2 km/s (or 25,000 mph)."),
        McqQuestion(104, "Physics", "Expert", "In a thermodynamic process, if the volume of an ideal gas remains constant, the process is called:", listOf("Isobaric", "Isothermal", "Isochoric", "Adiabatic"), 2, "An isochoric process is one in which the volume of the system remains constant (dV = 0). No boundary work is done."),

        // Anatomy (MBBS Prep)
        McqQuestion(201, "Anatomy", "Beginner", "Which bone is commonly known as the collarbone?", listOf("Scapula", "Sternum", "Clavicle", "Humerus"), 2, "The clavicle is a long bone that serves as a strut between the scapula and the sternum. It is commonly called the collarbone."),
        McqQuestion(202, "Anatomy", "Intermediate", "How many cervical vertebrae are present in the human spine?", listOf("5", "7", "12", "8"), 1, "There are exactly 7 cervical vertebrae (C1-C7) in almost all mammals, including humans."),
        McqQuestion(203, "Anatomy", "Advanced", "Which cranial nerve innervates the muscles of mastication?", listOf("CN V (Trigeminal)", "CN VII (Facial)", "CN IX (Glossopharyngeal)", "CN XII (Hypoglossal)"), 0, "The mandibular division of CN V (Trigeminal Nerve) innervates the muscles of mastication (chewing)."),
        McqQuestion(204, "Anatomy", "Expert", "A patient presents with 'winging of scapula'. Damage to which nerve is most likely responsible?", listOf("Thoracodorsal nerve", "Long thoracic nerve", "Axillary nerve", "Suprascapular nerve"), 1, "The serratus anterior muscle is innervates by the long thoracic nerve. Damage causes the scapula to protrude, resembling a wing.", isMedicalCase = true),

        // Loksewa (GK & IQ/Constitution)
        McqQuestion(301, "General Knowledge", "Beginner", "Under the Constitution of Nepal, which Article guarantees the Right to Education?", listOf("Article 16", "Article 24", "Article 31", "Article 48"), 2, "Article 31 of the Constitution of Nepal guarantees the right to compulsory and free education up to the basic level and secondary level education from the state."),
        McqQuestion(302, "General Knowledge", "Intermediate", "In which Nepalese district does the famous Rara Lake lie?", listOf("Mustang", "Mugu", "Dolpa", "Humla"), 1, "Rara Lake, the largest lake of Nepal, is situated in Mugu district of Karnali Province."),
        McqQuestion(303, "General Knowledge", "Advanced", "State standard ratio: If 20 people can dig a trench in 6 days, how many people are required to dig it in 4 days?", listOf("15 people", "24 people", "30 people", "40 people"), 2, "Man-days remain equal. 20 * 6 = 120 man-days. If completed in 4 days: 120 / 4 = 30 people."),
        McqQuestion(304, "General Knowledge", "Expert", "Which Nepalese prime minister signed the Sugauli Treaty on behalf of Nepal?", listOf("Bhimsen Thapa", "Gajraj Mishra", "Chandra Shamsher", "Rajendra Bikram Shah"), 1, "Although Bhimsen Thapa was PM, Gajraj Mishra and Chandra Shekhar Upadhyaya signed the historic Sugauli Treaty in Dec 1815/Mar 1816.")
    )

    // Full Mock Exam Modules
    val mockExams: List<MockExam> = listOf(
        MockExam(
            id = "mock_see_sci",
            title = "SEE National Science & Tech Mock Drill",
            category = "SEE (Class 10)",
            durationMinutes = 15,
            totalQuestions = 3,
            passMarks = 40,
            questions = listOf(
                McqQuestion(1, "Science", "Beginner", "What is the force of attraction between any two bodies due to their masses?", listOf("Magnetic Force", "Electrostatic Force", "Gravitational Force", "Frictional Force"), 2, "Sir Isaac Netwon formulated that gravity attracts all objects based on mass: F = G*(m1*m2)/d²."),
                McqQuestion(2, "Science", "Intermediate", "Which element is commonly used as a moderator in nuclear reactors?", listOf("Uranium-235", "Heavy water", "Plutonium", "Helium gas"), 1, "Heavy water (D2O) slows down neutrons securely so they split further atoms efficiently without excessive heat explosion."),
                McqQuestion(3, "Science", "Advanced", "Which division of plants is known as the 'amphibians of the plant kingdom'?", listOf("Thallophyta", "Pteridophyta", "Bryophyta", "Gymnosperms"), 2, "Bryophytes (mosses and liverworts) need soil to anchor but depend strictly on external water films for sexual reproduction.")
            )
        ),
        MockExam(
            id = "mock_mbbs_prep",
            title = "NEB & IOM Integrated MBBS Mock Exam",
            category = "MBBS Preparation",
            durationMinutes = 15,
            totalQuestions = 3,
            passMarks = 50,
            questions = listOf(
                McqQuestion(11, "Medical", "Expert", "A 45-year-old male exhibits a standard foot drop with loss of sensation on the dorsum of his foot. Which nerve is compressed?", listOf("Tibial Nerve", "Deep Peroneal Nerve", "Common Peroneal Nerve", "Sciatic Trunk"), 2, "Common peroneal nerve divides around the fibular neck. Damage leads to paralysis of anterior compartment muscles (foot drop) and sensory deficit.", isMedicalCase = true),
                McqQuestion(12, "Medical", "Intermediate", "Which vitamin acts as a crucial cofactor in blood coagulation cascade synthesis?", listOf("Vitamin A", "Vitamin C", "Vitamin K", "Vitamin E"), 2, "Vitamin K is essential for gamma-carboxylation of clotting factors II, VII, IX, and X in the liver."),
                McqQuestion(13, "Medical", "Advanced", "The arterial blood gas (ABG) report of a diabetic patient shows pH 7.21, decreased bicarbonate, and decreased pCO2. What is this state?", listOf("Respiratory Acidosis", "Metabolic Acidosis with Compensation", "Metabolic Alkalosis", "Respiratory Alkalosis"), 1, "pH < 7.35 implies acidosis; low bicarbonate means metabolic cause; decreased pCO2 marks hyperventilation compensation.")
            )
        ),
        MockExam(
            id = "mock_loksewa",
            title = "Loksewa Section Officer IQ & GK Mock Drill",
            category = "Loksewa & Civil",
            durationMinutes = 15,
            totalQuestions = 3,
            passMarks = 40,
            questions = listOf(
                McqQuestion(21, "Loksewa", "Beginner", "Who is the executive head of the Nepalese state according to the Constitution?", listOf("The President", "The Chief Justice", "The Prime Minister", "The Speaker of House"), 2, "Article 75 vests executive authority of Nepal in the Council of Ministers, headed by the Prime Minister."),
                McqQuestion(22, "Loksewa", "Intermediate", "Number Series Puzzle: What completes 2, 6, 12, 20, 30, __?", listOf("36", "40", "42", "48"), 2, "The difference increases sequentially: +4, +6, +8, +10. Next serves +12, making 30 + 12 = 42."),
                McqQuestion(23, "Loksewa", "Advanced", "Which district in Nepal is famous for apples, also nicknamed 'the district across the Himalayas'?", listOf("Manang", "Mustang", "Jumla", "Solukhumbu"), 1, "Mustang is highly famous for delicious apples (especially Marpha) and lies in the rain shadow area behind the grand Annapurna Himalayas.")
            )
        )
    )
}
