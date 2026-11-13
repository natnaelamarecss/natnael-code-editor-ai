package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioRose
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun AiProjectBuilderDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onBuildProject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }

    val presetIdeas = listOf(
        "Modern e-commerce landing page with shopping cart, products catalog, and interactive checkout modal",
        "Personal developer portfolio with glowing dark theme, interactive skill bars, and contact form",
        "Python data analysis CLI with sorting algorithms, prime finder, and statistical summaries",
        "React task management kanban board with categories, progress metrics, and filter tags"
    )

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = StudioCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Project Builder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }
                if (!isLoading) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryDark
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Describe any application, game, or tool you want to build. Natnael AI will architect the files and generate complete source code.",
                    fontSize = 13.sp,
                    color = TextSecondaryDark,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = {
                        Text(
                            "e.g. Create a weather dashboard that shows 5-day forecasts with custom animated SVG icons and search input",
                            fontSize = 13.sp,
                            color = TextSecondaryDark
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("ai_project_prompt_input"),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = StudioRose,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quick Inspiration:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                for (idea in presetIdeas.take(2)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioSurfaceDark)
                            .border(1.dp, StudioBorderDark, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "💡 $idea",
                            fontSize = 11.sp,
                            color = TextSecondaryDark,
                            lineHeight = 15.sp,
                            modifier = Modifier.testTag("idea_${idea.take(10)}")
                        )
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = StudioCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Generating project files with Gemini AI...",
                            fontSize = 13.sp,
                            color = StudioCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (prompt.isNotBlank()) {
                        onBuildProject(prompt)
                    }
                },
                enabled = !isLoading && prompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = StudioCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("generate_project_button")
            ) {
                Text(
                    text = if (isLoading) "Generating..." else "Generate Project",
                    color = com.example.ui.theme.StudioBackgroundDark,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!isLoading) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        },
        containerColor = StudioCardDark
    )
}
