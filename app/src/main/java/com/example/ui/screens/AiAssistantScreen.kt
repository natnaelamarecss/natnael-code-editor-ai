package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModelTier
import com.example.data.model.ChatMessage
import com.example.data.model.CodeSnippet
import com.example.data.model.ProjectFile
import com.example.data.repository.AiRepository
import com.example.ui.components.AiMessageCard
import com.example.ui.components.ModelSelectorRow
import com.example.ui.components.QuickActionsRow
import com.example.ui.theme.StudioBackgroundDark
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioRose
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AiAssistantScreen(
    aiRepository: AiRepository,
    currentFileContext: ProjectFile? = null,
    onApplySnippetToProject: (CodeSnippet) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTier by remember { mutableStateOf(AiModelTier.GENERAL) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var activeJob by remember { mutableStateOf<Job?>(null) }

    val messages = remember {
        mutableStateListOf<ChatMessage>().apply {
            add(
                ChatMessage(
                    id = "msg_welcome",
                    role = "model",
                    content = "Hello! I am your Natnael AI multi-turn coding chatbot. I maintain full conversation context so we can discuss, design, debug, and refine code together.",
                    codeSnippets = emptyList()
                )
            )
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(promptText: String) {
        if (promptText.isBlank() || isGenerating) return

        val userMsgId = UUID.randomUUID().toString()
        val userMsg = ChatMessage(
            id = userMsgId,
            role = "user",
            content = promptText
        )
        messages.add(userMsg)
        inputText = ""

        val aiMsgId = UUID.randomUUID().toString()
        val aiMsg = ChatMessage(
            id = aiMsgId,
            role = "model",
            content = ""
        )
        messages.add(aiMsg)

        isGenerating = true
        activeJob = coroutineScope.launch {
            val accumulated = StringBuilder()
            try {
                // Pass all previous turns so Gemini maintains conversational history across turns
                val history = messages.filter { it.id != aiMsgId && it.content.isNotBlank() }
                aiRepository.streamMultiTurnChat(
                    history = history,
                    newPrompt = promptText,
                    currentFileContext = currentFileContext?.content,
                    tier = selectedTier
                ).collect { chunk ->
                    accumulated.append(chunk)
                    val snippets = aiRepository.parseCodeSnippets(accumulated.toString())
                    val idx = messages.indexOfFirst { it.id == aiMsgId }
                    if (idx != -1) {
                        messages[idx] = messages[idx].copy(
                            content = accumulated.toString(),
                            codeSnippets = snippets
                        )
                    }
                }
            } catch (e: Exception) {
                val idx = messages.indexOfFirst { it.id == aiMsgId }
                if (idx != -1) {
                    messages[idx] = messages[idx].copy(
                        content = accumulated.ifBlank { "Error generating response: ${e.message}" }.toString()
                    )
                }
            } finally {
                isGenerating = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("ai_assistant_screen")
    ) {
        // Assistant Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(StudioSurfaceDark)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(StudioPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = StudioPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NATNAEL AI ASSISTANT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPurple,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = selectedTier.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }
            }

            Row {
                IconButton(
                    onClick = {
                        messages.clear()
                        messages.add(
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                role = "model",
                                content = "New session started! How can I assist you with your code?"
                            )
                        )
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = StudioCyan
                    )
                }
            }
        }

        Divider(color = StudioBorderDark)

        // Three Model Tiers Row
        ModelSelectorRow(
            selectedTier = selectedTier,
            onSelectTier = { selectedTier = it }
        )

        // Context Indicator if a file is open
        if (currentFileContext != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioCardDark)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "📄 Active Context: ${currentFileContext.name} (${currentFileContext.language})",
                    fontSize = 11.sp,
                    color = StudioCyan,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Quick action chips
        QuickActionsRow(
            onActionClick = { prompt -> sendMessage(prompt) }
        )

        Divider(color = StudioBorderDark, thickness = 1.dp)

        // Messages Stream List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(StudioBackgroundDark),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                AiMessageCard(
                    message = msg,
                    onApplySnippet = onApplySnippetToProject
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = StudioCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Natnael AI is reasoning and writing code...",
                            fontSize = 12.sp,
                            color = StudioCyan
                        )
                    }
                }
            }
        }

        Divider(color = StudioBorderDark)

        // Prompt Input Bottom Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurfaceDark)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp)),
                color = StudioCardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = TextStyle(
                        color = TextPrimaryDark,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(StudioCyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                sendMessage(inputText.trim())
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Ask Natnael AI or request code...",
                                    fontSize = 14.sp,
                                    color = TextSecondaryDark
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_prompt_input")
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGenerating) {
                IconButton(
                    onClick = {
                        activeJob?.cancel()
                        isGenerating = false
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StudioRose)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = StudioBackgroundDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            sendMessage(inputText.trim())
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) StudioCyan else StudioCardDark)
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) StudioBackgroundDark else TextMutedDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
