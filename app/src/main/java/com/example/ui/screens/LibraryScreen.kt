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
import com.example.data.local.LibraryItemEntity
import com.example.ui.theme.*
import com.example.viewmodel.SikshyaaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(viewModel: SikshyaaViewModel) {
    val items by viewModel.libraryItems.collectAsState()
    val searchQuery by viewModel.librarySearchText.collectAsState()
    val selectedItem by viewModel.selectedLibraryItem.collectAsState()

    var activeCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Notes", "Books", "Solutions", "Formula Sheets", "Loksewa Materials", "Medical Notes")

    // Filter logic
    val filteredItems = items.filter { item ->
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                            item.subject.contains(searchQuery, ignoreCase = true)
        val matchesCat = activeCategory == "All" || item.category == activeCategory
        matchesSearch && matchesCat
    }

    // Toggle Reader Screen vs Shelf Screen
    if (selectedItem != null) {
        PdfSimulationReaderScreen(
            item = selectedItem!!,
            viewModel = viewModel,
            onClose = { viewModel.selectLibraryItem(null) }
        )
    } else {
        // Shelf Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sikshyaa Digital Library",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input"),
                placeholder = { Text("Search by book title or subject...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Glass") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchText("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Categories list Slider
            Text(
                text = "Resource Categories:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == activeCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("lib_cat_chip_${cat.lowercase().replace(" ","_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Resource Shelf Grid
            Text(
                text = "Available PDFs & Syllabus Guides (${filteredItems.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Empty Shelf", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Text("No PDF materials match your filters.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredItems.forEach { item ->
                        LibraryItemCard(item = item, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryItemCard(item: LibraryItemEntity, viewModel: SikshyaaViewModel) {
    var isDownloadingLocally by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.isDownloaded) {
                    viewModel.selectLibraryItem(item)
                }
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // PDF Visual Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        if (item.isDownloaded) MaterialTheme.colorScheme.tertiary else Color.LightGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isDownloaded) Icons.Default.Article else Icons.Default.FileDownload,
                    contentDescription = "PDF type",
                    tint = if (item.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Info Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.subject,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "• ${item.category}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${item.numberOfPages} Pages • Author: ${item.author}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                )
                
                if (isDownloadingLocally && !item.isDownloaded) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }

            // Action section: Download vs Open / Bookmark
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleBookmark(item) },
                    modifier = Modifier.testTag("bookmark_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark notes",
                        tint = if (item.isBookmarked) StreakGold else TextGray
                    )
                }

                if (!item.isDownloaded) {
                    FilledTonalButton(
                        onClick = {
                            isDownloadingLocally = true
                            viewModel.simulatePdfDownload(item)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("download_${item.id}"),
                        enabled = !isDownloadingLocally
                    ) {
                        Text("Get", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Offline Access unlocked",
                        tint = XpGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Full PDF Simulated Reader Screen with Annotation & Highlighting!
@Composable
fun PdfSimulationReaderScreen(
    item: LibraryItemEntity,
    viewModel: SikshyaaViewModel,
    onClose: () -> Unit
) {
    val activePage by viewModel.pdfReadingPage.collectAsState()
    var localNoteText by remember(item.id) { mutableStateOf(item.localNotes) }
    var highlightedText by remember { mutableStateOf("") }
    
    val simulatedLessons = getSimulatedPdfPhrases(item.title)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Reader Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Exit Reader")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(180.dp)
                    )
                    Text(
                        text = "Page $activePage of ${item.numberOfPages}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }
            }

            Row {
                IconButton(onClick = { viewModel.toggleBookmark(item) }) {
                    Icon(
                        imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Quick Bookmark",
                        tint = if (item.isBookmarked) StreakGold else TextGray
                    )
                }
                IconButton(
                    onClick = {
                        val hl = simulatedLessons.getOrNull(activePage - 1) ?: "High weightage formula segment."
                        viewModel.addHighlight(item, hl)
                    },
                    modifier = Modifier.testTag("highlight_button")
                ) {
                    Icon(Icons.Default.DriveFileRenameOutline, contentDescription = "Simulate Text Highlight", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.tertiary)

        // Inner Split Screen Scroll: Simulated PDF content vs Sidebar Annotations
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visual PDF view Simulation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SIKSHYAA OFFICIAL CLASS MATERIAL",
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp)
                        )
                        Text(
                            text = "PAGE $activePage",
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    HorizontalDivider()

                    Text(
                        text = simulatedLessons.getOrNull(activePage - 1) ?: "Review standard syllabus documentation.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                    
                    Text(
                        text = "--- End of Page $activePage ---",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Stepper Page Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.setPdfPage(activePage - 1) },
                    enabled = activePage > 1,
                    modifier = Modifier.testTag("prev_page_btn")
                ) {
                    Icon(Icons.Default.NavigateBefore, contentDescription = "Behind")
                    Text("Prev Page")
                }

                Button(
                    onClick = { viewModel.setPdfPage(activePage + 1) },
                    enabled = activePage < item.numberOfPages,
                    modifier = Modifier.testTag("next_page_btn")
                ) {
                    Text("Next Page")
                    Icon(Icons.Default.NavigateNext, contentDescription = "Front")
                }
            }

            HorizontalDivider()

            // Highlight Lists Indicator
            if (item.highlightsJson.isNotEmpty()) {
                Text(
                    text = "Saved Highlights on this E-Book:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )

                item.highlightsJson.split("|||").forEach { hl ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StreakGold.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, StreakGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Highlight", tint = StreakGold, modifier = Modifier.size(16.dp))
                            Text(hl, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.weight(1f))
                        }
                    }
                }

                TextButton(
                    onClick = { viewModel.clearHighlights(item) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Clear Highlights", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            }

            // Annotation note input card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.NoteAlt, contentDescription = "Write Notes Icon", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Offline study notepad annotation:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }

                    OutlinedTextField(
                        value = localNoteText,
                        onValueChange = { localNoteText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("note_input_${item.id}"),
                        placeholder = { Text("Add personal study notes, equations, or important formula highlights here...") },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedContainerColor = MaterialTheme.colorScheme.background
                        )
                    )

                    Button(
                        onClick = { viewModel.saveLibraryNotes(item, localNoteText) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_notes_btn")
                    ) {
                        Text("Save Notes")
                    }
                }
            }
        }
    }
}

private fun getSimulatedPdfPhrases(title: String): List<String> {
    return when {
         title.contains("Physics") -> {
             listOf(
                 "CHAPTER 1: MECHANICS AND LAWS OF GRAVITATION\n\nNewton's universal law of gravitation states that every particle in the universe attracts every other particle with a force proportional to the product of their masses and inversely proportional to the square of distance:\n\nF = G * (m1 * m2) / d²\n\nWhere the gravitational constant G is approximately 6.674e-11 N m²/kg². This force represents a conservative field.",
                 "CHAPTER 1.2: ROTATIONAL KINETICS AND TORQUE\n\nWhen a force of magnitude F acts at a displacement radius r from a pivot point, the resulting Torque is given by:\n\nτ = r * F * sin(θ)\n\nIt produces an angular acceleration (α) directly proportional to the moment of inertia (I) of the rotating body: τ = I * α.",
                 "CHAPTER 2: FLUID MECHANICS & ARISTOTELIAN FORCES\n\nArchimedes' Principle states that any body completely or partially submerged in a fluid is buoyed up by a force equal to the weight of fluid displaced by body:\n\nFb = ρ * V * g\n\nWhere ρ is the density of fluid, V is the displaced volume, and g is the local gravitational field vector."
             )
         }
         title.contains("Anatomy") -> {
             listOf(
                 "SECTION 1: BONES OF THE UPPER LIMB (CLAVICLE)\n\nThe clavicle is the only long bone in the human body that lies horizontally. It is subcutaneous and presents a double curvature: convex medially and concave laterally.\n\nIt transmits forces from the upper limb to the axial skeleton, maintaining the shoulder girdle away from the thorax for maximum mobility.",
                 "SECTION 2: MUSCLES OF THE PEC ZONE\n\nPectoralis Major acts to adduct, flex, and medially rotate the humerus bone.\n\nInnervation: Medial and lateral pectoral nerves (roots C5-T1). Clavicular head flexes humerus; sternocostal head extends it.",
                 "SECTION 3: HISTOLOGY & BONE DENSITIES\n\nCompact bone forms the hard shell-like cortex while trabecular bone forms the internal cancellous networks. Osteocytes communicate via networks of canaliculi channels."
             )
         }
         else -> {
             listOf(
                 "SIKSHYAA SYLLABUS DOCUMENT REVIEW - CHAPTER 1\n\nThis material contains high weightage exam content. Review and compile important summary formula cards in your custom library organizer.",
                 "SIKSHYAA SYLLABUS DOCUMENT REVIEW - CHAPTER 2\n\nEnsure that you cross reference this topic with the old questions PDF to find previous question structures under SEE/NEB boards.",
                 "SIKSHYAA SYLLABUS DOCUMENT REVIEW - CHAPTER 3\n\nSave custom note cards below to sync them automatically across devices offline."
             )
         }
    }
}
