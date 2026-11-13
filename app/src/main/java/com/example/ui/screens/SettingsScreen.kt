package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModelTier
import com.example.data.model.EditorSettings
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackgroundDark
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioRose
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.data.repository.UserAccount
import com.example.util.ApkShareUtil
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentUser: UserAccount?,
    editorSettings: EditorSettings,
    onUpdateSettings: (EditorSettings) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSharingApk by remember { mutableStateOf(false) }
    val apkSize = remember { ApkShareUtil.getApkSizeDisplay(context) }

    var fontSize by remember { mutableFloatStateOf(editorSettings.fontSizeSp.toFloat()) }
    var isWordWrap by remember { mutableStateOf(editorSettings.isWordWrap) }
    var isLineNumbers by remember { mutableStateOf(editorSettings.isLineNumbersVisible) }
    var isAutoSave by remember { mutableStateOf(editorSettings.isAutoSaveEnabled) }
    var defaultTier by remember { mutableStateOf(editorSettings.defaultAiModel) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackgroundDark)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "Customize mobile editor, AI models, and account",
                fontSize = 13.sp,
                color = TextSecondaryDark
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Account Profile Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(StudioCardDark)
                                .border(1.5.dp, StudioCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = StudioCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.displayName ?: "Natnael Guest Developer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = currentUser?.email ?: "Offline Local Storage Mode",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (currentUser != null) StudioGreen else StudioAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentUser?.isGoogleAccount == true) "Google Cloud Sync Active" else if (currentUser != null) "Email Account Active (Local Studio)" else "Local On-Device Sandboxing",
                                    fontSize = 11.sp,
                                    color = if (currentUser != null) StudioGreen else StudioAmber,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (currentUser != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = StudioBorderDark)
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioRose.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = StudioRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Out of Account",
                                color = StudioRose,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Editor Preferences Section
        item {
            SettingsCategoryHeader(title = "EDITOR PREFERENCES", icon = Icons.Default.Code)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Font size slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Font Size", fontSize = 14.sp, color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
                        Text(text = "${fontSize.toInt()} sp", fontSize = 13.sp, color = StudioCyan, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = fontSize,
                        onValueChange = {
                            fontSize = it
                            onUpdateSettings(editorSettings.copy(fontSizeSp = it.toInt()))
                        },
                        valueRange = 10f..22f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioCyan,
                            activeTrackColor = StudioCyan,
                            inactiveTrackColor = StudioCardDark
                        )
                    )

                    Divider(color = StudioBorderDark)

                    // Line Numbers Toggle
                    SettingsToggleRow(
                        title = "Line Numbers Column",
                        subtitle = "Display numbered gutter along left edge",
                        checked = isLineNumbers,
                        onCheckedChange = {
                            isLineNumbers = it
                            onUpdateSettings(editorSettings.copy(isLineNumbersVisible = it))
                        }
                    )

                    Divider(color = StudioBorderDark)

                    // Word Wrap Toggle
                    SettingsToggleRow(
                        title = "Soft Word Wrap",
                        subtitle = "Wrap long code lines without horizontal scrolling",
                        checked = isWordWrap,
                        onCheckedChange = {
                            isWordWrap = it
                            onUpdateSettings(editorSettings.copy(isWordWrap = it))
                        }
                    )

                    Divider(color = StudioBorderDark)

                    // Auto Save Toggle
                    SettingsToggleRow(
                        title = "Auto Save Edits",
                        subtitle = "Save changes automatically during editing",
                        checked = isAutoSave,
                        onCheckedChange = {
                            isAutoSave = it
                            onUpdateSettings(editorSettings.copy(isAutoSaveEnabled = it))
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // AI Configuration Section
        item {
            SettingsCategoryHeader(title = "AI CONFIGURATION", icon = Icons.Default.AutoAwesome)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Default Model Tier",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Preferred assistant model for chat & code assistance",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AiModelTier.entries.forEach { tier ->
                        val isSelected = defaultTier == tier
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StudioCyan.copy(alpha = 0.15f) else StudioCardDark)
                                .border(1.dp, if (isSelected) StudioCyan else StudioBorderDark, RoundedCornerShape(8.dp))
                                .clickable {
                                    defaultTier = tier
                                    onUpdateSettings(editorSettings.copy(defaultAiModel = tier))
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tier.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) StudioCyan else TextPrimaryDark
                                )
                                Text(
                                    text = tier.description,
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            }
                            if (isSelected) {
                                Text(text = "Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioCyan)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Share App (.APK) Section
        item {
            SettingsCategoryHeader(title = "SHARE & DISTRIBUTE APP", icon = Icons.Default.Share)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_apk_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StudioCyan.copy(alpha = 0.15f))
                                .border(1.dp, StudioCyan, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = StudioCyan,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Share App (.apk file)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Direct offline installer package • $apkSize",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Share Natnael Code Editor AI directly with other Android devices via Bluetooth, WhatsApp, Telegram, Google Drive, or Nearby Share without requiring the Google Play Store.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (!isSharingApk) {
                                isSharingApk = true
                                coroutineScope.launch {
                                    ApkShareUtil.shareAppApk(context)
                                    isSharingApk = false
                                }
                            }
                        },
                        enabled = !isSharingApk,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_apk_button")
                    ) {
                        if (isSharingApk) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = StudioBackgroundDark,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Preparing APK package...",
                                color = StudioBackgroundDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = StudioBackgroundDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share App APK ($apkSize)",
                                color = StudioBackgroundDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // App Info / Version Branding
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Natnael Code Editor AI",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "v2.5.0 • Powered by Gemini AI",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = StudioCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = StudioCyan,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, color = TextPrimaryDark, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondaryDark)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = StudioCyan,
                checkedTrackColor = StudioCyan.copy(alpha = 0.3f),
                uncheckedTrackColor = StudioCardDark
            )
        )
    }
}
