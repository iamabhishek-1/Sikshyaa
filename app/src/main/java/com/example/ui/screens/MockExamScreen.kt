package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MockExamResultEntity
import com.example.ui.theme.*
import com.example.viewmodel.SikshyaaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MockExamScreen(viewModel: SikshyaaViewModel) {
    val activeExam by viewModel.activeMockExam.collectAsState()
    val isFinished by viewModel.isExamFinished.collectAsState()
    val examResults by viewModel.examResults.collectAsState()

    // Switch between Exam catalog lobby vs Active timing exam vs Result screens
    when {
        activeExam != null && !isFinished -> {
            ActiveExamPlayground(viewModel)
        }
        activeExam != null && isFinished -> {
            ExamShowcaseResults(viewModel)
        }
        else -> {
            ExamLobbyCatalog(viewModel, examResults)
        }
    }
}

// 1. Exam Selection Lobby Catalog
@Composable
fun ExamLobbyCatalog(viewModel: SikshyaaViewModel, results: List<MockExamResultEntity>) {
    val examsList = viewModel.availableMockExams

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Mock Exam Hall",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )
            Text(
                text = "Prepare using live simulated tests conforming strictly to SEE, NEB, IOM, and Loksewa boards.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            )
        }

        Text(
            text = "Active Mock Practice Drill Rooms:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        )

        examsList.forEach { exam ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = exam.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = "Exam Length", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                            Text("${exam.durationMinutes} mins", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }

                    Text(
                        text = exam.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${exam.totalQuestions} MCQ Questions",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )

                        Button(
                            onClick = { viewModel.startMockExam(exam.id) },
                            modifier = Modifier.testTag("start_exam_${exam.id}")
                        ) {
                            Text("Start Exam Drill")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // History of completed Exams
        if (results.isNotEmpty()) {
            Text(
                text = "Your Recent Exam Attempts History:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )

            results.forEach { res ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (res.accuracy >= 0.5f) Icons.Default.Check else Icons.Default.Info,
                            contentDescription = "Accuracy state icon",
                            tint = if (res.accuracy >= 0.5f) XpGreen else AlertRed
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = res.examName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Score: ${res.score}/${res.totalQuestions} (${(res.accuracy * 100).toInt()}% match) • Time: ${res.durationSeconds}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextNavy
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (res.accuracy >= 0.5f) XpGreen.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = if (res.accuracy >= 0.5f) "PASSED" else "FAIL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (res.accuracy >= 0.5f) XpGreen else AlertRed
                            )
                        }
                    }
                }
            }
        }
    }
}

// 2. Active timed Exam Room Simulator
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveExamPlayground(viewModel: SikshyaaViewModel) {
    val exam by viewModel.activeMockExam.collectAsState()
    val currentIndex by viewModel.currentMockIndex.collectAsState()
    val answers by viewModel.mockSelectedAnswers.collectAsState()
    val timer by viewModel.examTimeRemaining.collectAsState()

    val currentExam = exam ?: return
    val currentQ = currentExam.questions.getOrNull(currentIndex) ?: return

    // Format timer to MM:SS
    val minutes = timer / 60
    val seconds = timer % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Countdown Clock Header Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.12f)),
            border = BorderStroke(1.5.dp, if (timer < 60) AlertRed else BorderGray)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.HourglassBottom, contentDescription = "Clock ticks", tint = AlertRed)
                    Text("Time Remaining:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AlertRed))
                }
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = AlertRed, letterSpacing = 1.sp)
                )
            }
        }

        // Question Progress Steps indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${currentIndex + 1} of ${currentExam.questions.size}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )

            TextButton(
                onClick = { viewModel.submitMockExam() },
                modifier = Modifier.testTag("submit_exam_top_btn")
            ) {
                Text("Submit Paper", color = AlertRed, fontWeight = FontWeight.Bold)
            }
        }

        // Active Question Content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = currentQ.questionText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )

                // Select Radio Option Rows
                currentQ.options.forEachIndexed { optIdx, optText ->
                    val isSelected = answers[currentIndex] == optIdx

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.answerMockQuestion(optIdx) }
                            .testTag("mock_opt_${optIdx}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.White),
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.answerMockQuestion(optIdx) },
                                modifier = Modifier.testTag("radio_opt_${optIdx}")
                            )
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Question quick jump hub
        Text(
            text = "Question Sheet Progress Hub:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currentExam.questions.forEachIndexed { qIdx, _ ->
                val isAnswered = answers.containsKey(qIdx)
                val isCurrent = qIdx == currentIndex

                OutlinedButton(
                    onClick = { viewModel.nextMockQuestion() }, // Quick mock transition index selector
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isAnswered -> XpGreen.copy(alpha = 0.15f)
                            else -> Color.White
                        }
                    ),
                    modifier = Modifier.size(44.dp).testTag("hub_q_$qIdx"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "${qIdx + 1}",
                        color = if (isCurrent) Color.White else if (isAnswered) XpGreen else TextNavy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Navigation Steppers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.prevMockQuestion() },
                enabled = currentIndex > 0,
                modifier = Modifier.testTag("mock_prev_btn")
            ) {
                Text("Previous")
            }

            if (currentIndex < currentExam.questions.size - 1) {
                Button(
                    onClick = { viewModel.nextMockQuestion() },
                    modifier = Modifier.testTag("mock_next_btn")
                ) {
                    Text("Next Chapter")
                }
            } else {
                Button(
                    onClick = { viewModel.submitMockExam() },
                    colors = ButtonDefaults.buttonColors(containerColor = XpGreen),
                    modifier = Modifier.testTag("submit_exam_finalize_btn")
                ) {
                    Text("Submit Exam")
                }
            }
        }
    }
}

