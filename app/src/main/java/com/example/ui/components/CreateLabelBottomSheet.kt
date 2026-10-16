package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import com.example.model.ChatLabel
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Conversation

/**
 * 20 Colors Palette exactly matching Screenshot 1 ("Choose colour" 4x5 grid)
 */
val WhatsAppListColors = listOf(
    // Row 1
    "#FF5722", // 1. Deep Orange
    "#007AFF", // 2. Bright Blue
    "#FFA000", // 3. Amber / Golden Yellow
    "#AB47BC", // 4. Purple
    "#78909C", // 5. Blue Gray
    // Row 2
    "#C0CA33", // 6. Lime / Olive Green
    "#E91E63", // 7. Pink / Magenta
    "#6D4C41", // 8. Earth Brown
    "#00838F", // 9. Deep Cyan / Ocean Blue
    "#FDD835", // 10. Sunny Yellow
    // Row 3
    "#00897B", // 11. Emerald / Jade Teal
    "#F48FB1", // 12. Soft Pastel Pink
    "#80CBC4", // 13. Mint Green
    "#D32F2F", // 14. Crimson Red
    "#BDBDBD", // 15. Light Gray
    // Row 4
    "#9CCC65", // 16. Light Grass Green
    "#BCAAA4", // 17. Caramel / Warm Tan
    "#81D4FA", // 18. Sky Blue
    "#D1C4E9", // 19. Soft Lavender
    "#7E57C2"  // 20. Indigo / Violet
)

/**
 * Daily-use suggestions replacing business/order labels as requested by user.
 */
data class DailyLabelSuggestion(
    val name: String,
    val colorHex: String
)

val DailyUseSuggestions = listOf(
    DailyLabelSuggestion("Family", "#FF5722"),
    DailyLabelSuggestion("Friends", "#00897B"),
    DailyLabelSuggestion("Work", "#007AFF"),
    DailyLabelSuggestion("College", "#AB47BC"),
    DailyLabelSuggestion("Important", "#E91E63"),
    DailyLabelSuggestion("Favorites", "#FFA000"),
    DailyLabelSuggestion("Urgent", "#D32F2F"),
    DailyLabelSuggestion("Office", "#00838F")
)

