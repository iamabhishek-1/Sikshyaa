package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextNavy
import com.example.viewmodel.SikshyaaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: SikshyaaViewModel) {
    var selectedTabIdx by remember { mutableStateOf(0) }

    val tabs = listOf(
        NavigationItem("Home", Icons.Default.Home, Icons.Outlined.Home),
        NavigationItem("Learn", Icons.Default.School, Icons.Outlined.School),
        NavigationItem("Practice", Icons.Default.CheckCircle, Icons.Outlined.CheckCircle),
        NavigationItem("Library", Icons.Default.MenuBook, Icons.Outlined.MenuBook),
        NavigationItem("AI Tutor", Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
        NavigationItem("Profile", Icons.Default.AccountCircle, Icons.Outlined.AccountCircle)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = "Logo Bolt",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Sikshyaa",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Premium Tag Pill
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Premium Unlocked",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Theme Toggle switcher
                            val isDark by viewModel.isDarkTheme.collectAsState()
                            IconButton(
                                onClick = { viewModel.toggleTheme() },
                                modifier = Modifier.size(36.dp).testTag("theme_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme Mode",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("app_bottom_bar")
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIdx == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIdx = index },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 9.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = "${tab.title} Tab"
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.title.lowercase().replace(" ", "_")}")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing // Prevent camera notch clipping perfectly
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Animate transition across tabs smoothly
            AnimatedContent(
                targetState = selectedTabIdx,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "Core Screens navigation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTab = { selectedTabIdx = it }
                    )
                    1 -> LearnScreen(viewModel = viewModel)
                    2 -> PracticeScreen(viewModel = viewModel)
                    3 -> LibraryScreen(viewModel = viewModel)
                    4 -> AiTutorScreen(viewModel = viewModel)
                    5 -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector
)
