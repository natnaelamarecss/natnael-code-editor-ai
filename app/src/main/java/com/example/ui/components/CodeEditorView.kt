package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectFile
import com.example.ui.theme.StudioBackgroundDark
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun CodeEditorView(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    activeFile: ProjectFile?,
    openFiles: List<ProjectFile>,
    onSelectFile: (ProjectFile) -> Unit,
    onCloseFile: (ProjectFile) -> Unit,
    onNewFileClick: () -> Unit,
    fontSizeSp: Int = 14,
    isWordWrap: Boolean = false,
    isLineNumbersVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val language = activeFile?.language ?: "text"
    val visualTransformation = remember(language) { CodeVisualTransformation(language) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackgroundDark)
            .testTag("code_editor_view")
    ) {
        // Multi-file tabs bar
        if (openFiles.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = openFiles.indexOf(activeFile).coerceAtLeast(0),
                edgePadding = 8.dp,
                containerColor = StudioCardDark,
                contentColor = StudioCyan,
                divider = {
                    Divider(color = StudioBorderDark, thickness = 1.dp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                openFiles.forEach { file ->
                    val isSelected = file.id == activeFile?.id
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectFile(file) },
                        modifier = Modifier
                            .height(38.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = getFileIcon(file.name),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = file.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) StudioCyan else TextSecondaryDark,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onCloseFile(file) }
                                    .padding(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close tab",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.height(14.dp).width(14.dp)
                                )
                            }
                        }
                    }
                }

                // Add file button in tabs
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onNewFileClick,
                        modifier = Modifier.height(28.dp).width(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add new file",
                            tint = StudioCyan
                        )
                    }
                }
            }
        }

        // Editor canvas with line numbers and code input
        val lineCount = remember(textFieldValue.text) {
            val lines = textFieldValue.text.lines().size
            maxOf(lines, 1)
        }

        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(StudioBackgroundDark)
        ) {
            // Line numbers gutter
            if (isLineNumbersVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 38.dp)
                        .background(StudioSurfaceDark)
                        .verticalScroll(verticalScrollState)
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lineCount) {
                        Text(
                            text = "$i",
                            color = TextMutedDark,
                            fontSize = fontSizeSp.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = (fontSizeSp + 6).sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(StudioBorderDark)
                )
            }

            // Code input area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(verticalScrollState)
                    .then(if (!isWordWrap) Modifier.horizontalScroll(horizontalScrollState) else Modifier)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        color = TextPrimaryDark,
                        fontSize = fontSizeSp.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = (fontSizeSp + 6).sp
                    ),
                    cursorBrush = SolidColor(StudioCyan),
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Ascii
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("code_input_field")
                )
            }
        }
    }
}

fun getFileIcon(name: String): String {
    return when {
        name.endsWith(".html") -> "🌐"
        name.endsWith(".css") -> "🎨"
        name.endsWith(".js") -> "⚡"
        name.endsWith(".jsx") || name.endsWith(".tsx") -> "⚛️"
        name.endsWith(".py") -> "🐍"
        name.endsWith(".json") -> "📋"
        name.endsWith(".md") -> "📝"
        name.endsWith(".java") -> "☕"
        name.endsWith(".c") || name.endsWith(".cpp") -> "⚙️"
        else -> "📄"
    }
}
