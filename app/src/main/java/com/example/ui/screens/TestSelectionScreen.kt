package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.NeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSelectionScreen(
    viewModel: NeetViewModel,
    modifier: Modifier = Modifier
) {
    val selectedChapter by viewModel.selectedChapter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Test Difficulty") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("chapter_list") }) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            selectedChapter?.let { chapter ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Selected Chapter:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chapter.displayLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // TEST 1: EASY LEVEL CARD
            Card(
                onClick = { viewModel.selectDifficulty("Easy") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_type_easy_button"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEST 1: EASY LEVEL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Diff: 40%") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Total Questions: 180 (All chapter-exclusive)", style = MaterialTheme.typography.bodyMedium)
                    Text("• Time Limit: 3 hours (180 minutes)", style = MaterialTheme.typography.bodyMedium)
                    Text("• Composition: 80 PYQs + 100 NCERT-based", style = MaterialTheme.typography.bodyMedium)
                    Text("• Marking: +4 for correct, -1 for wrong", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // TEST 2: MEDIUM LEVEL CARD
            Card(
                onClick = { viewModel.selectDifficulty("Medium") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_type_medium_button"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEST 2: MEDIUM LEVEL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Diff: 60%") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Total Questions: 180 (All chapter-exclusive)", style = MaterialTheme.typography.bodyMedium)
                    Text("• Time Limit: 3 hours (180 minutes)", style = MaterialTheme.typography.bodyMedium)
                    Text("• Composition: 90 PYQs + 60 NCERT-based", style = MaterialTheme.typography.bodyMedium)
                    Text("• Marking: +4 for correct, -1 for wrong", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // TEST 3: HARD LEVEL CARD
            Card(
                onClick = { viewModel.selectDifficulty("Hard") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_type_hard_button"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEST 3: HARD LEVEL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Diff: 80%") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Total Questions: 180 (All chapter-exclusive)", style = MaterialTheme.typography.bodyMedium)
                    Text("• Time Limit: 3 hours (180 minutes)", style = MaterialTheme.typography.bodyMedium)
                    Text("• Composition: 120 PYQs + 60 High-level application", style = MaterialTheme.typography.bodyMedium)
                    Text("• Marking: +4 for correct, -1 for wrong", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