// 3. Timed Exam Submission Showcase results
@Composable
fun ExamShowcaseResults(viewModel: SikshyaaViewModel) {
    val exam by viewModel.activeMockExam.collectAsState()
    val answers by viewModel.mockSelectedAnswers.collectAsState()

    val currentExam = exam ?: return

    var correctCount = 0
    currentExam.questions.forEachIndexed { idx, q ->
        val selected = answers[idx]
        if (selected != null && selected == q.correctIndex) {
            correctCount += 1
        }
    }

    val percentage = (correctCount.toFloat() / currentExam.questions.size.toFloat()) * 100
    val didPass = percentage >= currentExam.passMarks

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visual Success banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (didPass) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (didPass) Icons.Default.EmojiEvents else Icons.Default.CancelPresentation,
                    contentDescription = "Trophy result",
                    tint = if (didPass) StreakGold else AlertRed,
                    modifier = Modifier.size(54.dp)
                )

                Text(
                    text = if (didPass) "Congratulations! You Passed!" else "Attempt Unsuccessful - Keep Studying!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = if (didPass) Color(0xFF2E7D32) else Color(0xFFC62828))
                )

                Text(
                    text = "Sikshyaa Mock Accuracy Report for:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                )

                Text(
                    text = currentExam.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Accuracy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("${percentage.toInt()}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Score Board", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("$correctCount / ${currentExam.questions.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Rank Forecast", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(if (didPass) "#45 (Topper)" else "Need Revision", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Detailed Review list items
        Text(
            text = "Exam Mistake & Solution Analysis Sheet:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        )

        currentExam.questions.forEachIndexed { qIdx, q ->
            val studentAnsIdx = answers[qIdx]
            val isCorrect = studentAnsIdx != null && studentAnsIdx == q.correctIndex

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (isCorrect) XpGreen.copy(alpha = 0.5f) else AlertRed.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = "Correct marker",
                            tint = if (isCorrect) XpGreen else AlertRed
                        )
                        Text(
                            text = "Question #${qIdx + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (isCorrect) XpGreen else AlertRed)
                        )
                    }

                    Text(
                        text = q.questionText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Column {
                        Text(
                            text = "Your Answer: " + (studentAnsIdx?.let { q.options.getOrNull(it) } ?: "Skipped"),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCorrect) Color.Gray else AlertRed
                        )
                        Text(
                            text = "Correct Answer: " + q.options[q.correctIndex],
                            style = MaterialTheme.typography.bodySmall.copy(color = XpGreen, fontWeight = FontWeight.Bold)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Solution Review: " + q.solutionExplanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.exitMockExam() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("exit_exam_hall_btn")
        ) {
            Text("Exit Exam Room")
        }
    }
}
