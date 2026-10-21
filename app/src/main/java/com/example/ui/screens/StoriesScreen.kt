package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel
import com.example.ui.screens.status.MyStatusHeaderRow
import com.example.ui.screens.status.StatusListItem
import com.example.ui.screens.status.StatusStoryViewerScreen
import com.example.ui.screens.status.TextStatusComposerScreen
import com.example.ui.screens.status.UserStatusGroup

@Composable
fun StoriesScreen(
    viewModel: ChatViewModel,
    onCameraClick: () -> Unit = {},
    onTextStatusClick: () -> Unit = {}
) {
    val stories by viewModel.stories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeStoryGroupToView by remember { mutableStateOf<UserStatusGroup?>(null) }
    var isViewingMyStatus by remember { mutableStateOf(false) }
    var showInternalTextStatusComposer by remember { mutableStateOf(false) }
    var isViewedSectionExpanded by remember { mutableStateOf(true) }

    // Group stories by userId
    val statusGroups by remember(stories, currentUser.id) {
        derivedStateOf {
            val myId = currentUser.id
            val myStories = stories.filter { it.userId == myId || it.userId == "me" }
            val otherStories = stories.filter { it.userId != myId && it.userId != "me" }

            val myGroup = if (myStories.isNotEmpty()) {
                UserStatusGroup(
                    userId = myId,
                    userName = "My Status",
                    userAvatar = currentUser.avatarUrl,
                    items = myStories,
                    hasUnread = myStories.any { !it.viewed },
                    latestTimestamp = myStories.maxOfOrNull { it.timestamp } ?: 0L
                )
            } else null

            val contactGroups = otherStories
                .groupBy { it.userId }
                .map { (userId, userItems) ->
                    val first = userItems.first()
                    UserStatusGroup(
                        userId = userId,
                        userName = first.userName.ifBlank { "Contact" },
                        userAvatar = first.userAvatar,
                        items = userItems,
                        hasUnread = userItems.any { !it.viewed },
                        latestTimestamp = userItems.maxOfOrNull { it.timestamp } ?: 0L
                    )
                }
                .sortedByDescending { it.latestTimestamp }

            val recentUpdates = contactGroups.filter { it.hasUnread }
            val viewedUpdates = contactGroups.filter { !it.hasUnread }

            Triple(myGroup, recentUpdates, viewedUpdates)
        }
    }

    val (myStatusGroup, recentUpdates, viewedUpdates) = statusGroups

    // 1. Full-screen WhatsApp Status Story Viewer
    activeStoryGroupToView?.let { group ->
        StatusStoryViewerScreen(
            group = group,
            isMyStatus = isViewingMyStatus,
            onDismiss = {
                activeStoryGroupToView = null
                isViewingMyStatus = false
            },
            onStoryViewed = { storyId ->
                viewModel.markStoryViewed(storyId)
            },
            onDeleteStory = { storyId ->
                viewModel.deleteStory(storyId)
            },
            onReplyToStory = { story, replyText ->
                viewModel.replyToStory(story, replyText)
            },
            onNextGroup = {
                // If there are other recent updates, advance to next
                if (!isViewingMyStatus) {
                    val currentIndex = recentUpdates.indexOfFirst { it.userId == group.userId }
                    if (currentIndex != -1 && currentIndex < recentUpdates.size - 1) {
                        activeStoryGroupToView = recentUpdates[currentIndex + 1]
                    } else {
                        activeStoryGroupToView = null
                    }
                } else {
                    activeStoryGroupToView = null
                    isViewingMyStatus = false
                }
            },
            onPrevGroup = {
                if (!isViewingMyStatus) {
                    val currentIndex = recentUpdates.indexOfFirst { it.userId == group.userId }
                    if (currentIndex > 0) {
                        activeStoryGroupToView = recentUpdates[currentIndex - 1]
                    }
                }
            }
        )
        return
    }

    // 2. Full-screen WhatsApp Text Status Composer
    if (showInternalTextStatusComposer) {
        TextStatusComposerScreen(
            onDismiss = { showInternalTextStatusComposer = false },
            onPostStatus = { text, colorHex, fontIdx ->
                viewModel.addStory(
                    caption = text,
                    mediaUrl = null,
                    isImage = false,
                    backgroundColor = colorHex,
                    fontIndex = fontIdx
                )
                showInternalTextStatusComposer = false
            }
        )
        return
    }

    // 3. Main Updates / Status Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("stories_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Status",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }

            // My Status Row
            item {
                MyStatusHeaderRow(
                    myGroup = myStatusGroup,
                    onViewMyStatus = {
                        if (myStatusGroup != null && myStatusGroup.items.isNotEmpty()) {
                            isViewingMyStatus = true
                            activeStoryGroupToView = myStatusGroup
                        } else {
                            onCameraClick()
                        }
                    },
                    onAddCameraStatus = onCameraClick,
                    onAddTextStatus = { showInternalTextStatusComposer = true }
                )
            }

            // Divider below My Status
            item {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Recent Updates Section
            if (recentUpdates.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent updates",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    )
                }

                items(recentUpdates, key = { it.userId }) { group ->
                    StatusListItem(
                        group = group,
                        onClick = {
                            isViewingMyStatus = false
                            activeStoryGroupToView = group
                        }
                    )
                }
            }

            // Viewed Updates Section (Collapsible)
            if (viewedUpdates.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isViewedSectionExpanded = !isViewedSectionExpanded }
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Viewed updates",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (isViewedSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isViewedSectionExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (isViewedSectionExpanded) {
                    items(viewedUpdates, key = { it.userId }) { group ->
                        StatusListItem(
                            group = group,
                            onClick = {
                                isViewingMyStatus = false
                                activeStoryGroupToView = group
                            }
                        )
                    }
                }
            }

            // WhatsApp E2EE Encryption Notice
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Your status updates are end-to-end encrypted",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}
