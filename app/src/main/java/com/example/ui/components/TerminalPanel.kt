package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionOutput
import com.example.data.model.OutputType
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioRose
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun TerminalPanel(
    outputs: List<ExecutionOutput>,
    onSendCommand: (String) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(outputs.size) {
        if (outputs.isNotEmpty()) {
            listState.animateScrollToItem(outputs.size - 1)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("terminal_panel"),
        color = StudioCardDark,
        tonalElevation = 10.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Terminal Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(StudioCardDark)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TERMINAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SH",
                            color = StudioGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val fullText = outputs.joinToString("\n") { it.text }
                            clipboardManager.setText(AnnotatedString(fullText))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy terminal",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear terminal",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFullScreen,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Toggle full screen",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close terminal",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Divider(color = StudioBorderDark, thickness = 1.dp)

            // Terminal Logs
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(com.example.ui.theme.StudioBackgroundDark)
                    .padding(8.dp)
            ) {
                items(outputs, key = { it.id }) { out ->
                    val color = when (out.type) {
                        OutputType.STDOUT -> TextPrimaryDark
                        OutputType.STDERR -> StudioRose
                        OutputType.WARN -> StudioAmber
                        OutputType.INFO -> StudioCyan
                        OutputType.SUCCESS -> StudioGreen
                    }

                    Text(
                        text = out.text,
                        color = color,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }

            Divider(color = StudioBorderDark, thickness = 1.dp)

            // Command Prompt Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioCardDark)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "natnael@studio:~$ ",
                    color = StudioGreen,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                BasicTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    textStyle = TextStyle(
                        color = TextPrimaryDark,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(StudioCyan),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (commandInput.isNotBlank()) {
                                onSendCommand(commandInput)
                                commandInput = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_input")
                )
            }
        }
    }
}
