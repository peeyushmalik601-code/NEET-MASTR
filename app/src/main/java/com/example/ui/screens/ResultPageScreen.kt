package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultPageScreen(
    viewModel: NeetViewModel,
    modifier: Modifier = Modifier
) {
    val result by viewModel.lastAttemptResult.collectAsState()
    val activeQuestions by viewModel.activeQuestions.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()

    var showSolutions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Performance Report", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error generating report. No result recorded.")
            }
        } else {
            val res = result!!
            val totalQuestions = activeQuestions.size

            if (showSolutions) {
                // Expanded solutions view
                Column(
                    modifier = modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Solutions & Explanations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showSolutions = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Summary", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(activeQuestions) { index, question ->
                            val userAns = userAnswers[index]
                            val correct = question.correctAnswer
                            val isCorrect = userAns == correct

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (userAns == null) {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    } else if (isCorrect) {
                                        Color(0xFFE8F5E9) // soft green
                                    } else {
                                        Color(0xFFFFEBEE) // soft red
                                    }
                                ),
                                border = if (userAns != null) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Question ${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (userAns == null) {
                                                Icon(Icons.Filled.HelpOutline, "Unattempted", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Unattempted", fontSize = 12.sp, color = Color.Gray)
                                            } else if (isCorrect) {
                                                Icon(Icons.Filled.CheckCircle, "Correct", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Correct (+4)", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                            } else {
                                                Icon(Icons.Filled.Cancel, "Incorrect", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Wrong (-1)", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = question.questionText, style = MaterialTheme.typography.bodyMedium)

                                    Spacer(modifier = Modifier.height(12.dp))
                                    // List Options
                                    Text("A: ${question.optionA}", fontSize = 13.sp)
                                    Text("B: ${question.optionB}", fontSize = 13.sp)
                                    Text("C: ${question.optionC}", fontSize = 13.sp)
                                    Text("D: ${question.optionD}", fontSize = 13.sp)

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Your Answer: ${userAns ?: "None"} • Correct Answer: $correct",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Solution:\n${question.explanation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Main analytics panel
                Column(
                    modifier = modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = res.chapterName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "NEET Score Diagnostic",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Final Score Ring Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "OBTAINED SCORE",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${res.score} / 720",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (res.score >= 500) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = (res.score.coerceIn(0, 720).toFloat() / 720f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Metrics table Grid
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "ANALYTICS BREAKDOWN",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Divider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Correct Answers (+4)")
                                Text("${res.correctCount}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Incorrect Answers (-1)")
                                Text("${res.incorrectCount}", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Unattempted Questions")
                                Text("${res.unattemptedCount}", color = Color.Gray)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Accuracy Level")
                                val totalAttempted = res.correctCount + res.incorrectCount
                                val accuracy = if (totalAttempted > 0) (res.correctCount.toFloat() / totalAttempted.toFloat() * 100f).toInt() else 0
                                Text("$accuracy%", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Time Spent")
                                val minutes = res.timeTakenSeconds / 60
                                val remainingSecs = res.timeTakenSeconds % 60
                                Text("${minutes}m ${remainingSecs}s", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons Block
                    Button(
                        onClick = { showSolutions = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("view_solutions_button")
                    ) {
                        Icon(Icons.Filled.List, "Solutions")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("VIEW SOLUTIONS")
                    }

                    OutlinedButton(
                        onClick = { viewModel.startTest() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reattempt_test_button")
                    ) {
                        Text("RE-ATTEMPT TEST")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.resetTestActiveState()
                            viewModel.navigateTo("subject_selection")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("select_new_chapter_button")
                    ) {
                        Text("SELECT NEW CHAPTER")
                    }
                }
            }
        }
    }
}
