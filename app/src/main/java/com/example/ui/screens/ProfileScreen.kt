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
import com.example.data.local.UserProgressEntity
import com.example.ui.theme.*
import com.example.viewmodel.SikshyaaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: SikshyaaViewModel) {
    val progressRef by viewModel.userProgress.collectAsState()
    val progress = progressRef ?: UserProgressEntity()

    // Editor states local fields
    var editMode by remember { mutableStateOf(false) }
    var tempName by remember(progress.id) { mutableStateOf(progress.name) }
    var tempSchool by remember(progress.id) { mutableStateOf(progress.schoolCollege) }
    var tempClass by remember(progress.id) { mutableStateOf(progress.studentClass) }
    var tempLang by remember(progress.id) { mutableStateOf(progress.preferredLanguage) }
    var tempTargets by remember(progress.id) { mutableStateOf(progress.targetExams) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar visual representation and greeting banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Avatar profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = progress.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = "Enrolled Level: ${progress.studentClass}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Current Stream: ${progress.schoolCollege}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = { editMode = !editMode },
                    modifier = Modifier.testTag("toggle_profile_edit_btn")
                ) {
                    Icon(
                        imageVector = if (editMode) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = "Edit Profile Info"
                    )
                }
            }
        }

        // Expanded metadata details card or edit modes
        AnimatedContent(
            targetState = editMode,
            label = "Editor animations"
        ) { isEditing ->
            if (isEditing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Edit Student Information Data:", fontWeight = FontWeight.Bold, color = TextNavy)

                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name")
                        )

                        OutlinedTextField(
                            value = tempSchool,
                            onValueChange = { tempSchool = it },
                            label = { Text("School / College Name") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_school")
                        )

                        OutlinedTextField(
                            value = tempClass,
                            onValueChange = { tempClass = it },
                            label = { Text("Enrolled Class Level") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_class")
                        )

                        OutlinedTextField(
                            value = tempLang,
                            onValueChange = { tempLang = it },
                            label = { Text("Preferred Languages") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_lang")
                        )

                        OutlinedTextField(
                            value = tempTargets,
                            onValueChange = { tempTargets = it },
                            label = { Text("Target Exams to Crack") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_targets")
                        )

                        Button(
                            onClick = {
                                viewModel.updateStudentProfile(tempName, tempClass, tempSchool, tempLang, tempTargets)
                                editMode = false
                            },
                            modifier = Modifier.align(Alignment.End).testTag("save_profile_btn")
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Student Profile Details", fontWeight = FontWeight.Bold, color = TextNavy)
                        HorizontalDivider()

                        ProfileDetailRow("School / College", progress.schoolCollege)
                        ProfileDetailRow("Target Exams", progress.targetExams)
                        ProfileDetailRow("Preferred Language", progress.preferredLanguage)
                        ProfileDetailRow("Rank Placement Status", progress.rankEstimation)
                    }
                }
            }
        }

        // Analytics Graphical Bars
        Text(
            text = "Learning analytics & Trends",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Weekly Learning Session Hours", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Total: 7.4 hrs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Icon(Icons.Default.TrendingUp, contentDescription = "Trend up indicators", tint = XpGreen)
                }

                // Graphical Columns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val weeklyHours = listOf(1.2f, 0.8f, 1.5f, 0.5f, 1.8f, 2.1f, 0.9f)
                    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

                    weeklyHours.forEachIndexed { idx, hrs ->
                        val barHeightFactor = hrs / 2.5f

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("${hrs}h", fontSize = 9.sp, color = TextGray)
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(barHeightFactor)
                                    .background(
                                        if (hrs >= 1.5f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Text(days[idx], fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Gamified badges / items lists
        Text(
            text = "Unlocked Academic Badges",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        )

        val unlockedAchievements = listOf(
            Triple("Streak Master Badge", "Maintain a 3+ Days study milestone without disruption.", StreakGold),
            Triple("AI Pioneer Credential", "Ask the Sikshyaa Chat Tutor 10+ critical concept definitions.", Color(0xFF673AB7)),
            Triple("Mock Gladiator Badge", "Record a pass score in a Full-length simulation exam.", Color(0xFF00796B))
        )

        unlockedAchievements.forEach { ach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(ach.third.copy(alpha = 0.15f), shape = RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OfflineBolt,
                            contentDescription = "Badge trophy",
                            tint = ach.third,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(ach.first, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(ach.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = TextGray, fontSize = 13.sp)
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color = TextNavy,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(200.dp),
            textAlign = TextAlign.End
        )
    }
}
