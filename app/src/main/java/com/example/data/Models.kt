package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subject: String, // "Physics", "Chemistry", "Biology"
    val classLevel: Int, // 11 or 12
    val chapterNumber: Int,
    val subCategory: String = "" // "Botany", "Zoology", or empty
) {
    val displayLabel: String
        get() = "Chapter $chapterNumber: $name"
}

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chapterId: Int,
    val questionText: String,
    val subject: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val explanation: String,
    val isPyq: Boolean = false,
    val pyqYear: String = "" // e.g. "2023"
)

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chapterId: Int,
    val chapterName: String,
    val subject: String,
    val difficulty: String,
    val score: Int, // max 720
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val timeTakenSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)
