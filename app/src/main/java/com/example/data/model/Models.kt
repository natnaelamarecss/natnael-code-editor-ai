package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val defaultModel: String = "NATNAEL THINKING",
    val editorTheme: String = "Github Dark",
    val fontSize: Int = 14,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Project(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val template: String = "html",
    val language: String = "javascript",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProjectFile(
    @DocumentId val id: String = "",
    val projectId: String = "",
    val userId: String = "",
    val name: String = "",
    val path: String = "",
    val content: String = "",
    val language: String = "javascript",
    val isFolder: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val model: String = "NATNAEL THINKING",
    val isPinned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class CodeSnippet(
    val fileName: String = "",
    val language: String = "",
    val code: String = ""
)

data class ChatMessage(
    val id: String = "",
    val role: String = "user", // "user" or "model"
    val content: String = "",
    val codeSnippets: List<CodeSnippet> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class ExecutionOutput(
    val id: String = "",
    val text: String = "",
    val type: OutputType = OutputType.STDOUT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class OutputType {
    STDOUT, STDERR, INFO, WARN, SUCCESS
}

enum class AiModelTier(
    val displayName: String,
    val subtitle: String,
    val description: String,
    val geminiModelName: String
) {
    FAST(
        displayName = "NATNAEL FAST",
        subtitle = "Ultra Fast Tasks",
        description = "High-speed answers, quick syntax lookups, and fast code suggestions.",
        geminiModelName = "gemini-3.1-flash-lite"
    ),
    GENERAL(
        displayName = "NATNAEL GENERAL",
        subtitle = "General Tasks & Refactoring",
        description = "General coding, clear explanations, and modern development.",
        geminiModelName = "gemini-3.8-flash"
    ),
    REASONING(
        displayName = "NATNAEL REASONING",
        subtitle = "Deep Reasoning & Logic",
        description = "Deep reasoning tasks, complex algorithm design, and bug diagnosis.",
        geminiModelName = "gemini-4-carbon"
    ),
    PRO(
        displayName = "NATNAEL PRO",
        subtitle = "Complex Interactive Tasks",
        description = "Particularly complex interactive tasks, full-stack architectures, and systems.",
        geminiModelName = "gemini-4-argon"
    )
}

data class EditorSettings(
    val fontSizeSp: Int = 14,
    val tabSize: Int = 2,
    val isWordWrap: Boolean = false,
    val isLineNumbersVisible: Boolean = true,
    val isAutoBracketClosing: Boolean = true,
    val isAutoSaveEnabled: Boolean = true,
    val editorTheme: String = "Github Dark",
    val defaultAiModel: AiModelTier = AiModelTier.GENERAL
)
