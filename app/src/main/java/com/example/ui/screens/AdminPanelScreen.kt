package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChapterEntity
import com.example.ui.NeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: NeetViewModel,
    modifier: Modifier = Modifier
) {
    val chapters by viewModel.chapters.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Chapters", "Add Question", "CSV Import", "Analytics Logs")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("subject_selection") }) {
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
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ChaptersTab(chapters, viewModel)
                1 -> AddQuestionTab(chapters, viewModel)
                2 -> CsvImportTab(viewModel)
                3 -> HistoryTab(attempts, viewModel)
            }
        }
    }
}

@Composable
fun ChaptersTab(chapters: List<ChapterEntity>, viewModel: NeetViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var chName by remember { mutableStateOf("") }
    var chSubject by remember { mutableStateOf("Physics") }
    val subjects = listOf("Physics", "Chemistry", "Biology")
    var chClass by remember { mutableIntStateOf(11) }
    var chNum by remember { mutableStateOf("") }
    var chSubCat by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create New Chapter") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chName,
                        onValueChange = { chName = it },
                        label = { Text("Chapter Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = chNum,
                        onValueChange = { chNum = it },
                        label = { Text("Chapter Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Subject:")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        subjects.forEach { sub ->
                            FilterChip(
                                selected = chSubject == sub,
                                onClick = { chSubject = sub },
                                label = { Text(sub) }
                            )
                        }
                    }
                    Text("Class Level:")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(11, 12).forEach { cl ->
                            FilterChip(
                                selected = chClass == cl,
                                onClick = { chClass = cl },
                                label = { Text("Class $cl") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = chSubCat,
                        onValueChange = { chSubCat = it },
                        label = { Text("Subcategory (e.g. Botany/Zoology)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = chNum.toIntOrNull() ?: 1
                        viewModel.addChapter(chName, chSubject, chClass, num, chSubCat)
                        showAddDialog = false
                        chName = ""
                        chNum = ""
                        chSubCat = ""
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().testTag("add_chapter_btn")
        ) {
            Icon(Icons.Filled.Add, "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("ADD CUSTOM CHAPTER")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chapters) { chapter ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Ch ${chapter.chapterNumber}: ${chapter.name}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "${chapter.subject} • Class ${chapter.classLevel} ${if (chapter.subCategory.isNotEmpty()) "• " + chapter.subCategory else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(onClick = { viewModel.deleteChapter(chapter.id) }) {
                            Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddQuestionTab(chapters: List<ChapterEntity>, viewModel: NeetViewModel) {
    var qText by remember { mutableStateOf("") }
    var qOptionA by remember { mutableStateOf("") }
    var qOptionB by remember { mutableStateOf("") }
    var qOptionC by remember { mutableStateOf("") }
    var qOptionD by remember { mutableStateOf("") }
    var qCorrectAns by remember { mutableStateOf("A") }
    var qExplanation by remember { mutableStateOf("") }
    var qIsPyq by remember { mutableStateOf(false) }
    var qPyqYear by remember { mutableStateOf("") }
    var qDiff by remember { mutableStateOf("Easy") }

    var expandedCh by remember { mutableStateOf(false) }
    var selectedCh by remember { mutableStateOf<ChapterEntity?>(null) }

    LaunchedEffect(chapters) {
        if (chapters.isNotEmpty() && selectedCh == null) {
            selectedCh = chapters.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Assign to Chapter:", fontWeight = FontWeight.Bold)
        Box {
            OutlinedButton(
                onClick = { expandedCh = true },
                modifier = Modifier.fillMaxWidth().testTag("select_chapter_dropdown")
            ) {
                Text(selectedCh?.displayLabel ?: "Select Chapter")
            }
            DropdownMenu(expanded = expandedCh, onDismissRequest = { expandedCh = false }) {
                chapters.forEach { ch ->
                    DropdownMenuItem(
                        text = { Text(ch.displayLabel) },
                        onClick = {
                            selectedCh = ch
                            expandedCh = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = qText,
            onValueChange = { qText = it },
            label = { Text("Question Text") },
            modifier = Modifier.fillMaxWidth().testTag("q_text_input")
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = qOptionA,
                onValueChange = { qOptionA = it },
                label = { Text("Option A") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = qOptionB,
                onValueChange = { qOptionB = it },
                label = { Text("Option B") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = qOptionC,
                onValueChange = { qOptionC = it },
                label = { Text("Option C") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = qOptionD,
                onValueChange = { qOptionD = it },
                label = { Text("Option D") },
                modifier = Modifier.weight(1f)
            )
        }

        Text("Correct Option:")
        Row {
            listOf("A", "B", "C", "D").forEach { opt ->
                FilterChip(
                    selected = qCorrectAns == opt,
                    onClick = { qCorrectAns = opt },
                    label = { Text(opt) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        Text("Set Question Difficulty Level:")
        Row {
            listOf("Easy", "Medium", "Hard").forEach { dif ->
                FilterChip(
                    selected = qDiff == dif,
                    onClick = { qDiff = dif },
                    label = { Text(dif) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = qIsPyq, onCheckedChange = { qIsPyq = it })
            Text("Is Previous Year Question (PYQ)")
        }

        if (qIsPyq) {
            OutlinedTextField(
                value = qPyqYear,
                onValueChange = { qPyqYear = it },
                label = { Text("PYQ Tag (e.g. NEET 2023)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = qExplanation,
            onValueChange = { qExplanation = it },
            label = { Text("Detailed Solution/Explanation") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val chId = selectedCh?.id ?: 1
                val subject = selectedCh?.subject ?: "Physics"
                viewModel.addQuestion(
                    chapterId = chId,
                    text = qText,
                    subject = subject,
                    difficulty = qDiff,
                    optA = qOptionA,
                    optB = qOptionB,
                    optC = qOptionC,
                    optD = qOptionD,
                    correctAns = qCorrectAns,
                    explanation = qExplanation,
                    isPyq = qIsPyq,
                    pyqYear = if (qIsPyq) qPyqYear else "NCERT Based"
                )
                // Clear fields
                qText = ""
                qOptionA = ""
                qOptionB = ""
                qOptionC = ""
                qOptionD = ""
                qExplanation = ""
                qIsPyq = false
                qPyqYear = ""
            },
            modifier = Modifier.fillMaxWidth().testTag("save_question_btn"),
            enabled = qText.isNotEmpty() && qOptionA.isNotEmpty() && qOptionB.isNotEmpty()
        ) {
            Text("SAVE QUESTION DATA")
        }
    }
}

@Composable
fun CsvImportTab(viewModel: NeetViewModel) {
    var rawCsv by remember { mutableStateOf("") }
    var responseLogs by remember { mutableStateOf("") }

    val sampleCsv = """ChapterId,QuestionText,Subject,Difficulty,OptionA,OptionB,OptionC,OptionD,CorrectOption,Explanation,IsPyq,PyqYear
3,"A body of mass 5 kg undergoes acceleration under pull. Find net force","Physics","Easy","30 N","10 N","50 N","20 N","D","Force is Mass * Acceleration, 30N/2",true,"NEET 2021"
8,"Isothermal physical processes preserve temperature. Correct?","Physics","Medium","Yes","No","Partially","None of above","A","Preserved isothermally",false,"NCERT Based"
"""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Import CSV Questions Grid:", fontWeight = FontWeight.Bold)
        Text(
            "Format: ChapterId, QuestionText, Subject, Difficulty, OptionA, OptionB, OptionC, OptionD, CorrectOption, Explanation, IsPyq, PyqYear",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = { rawCsv = sampleCsv },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOAD SAMPLE FORMAT CSV")
        }

        OutlinedTextField(
            value = rawCsv,
            onValueChange = { rawCsv = it },
            placeholder = { Text("Paste CSV lines here...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .testTag("csv_input_field"),
            maxLines = 15
        )

        Button(
            onClick = {
                viewModel.importCsvQuestions(rawCsv, defaultChapterId = 1) { count ->
                    responseLogs = "Successfully parsed and saved $count questions to Chapter-wise database!"
                    rawCsv = ""
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("csv_submit_btn"),
            enabled = rawCsv.isNotBlank()
        ) {
            Text("PARSED AND IMPORT CSV DATA")
        }

        if (responseLogs.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = responseLogs,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HistoryTab(attempts: List<com.example.data.TestAttemptEntity>, viewModel: NeetViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Saved Attempts: ${attempts.size}", fontWeight = FontWeight.Bold)
            IconButton(onClick = { viewModel.clearHistory() }) {
                Icon(Icons.Filled.Restore, "Clear Logs", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (attempts.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No past simulated attempts recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(attempts) { att ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(att.chapterName, fontWeight = FontWeight.Bold)
                                Text("${att.score} Marks", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "Subject: ${att.subject} • Level: ${att.difficulty}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Correct: ${att.correctCount} | Wrong: ${att.incorrectCount}", fontSize = 12.sp)
                                val m = att.timeTakenSeconds / 60
                                val s = att.timeTakenSeconds % 60
                                Text("Time: ${m}m ${s}s", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
