package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.*
import com.example.viewmodel.SikshyaaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiTutorScreen(viewModel: SikshyaaViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isTyping by viewModel.isAiTyping.collectAsState()
    val activeMode by viewModel.aiTutorMode.collectAsState()
    val vocalActive by viewModel.vocalFeedbackSimulated.collectAsState()

    var inputMessageText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showOcrScannerSim by remember { mutableStateOf(false) }
    var scanProgressPercent by remember { mutableStateOf(0f) }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(chatHistory.size, isTyping) {
        if (chatHistory.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    val assistantModes = listOf("Regular", "Exam Mode", "Quick Revision Mode", "Viva Practice Mode", "Interview Preparation Mode")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AI Chat Assistant Mode Filter Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Sparkle",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sikshyaa AI Chat Tutor",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Play feedback button
                    IconButton(
                        onClick = { viewModel.triggerVoiceFeedback() },
                        modifier = Modifier.testTag("voice_feed_btn")
                    ) {
                        Icon(
                            imageVector = if (vocalActive) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Simulated Speak",
                            tint = if (vocalActive) XpGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    // Clear Chat history button
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.testTag("clear_chat_btn")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = AlertRed)
                    }
                }
            }

            // Modes Horizontal Slider
            ScrollableTabRow(
                selectedTabIndex = assistantModes.indexOf(activeMode).coerceAtLeast(0),
                edgePadding = 0.dp,
                divider = {},
                modifier = Modifier.testTag("tutor_modes_slider")
            ) {
                assistantModes.forEach { mode ->
                    Tab(
                        selected = (activeMode == mode),
                        onClick = { viewModel.setAiMode(mode) },
                        text = {
                            Text(
                                text = mode,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.tertiary)

        // Main Dialog Chat Bubbles List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chatHistory.isEmpty()) {
                // Empty state greeting recommendations
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Robot assistant",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Welcome to Sikshyaa AI!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "I am powered by the advanced gemini model and customized with Nepali academic syllabus guides. Ask me to solve questions, explain anatomy, list fundamental rights, or quiz you!",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Prompt suggestion widgets
                    Text(
                        text = "Suggested Questions:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    SuggestedPromptPill("Translate: 'What are the main causes of soil erosion in Nepal' to Nepali") { inputMessageText = it }
                    SuggestedPromptPill("Explain Newtonian Mechanics second law with calculations") { inputMessageText = it }
                    SuggestedPromptPill("Explain clinical significance of Common Peroneal Nerve damage") { inputMessageText = it }
                    SuggestedPromptPill("Brief the Constitution of Nepal Article 31 Fundamental Rights") { inputMessageText = it }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatHistory) { msg ->
                        ChatBubbleRow(msg = msg, vocalActive = vocalActive)
                    }

                    if (isTyping) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(bottomEnd = 12.dp, topStart = 12.dp, topEnd = 12.dp))
                                        .background(Color.LightGray.copy(alpha = 0.3f))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.5.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Sikshyaa Tutor is thinking...",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Voice simulator playback notification bar
        if (vocalActive && isTyping) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(XpGreen.copy(alpha = 0.15f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = "Feedback simulation", tint = XpGreen)
                    Text("AI Read-aloud will speak as soon as text is formulated!", style = MaterialTheme.typography.labelSmall.copy(color = XpGreen, fontWeight = FontWeight.Bold))
                }
            }
        }

        // Bottom Text Input Controls Bar
        HorizontalDivider(color = MaterialTheme.colorScheme.tertiary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Camera scanner trigger button
            IconButton(
                onClick = {
                    showOcrScannerSim = true
                    scanProgressPercent = 0f
                },
                modifier = Modifier.testTag("scanner_sim_btn")
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Scan Note Scanner", tint = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = inputMessageText,
                onValueChange = { inputMessageText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input"),
                placeholder = { Text("Ask anything...") },
                maxLines = 3,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedContainerColor = MaterialTheme.colorScheme.background
                )
            )

            IconButton(
                onClick = {
                    if (inputMessageText.isNotBlank()) {
                        viewModel.sendTutorMessage(inputMessageText)
                        inputMessageText = ""
                    }
                },
                modifier = Modifier
                    .testTag("send_msg_btn")
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send text", tint = Color.White)
            }
        }
    }

    // Camera Scan simulate Dialogue Box
    if (showOcrScannerSim) {
        val scope = rememberCoroutineScope()
        
        LaunchedEffect(showOcrScannerSim) {
            while (scanProgressPercent < 1.0f) {
                delay(30)
                scanProgressPercent += 0.05f
            }
            delay(500)
            showOcrScannerSim = false
            inputMessageText = "Taking a picture of my notes: 'Explain the difference between skeletal, smooth, and cardiac muscles under Gross Anatomy MBBS'"
        }

        AlertDialog(
            onDismissRequest = { showOcrScannerSim = false },
            confirmButton = {},
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = "OCR scanning", tint = MaterialTheme.colorScheme.primary)
                    Text("Sikshyaa OCR Camera Scanner", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        // Matrix scanner look details
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(
                                progress = { scanProgressPercent },
                                modifier = Modifier.size(60.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Aligning camera to page... ${(scanProgressPercent * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
                        }
                    }
                    Text("Position your textbook or handwritten homework notes within the frame. Our AI OCR engine will extract text and explain answers instantly!", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            }
        )
    }
}

@Composable
fun SuggestedPromptPill(text: String, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(text) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.HelpOutline, contentDescription = "Recommended Ask", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface))
        }
    }
}

@Composable
fun ChatBubbleRow(msg: ChatMessageEntity, vocalActive: Boolean) {
    val isUser = msg.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .testTag("chat_bubble_${msg.id}"),
            shape = if (isUser) {
                RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 16.dp, bottomEnd = 0.dp)
            } else {
                RoundedCornerShape(topStart = 16.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp)
            },
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isUser) "Student" else "Sikshyaa AI Tutor (${msg.mode})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    text = msg.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}
