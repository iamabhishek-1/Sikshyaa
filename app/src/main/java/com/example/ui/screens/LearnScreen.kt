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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseProgressEntity
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextNavy
import com.example.ui.theme.XpGreen
import com.example.viewmodel.SikshyaaViewModel

@Composable
fun LearnScreen(viewModel: SikshyaaViewModel) {
    val coursesRef by viewModel.coursesProgress.collectAsState()
    val coursesByLevel = viewModel.getCoursesByLevel()

    var selectedLevel by remember { mutableStateOf("SEE (Class 10)") }
    var selectedSubject by remember { mutableStateOf("") }
    
    // Auto-select first subject in level
    LaunchedEffect(selectedLevel) {
        selectedSubject = coursesByLevel[selectedLevel]?.firstOrNull() ?: ""
    }

    var selectedLectureId by remember { mutableStateOf<String?>(null) }
    var showVideoPlayerSim by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Stream / Syllabus Filter Row
        ScrollableTabRow(
            selectedTabIndex = coursesByLevel.keys.indexOf(selectedLevel).coerceAtLeast(0),
            edgePadding = 16.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth().testTag("learn_level_tabs")
        ) {
            coursesByLevel.keys.forEach { level ->
                Tab(
                    selected = (selectedLevel == level),
                    onClick = { selectedLevel = level },
                    text = { Text(text = level, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar for Subjects
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                coursesByLevel[selectedLevel]?.forEach { subject ->
                    val isSelected = selectedSubject == subject
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSubject = subject }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent
                            )
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = subject,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            VerticalDivider(
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxHeight().width(1.dp)
            )

            // Main Content Area: Videos and Notes List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "$selectedSubject Lessons",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )

                // Match with local DB course progress to find state
                val matchingCourse = coursesRef.find {
                    it.category.contains(selectedLevel.take(5)) && it.subject.contains(selectedSubject.take(5))
                } ?: CourseProgressEntity(
                    courseId = "temp",
                    category = selectedLevel,
                    subject = selectedSubject,
                    title = "Foundation Syllabus Lectures",
                    videosCompleted = 1,
                    totalVideos = 6,
                    notesRead = 1,
                    totalNotes = 3,
                    completedPercent = 30
                )

                // Progress Indicator Ring Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Subject Progress",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = matchingCourse.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Videos: ${matchingCourse.videosCompleted}/${matchingCourse.totalVideos} • Notes: ${matchingCourse.notesRead}/${matchingCourse.totalNotes}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            )
                        }

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { matchingCourse.completedPercent.toFloat() / 100f },
                                modifier = Modifier.size(54.dp),
                                color = XpGreen,
                                strokeWidth = 5.dp
                            )
                            Text(
                                text = "${matchingCourse.completedPercent}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = XpGreen)
                            )
                        }
                    }
                }

                // Video Lectures List
                Text(
                    text = "Video Lectures (" + matchingCourse.totalVideos + ")",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )

                // Interactive Video Entries
                for (lectureIdx in 1..matchingCourse.totalVideos) {
                    val isLectureDone = lectureIdx <= matchingCourse.videosCompleted
                    val lectureName = getLectureTitle(selectedSubject, lectureIdx)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLectureId = "lec_$lectureIdx"
                                showVideoPlayerSim = true
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLectureDone) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                        )
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
                                    .size(40.dp)
                                    .background(
                                        if (isLectureDone) XpGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLectureDone) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                                    contentDescription = "Lecture Indicator",
                                    tint = if (isLectureDone) XpGreen else MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lecture #0$lectureIdx",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                )
                                Text(
                                    text = lectureName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (!isLectureDone) {
                                TextButton(
                                    onClick = {
                                        viewModel.completeLessonVideo(matchingCourse.courseId)
                                    },
                                    modifier = Modifier.testTag("complete_lecture_${lectureIdx}")
                                ) {
                                    Text("Done", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Already Finished",
                                    tint = XpGreen
                                )
                            }
                        }
                    }
                }

                // Reference Reading Notes List
                Text(
                    text = "High Yield Notes & Revision Formulas",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )

                for (noteIdx in 1..matchingCourse.totalNotes) {
                    val isNoteRead = noteIdx <= matchingCourse.notesRead
                    val noteTitle = getNoteTitle(selectedSubject, noteIdx)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = "Syllabus Notes",
                                tint = MaterialTheme.colorScheme.secondary
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Revision Note #0$noteIdx",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                )
                                Text(
                                    text = noteTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (!isNoteRead) {
                                FilledTonalButton(
                                    onClick = {
                                        viewModel.completeNotesRead(matchingCourse.courseId)
                                    },
                                    modifier = Modifier.testTag("read_notes_${noteIdx}")
                                ) {
                                    Text("Mark Read")
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Read Already",
                                    tint = XpGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Video Player Dialogue box Simulation
    if (showVideoPlayerSim) {
        AlertDialog(
            onDismissRequest = { showVideoPlayerSim = false },
            confirmButton = {
                Button(onClick = { showVideoPlayerSim = false }) {
                    Text("Close Class")
                }
            },
            title = {
                Text(
                    text = "Sikshyaa Lecture Hall Player",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visual Simulator Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = "Active Playing",
                                tint = Color.LightGray,
                                modifier = Modifier.size(36.dp)
                            )
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.DarkGray
                            )
                            Text(
                                "Streaming: Speed 1.2x • 1080p Webcast",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        text = "This simulates our edge-cached offline-sync lecture tool. Closing this will not automatically mark it completed—use the 'Done' action buttons on the lecture card to log study streaks!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }
}

private fun getLectureTitle(subject: String, index: Int): String {
    return when {
        subject.contains("Physic", true) -> {
            listOf(
                "Introduction to Classical Mechanics & Units",
                "Newton's Law of Universal Gravitation",
                "Work, Energy & Conservative Forces",
                "Rotational Kinematics & Kinetic Energy",
                "Coulomb's Law & Electric Fields",
                "Magnetic Induction & AC Circuits",
                "Refraction & Polarisation of Lights",
                "Modern Nuclear Physics Breakdown"
            ).getOrNull(index - 1) ?: "Interactive Physics Lecture #$index"
        }
        subject.contains("Anatomy", true) -> {
            listOf(
                "Osteology of the Upper Limb Clavicle & Scapula",
                "Axilia Complex, Brachial Plexus Pathways",
                "Gross Anatomy of Anterior Forearm Compartment",
                "Functional Anatomy of Hand Muscles & Grip",
                "Nerve Injury Clinical Correlation: Wrist Drop",
                "Joints of Upper Limbs & Shoulder Dislocation"
            ).getOrNull(index - 1) ?: "MBBS Clinical Lecture #$index"
        }
        subject.contains("Knowledge", true) || subject.contains("GK", true) -> {
            listOf(
                "Geographical Boundaries & Rivers of Nepal",
                "Administrative Divisions: Federalism & 7 Provinces",
                "Key Historical Milestones: Shah, Rana & Democratic Eras",
                "International Relations & SAARC Organization Org",
                "Major National Heritage Parks & Protected Animals",
                "Latest Current Affairs & Scientific Milestones"
            ).getOrNull(index - 1) ?: "Loksewa GK Lecture #$index"
        }
        else -> "Lecture Overview Chapter #$index - Detailed Video syllabus guide."
    }
}

private fun getNoteTitle(subject: String, index: Int): String {
    return when {
        subject.contains("Physic", true) -> {
            listOf(
                "Summary Force Laws & Friction Coefficients",
                "Electrostatics Equations Companion sheet",
                "Essential Sound Interference Rules"
            ).getOrNull(index - 1) ?: "Core Formula Formula sheet #$index"
        }
        subject.contains("Anatomy", true) -> {
            listOf(
                "Gross Muscles Insertion, Action & Origin Table",
                "Vascular Supply of the Forearm Schematics",
                "High-Yield Clinical Viva Checklist Cards"
            ).getOrNull(index - 1) ?: "High-Yield Viva Notes #$index"
        }
        else -> "Standard Study Guide Revision card #$index"
    }
}
