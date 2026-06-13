package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.NeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestInstructionsScreen(
    viewModel: NeetViewModel,
    modifier: Modifier = Modifier
) {
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Instructions") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("test_selection") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Assignment,
                contentDescription = "Instructions",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "NEET Exam Simulation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "EXAM PROFILE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider()
                    Text("• Target Chapter: ${selectedChapter?.name ?: "Unknown"}", fontWeight = FontWeight.Bold)
                    Text("• Difficulty Setup: $selectedDifficulty Level", fontWeight = FontWeight.SemiBold)
                    Text("• Total Questions: 180 questions")
                    Text("• Time Allowance: 3 Hours (180 Minutes)")
                    Text("• Marking System: +4 (Correct) / -1 (Wrong) / 0 (Unattempted)")
                    Text("• Maximum Score obtainable: 720 Marks")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CRITICAL RULES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = "1. Once you click 'Start Test', the 3-hour timer starts immediately. No pauses are allowed.\n" +
                        "2. Auto-Submit will trigger when the clock hits 00:00:00.\n" +
                        "3. Answer options can be changed at any time or cleared using 'Clear Response'.\n" +
                        "4. Questions can be flagged with 'Mark for Review' to easily return to them using the matrix list.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.startTest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_test_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "START TEST NOW",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