fun parseHexColorSafely(hex: String, fallback: Color = Color(0xFF007AFF)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

/**
 * Full WhatsApp "New list" experience matching Screenshots 1, 2, and 3:
 * - Step 1: "New list" with text input, color badge, and daily use suggestions (Screenshot 2).
 * - Color Picker: "Choose colour" 20-color grid with selection checkmark (Screenshot 1).
 * - Step 2: "Add to list" contact selector with search and checkmark FAB (Screenshot 3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLabelBottomSheet(
    sheetState: SheetState,
    conversations: List<Conversation>,
    initialLabel: ChatLabel? = null,
    onDismissRequest: () -> Unit,
    onSaveLabel: (name: String, colorHex: String, selectedChatIds: List<String>) -> Unit,
    onDeleteLabel: ((String) -> Unit)? = null
) {
    var currentStep by remember { mutableStateOf(1) } // 1 = New list, 2 = Add to list
    var labelName by remember(initialLabel) { mutableStateOf(initialLabel?.name ?: "") }
    var selectedColor by remember(initialLabel) {
        mutableStateOf(initialLabel?.colorHex ?: WhatsAppListColors.first())
    }
    var showColorPicker by remember { mutableStateOf(false) }
    val selectedChatIds = remember(initialLabel) {
        mutableStateListOf<String>().apply {
            if (initialLabel != null) {
                addAll(initialLabel.chatIds)
            }
        }
    }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Hardware back navigation handler
    BackHandler(enabled = currentStep == 2 || showColorPicker) {
        if (showColorPicker) {
            showColorPicker = false
        } else if (currentStep == 2) {
            currentStep = 1
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFD1D5DB)
            ) {}
        },
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
            .testTag("create_label_bottom_sheet")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp, max = 650.dp)
        ) {
            if (showColorPicker) {
                // ==========================================
                // SCREEN 1: "Choose colour" (Screenshot 1)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .testTag("choose_colour_view")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose colour",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111B21),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showColorPicker = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Color Picker",
                                tint = Color(0xFF54656F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4x5 Grid of 20 colors exactly like Screenshot 1
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        items(WhatsAppListColors) { hex ->
                            val color = parseHexColorSafely(hex)
                            val isSelected = selectedColor.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        selectedColor = hex
                                        showColorPicker = false
                                    }
                                    .testTag("color_swatch_$hex"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (currentStep == 1) {
                // ==========================================
                // SCREEN 2: "New list" (Screenshot 2)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .testTag("new_list_step1_view")
                ) {
                    // Header with X and "New list"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(36.dp)
                                .testTag("close_new_list_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF111B21),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Text(
                            text = if (initialLabel != null) "Edit list" else "New list",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111B21),
                            modifier = Modifier.align(Alignment.Center)
                        )

                        if (initialLabel != null && onDeleteLabel != null && initialLabel.id !in listOf("all", "direct", "groups", "sms")) {
                            IconButton(
                                onClick = {
                                    onDeleteLabel(initialLabel.id)
                                    onDismissRequest()
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(36.dp)
                                    .testTag("delete_label_header_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete list",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // "List name" label
                    Text(
                        text = "List name",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF54656F),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Text Input Field with Circular Color Badge (Screenshot 2)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF111B21)),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (labelName.isEmpty()) {
                                    Text(
                                        text = "Example: Family, Work, Friends",
                                        color = Color(0xFF8696A0),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                BasicTextField(
                                    value = labelName,
                                    onValueChange = { labelName = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 15.5.sp,
                                        color = Color(0xFF111B21),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF007AFF)),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("list_name_text_field")
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Circular Color Preview Badge that opens Color Picker (Screenshot 2)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColorSafely(selectedColor))
                                    .clickable { showColorPicker = true }
                                    .testTag("badge_open_color_picker")
                            )
                        }
                    }

                    // Helper Explanatory Text
                    Text(
                        text = "Any list you create becomes a filter at the top of your Chats tab.",
                        fontSize = 13.sp,
                        color = Color(0xFF667781),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 18.dp)
                    )

                    // "Suggestions" Section with Daily-Use Labels
                    Text(
                        text = "Suggestions",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF54656F),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 240.dp)
                    ) {
                        items(DailyUseSuggestions) { suggestion ->
                            val color = parseHexColorSafely(suggestion.colorHex)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        labelName = suggestion.name
                                        selectedColor = suggestion.colorHex
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .testTag("suggestion_${suggestion.name}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = suggestion.name,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF111B21)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (initialLabel != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (labelName.isNotBlank()) {
                                        onSaveLabel(labelName.trim(), selectedColor, selectedChatIds.toList())
                                        onDismissRequest()
                                    }
                                },
                                enabled = labelName.isNotBlank(),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("save_edit_label_button")
                            ) {
                                Text("Save", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (labelName.isNotBlank()) {
                                        currentStep = 2
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    contentColor = Color(0xFF111827)
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .testTag("edit_chats_button")
                            ) {
                                Text("Edit chats (${selectedChatIds.size})", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (labelName.isNotBlank()) {
                                    currentStep = 2
                                }
                            },
                            enabled = labelName.isNotBlank(),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF007AFF),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE9EDEF),
                                disabledContentColor = Color(0xFF8696A0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(bottom = 4.dp)
                                .testTag("add_people_or_groups_button")
                        ) {
                            Text(
                                text = "Add people or groups",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // SCREEN 3: "Add to list" (Screenshot 3)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("add_to_list_step2_view")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        // Top Bar: Back arrow, Title, Search icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            IconButton(
                                onClick = { currentStep = 1 },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("back_to_step1_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF111B21)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Add to list",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111B21),
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { isSearchActive = !isSearchActive },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF111B21)
                                )
                            }
                        }

                        // Search Bar (if search is active)
                        AnimatedVisibility(visible = isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search chats or contacts...", fontSize = 14.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF007AFF),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .testTag("add_to_list_search_input")
                            )
                        }

                        // Subtitle note
                        Text(
                            text = "Add as many people or groups as you want. Only you can see who's included on your lists.",
                            fontSize = 13.sp,
                            color = Color(0xFF667781),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        HorizontalDivider(color = Color(0xFFE9EDEF))

                        // Section header
                        Text(
                            text = "Frequently contacted",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF54656F),
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                        )

                        // Filtered conversations
                        val filteredList = remember(conversations, searchQuery) {
                            if (searchQuery.isBlank()) conversations
                            else conversations.filter {
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.lastMessageText.contains(searchQuery, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = 70.dp)
                        ) {
                            items(filteredList, key = { it.id }) { chat ->
                                val isSelected = selectedChatIds.contains(chat.id)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) selectedChatIds.remove(chat.id)
                                            else selectedChatIds.add(chat.id)
                                        }
                                        .padding(vertical = 10.dp)
                                        .testTag("chat_select_item_${chat.id}")
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (chat.isGroup) Color(0xFFE2EDFC)
                                                else parseHexColorSafely(selectedColor).copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (chat.isGroup) {
                                            Icon(
                                                imageVector = Icons.Default.Groups,
                                                contentDescription = null,
                                                tint = Color(0xFF007AFF),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Text(
                                                text = chat.title.take(1).uppercase(),
                                                color = parseHexColorSafely(selectedColor),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chat.title,
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF111B21)
                                        )
                                        if (chat.lastMessageText.isNotBlank()) {
                                            Text(
                                                text = chat.lastMessageText,
                                                fontSize = 13.sp,
                                                color = Color(0xFF667781),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Selection checkbox / badge
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color(0xFF007AFF) else Color.Transparent
                                            )
                                            .border(
                                                width = if (isSelected) 0.dp else 1.5.dp,
                                                color = if (isSelected) Color.Transparent else Color(0xFFD1D5DB),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom-Right Checkmark FAB (Screenshot 3)
                    FloatingActionButton(
                        onClick = {
                            if (labelName.isNotBlank()) {
                                onSaveLabel(labelName.trim(), selectedColor, selectedChatIds.toList())
                                onDismissRequest()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Color(0xFF111B21),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 20.dp)
                            .size(56.dp)
                            .testTag("save_list_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save list",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet displayed when a user long-presses a label chip on the Home page.
 * Provides quick actions: Filter, Edit list (name, color, members), and Delete list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelOptionsBottomSheet(
    label: ChatLabel,
    onDismissRequest: () -> Unit,
    onOpenList: () -> Unit,
    onEditList: () -> Unit,
    onDeleteList: () -> Unit
) {
    val isDeletable = label.id !in listOf("all", "direct", "groups", "sms")
    val labelColor = parseHexColorSafely(label.colorHex)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFD1D5DB)
            ) {}
        },
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("label_options_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header: Label preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(labelColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(labelColor)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = if (isDeletable) "${label.chatIds.size} chats in this list" else "Default chat tab",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(6.dp))

            // Option 1: Open / View this list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onOpenList()
                        onDismissRequest()
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp)
                    .testTag("label_option_filter"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF0160E3),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Filter chats by this list",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
            }

            // Option 2: Edit list (name, color, chats)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onDismissRequest()
                        onEditList()
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp)
                    .testTag("label_option_edit"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = Color(0xFF007AFF),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Edit list name & members",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
            }

            // Option 3: Delete list (only for non-system labels)
            if (isDeletable) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onDismissRequest()
                            onDeleteList()
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .testTag("label_option_delete"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Delete list",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}
