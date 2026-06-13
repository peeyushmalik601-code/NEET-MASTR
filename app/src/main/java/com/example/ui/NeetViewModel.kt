package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NeetViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = NeetRepository(database)

    // Flow lists observed by UI
    val chapters: StateFlow<List<ChapterEntity>> = repository.allChaptersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attempts: StateFlow<List<TestAttemptEntity>> = repository.allAttemptsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuestions: StateFlow<List<QuestionEntity>> = repository.getAllQuestionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Selection States
    private val _selectedSubject = MutableStateFlow("Physics")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedClass = MutableStateFlow(11) // 11 or 12
    val selectedClass: StateFlow<Int> = _selectedClass.asStateFlow()

    private val _selectedChapter = MutableStateFlow<ChapterEntity?>(null)
    val selectedChapter: StateFlow<ChapterEntity?> = _selectedChapter.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("Easy") // "Easy", "Medium", "Hard"
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    // Active Test Engine States
    private val _activeQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val activeQuestions: StateFlow<List<QuestionEntity>> = _activeQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // Index -> Answer ("A", "B", "C", "D") or empty/null
    private val _userAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, String>> = _userAnswers.asStateFlow()

    // Set of indices that are flagged for review
    private val _flaggedQuestions = MutableStateFlow<Set<Int>>(emptySet())
    val flaggedQuestions: StateFlow<Set<Int>> = _flaggedQuestions.asStateFlow()

    // Timer (in seconds) - 3 Hours = 180 Minutes = 10800 Seconds
    private val _timerSeconds = MutableStateFlow(10800)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTestActive = MutableStateFlow(false)
    val isTestActive: StateFlow<Boolean> = _isTestActive.asStateFlow()

    private val _testSubmitted = MutableStateFlow(false)
    val testSubmitted: StateFlow<Boolean> = _testSubmitted.asStateFlow()

    // Alert display flags
    private val _showAlert30Min = MutableStateFlow(false)
    val showAlert30Min: StateFlow<Boolean> = _showAlert30Min.asStateFlow()

    private val _showAlert10Min = MutableStateFlow(false)
    val showAlert10Min: StateFlow<Boolean> = _showAlert10Min.asStateFlow()

    // Saved Attempt Result
    private val _lastAttemptResult = MutableStateFlow<TestAttemptEntity?>(null)
    val lastAttemptResult: StateFlow<TestAttemptEntity?> = _lastAttemptResult.asStateFlow()

    private var timerJob: Job? = null

    // Navigation controller state helper (if needed)
    private val _currentRoute = MutableStateFlow("subject_selection")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        navigateTo("class_selection")
    }

    fun selectClass(classLevel: Int) {
        _selectedClass.value = classLevel
        navigateTo("chapter_list")
    }

    fun selectChapter(chapter: ChapterEntity) {
        _selectedChapter.value = chapter
        navigateTo("test_selection")
    }

    fun selectDifficulty(difficulty: String) {
        _selectedDifficulty.value = difficulty
        navigateTo("test_instructions")
    }

    fun startTest() {
        val chapter = selectedChapter.value ?: return
        val difficulty = selectedDifficulty.value

        viewModelScope.launch {
            // Fetch exactly 180 questions for this test level
            val testQuestions = repository.getQuestionsForTest(chapter, difficulty)
            _activeQuestions.value = testQuestions
            _currentQuestionIndex.value = 0
            _userAnswers.value = emptyMap()
            _flaggedQuestions.value = emptySet()
            
            // 3 hours countdown = 10800 seconds
            _timerSeconds.value = 10800
            _showAlert30Min.value = false
            _showAlert10Min.value = false
            _isTestActive.value = true
            _testSubmitted.value = false
            _lastAttemptResult.value = null

            navigateTo("test_page")
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0 && _isTestActive.value) {
                delay(1000)
                _timerSeconds.value -= 1

                // Warnings triggers
                if (_timerSeconds.value == 1800) { // 30 mins
                    _showAlert30Min.value = true
                }
                if (_timerSeconds.value == 600) { // 10 mins
                    _showAlert10Min.value = true
                }
            }
            if (_timerSeconds.value == 0 && _isTestActive.value) {
                // Auto-submit
                submitTest()
            }
        }
    }

    fun stopTimerWarnings() {
        _showAlert30Min.value = false
        _showAlert10Min.value = false
    }

    fun setQuestionIndex(index: Int) {
        if (index in 0 until _activeQuestions.value.size) {
            _currentQuestionIndex.value = index
        }
    }

    fun selectAnswerForCurrent(option: String) {
        val idx = _currentQuestionIndex.value
        val answers = _userAnswers.value.toMutableMap()
        answers[idx] = option
        _userAnswers.value = answers
    }

    fun clearResponseForCurrent() {
        val idx = _currentQuestionIndex.value
        val answers = _userAnswers.value.toMutableMap()
        answers.remove(idx)
        _userAnswers.value = answers
    }

    fun toggleFlagForCurrent() {
        val idx = _currentQuestionIndex.value
        val flags = _flaggedQuestions.value.toMutableSet()
        if (flags.contains(idx)) {
            flags.remove(idx)
        } else {
            flags.add(idx)
        }
        _flaggedQuestions.value = flags
    }

    fun submitTest() {
        timerJob?.cancel()
        _isTestActive.value = false
        _testSubmitted.value = true

        val chapter = selectedChapter.value ?: return
        val questions = _activeQuestions.value
        val answers = _userAnswers.value

        var correct = 0
        var incorrect = 0
        var unattempted = 0

        for (i in questions.indices) {
            val correctAns = questions[i].correctAnswer
            val userAns = answers[i]

            if (userAns == null) {
                unattempted++
            } else if (userAns.equals(correctAns, ignoreCase = true)) {
                correct++
            } else {
                incorrect++
            }
        }

        // Marking Scheme: +4 for correct, -1 for wrong, 0 for unattempted
        val score = (correct * 4) - incorrect
        val timeTakenSeconds = 10800 - _timerSeconds.value

        val attempt = TestAttemptEntity(
            chapterId = chapter.id,
            chapterName = chapter.name,
            subject = chapter.subject,
            difficulty = selectedDifficulty.value,
            score = score,
            correctCount = correct,
            incorrectCount = incorrect,
            unattemptedCount = unattempted,
            timeTakenSeconds = timeTakenSeconds.toLong()
        )

        viewModelScope.launch {
            repository.insertAttempt(attempt)
            _lastAttemptResult.value = attempt
            navigateTo("result_page")
        }
    }

    fun resetTestActiveState() {
        _activeQuestions.value = emptyList()
        _userAnswers.value = emptyMap()
        _flaggedQuestions.value = emptySet()
        _timerSeconds.value = 10800
        _isTestActive.value = false
        _testSubmitted.value = false
    }

    // === ADMIN SERVICE ACTIONS ===
    fun addChapter(name: String, subject: String, classLevel: Int, chapterNumber: Int, subCategory: String) {
        viewModelScope.launch {
            repository.insertChapter(
                ChapterEntity(
                    name = name,
                    subject = subject,
                    classLevel = classLevel,
                    chapterNumber = chapterNumber,
                    subCategory = subCategory
                )
            )
        }
    }

    fun editChapter(id: Int, name: String, subject: String, classLevel: Int, chapterNumber: Int, subCategory: String) {
        viewModelScope.launch {
            repository.updateChapter(
                ChapterEntity(
                    id = id,
                    name = name,
                    subject = subject,
                    classLevel = classLevel,
                    chapterNumber = chapterNumber,
                    subCategory = subCategory
                )
            )
        }
    }

    fun deleteChapter(id: Int) {
        viewModelScope.launch {
            repository.deleteChapterById(id)
        }
    }

    fun addQuestion(
        chapterId: Int,
        text: String,
        subject: String,
        difficulty: String,
        optA: String,
        optB: String,
        optC: String,
        optD: String,
        correctAns: String,
        explanation: String,
        isPyq: Boolean,
        pyqYear: String
    ) {
        viewModelScope.launch {
            repository.insertQuestion(
                QuestionEntity(
                    chapterId = chapterId,
                    questionText = text,
                    subject = subject,
                    difficulty = difficulty,
                    optionA = optA,
                    optionB = optB,
                    optionC = optC,
                    optionD = optD,
                    correctAnswer = correctAns,
                    explanation = explanation,
                    isPyq = isPyq,
                    pyqYear = pyqYear
                )
            )
        }
    }

    fun deleteQuestion(id: Int) {
        viewModelScope.launch {
            repository.deleteQuestionById(id)
        }
    }

    fun importCsvQuestions(csvText: String, defaultChapterId: Int, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importQuestionsFromCsv(csvText, defaultChapterId)
            onResult(count)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllAttempts()
        }
    }

    // Helper functions for HH:MM:SS Formatting
    fun formatTimerText(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}
