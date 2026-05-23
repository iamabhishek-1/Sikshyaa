package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CourseProgressEntity
import com.example.data.local.LibraryItemEntity
import com.example.data.local.MockExamResultEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.McqQuestion
import com.example.data.repository.MockExam
import com.example.data.repository.SikshyaaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class SikshyaaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SikshyaaRepository

    // --- Core Reactive Flows ---
    val userProgress: StateFlow<UserProgressEntity?>
    val libraryItems: StateFlow<List<LibraryItemEntity>>
    val coursesProgress: StateFlow<List<CourseProgressEntity>>
    val examResults: StateFlow<List<MockExamResultEntity>>
    val chatHistory: StateFlow<List<ChatMessageEntity>>

    // --- Dark Theme Toggle (Light mode by default for eye comfort) ---
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SikshyaaRepository(database)

        userProgress = repository.userProgressRef.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        libraryItems = repository.allLibraryItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        coursesProgress = repository.allCoursesProgress.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        examResults = repository.allExamResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        chatHistory = repository.allChatMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data on startup
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // --- Onboarding / Student Profile ---
    fun updateStudentProfile(name: String, studentClass: String, schoolCollege: String, lang: String, target: String) {
        viewModelScope.launch {
            val current = userProgress.value ?: UserProgressEntity()
            val updated = current.copy(
                name = name,
                studentClass = studentClass,
                schoolCollege = schoolCollege,
                preferredLanguage = lang,
                targetExams = target
            )
            repository.saveProgress(updated)
        }
    }

    // Increment STUDY TIME
    fun addStudyMinutes(mins: Int) {
        viewModelScope.launch {
            val current = userProgress.value ?: UserProgressEntity()
            val updated = current.copy(
                studyTimeMinutes = current.studyTimeMinutes + mins,
                totalXp = current.totalXp + (mins * 5)
            )
            repository.saveProgress(updated)
        }
    }

    // --- Course / Lesson Completions ---
    fun completeLessonVideo(courseId: String) {
        viewModelScope.launch {
            val courses = coursesProgress.value
            val course = courses.find { it.courseId == courseId } ?: return@launch
            if (course.videosCompleted < course.totalVideos) {
                val updatedVideos = course.videosCompleted + 1
                val totalSteps = course.totalVideos + course.totalNotes
                val completedSteps = updatedVideos + course.notesRead
                val percent = ((completedSteps.toFloat() / totalSteps.toFloat()) * 100).toInt()

                val updatedCourse = course.copy(
                    videosCompleted = updatedVideos,
                    completedPercent = percent
                )
                repository.updateCourseProgress(updatedCourse)

                // Update XP
                val currentProgress = userProgress.value ?: UserProgressEntity()
                repository.saveProgress(currentProgress.copy(
                    totalXp = currentProgress.totalXp + 50,
                    completedLessons = currentProgress.completedLessons + 1
                ))
            }
        }
    }

    fun completeNotesRead(courseId: String) {
        viewModelScope.launch {
            val courses = coursesProgress.value
            val course = courses.find { it.courseId == courseId } ?: return@launch
            if (course.notesRead < course.totalNotes) {
                val updatedNotes = course.notesRead + 1
                val totalSteps = course.totalVideos + course.totalNotes
                val completedSteps = course.videosCompleted + updatedNotes
                val percent = ((completedSteps.toFloat() / totalSteps.toFloat()) * 100).toInt()

                val updatedCourse = course.copy(
                    notesRead = updatedNotes,
                    completedPercent = percent
                )
                repository.updateCourseProgress(updatedCourse)

                // Update XP
                val currentProgress = userProgress.value ?: UserProgressEntity()
                repository.saveProgress(currentProgress.copy(
                    totalXp = currentProgress.totalXp + 30
                ))
            }
        }
    }

    // --- Practice MCQ Session ---
    private val _practiceSubject = MutableStateFlow("Physics")
    val practiceSubject: StateFlow<String> = _practiceSubject.asStateFlow()

    private val _practiceDifficulty = MutableStateFlow("Beginner")
    val practiceDifficulty: StateFlow<String> = _practiceDifficulty.asStateFlow()

    private val _currentPracticeIndex = MutableStateFlow(0)
    val currentPracticeIndex: StateFlow<Int> = _currentPracticeIndex.asStateFlow()

    private val _selectedPracticeOption = MutableStateFlow<Int?>(null)
    val selectedPracticeOption: StateFlow<Int?> = _selectedPracticeOption.asStateFlow()

    private val _practiceAnswerRevealed = MutableStateFlow(false)
    val practiceAnswerRevealed: StateFlow<Boolean> = _practiceAnswerRevealed.asStateFlow()

    fun setPracticeFilter(subject: String, difficulty: String) {
        _practiceSubject.value = subject
        _practiceDifficulty.value = difficulty
        _currentPracticeIndex.value = 0
        _selectedPracticeOption.value = null
        _practiceAnswerRevealed.value = false
    }

    fun getFilteredPracticeQuestions(): List<McqQuestion> {
        return repository.practiceQuestions.filter {
            it.subject.contains(practiceSubject.value, ignoreCase = true) &&
            it.difficulty.equals(practiceDifficulty.value, ignoreCase = true)
        }.ifEmpty {
            repository.practiceQuestions.filter { it.subject.contains(practiceSubject.value, ignoreCase = true) }
        }
    }

    fun selectPracticeOption(optionIndex: Int) {
        if (_practiceAnswerRevealed.value) return
        _selectedPracticeOption.value = optionIndex
        _practiceAnswerRevealed.value = true

        // Update score stats
        viewModelScope.launch {
            val questions = getFilteredPracticeQuestions()
            val currentQ = questions.getOrNull(_currentPracticeIndex.value) ?: return@launch
            val isCorrect = optionIndex == currentQ.correctIndex

            val currentProgress = userProgress.value ?: UserProgressEntity()
            val totalQuestions = currentProgress.completedQuestions + 1
            val correctTotal = if (isCorrect) (currentProgress.completedQuestions * currentProgress.accuracyRate).toInt() + 1
                               else (currentProgress.completedQuestions * currentProgress.accuracyRate).toInt()

            val updatedXp = if (isCorrect) currentProgress.totalXp + 15 else currentProgress.totalXp + 5
            val updatedProgress = currentProgress.copy(
                completedQuestions = totalQuestions,
                accuracyRate = correctTotal.toFloat() / totalQuestions.toFloat(),
                totalXp = updatedXp,
                streak = if (isCorrect) currentProgress.streak + 1 else currentProgress.streak
            )
            repository.saveProgress(updatedProgress)
        }
    }

    fun nextPracticeQuestion() {
        val questions = getFilteredPracticeQuestions()
        if (_currentPracticeIndex.value < questions.size - 1) {
            _currentPracticeIndex.value += 1
            _selectedPracticeOption.value = null
            _practiceAnswerRevealed.value = false
        } else {
            // cycle to initial
            _currentPracticeIndex.value = 0
            _selectedPracticeOption.value = null
            _practiceAnswerRevealed.value = false
        }
    }

    // --- Mock Exam Session Engine ---
    val availableMockExams: List<MockExam> = repository.mockExams

    private val _activeMockExam = MutableStateFlow<MockExam?>(null)
    val activeMockExam: StateFlow<MockExam?> = _activeMockExam.asStateFlow()

    private val _currentMockIndex = MutableStateFlow(0)
    val currentMockIndex: StateFlow<Int> = _currentMockIndex.asStateFlow()

    private val _mockSelectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // Q index -> Selected Option index
    val mockSelectedAnswers: StateFlow<Map<Int, Int>> = _mockSelectedAnswers.asStateFlow()

    private val _examTimeRemaining = MutableStateFlow(0)
    val examTimeRemaining: StateFlow<Int> = _examTimeRemaining.asStateFlow()

    private val _isExamFinished = MutableStateFlow(false)
    val isExamFinished: StateFlow<Boolean> = _isExamFinished.asStateFlow()

    private var examTimer: Timer? = null

    fun startMockExam(examId: String) {
        val exam = availableMockExams.find { it.id == examId } ?: return
        _activeMockExam.value = exam
        _currentMockIndex.value = 0
        _mockSelectedAnswers.value = emptyMap()
        _examTimeRemaining.value = exam.durationMinutes * 60
        _isExamFinished.value = false

        examTimer?.cancel()
        examTimer = Timer()
        examTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (_examTimeRemaining.value > 0 && !_isExamFinished.value) {
                    _examTimeRemaining.value -= 1
                } else {
                    submitMockExam()
                }
            }
        }, 1000L, 1000L)
    }

    fun answerMockQuestion(optionIndex: Int) {
        val answers = _mockSelectedAnswers.value.toMutableMap()
        answers[_currentMockIndex.value] = optionIndex
        _mockSelectedAnswers.value = answers
    }

    fun nextMockQuestion() {
        val exam = _activeMockExam.value ?: return
        if (_currentMockIndex.value < exam.questions.size - 1) {
            _currentMockIndex.value += 1
        }
    }

    fun prevMockQuestion() {
        if (_currentMockIndex.value > 0) {
            _currentMockIndex.value -= 1
        }
    }

    fun submitMockExam() {
        examTimer?.cancel()
        if (_isExamFinished.value) return
        val exam = _activeMockExam.value ?: return
        _isExamFinished.value = true

        val selected = _mockSelectedAnswers.value
        var correctCount = 0
        exam.questions.forEachIndexed { idx, q ->
            val ans = selected[idx]
            if (ans != null && ans == q.correctIndex) {
                correctCount += 1
            }
        }

        val percentage = (correctCount.toFloat() / exam.questions.size.toFloat()) * 100
        val durationSecondsUsed = (exam.durationMinutes * 60) - _examTimeRemaining.value

        viewModelScope.launch {
            val result = MockExamResultEntity(
                examName = exam.title,
                category = exam.category,
                score = correctCount,
                totalQuestions = exam.questions.size,
                accuracy = correctCount.toFloat() / exam.questions.size.toFloat(),
                durationSeconds = durationSecondsUsed
            )
            repository.insertExamResult(result)

            // Update student profile stats
            val currentProgress = userProgress.value ?: UserProgressEntity()
            val totalXpEarned = 100 + (correctCount * 30)
            repository.saveProgress(currentProgress.copy(
                totalXp = currentProgress.totalXp + totalXpEarned
            ))
        }
    }

    fun exitMockExam() {
        examTimer?.cancel()
        _activeMockExam.value = null
        _isExamFinished.value = false
        _mockSelectedAnswers.value = emptyMap()
    }

    // --- AI Chat Tutor Actions ---
    private val _aiTutorMode = MutableStateFlow("Regular")
    val aiTutorMode: StateFlow<String> = _aiTutorMode.asStateFlow()

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    private val _vocalFeedbackSimulated = MutableStateFlow(false)
    val vocalFeedbackSimulated: StateFlow<Boolean> = _vocalFeedbackSimulated.asStateFlow()

    fun setAiMode(mode: String) {
        _aiTutorMode.value = mode
    }

    fun sendTutorMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // Save user chat locally
            repository.saveChatMessage("user", text, _aiTutorMode.value)
            _isAiTyping.value = true

            // Generate responses via Gemini remote datasource
            repository.askAiTutor(text, _aiTutorMode.value)
            _isAiTyping.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    fun triggerVoiceFeedback() {
        _vocalFeedbackSimulated.value = !_vocalFeedbackSimulated.value
    }

    // --- Library Notes & PDFs Actions ---
    private val _librarySearchText = MutableStateFlow("")
    val librarySearchText: StateFlow<String> = _librarySearchText.asStateFlow()

    private val _selectedLibraryItem = MutableStateFlow<LibraryItemEntity?>(null)
    val selectedLibraryItem: StateFlow<LibraryItemEntity?> = _selectedLibraryItem.asStateFlow()

    private val _pdfReadingPage = MutableStateFlow(1)
    val pdfReadingPage: StateFlow<Int> = _pdfReadingPage.asStateFlow()

    fun updateSearchText(query: String) {
        _librarySearchText.value = query
    }

    fun toggleBookmark(item: LibraryItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(isBookmarked = !item.isBookmarked)
            repository.updateLibraryItem(updated)
            if (_selectedLibraryItem.value?.id == item.id) {
                _selectedLibraryItem.value = updated
            }
        }
    }

    fun simulatePdfDownload(item: LibraryItemEntity) {
        viewModelScope.launch {
            // Simulate brief modern loading delay
            val updated = item.copy(isDownloaded = true)
            delay(1200)
            repository.updateLibraryItem(updated)
            if (_selectedLibraryItem.value?.id == item.id) {
                _selectedLibraryItem.value = updated
            }
        }
    }

    fun selectLibraryItem(item: LibraryItemEntity?) {
        _selectedLibraryItem.value = item
        _pdfReadingPage.value = item?.lastReadPage?.coerceAtLeast(1) ?: 1
    }

    fun setPdfPage(page: Int) {
        val item = _selectedLibraryItem.value ?: return
        val cleanPage = page.coerceIn(1, item.numberOfPages)
        _pdfReadingPage.value = cleanPage
        viewModelScope.launch {
            repository.updateLibraryItem(item.copy(lastReadPage = cleanPage))
        }
    }

    fun saveLibraryNotes(item: LibraryItemEntity, notesText: String) {
        viewModelScope.launch {
            repository.updateLibraryItem(item.copy(localNotes = notesText))
            _selectedLibraryItem.value = item.copy(localNotes = notesText)
        }
    }

    fun addHighlight(item: LibraryItemEntity, highlightText: String) {
        viewModelScope.launch {
            val currentHighlights = if (item.highlightsJson.isEmpty()) {
                highlightText
            } else {
                "${item.highlightsJson}|||$highlightText"
            }
            val updated = item.copy(highlightsJson = currentHighlights)
            repository.updateLibraryItem(updated)
            _selectedLibraryItem.value = updated
        }
    }

    fun clearHighlights(item: LibraryItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(highlightsJson = "")
            repository.updateLibraryItem(updated)
            _selectedLibraryItem.value = updated
        }
    }

    fun getCoursesByLevel(): Map<String, List<String>> {
        return repository.getCoursesByLevel()
    }

    override fun onCleared() {
        super.onCleared()
        examTimer?.cancel()
    }
}
