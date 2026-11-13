package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.executor.CodeExecutionEngine
import com.example.data.model.Project
import com.example.data.model.ProjectFile
import com.example.ui.components.CodeEditorView
import com.example.ui.components.FileExplorerDrawer
import com.example.ui.components.KeyboardAccessoryBar
import com.example.ui.components.LivePreviewDialog
import com.example.ui.components.TerminalPanel
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
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    project: Project,
    files: List<ProjectFile>,
    executionEngine: CodeExecutionEngine,
    onBack: () -> Unit,
    onSaveFile: (fileId: String, content: String) -> Unit,
    onCreateFile: (name: String, isFolder: Boolean) -> Unit,
    onDeleteFile: (ProjectFile) -> Unit,
    onDuplicateFile: (ProjectFile) -> Unit,
    onOpenAiWithFile: (ProjectFile) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeFile by remember(files) {
        val initial = files.firstOrNull { it.name.endsWith(".html") || it.name.endsWith(".py") || it.name.endsWith(".jsx") }
            ?: files.firstOrNull()
        mutableStateOf(initial)
    }

    val openTabs = remember(files) {
        mutableStateListOf<ProjectFile>().apply {
            if (activeFile != null && !contains(activeFile)) {
                add(activeFile!!)
            }
            // Add other core files
            files.take(4).forEach { f ->
                if (!contains(f)) add(f)
            }
        }
    }

    var editorTextValue by remember {
        mutableStateOf(TextFieldValue(activeFile?.content ?: ""))
    }

    var isSaved by remember { mutableStateOf(true) }
    var isExplorerOpen by remember { mutableStateOf(false) }
    var isTerminalOpen by remember { mutableStateOf(false) }
    var isTerminalFullScreen by remember { mutableStateOf(false) }
    var isPreviewOpen by remember { mutableStateOf(false) }
    var isMoreMenuOpen by remember { mutableStateOf(false) }

    // Undo / Redo history stacks
    val undoStack = remember { mutableStateListOf<String>() }
    val redoStack = remember { mutableStateListOf<String>() }

    // Update text field value when active file changes
    LaunchedEffect(activeFile?.id) {
        val content = activeFile?.content ?: ""
        editorTextValue = TextFieldValue(content, TextRange(content.length))
        isSaved = true
        undoStack.clear()
        redoStack.clear()
    }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("editor_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Editor Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(StudioSurfaceDark)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimaryDark
                    )
                }

                // File Explorer toggle button
                IconButton(onClick = { isExplorerOpen = !isExplorerOpen }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Explorer",
                        tint = if (isExplorerOpen) StudioCyan else TextSecondaryDark
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Project & File Name Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeFile?.name ?: "No file open",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Saved Status Dot
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isSaved) StudioGreen else StudioAmber)
                        )
                    }
                    Text(
                        text = project.name,
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        maxLines = 1
                    )
                }

                // Save button
                IconButton(
                    onClick = {
                        activeFile?.let { file ->
                            onSaveFile(file.id, editorTextValue.text)
                            isSaved = true
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save file",
                        tint = if (!isSaved) StudioAmber else TextSecondaryDark
                    )
                }

                // Terminal Toggle
                IconButton(
                    onClick = { isTerminalOpen = !isTerminalOpen },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Terminal",
                        tint = if (isTerminalOpen) StudioCyan else TextSecondaryDark
                    )
                }

                // AI Assistant Pill/Button
                IconButton(
                    onClick = { activeFile?.let { onOpenAiWithFile(it) } },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = StudioPurple
                    )
                }

                // RUN / PREVIEW Primary Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioGreen)
                        .clickable {
                            // Automatically save before running
                            activeFile?.let { onSaveFile(it.id, editorTextValue.text) }
                            isSaved = true

                            if (activeFile?.language == "python" || activeFile?.name?.endsWith(".py") == true) {
                                isTerminalOpen = true
                                executionEngine.executeTerminalCommand("python ${activeFile?.name ?: "main.py"}", files, activeFile)
                            } else {
                                isPreviewOpen = true
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("run_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run",
                            tint = StudioBackgroundDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Run",
                            color = StudioBackgroundDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // More Menu
                Box {
                    IconButton(onClick = { isMoreMenuOpen = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextSecondaryDark)
                    }
                    DropdownMenu(
                        expanded = isMoreMenuOpen,
                        onDismissRequest = { isMoreMenuOpen = false },
                        modifier = Modifier.background(StudioCardDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Web Preview", color = TextPrimaryDark) },
                            onClick = {
                                isMoreMenuOpen = false
                                isPreviewOpen = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open Terminal", color = TextPrimaryDark) },
                            onClick = {
                                isMoreMenuOpen = false
                                isTerminalOpen = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Explain Current Code with AI", color = StudioPurple) },
                            onClick = {
                                isMoreMenuOpen = false
                                activeFile?.let { onOpenAiWithFile(it) }
                            }
                        )
                    }
                }
            }

            Divider(color = StudioBorderDark)

            // Main Code Editor Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isTerminalOpen && !isTerminalFullScreen) 0.6f else 1f)
            ) {
                CodeEditorView(
                    textFieldValue = editorTextValue,
                    onValueChange = { newVal ->
                        if (newVal.text != editorTextValue.text) {
                            undoStack.add(editorTextValue.text)
                            if (undoStack.size > 50) undoStack.removeAt(0)
                            redoStack.clear()
                            isSaved = false
                        }
                        editorTextValue = newVal
                    },
                    activeFile = activeFile,
                    openFiles = openTabs,
                    onSelectFile = { selected ->
                        activeFile = selected
                        if (!openTabs.contains(selected)) {
                            openTabs.add(selected)
                        }
                    },
                    onCloseFile = { toClose ->
                        openTabs.remove(toClose)
                        if (activeFile?.id == toClose.id) {
                            activeFile = openTabs.firstOrNull()
                        }
                    },
                    onNewFileClick = { isExplorerOpen = true }
                )
            }

            // Keyboard Accessory Toolbar (Always pinned above keyboard)
            KeyboardAccessoryBar(
                onInsertSymbol = { sym ->
                    val curPos = editorTextValue.selection.start
                    val text = editorTextValue.text

                    // Smart bracket auto-closing
                    val (toInsert, cursorOffset) = when (sym) {
                        "{" -> "{}" to 1
                        "(" -> "()" to 1
                        "[" -> "[]" to 1
                        "\"" -> "\"\"" to 1
                        "'" -> "''" to 1
                        "`" -> "``" to 1
                        "<" -> "<>" to 1
                        else -> sym to sym.length
                    }

                    val newText = text.substring(0, curPos) + toInsert + text.substring(curPos)
                    undoStack.add(text)
                    isSaved = false
                    editorTextValue = TextFieldValue(
                        text = newText,
                        selection = TextRange(curPos + cursorOffset)
                    )
                },
                onMoveCursorLeft = {
                    val pos = (editorTextValue.selection.start - 1).coerceAtLeast(0)
                    editorTextValue = editorTextValue.copy(selection = TextRange(pos))
                },
                onMoveCursorRight = {
                    val pos = (editorTextValue.selection.start + 1).coerceAtMost(editorTextValue.text.length)
                    editorTextValue = editorTextValue.copy(selection = TextRange(pos))
                },
                onIndent = {
                    val curPos = editorTextValue.selection.start
                    val text = editorTextValue.text
                    val newText = text.substring(0, curPos) + "  " + text.substring(curPos)
                    undoStack.add(text)
                    isSaved = false
                    editorTextValue = TextFieldValue(text = newText, selection = TextRange(curPos + 2))
                },
                onUnindent = {
                    val curPos = editorTextValue.selection.start
                    val text = editorTextValue.text
                    if (curPos >= 2 && text.substring(curPos - 2, curPos) == "  ") {
                        val newText = text.substring(0, curPos - 2) + text.substring(curPos)
                        undoStack.add(text)
                        isSaved = false
                        editorTextValue = TextFieldValue(text = newText, selection = TextRange(curPos - 2))
                    }
                },
                onUndo = {
                    if (undoStack.isNotEmpty()) {
                        val last = undoStack.removeAt(undoStack.size - 1)
                        redoStack.add(editorTextValue.text)
                        editorTextValue = TextFieldValue(last, TextRange(last.length))
                        isSaved = false
                    }
                },
                onRedo = {
                    if (redoStack.isNotEmpty()) {
                        val next = redoStack.removeAt(redoStack.size - 1)
                        undoStack.add(editorTextValue.text)
                        editorTextValue = TextFieldValue(next, TextRange(next.length))
                        isSaved = false
                    }
                }
            )

            // Integrated Mobile Terminal Bottom Panel
            if (isTerminalOpen) {
                TerminalPanel(
                    outputs = executionEngine.terminalOutputs.value,
                    onSendCommand = { cmd ->
                        executionEngine.executeTerminalCommand(cmd, files, activeFile)
                    },
                    onClear = { executionEngine.clearTerminal() },
                    onClose = { isTerminalOpen = false },
                    isFullScreen = isTerminalFullScreen,
                    onToggleFullScreen = { isTerminalFullScreen = !isTerminalFullScreen },
                    modifier = if (isTerminalFullScreen) Modifier.fillMaxSize() else Modifier.weight(0.4f)
                )
            }
        }

        // Slide-out File Explorer Drawer
        if (isExplorerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(com.example.ui.theme.StudioBackgroundDark.copy(alpha = 0.5f))
                    .clickable { isExplorerOpen = false }
            ) {
                FileExplorerDrawer(
                    projectName = project.name,
                    files = files,
                    activeFile = activeFile,
                    onSelectFile = { file ->
                        activeFile = file
                        if (!openTabs.contains(file)) openTabs.add(file)
                        isExplorerOpen = false
                    },
                    onCreateFile = { name, isFolder ->
                        onCreateFile(name, isFolder)
                    },
                    onDeleteFile = onDeleteFile,
                    onDuplicateFile = onDuplicateFile,
                    onCloseDrawer = { isExplorerOpen = false },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }

        // Live Website Preview Dialog
        if (isPreviewOpen) {
            val htmlBundle = executionEngine.buildHtmlPreviewBundle(files)
            LivePreviewDialog(
                htmlBundle = htmlBundle,
                executionEngine = executionEngine,
                onDismiss = { isPreviewOpen = false }
            )
        }
    }
}
