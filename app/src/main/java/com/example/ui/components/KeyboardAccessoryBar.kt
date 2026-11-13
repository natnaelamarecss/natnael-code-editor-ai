package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.TextPrimaryDark

@Composable
fun KeyboardAccessoryBar(
    onInsertSymbol: (String) -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onIndent: () -> Unit,
    onUnindent: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbols = listOf(
        "TAB", "{", "}", "(", ")", "[", "]", "<", ">",
        "=", "=>", ";", ":", "\"", "'", "`", "$",
        "/", "\\", "|", "&", "!", "+", "-", "*", "%", "_", "."
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("keyboard_accessory_bar"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo & Redo Quick Buttons
            AccessoryActionChip(label = "↶", tooltip = "Undo", onClick = onUndo)
            AccessoryActionChip(label = "↷", tooltip = "Redo", onClick = onRedo)

            // Cursor movement controls
            AccessoryActionChip(label = "◀", tooltip = "Left", onClick = onMoveCursorLeft)
            AccessoryActionChip(label = "▶", tooltip = "Right", onClick = onMoveCursorRight)

            // Indentation
            AccessoryActionChip(label = "⇥", tooltip = "Indent", onClick = onIndent)
            AccessoryActionChip(label = "⇤", tooltip = "Unindent", onClick = onUnindent)

            // Programming Symbols
            for (sym in symbols) {
                if (sym == "TAB") {
                    AccessorySymbolChip(symbol = "TAB", isAccent = true) {
                        onInsertSymbol("  ")
                    }
                } else {
                    AccessorySymbolChip(symbol = sym) {
                        onInsertSymbol(sym)
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessorySymbolChip(
    symbol: String,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .widthIn(min = 34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isAccent) StudioCyan.copy(alpha = 0.2f) else StudioCardDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (isAccent) StudioCyan else TextPrimaryDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun AccessoryActionChip(
    label: String,
    tooltip: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .widthIn(min = 34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StudioCardDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = StudioCyan,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
