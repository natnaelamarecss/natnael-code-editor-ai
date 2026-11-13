package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.model.ProjectFile
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioRose
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun FileExplorerDrawer(
    projectName: String,
    files: List<ProjectFile>,
    activeFile: ProjectFile?,
    onSelectFile: (ProjectFile) -> Unit,
    onCreateFile: (name: String, isFolder: Boolean) -> Unit,
    onDeleteFile: (ProjectFile) -> Unit,
    onDuplicateFile: (ProjectFile) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isNewFileDialogOpen by remember { mutableStateOf(false) }
    var isNewFolderDialogOpen by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .testTag("file_explorer_drawer"),
        color = StudioSurfaceDark,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EXPLORER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = projectName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        maxLines = 1
                    )
                }
                Row {
                    IconButton(
                        onClick = { isNewFileDialogOpen = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New file",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { isNewFolderDialogOpen = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New folder",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close explorer",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search files...", fontSize = 12.sp, color = TextSecondaryDark) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = StudioBorderDark)
            Spacer(modifier = Modifier.height(8.dp))

            // Files list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredFiles, key = { it.id }) { file ->
                    val isSelected = file.id == activeFile?.id
                    var menuExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) StudioCyan.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { onSelectFile(file) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (file.isFolder) "📁" else getFileIcon(file.name),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = file.name,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) StudioCyan else TextPrimaryDark,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )

                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
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
                                        onDuplicateFile(file)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = StudioRose) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRose)
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onDeleteFile(file)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New File Dialog
    if (isNewFileDialogOpen) {
        AlertDialog(
            onDismissRequest = { isNewFileDialogOpen = false },
            title = { Text("Create New File", color = TextPrimaryDark) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text("e.g. component.js, style.css, utils.py") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            onCreateFile(newFileName.trim(), false)
                            newFileName = ""
                            isNewFileDialogOpen = false
                        }
                    }
                ) {
                    Text("Create", color = StudioCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewFileDialogOpen = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = StudioCardDark
        )
    }

    // New Folder Dialog
    if (isNewFolderDialogOpen) {
        AlertDialog(
            onDismissRequest = { isNewFolderDialogOpen = false },
            title = { Text("Create New Folder", color = TextPrimaryDark) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text("e.g. components, utils, assets") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            onCreateFile(newFileName.trim(), true)
                            newFileName = ""
                            isNewFolderDialogOpen = false
                        }
                    }
                ) {
                    Text("Create", color = StudioCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewFolderDialogOpen = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = StudioCardDark
        )
    }
}
