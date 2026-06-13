package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.filled.CheckCircle
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPageScreen(
    viewModel: NeetViewModel,
    modifier: Modifier = Modifier
) {
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val activeQuestions by viewModel.activeQuestions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val flaggedQuestions by viewModel.flaggedQuestions.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()

    val showAlert30Min by viewModel.showAlert30Min.collectAsState()
    val showAlert10Min by viewModel.showAlert10Min.collectAsState()

    var showSubmitConfirm by remember { mutableStateOf(false) }
    var showGridPanel by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // 180-Minutes Warning Alerts
    if (showAlert30Min) {
        AlertDialog(
            onDismissRequest = { viewModel.stopTimerWarnings() },
            title = { Text("Time Warning") },
            text = { Text("Only 30 minutes remaining! Work efficiently to attempt all 180 questions.") },
            icon = { Icon(Icons.Filled.Warning, contentDescription = "Time Warning", tint = MaterialTheme.colorScheme.error) },
            confirmButton = {
                TextButton(onClick = { viewModel.stopTimerWarnings() }) { Text("Dismiss") }
            }
        )
    }

    if (showAlert10Min) {
        AlertDialog(
            onDismissRequest = { viewModel.stopTimerWarnings() },
            title = { Text("Critical Time Alarm") },
            text = { Text("Only 10 minutes left! Verify your marked reviews and prep for final submit.") },
            icon = { Icon(Icons.Filled.Warning, contentDescription = "Time Alert", tint = MaterialTheme.colorScheme.error) },
            confirmButton = {
                TextButton(onClick = { viewModel.stopTimerWarnings() }) { Text("OK", fontWeight = FontWeight.Bold) }
            }
        )
    }

    // Confirm Submission
    if (showSubmitConfirm) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Submit Exam?") },
            text = { 
                val attemptedCount = userAnswers.size
                val left = activeQuestions.size - attemptedCount
                Text("You have completed $attemptedCount / ${activeQuestions.size} questions. Submit now to get immediate detailed analytics?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirm = false
                        viewModel.submitTest()
                    },
                    modifier = Modifier.testTag("submit_confirm_yes")
                ) {
                    Text("Submit Test")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirm = false }) { Text("Continue Testing") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = selectedChapter?.name ?: "Exam Session",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = " NEET Mock - $selectedDifficulty Level",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    // Constant Timer Visible
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (timerSeconds < 1800) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("timer_display_card")
                    ) {
                        Text(
                            text = viewModel.formatTimerText(timerSeconds),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (timerSeconds < 1800) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Collapsible Matrix Grid button
                    IconButton(onClick = { showGridPanel = !showGridPanel }) {
                        Icon(
                            imageVector = Icons.Filled.GridOn,
                            contentDescription = "Question Drawer",
                            tint = if (showGridPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Main body containing current question or question list matrix
            if (showGridPanel) {
                // Question grid drawer view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question Grid Navigation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showGridPanel = false }) { Text("Back to Question") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 48.dp),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(activeQuestions) { index, _ ->
                            val isAnswered = userAnswers.containsKey(index)
                            val isFlagged = flaggedQuestions.contains(index)
                            val isCurrent = index == currentQuestionIndex

                            val containerColor = when {
                                isCurrent -> MaterialTheme.colorScheme.primary
                                isFlagged -> Color(0xFF512DA8) // Purple for review
                                isAnswered -> Color(0xFF00796B) // answered
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val contentColor = when {
                                isCurrent || isFlagged || isAnswered -> Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(containerColor, CircleShape)
                                    .clickable {
                                        viewModel.setQuestionIndex(index)
                                        showGridPanel = false
                                    }
                                    .testTag("grid_index_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
            } else {
                // Normal Question layout
                if (activeQuestions.isNotEmpty()) {
                    val currentQuestion = activeQuestions[currentQuestionIndex]
                    val answer = userAnswers[currentQuestionIndex]
                    val isFlagged = flaggedQuestions.contains(currentQuestionIndex)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Question profile & metadata tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${currentQuestionIndex + 1} of 180",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Tag indicating PYQ or NCERT Based
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = currentQuestion.pyqYear.ifEmpty { "General Study" },
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (currentQuestion.isPyq) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        // Question text card representation
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = currentQuestion.questionText,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                lineHeight = 24.sp
                            )
                        }

                        // Option A
                        OptionSelectorCard(
                            label = "A",
                            content = currentQuestion.optionA,
                            isSelected = answer == "A",
                            onClick = { viewModel.selectAnswerForCurrent("A") },
                            modifier = Modifier.testTag("option_a_card")
                        )

                        // Option B
                        OptionSelectorCard(
                            label = "B",
                            content = currentQuestion.optionB,
                            isSelected = answer == "B",
                            onClick = { viewModel.selectAnswerForCurrent("B") },
                            modifier = Modifier.testTag("option_b_card")
                        )

                        // Option C
                        OptionSelectorCard(
                            label = "C",
                            content = currentQuestion.optionC,
                            isSelected = answer == "C",
                            onClick = { viewModel.selectAnswerForCurrent("C") },
                            modifier = Modifier.testTag("option_c_card")
                        )

                        // Option D
                        OptionSelectorCard(
                            label = "D",
                            content = currentQuestion.optionD,
                            isSelected = answer == "D",
                            onClick = { viewModel.selectAnswerForCurrent("D") },
                            modifier = Modifier.testTag("option_d_card")
                        )
                    }

                    // Action Controls Block
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.ui.graphics.RectangleShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // First Row: Marking & review Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { viewModel.toggleFlagForCurrent() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFlagged) Color(0xFF512DA8) else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isFlagged) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("mark_for_review_button")
                                ) {
                                    Icon(
                                        imageVector = if (isFlagged) Icons.Filled.Flag else Icons.Outlined.Flag,
                                        contentDescription = "Review"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Review")
                                }

                                TextButton(
                                    onClick = { viewModel.clearResponseForCurrent() },
                                    modifier = Modifier.testTag("clear_response_button")
                                ) {
                                    Text("Clear Response", color = MaterialTheme.colorScheme.error)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Second Row: Directional Navigation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.setQuestionIndex(currentQuestionIndex - 1) },
                                    enabled = currentQuestionIndex > 0,
                                    modifier = Modifier.testTag("prev_button")
                                ) {
                                    Text("Previous")
                                }

                                Button(
                                    onClick = { showSubmitConfirm = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.testTag("submit_test_button")
                                ) {
                                    Text("Submit Exam")
                                }

                                Button(
                                    onClick = { viewModel.setQuestionIndex(currentQuestionIndex + 1) },
                                    enabled = currentQuestionIndex < activeQuestions.size - 1,
                                    modifier = Modifier.testTag("next_button")
                                ) {
                                    Text("Next")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionSelectorCard(
    label: String,
    content: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
