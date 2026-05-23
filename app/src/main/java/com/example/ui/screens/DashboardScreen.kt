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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProgressEntity
import com.example.ui.theme.*
import com.example.viewmodel.SikshyaaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: SikshyaaViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val progress by viewModel.userProgress.collectAsState()
    val courses by viewModel.coursesProgress.collectAsState()

    val actualProgress = progress ?: UserProgressEntity()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Welcome Card Header with Gradient Accent
        WelcomeHeaderSection(actualProgress)

        // 2. Daily Goal & Streak Stats Container
        StatsStreakRow(actualProgress, viewModel)

        // 3. Quick Action Buttons Grid
        QuickActionsGrid(onNavigateToTab)

        // 4. Daily MCQ Question Quick Challenge Widget
        DailyChallengeWidget(viewModel, onNavigateToTab)

        // 5. Recommended Lessons & Continue Progress List
        ContinueLearningSection(courses, onNavigateToTab)

        // 6. Weak Subject & Security Alerts
        AlertsSection(actualProgress)
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun WelcomeHeaderSection(progress: UserProgressEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("welcome_header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Academic Icon",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "SIKSHYAA SUPER APP",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f),
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Namaste, ${progress.name}!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Preparing for: ${progress.targetExams} (${progress.studentClass})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Level indicator pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = progress.rankEstimation,
                        color = StreakGold,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsStreakRow(progress: UserProgressEntity, viewModel: SikshyaaViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Daily Streak Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(100.dp)
                .clickable { viewModel.addStudyMinutes(15) }, // Interactive simulator
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Fire",
                        tint = StreakGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Streak Status",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextGray, fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "${progress.streak} Days",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = "Tap to study 15m",
                    style = MaterialTheme.typography.bodySmall.copy(color = StreakGold, fontSize = 10.sp)
                )
            }
        }

        // XP Progress Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = "XP Gold Medals",
                        tint = XpGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Total XP Earned",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "${progress.totalXp} XP",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = "${progress.studyTimeMinutes} min studied",
                    style = MaterialTheme.typography.bodySmall.copy(color = XpGreen, fontSize = 10.sp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickActionsGrid(onNavigateToTab: (Int) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val itemModifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()

            QuickActionItem(
                title = "Ask AI",
                icon = Icons.Outlined.AutoAwesome,
                color = Color(0xFFE3F2FD),
                iconColor = Color(0xFF1976D2),
                modifier = itemModifier,
                onClick = { onNavigateToTab(4) } // AI Tutor Tab is 4
            )

            QuickActionItem(
                title = "Practice MCQ",
                icon = Icons.Outlined.CheckCircle,
                color = Color(0xFFE8F5E9),
                iconColor = Color(0xFF388E3C),
                modifier = itemModifier,
                onClick = { onNavigateToTab(2) } // Practice is 2
            )

            QuickActionItem(
                title = "Study PDFs",
                icon = Icons.Outlined.LibraryBooks,
                color = Color(0xFFFFF3E0),
                iconColor = Color(0xFFF57C00),
                modifier = itemModifier,
                onClick = { onNavigateToTab(3) } // Library is 3
            )
        }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }
    }
}

@Composable
fun DailyChallengeWidget(viewModel: SikshyaaViewModel, onNavigateToTab: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Challenge Gold Star",
                        tint = StreakGold
                    )
                    Text(
                        text = "Daily MCQ Challenge",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StreakGold.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+15 XP",
                        color = StreakGold,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Text(
                text = "Under the Constitution of Nepal, which Article guarantees the Right to Education?",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        viewModel.setPracticeFilter("General Knowledge", "Beginner")
                        onNavigateToTab(2) // Jump to Practice Tab
                    },
                    modifier = Modifier.testTag("solve_challenge_button")
                ) {
                    Text("Solve Now")
                }
            }
        }
    }
}

@Composable
fun ContinueLearningSection(
    courses: List<com.example.data.local.CourseProgressEntity>,
    onNavigateToTab: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Continue Learning",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (courses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No enrolled courses yet. Let's enroll now!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        } else {
            courses.take(2).forEach { course ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTab(1) }, // Study tab is 1
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (course.completedPercent == 100) Icons.Default.Check else Icons.Default.PlayArrow,
                                contentDescription = "Topic icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${course.subject} • ${course.category}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            )
                            LinearProgressIndicator(
                                progress = { course.completedPercent.toFloat() / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Text(
                            text = "${course.completedPercent}%",
                            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertsSection(progress: UserProgressEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
        border = BorderStroke(1.dp, Color(0xFFFFEBA2))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert Gold Indicator",
                tint = Color(0xFF856404)
            )
            Column {
                Text(
                    text = "Weak Chapter Alert",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF856404))
                )
                Text(
                    text = "Your accuracy in Clinical Pharmacology under MBBS is 42%. We recommend asking the AI Tutor to do a quick revision!",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF856404))
                )
            }
        }
    }
}
