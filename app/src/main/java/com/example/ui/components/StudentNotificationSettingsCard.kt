package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodel.AcademyViewModel

/**
 * A Material 3 toggle settings card allowing students to subscribe or
 * unsubscribe from push notifications regarding new lectures and upcoming tests.
 */
@Composable
fun StudentNotificationSettingsCard(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val lectureEnabled by viewModel.lectureNotificationsEnabled.collectAsStateWithLifecycle()
    val testEnabled by viewModel.testNotificationsEnabled.collectAsStateWithLifecycle()
    val isAnySubscribed = lectureEnabled || testEnabled

    var testNotificationSent by remember { mutableStateOf(false) }

    // Android 13+ POST_NOTIFICATIONS runtime permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleAllNotifications(true)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("notification_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Master Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isAnySubscribed) AmberGold.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAnySubscribed) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Notifications",
                            tint = if (isAnySubscribed) RoyalNavy else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Push Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAnySubscribed) "Subscribed to class & test alerts" else "All alerts muted",
                            fontSize = 11.sp,
                            color = if (isAnySubscribed) EmeraldGreen else Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Master Toggle Switch
                Switch(
                    checked = isAnySubscribed,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleAllNotifications(enabled)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberGold,
                        checkedTrackColor = RoyalNavy,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    ),
                    modifier = Modifier.testTag("switch_master_notifications")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Sub-Option 1: New Lectures & Live Classes
            NotificationToggleRow(
                title = "New Lectures & Live Classes",
                subtitle = "Alerts 15 mins before Banshraj Sir goes live & new uploads",
                icon = Icons.Default.VideoLibrary,
                iconTint = Color(0xFF2563EB),
                isChecked = lectureEnabled,
                onCheckedChange = { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    viewModel.toggleLectureNotifications(enabled)
                },
                testTag = "switch_lecture_notifications"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-Option 2: Upcoming Tests & DPPs
            NotificationToggleRow(
                title = "Upcoming Tests & DPP Deadlines",
                subtitle = "Reminders for scheduled All-India Tests and daily practice problem releases",
                icon = Icons.Default.Quiz,
                iconTint = AmberGold,
                isChecked = testEnabled,
                onCheckedChange = { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    viewModel.toggleTestNotifications(enabled)
                },
                testTag = "switch_test_notifications"
            )

            // Preview / Test Notification Action Button
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {
                    testNotificationSent = true
                    viewModel.toggleLectureNotifications(lectureEnabled)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_send_test_notification"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = RoyalNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Preview Sample Alert Notification",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalNavy
                )
            }

            // Confirmation banner when preview is tapped
            AnimatedVisibility(
                visible = testNotificationSent,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sample Notification Dispatched: \"Electrostatics Live Class with Banshraj Sir starts in 15 mins!\"",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AmberGold,
                checkedTrackColor = RoyalNavy
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
