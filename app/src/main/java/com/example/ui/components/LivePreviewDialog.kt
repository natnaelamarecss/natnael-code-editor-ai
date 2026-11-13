package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.executor.CodeExecutionEngine
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

enum class ViewportMode(val label: String, val widthDp: Int?) {
    RESPONSIVE("Full", null),
    COMPACT("360px", 360),
    STANDARD("390px", 390),
    EXPANDED("430px", 430)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LivePreviewDialog(
    htmlBundle: String,
    executionEngine: CodeExecutionEngine,
    onDismiss: () -> Unit
) {
    var viewportMode by remember { mutableStateOf(ViewportMode.RESPONSIVE) }
    var refreshTrigger by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("live_preview_dialog"),
            color = StudioSurfaceDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Preview Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(StudioCardDark)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LIVE PREVIEW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        // Viewport width switchers
                        ViewportPill(
                            label = "Full",
                            isSelected = viewportMode == ViewportMode.RESPONSIVE,
                            onClick = { viewportMode = ViewportMode.RESPONSIVE }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ViewportPill(
                            label = "390px",
                            isSelected = viewportMode == ViewportMode.STANDARD,
                            onClick = { viewportMode = ViewportMode.STANDARD }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ViewportPill(
                            label = "430px",
                            isSelected = viewportMode == ViewportMode.EXPANDED,
                            onClick = { viewportMode = ViewportMode.EXPANDED }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { refreshTrigger++ }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = StudioCyan
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close preview",
                                tint = TextSecondaryDark
                            )
                        }
                    }
                }

                Divider(color = StudioBorderDark)

                // Preview Content Area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(com.example.ui.theme.StudioBackgroundDark),
                    contentAlignment = Alignment.Center
                ) {
                    val containerModifier = if (viewportMode.widthDp != null) {
                        Modifier
                            .width(viewportMode.widthDp!!.dp)
                            .fillMaxSize()
                            .border(1.dp, StudioBorderDark)
                    } else {
                        Modifier.fillMaxSize()
                    }

                    Box(modifier = containerModifier) {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.allowContentAccess = true
                                    settings.allowFileAccess = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true

                                    addJavascriptInterface(executionEngine, "AndroidTerminal")

                                    webChromeClient = object : WebChromeClient() {}
                                    webViewClient = object : WebViewClient() {}

                                    loadDataWithBaseURL("https://preview.local/", htmlBundle, "text/html", "UTF-8", null)
                                }
                            },
                            update = { webView ->
                                if (refreshTrigger >= 0) {
                                    webView.loadDataWithBaseURL("https://preview.local/", htmlBundle, "text/html", "UTF-8", null)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewportPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) StudioCyan.copy(alpha = 0.2f) else StudioSurfaceDark)
            .border(
                1.dp,
                if (isSelected) StudioCyan else StudioBorderDark,
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) StudioCyan else TextSecondaryDark,
            modifier = Modifier.testTag("viewport_${label}")
        )
    }
}
