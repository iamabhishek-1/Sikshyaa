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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.McqQuestion
import com.example.ui.theme.*
import com.example.viewmodel.SikshyaaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(viewModel: SikshyaaViewModel) {
    val activeSubject by viewModel.practiceSubject.collectAsState()
    val activeDifficulty by viewModel.practiceDifficulty.collectAsState()
    val questionIndex by viewModel.currentPracticeIndex.collectAsState()
    val selectedOptionIndex by viewModel.selectedPracticeOption.collectAsState()
    val answerRevealed by viewModel.practiceAnswerRevealed.collectAsState()

    val subjects = listOf("Physics", "Anatomy", "General Knowledge")
    val difficulties = listOf("Beginner", "Intermediate", "Advanced", "Expert")

    val matchingQuestions = viewModel.getFilteredPracticeQuestions()
    val currentQuestion = matchingQuestions.getOrNull(questionIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Selector Headers
        Text(
            text = "Topic-wise MCQ Practice",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        )

        // Subject selector row
        Text(
            text = "Select Study Subject:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjects.forEach { s ->
                val isSelected = (activeSubject == s)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setPracticeFilter(s, activeDifficulty) },
                    label = { Text(s, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("practice_subject_${s.lowercase().replace(" ","_")}")
                )
            }
        }

        // Difficulty selector row
        Text(
            text = "Filter Difficulty Level:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            difficulties.forEach { d ->
                val isSelected = (activeDifficulty == d)
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setPracticeFilter(activeSubject, d) },
                    label = { Text(d) },
                    modifier = Modifier.testTag("practice_difficulty_${d.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Active MCQ Card
        if (currentQuestion == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No questions found",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "No questions registered under $activeSubject ($activeDifficulty) yet.",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = TextNavy
                    )
                    TextButton(
                        onClick = { viewModel.setPracticeFilter("Physics", "Beginner") }
                    ) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            // Display Active MCQs Question
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${questionIndex + 1} of ${matchingQuestions.size}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )

                // Difficulty pill label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentQuestion.difficulty,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Clinical medical case marker
                    if (currentQuestion.isMedicalCase) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AlertRed.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "HIGH-YIELD MBBS CLINICAL MCQ",
                                color = AlertRed,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextNavy)
                    )

                    // Options List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        currentQuestion.options.forEachIndexed { optIdx, optText ->
                            val isSelected = selectedOptionIndex == optIdx
                            val isCorrect = optIdx == currentQuestion.correctIndex
                            
                            // Determine gorgeous color schemes based on tap status
                            val optionBg = when {
                                answerRevealed && isCorrect -> Color(0xFFE8F5E9) // Soft Green
                                answerRevealed && isSelected && !isCorrect -> Color(0xFFFFEBEE) // Soft Red
                                isSelected -> MaterialTheme.colorScheme.tertiary
                                else -> Color.White
                            }

                            val borderStroke = when {
                                answerRevealed && isCorrect -> BorderStroke(1.5.dp, Color(0xFF388E3C))
                                answerRevealed && isSelected && !isCorrect -> BorderStroke(1.5.dp, Color(0xFFD32F2F))
                                isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                else -> BorderStroke(1.dp, BorderGray)
                            }

                            val tColor = when {
                                answerRevealed && isCorrect -> Color(0xFF2E7D32)
                                answerRevealed && isSelected && !isCorrect -> Color(0xFFC62828)
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> TextNavy
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectPracticeOption(optIdx)
                                    }
                                    .testTag("option_$optIdx"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = optionBg),
                                border = borderStroke
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A' + optIdx).toString(),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Text(
                                        text = optText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = tColor
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (answerRevealed) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else if (isSelected) Icons.Default.Cancel else Icons.Default.ArrowForward,
                                            contentDescription = "Correctness Indicator",
                                            tint = if (isCorrect) Color(0xFF2E7D32) else if (isSelected) Color(0xFFC62828) else Color.Transparent
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Solution Explanation Block (Revealed after answers selected)
                    AnimatedVisibility(
                        visible = answerRevealed,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = "Explanation Notebook",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "High-Yield Solution Notes:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Text(
                                    text = currentQuestion.solutionExplanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Next button cycle
                    if (answerRevealed) {
                        Button(
                            onClick = { viewModel.nextPracticeQuestion() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("next_practice_button")
                        ) {
                            Text("Next Question")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.NavigateNext, contentDescription = "Arrow Next")
                        }
                    }
                }
            }
        }
    }
}
