package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MorphRepository
import com.example.model.MorphChecklistItem
import com.example.model.MorphNote
import com.example.model.MorphPalette
import com.example.model.UserProfile
import com.example.ui.components.MorphAuthBottomSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphScreen(
    repository: MorphRepository = MorphRepository.getInstance(LocalContext.current),
    currentUser: UserProfile? = null
) {
    val context = LocalContext.current
    val connectedAccount by repository.connectedAccountFlow.collectAsState()
    val notes by repository.notesFlow.collectAsState()

    LaunchedEffect(connectedAccount?.accountId) {
        if (connectedAccount != null) {
            repository.syncWithServer()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var selectedTag by remember { mutableStateOf("All") }

    var editingNote by remember { mutableStateOf<MorphNote?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var newNoteAsChecklist by remember { mutableStateOf(false) }
    var showAuthSheet by remember { mutableStateOf(false) }
    var pendingCreateAfterAuth by remember { mutableStateOf<Boolean?>(null) } // false = note, true = checklist

    // Filter notes by search query and tag
    val filteredNotes = remember(notes, searchQuery, selectedTag) {
        notes.filter { !it.isArchived }.filter { note ->
            val matchesQuery = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.checklist.any { it.text.contains(searchQuery, ignoreCase = true) }

            val matchesTag = when (selectedTag) {
                "All" -> true
                "Pinned" -> note.isPinned
                else -> note.labels.contains(selectedTag)
            }
            matchesQuery && matchesTag
        }
    }

    val pinnedNotes = remember(filteredNotes) { filteredNotes.filter { it.isPinned } }
    val otherNotes = remember(filteredNotes) { filteredNotes.filter { !it.isPinned } }

    val allTags = remember(notes) {
        val tags = mutableSetOf("All", "Pinned")
        notes.forEach { tags.addAll(it.labels) }
        tags.toList()
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Google Keep Style Floating Search Bar
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_morph_notes),
                            contentDescription = "Omkun Morph",
                            tint = Color(0xFF007AFF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search your notes in Morph...",
                                    fontSize = 15.sp,
                                    color = Color(0xFF757575)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("morph_search_input")
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF757575),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                imageVector = if (isGridView) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                                contentDescription = "Toggle View",
                                tint = Color(0xFF5F6368),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { showAuthSheet = true },
                            modifier = Modifier.testTag("morph_account_button")
                        ) {
                            val activeAcc = connectedAccount
                            if (activeAcc != null) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0160E3)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!activeAcc.avatarUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = activeAcc.avatarUrl,
                                                contentDescription = activeAcc.displayName,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                text = activeAcc.displayName.ifBlank { activeAcc.email }.take(1).uppercase(Locale.getDefault()),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .padding(1.5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = "Sign in to Morph",
                                    tint = Color(0xFF0160E3),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                // Filter Tag Chips
                if (connectedAccount != null && allTags.size > 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allTags) { tag ->
                            val isSelected = tag == selectedTag
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTag = tag },
                                label = { Text(tag, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE8F0FE),
                                    selectedLabelColor = Color(0xFF1A73E8)
                                )
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (connectedAccount != null) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    // Secondary FAB for Checklist
                    SmallFloatingActionButton(
                        onClick = {
                            isCreatingNew = true
                            newNoteAsChecklist = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A73E8),
                        elevation = FloatingActionButtonDefaults.elevation(2.dp),
                        modifier = Modifier.testTag("morph_new_checklist_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckBox,
                            contentDescription = "New Checklist",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Main FAB: New Note
                    FloatingActionButton(
                        onClick = {
                            isCreatingNew = true
                            newNoteAsChecklist = false
                        },
                        shape = RoundedCornerShape(18.dp),
                        containerColor = Color(0xFF007AFF),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(3.dp),
                        modifier = Modifier.testTag("morph_new_note_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Note",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (connectedAccount == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 420.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_morph_notes),
                                contentDescription = null,
                                tint = Color(0xFF0160E3),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sign in to Omkun Morph",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connect your Google or Omkun account to securely sync your personal notes, checklists, and let Oj AI read, create, or manage notes in your account.",
                            fontSize = 13.5.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 19.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(22.dp))
                        Button(
                            onClick = { showAuthSheet = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("morph_sign_in_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign in & Activate Morph",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_morph_notes),
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No notes in ${connectedAccount?.email ?: "Omkun Morph"} yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5F6368)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Tap the + button or ask Oj AI to create a note or checklist",
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            if (isGridView) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                            Text(
                                "PINNED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5F6368),
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(pinnedNotes, key = { it.id }) { note ->
                            MorphNoteCard(
                                note = note,
                                onClick = { editingNote = note },
                                onTogglePin = { repository.togglePin(note.id) },
                                onToggleChecklist = { index -> repository.toggleChecklistItem(note.id, index) }
                            )
                        }
                    }

                    if (otherNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                                Text(
                                    "OTHERS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5F6368),
                                    modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
                                )
                            }
                        }
                        items(otherNotes, key = { it.id }) { note ->
                            MorphNoteCard(
                                note = note,
                                onClick = { editingNote = note },
                                onTogglePin = { repository.togglePin(note.id) },
                                onToggleChecklist = { index -> repository.toggleChecklistItem(note.id, index) }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item {
                            Text(
                                "PINNED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5F6368),
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(pinnedNotes, key = { it.id }) { note ->
                            MorphNoteCard(
                                note = note,
                                onClick = { editingNote = note },
                                onTogglePin = { repository.togglePin(note.id) },
                                onToggleChecklist = { index -> repository.toggleChecklistItem(note.id, index) }
                            )
                        }
                    }

                    if (otherNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                Text(
                                    "OTHERS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5F6368),
                                    modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
                                )
                            }
                        }
                        items(otherNotes, key = { it.id }) { note ->
                            MorphNoteCard(
                                note = note,
                                onClick = { editingNote = note },
                                onTogglePin = { repository.togglePin(note.id) },
                                onToggleChecklist = { index -> repository.toggleChecklistItem(note.id, index) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Note Editor Dialog (Create / Edit)
    if (editingNote != null || isCreatingNew) {
        val targetNote = editingNote ?: MorphNote(
            isChecklist = newNoteAsChecklist,
            colorHex = MorphPalette.Default
        )

        MorphNoteEditorDialog(
            initialNote = targetNote,
            onDismiss = {
                editingNote = null
                isCreatingNew = false
            },
            onSave = { updated ->
                repository.saveNote(updated)
                editingNote = null
                isCreatingNew = false
                Toast.makeText(context, "Note saved to Omkun Morph", Toast.LENGTH_SHORT).show()
            },
            onDelete = { id ->
                repository.deleteNote(id)
                editingNote = null
                isCreatingNew = false
                Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // OAuth-Style Omkun Morph Account Login & Permission Sheet
    if (showAuthSheet) {
        MorphAuthBottomSheet(
            repository = repository,
            currentUser = currentUser,
            onDismiss = {
                showAuthSheet = false
                pendingCreateAfterAuth = null
            },
            onAuthenticated = { account ->
                Toast.makeText(context, "Signed in as ${account.email}", Toast.LENGTH_SHORT).show()
                val pendingChecklist = pendingCreateAfterAuth
                pendingCreateAfterAuth = null
                if (pendingChecklist != null) {
                    newNoteAsChecklist = pendingChecklist
                    isCreatingNew = true
                }
            }
        )
    }
}

/**
 * Google Keep style individual note card
 */
@Composable
fun MorphNoteCard(
    note: MorphNote,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleChecklist: (Int) -> Unit
) {
    val cardColor = remember(note.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(note.colorHex))
        } catch (_: Exception) {
            Color.White
        }
    }

    val isDefaultWhite = note.colorHex.equals("#FFFFFF", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardColor,
        border = if (isDefaultWhite) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
        shadowElevation = if (isDefaultWhite) 1.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("morph_note_card_${note.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Title and Pin button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF202124),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin Note",
                        tint = if (note.isPinned) Color(0xFF1A73E8) else Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Body: Checklist or Plain text
            if (note.isChecklist && note.checklist.isNotEmpty()) {
                val previewItems = note.checklist.take(4)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    previewItems.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleChecklist(index) }
                        ) {
                            Icon(
                                imageVector = if (item.isDone) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (item.isDone) Color(0xFF1A73E8) else Color(0xFF757575),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.text,
                                fontSize = 13.sp,
                                color = if (item.isDone) Color(0xFF757575) else Color(0xFF202124),
                                textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (note.checklist.size > 4) {
                        Text(
                            "+${note.checklist.size - 4} more items",
                            fontSize = 11.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                        )
                    }
                }
            } else if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    fontSize = 13.sp,
                    color = Color(0xFF3C4043),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            // Labels / tags
            if (note.labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    note.labels.take(3).forEach { label ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.05f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = Color(0xFF5F6368),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full Note Editor (Google Keep style)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphNoteEditorDialog(
    initialNote: MorphNote,
    onDismiss: () -> Unit,
    onSave: (MorphNote) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialNote.title) }
    var content by remember { mutableStateOf(initialNote.content) }
    var isChecklist by remember { mutableStateOf(initialNote.isChecklist) }
    var checklistItems by remember { mutableStateOf(initialNote.checklist) }
    var colorHex by remember { mutableStateOf(initialNote.colorHex) }
    var isPinned by remember { mutableStateOf(initialNote.isPinned) }
    var newChecklistText by remember { mutableStateOf("") }

    val editorColor = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (_: Exception) {
            Color.White
        }
    }

    Dialog(
        onDismissRequest = {
            // Auto-save on dismiss if anything was entered
            if (title.isNotBlank() || content.isNotBlank() || checklistItems.isNotEmpty()) {
                onSave(
                    initialNote.copy(
                        title = title.trim(),
                        content = content.trim(),
                        isChecklist = isChecklist,
                        checklist = checklistItems,
                        colorHex = colorHex,
                        isPinned = isPinned
                    )
                )
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = editorColor,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = editorColor),
                    navigationIcon = {
                        IconButton(onClick = {
                            if (title.isNotBlank() || content.isNotBlank() || checklistItems.isNotEmpty()) {
                                onSave(
                                    initialNote.copy(
                                        title = title.trim(),
                                        content = content.trim(),
                                        isChecklist = isChecklist,
                                        checklist = checklistItems,
                                        colorHex = colorHex,
                                        isPinned = isPinned
                                    )
                                )
                            } else {
                                onDismiss()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF202124)
                            )
                        }
                    },
                    title = {
                        Text(
                            "Omkun Morph Note",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF202124)
                        )
                    },
                    actions = {
                        IconButton(onClick = { isPinned = !isPinned }) {
                            Icon(
                                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin",
                                tint = if (isPinned) Color(0xFF1A73E8) else Color(0xFF5F6368)
                            )
                        }

                        IconButton(onClick = { isChecklist = !isChecklist }) {
                            Icon(
                                imageVector = if (isChecklist) Icons.Outlined.Notes else Icons.Outlined.CheckBox,
                                contentDescription = "Toggle Checklist",
                                tint = Color(0xFF5F6368)
                            )
                        }

                        if (initialNote.title.isNotBlank() || initialNote.content.isNotBlank() || initialNote.checklist.isNotEmpty()) {
                            IconButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, title)
                                    val text = buildString {
                                        if (title.isNotBlank()) append("$title\n\n")
                                        if (isChecklist) {
                                            checklistItems.forEach {
                                                append(if (it.isDone) "[x] " else "[ ] ")
                                                append("${it.text}\n")
                                            }
                                        } else {
                                            append(content)
                                        }
                                    }
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Morph Note"))
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share",
                                    tint = Color(0xFF5F6368)
                                )
                            }

                            IconButton(onClick = { onDelete(initialNote.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFD93025)
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                // Color Picker bar at bottom
                Surface(
                    color = editorColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            "Color palette",
                            fontSize = 11.sp,
                            color = Color(0xFF5F6368),
                            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(MorphPalette.Colors) { hex ->
                                val chipColor = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (_: Exception) {
                                    Color.White
                                }
                                val isSelected = hex.equals(colorHex, ignoreCase = true)

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(chipColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF1A73E8) else Color(0xFFBDBDBD),
                                            shape = CircleShape
                                        )
                                        .clickable { colorHex = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF1A73E8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Title Field
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            "Title",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF202124)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Content Field: Checklist or Plain text
                if (isChecklist) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(checklistItems, key = { it.id }) { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        checklistItems = checklistItems.map {
                                            if (it.id == item.id) it.copy(isDone = !it.isDone) else it
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isDone) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                        contentDescription = "Toggle",
                                        tint = if (item.isDone) Color(0xFF1A73E8) else Color(0xFF5F6368)
                                    )
                                }

                                Text(
                                    text = item.text,
                                    fontSize = 15.sp,
                                    color = if (item.isDone) Color(0xFF757575) else Color(0xFF202124),
                                    textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )

                                IconButton(
                                    onClick = {
                                        checklistItems = checklistItems.filter { it.id != item.id }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color(0xFF757575),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        item {
                            // Add new checklist item input
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF5F6368),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextField(
                                    value = newChecklistText,
                                    onValueChange = { newChecklistText = it },
                                    placeholder = {
                                        Text(
                                            "List item...",
                                            fontSize = 15.sp,
                                            color = Color(0xFF757575)
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                if (newChecklistText.isNotBlank()) {
                                    IconButton(onClick = {
                                        checklistItems = checklistItems + MorphChecklistItem(
                                            id = UUID.randomUUID().toString(),
                                            text = newChecklistText.trim(),
                                            isDone = false
                                        )
                                        newChecklistText = ""
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Add",
                                            tint = Color(0xFF1A73E8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = {
                            Text(
                                "Note...",
                                fontSize = 16.sp,
                                color = Color(0xFF757575)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = Color(0xFF202124),
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                Text(
                    text = "Edited ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(initialNote.updatedAt))}",
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier
                )
            }
        }
    }
}
