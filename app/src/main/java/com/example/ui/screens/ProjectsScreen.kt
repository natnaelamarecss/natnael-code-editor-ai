package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioRose
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun ProjectsScreen(
    projects: List<Project>,
    onSelectProject: (Project) -> Unit,
    onCreateProject: (name: String, template: String) -> Unit,
    onDeleteProject: (Project) -> Unit,
    onDuplicateProject: (Project) -> Unit,
    onExportProject: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var isNewProjectDialogOpen by remember { mutableStateOf(false) }
    var newProjName by remember { mutableStateOf("") }
    var newProjTemplate by remember { mutableStateOf("html") }

    val templates = listOf(
        "html" to "HTML/CSS/JS Web App",
        "react" to "React (JSX) Interactive",
        "python" to "Python 3.12 Script",
        "node" to "Node.js JavaScript",
        "blank" to "Blank Project"
    )

    val filteredProjects = remember(projects, searchQuery, selectedFilter) {
        projects.filter { proj ->
            val matchesQuery = proj.name.contains(searchQuery, ignoreCase = true) ||
                    proj.description.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "HTML" -> proj.template.equals("html", ignoreCase = true)
                "React" -> proj.template.equals("react", ignoreCase = true)
                "Python" -> proj.template.equals("python", ignoreCase = true)
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isNewProjectDialogOpen = true },
                containerColor = StudioCyan,
                contentColor = com.example.ui.theme.StudioBackgroundDark,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_new_project")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Project")
            }
        },
        containerColor = com.example.ui.theme.StudioBackgroundDark
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .testTag("projects_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Projects",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "Manage, create, duplicate, and export your mobile codebases",
                fontSize = 13.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search projects...", color = TextSecondaryDark, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "HTML", "React", "Python").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) StudioCyan.copy(alpha = 0.2f) else StudioSurfaceDark)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) StudioCyan else TextSecondaryDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Projects List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredProjects, key = { it.id }) { project ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectProject(project) }
                            .padding(vertical = 5.dp)
                            .testTag("project_card_${project.name}")
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
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (project.description.isNotBlank()) project.description else "Template: ${project.template}",
                                    fontSize = 12.sp,
                                    color = TextSecondaryDark,
                                    maxLines = 1
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Menu",
                                        tint = TextSecondaryDark
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier.background(StudioCardDark)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Duplicate", color = TextPrimaryDark) },
                                        leadingIcon = {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = StudioCyan)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onDuplicateProject(project)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Export ZIP", color = TextPrimaryDark) },
                                        leadingIcon = {
                                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = StudioCyan)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onExportProject(project)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = StudioRose) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRose)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onDeleteProject(project)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (isNewProjectDialogOpen) {
        AlertDialog(
            onDismissRequest = { isNewProjectDialogOpen = false },
            title = {
                Text(
                    text = "New Project",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newProjName,
                        onValueChange = { newProjName = it },
                        label = { Text("Project Name") },
                        placeholder = { Text("e.g. My Website, Calculator") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Select Template:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    for ((key, title) in templates) {
                        val isSelected = newProjTemplate == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StudioCyan.copy(alpha = 0.2f) else StudioSurfaceDark)
                                .clickable { newProjTemplate = key }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSelected) "●" else "○",
                                color = if (isSelected) StudioCyan else TextSecondaryDark,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                color = if (isSelected) StudioCyan else TextPrimaryDark,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newProjName.isNotBlank()) {
                            onCreateProject(newProjName.trim(), newProjTemplate)
                            newProjName = ""
                            isNewProjectDialogOpen = false
                        }
                    }
                ) {
                    Text("Create", color = StudioCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewProjectDialogOpen = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = StudioCardDark
        )
    }
}
