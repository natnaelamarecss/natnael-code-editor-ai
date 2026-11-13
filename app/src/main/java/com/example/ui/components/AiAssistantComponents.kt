package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModelTier
import com.example.data.model.ChatMessage
import com.example.data.model.CodeSnippet
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun ModelSelectorRow(
    selectedTier: AiModelTier,
    onSelectTier: (AiModelTier) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModelPill(
            tier = AiModelTier.FAST,
            icon = Icons.Default.Bolt,
            isSelected = selectedTier == AiModelTier.FAST,
            onClick = { onSelectTier(AiModelTier.FAST) }
        )
        ModelPill(
            tier = AiModelTier.GENERAL,
            icon = Icons.Default.DataObject,
            isSelected = selectedTier == AiModelTier.GENERAL,
            onClick = { onSelectTier(AiModelTier.GENERAL) }
        )
        ModelPill(
            tier = AiModelTier.REASONING,
            icon = Icons.Default.Psychology,
            isSelected = selectedTier == AiModelTier.REASONING,
            onClick = { onSelectTier(AiModelTier.REASONING) }
        )
        ModelPill(
            tier = AiModelTier.PRO,
            icon = Icons.Default.AutoAwesome,
            isSelected = selectedTier == AiModelTier.PRO,
            onClick = { onSelectTier(AiModelTier.PRO) }
        )
    }
}

@Composable
private fun ModelPill(
    tier: AiModelTier,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) StudioCyan else StudioBorderDark
    val bgColor = if (isSelected) StudioCyan.copy(alpha = 0.15f) else StudioSurfaceDark

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) StudioCyan else TextSecondaryDark,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = tier.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) StudioCyan else TextPrimaryDark
            )
        }
    }
}

@Composable
fun QuickActionsRow(
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        "Explain File" to "Explain what this current code does and break down key logic",
        "Fix Errors" to "Review this code, find any bugs or potential edge case errors, and fix them",
        "Add Unit Tests" to "Generate comprehensive unit tests for this code with clear assertions",
        "Optimize" to "Optimize this code for execution performance, cleaner syntax, and readability",
        "Refactor" to "Refactor this code into modular, production-ready architecture"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for ((label, prompt) in actions) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioSurfaceDark)
                    .border(1.dp, StudioBorderDark, RoundedCornerShape(8.dp))
                    .clickable { onActionClick(prompt) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AiMessageCard(
    message: ChatMessage,
    onApplySnippet: (CodeSnippet) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Author Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = if (isUser) "You" else "Natnael AI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUser) StudioPurple else StudioCyan
            )
        }

        // Bubble
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isUser) StudioCardDark else StudioSurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isUser) StudioPurple.copy(alpha = 0.4f) else StudioBorderDark),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = TextPrimaryDark,
                    lineHeight = 20.sp
                )

                // Code Snippets inside response
                if (message.codeSnippets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    for (snippet in message.codeSnippets) {
                        CodeSnippetCard(
                            snippet = snippet,
                            onApply = { onApplySnippet(snippet) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CodeSnippetCard(
    snippet: CodeSnippet,
    onApply: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.StudioBackgroundDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Snippet header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioCardDark)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = getFileIcon(snippet.fileName), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = snippet.fileName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Copy button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(snippet.code))
                            isCopied = true
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCopied) StudioGreen else TextSecondaryDark,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Apply to editor button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioCyan.copy(alpha = 0.2f))
                            .clickable(onClick = onApply)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Apply",
                            color = StudioCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(color = StudioBorderDark)

            // Code content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = snippet.code,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = com.example.ui.theme.SyntaxTag,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
