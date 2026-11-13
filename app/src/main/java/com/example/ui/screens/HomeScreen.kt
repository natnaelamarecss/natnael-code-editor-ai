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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.example.util.ApkShareUtil
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackgroundDark
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.data.repository.UserAccount

@Composable
fun HomeScreen(
    currentUser: UserAccount?,
    projects: List<Project>,
    onSelectProject: (Project) -> Unit,
    onCreateNewProject: (template: String) -> Unit,
    onNavigateToAi: () -> Unit,
    onOpenAiBuilder: () -> Unit,
    onNavigateToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSharingApk by remember { mutableStateOf(false) }
    val apkSize = remember { ApkShareUtil.getApkSizeDisplay(context) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackgroundDark)
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Hero Header & Profile Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    StudioCyan.copy(alpha = 0.12f),
                                    StudioPurple.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "NATNAEL CODE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioCyan,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Welcome back,",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = currentUser?.displayName ?: "Mobile Developer",
                                    fontSize = 14.sp,
                                    color = TextSecondaryDark
                                )
                            }

                            // Cloud Sync Status Avatar
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(StudioCardDark)
                                    .border(1.5.dp, StudioCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.Code,
                                    contentDescription = null,
                                    tint = if (currentUser != null) StudioGreen else StudioCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Code Anywhere. Build Anything. Powered by AI.",
                            fontSize = 13.sp,
                            color = TextSecondaryDark,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // AI Project Builder Banner Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurple.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAiBuilder)
                    .testTag("ai_builder_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(StudioCyan, StudioPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Project Builder",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Describe an app, let Gemini generate the full files",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Text(
                        text = "Build →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioCyan
                    )
                }
            }
        }

        // Share App (.APK) Banner Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyan.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isSharingApk) {
                            isSharingApk = true
                            coroutineScope.launch {
                                ApkShareUtil.shareAppApk(context)
                                isSharingApk = false
                            }
                        }
                    }
                    .testTag("home_share_apk_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioCyan.copy(alpha = 0.15f))
                            .border(1.dp, StudioCyan, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSharingApk) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = StudioCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share APK",
                                tint = StudioCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Share Natnael Code (.apk)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = if (isSharingApk) "Preparing APK package for sharing..." else "Export & send offline APK package • $apkSize",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioCyan)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Share",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioBackgroundDark
                        )
                    }
                }
            }
        }

        // Quick Actions Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "QUICK ACTIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StudioCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "New HTML",
                    subtitle = "Web App",
                    icon = "🌐",
                    modifier = Modifier.weight(1f),
                    onClick = { onCreateNewProject("html") }
                )
                QuickActionCard(
                    title = "New React",
                    subtitle = "JSX Components",
                    icon = "⚛️",
                    modifier = Modifier.weight(1f),
                    onClick = { onCreateNewProject("react") }
                )
                QuickActionCard(
                    title = "New Python",
                    subtitle = "Scripting & Data",
                    icon = "🐍",
                    modifier = Modifier.weight(1f),
                    onClick = { onCreateNewProject("python") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Ask AI Assistant",
                    subtitle = "Reasoning & Fixes",
                    icon = "✨",
                    accentColor = StudioPurple,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAi
                )
                QuickActionCard(
                    title = "All Projects",
                    subtitle = "${projects.size} Saved",
                    icon = "📂",
                    accentColor = StudioGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProjects
                )
            }
        }

        // Recent Projects Section
        item {
            Spacer(modifier = Modifier.height(26.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT PROJECTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View all",
                    fontSize = 12.sp,
                    color = StudioCyan,
                    modifier = Modifier.clickable(onClick = onNavigateToProjects)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (projects.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "💻", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No projects yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Create a project or ask AI to build one",
                            fontSize = 13.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
        } else {
            items(projects.take(4), key = { it.id }) { project ->
                RecentProjectRow(
                    project = project,
                    onClick = { onSelectProject(project) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Activity Stats Card
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "MOBILE STUDIO STATS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Projects", value = "${projects.size}")
                        StatItem(label = "AI Tiers", value = "3 Models")
                        StatItem(label = "Runtimes", value = "HTML/Py/JSX")
                        StatItem(label = "Storage", value = if (currentUser != null) "Cloud" else "Local")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: String,
    accentColor: Color = StudioCyan,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("quick_action_${title}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondaryDark,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentProjectRow(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("recent_project_${project.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioCardDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (project.template.lowercase()) {
                        "html" -> "🌐"
                        "react" -> "⚛️"
                        "python" -> "🐍"
                        else -> "📄"
                    },
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    maxLines = 1
                )
                Text(
                    text = if (project.description.isNotBlank()) project.description else "Template: ${project.template.uppercase()}",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioCyan.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = project.template.uppercase(),
                    color = StudioCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        Text(text = label, fontSize = 11.sp, color = TextSecondaryDark)
    }
}
