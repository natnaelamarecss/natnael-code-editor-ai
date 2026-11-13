package com.example.data.repository

import android.util.Log
import com.example.data.model.AiModelTier
import com.example.data.model.ChatMessage
import com.example.data.model.CodeSnippet
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiRepository"

data class GeneratedProjectResult(
    val projectName: String,
    val description: String,
    val template: String,
    val files: List<GeneratedFile>
)

data class GeneratedFile(
    val name: String,
    val path: String,
    val language: String,
    val content: String
)

class AiRepository {

    // 1. FAST: gemini-3.1-flash-lite for tasks that should happen fast
    private val fastModel by lazy {
        Firebase.ai.generativeModel(
            modelName = AiModelTier.FAST.geminiModelName,
            systemInstruction = content {
                text("You are Natnael Fast, a high-speed coding chatbot assistant in the Natnael Code Editor AI app. Your role is to deliver lightning-fast, concise answers, quick syntax lookups, and rapid code solutions.")
            }
        )
    }

    // 2. GENERAL: gemini-3.8-flash for general tasks
    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = AiModelTier.GENERAL.geminiModelName,
            systemInstruction = content {
                text("You are Natnael General, an expert software developer and coding assistant in the Natnael Code Editor AI app. Your role is to explain code, generate well-crafted functions, handle refactoring, write unit tests, and answer general programming questions. Always format code in markdown blocks with filename and language.")
            }
        )
    }

    // 3. REASONING: gemini-4-carbon for deep reasoning tasks
    private val reasoningModel by lazy {
        Firebase.ai.generativeModel(
            modelName = AiModelTier.REASONING.geminiModelName,
            systemInstruction = content {
                text("You are Natnael Reasoning, a deep reasoning AI coding specialist in the Natnael Code Editor AI app. Your role is deep algorithmic reasoning, complex multi-file logic analysis, thorough bug diagnosis, and architectural problem-solving.")
            }
        )
    }

    // 4. PRO: gemini-4-argon for particularly complex interactive tasks
    private val proModel by lazy {
        Firebase.ai.generativeModel(
            modelName = AiModelTier.PRO.geminiModelName,
            systemInstruction = content {
                text("You are Natnael Pro, an interactive full-stack software architect in the Natnael Code Editor AI app. Your role is handling particularly complex interactive tasks, scalable full-stack application development, distributed system patterns, and advanced code generation.")
            }
        )
    }

    // Structured JSON model for AI Project Builder
    private val projectBuilderModel by lazy {
        val fileSchema = Schema.obj(
            properties = mapOf(
                "name" to Schema.string(description = "Filename including extension, e.g. index.html or main.py"),
                "path" to Schema.string(description = "Relative path of file"),
                "language" to Schema.string(description = "Programming language (html, css, javascript, python, etc.)"),
                "content" to Schema.string(description = "Complete source code content for this file")
            )
        )

        val projectSchema = Schema.obj(
            properties = mapOf(
                "projectName" to Schema.string(description = "Short, catchy name for the project"),
                "description" to Schema.string(description = "Clear 1-2 sentence description of what the project does"),
                "template" to Schema.string(description = "Primary template (html, react, python, node)"),
                "files" to Schema.array(
                    items = fileSchema,
                    description = "List of all generated project source files"
                )
            )
        )

        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = projectSchema
        }

        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config,
            systemInstruction = content {
                text("You are the AI Project Builder for Natnael Code Editor AI. When the user describes an application or website, you generate a complete, working, runnable multi-file project with clean HTML/CSS/JS, React, or Python. Always provide complete, non-stubbed, functional code for each file.")
            }
        )
    }

    private fun getModel(tier: AiModelTier) = when (tier) {
        AiModelTier.FAST -> fastModel
        AiModelTier.GENERAL -> generalModel
        AiModelTier.REASONING -> reasoningModel
        AiModelTier.PRO -> proModel
    }

    /**
     * Multi-turn chat interface maintaining full conversation history with streaming output
     */
    fun streamMultiTurnChat(
        history: List<ChatMessage>,
        newPrompt: String,
        currentFileContext: String? = null,
        tier: AiModelTier = AiModelTier.GENERAL
    ): Flow<String> = flow {
        val model = getModel(tier)

        // Convert existing message list to Gemini content history for multi-turn conversational context
        val formattedHistory = history
            .filter { it.content.isNotBlank() }
            .map { msg ->
                content(if (msg.role == "user") "user" else "model") {
                    text(msg.content)
                }
            }

        val fullPrompt = buildString {
            if (!currentFileContext.isNullOrBlank()) {
                appendLine("Here is the code of the currently open file in Natnael Code:")
                appendLine("```")
                appendLine(currentFileContext)
                appendLine("```")
                appendLine()
            }
            appendLine(newPrompt)
        }

        withTimeout(60_000L) {
            val chat = model.startChat(formattedHistory)
            val responseStream = chat.sendMessageStream(fullPrompt)
            responseStream.collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                emit(chunkText)
            }
        }
    }

    suspend fun generateProject(userPrompt: String): Result<GeneratedProjectResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = "Create a complete, fully functional mobile-ready project based on this description: $userPrompt. Provide all required files so it can run immediately."
            val response = withTimeout(60_000L) {
                projectBuilderModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Model returned empty response.")
            val root = JSONObject(jsonText)
            val projectName = root.optString("projectName", "New Project")
            val description = root.optString("description", "")
            val template = root.optString("template", "html")
            val filesArray = root.getJSONArray("files")
            val filesList = mutableListOf<GeneratedFile>()

            for (i in 0 until filesArray.length()) {
                val fObj = filesArray.getJSONObject(i)
                filesList.add(
                    GeneratedFile(
                        name = fObj.optString("name", "file_${i + 1}"),
                        path = fObj.optString("path", fObj.optString("name", "file_${i + 1}")),
                        language = fObj.optString("language", "text"),
                        content = fObj.optString("content", "")
                    )
                )
            }

            Result.success(
                GeneratedProjectResult(
                    projectName = projectName,
                    description = description,
                    template = template,
                    files = filesList
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Project generation timed out", e)
            Result.failure(Exception("Project generation timed out. Please try a simpler prompt or retry."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Project generation failed", e)
            Result.failure(e)
        }
    }

    fun parseCodeSnippets(markdown: String): List<CodeSnippet> {
        val snippets = mutableListOf<CodeSnippet>()
        val regex = Regex("""```([a-zA-Z0-9_\-.:]*)\n([\s\S]*?)```""")
        val matches = regex.findAll(markdown)
        for (match in matches) {
            val langTag = match.groupValues[1].trim()
            val code = match.groupValues[2].trimEnd()

            var fileName = ""
            var language = langTag

            if (langTag.contains(":")) {
                val parts = langTag.split(":")
                language = parts[0]
                fileName = parts[1]
            } else if (langTag.contains(".")) {
                fileName = langTag
                language = langTag.substringAfterLast(".", "")
            }

            if (fileName.isBlank()) {
                fileName = when (language.lowercase()) {
                    "html" -> "index.html"
                    "css" -> "style.css"
                    "javascript", "js" -> "script.js"
                    "python", "py" -> "main.py"
                    "jsx" -> "App.jsx"
                    "json" -> "data.json"
                    else -> "code_snippet"
                }
            }

            snippets.add(
                CodeSnippet(
                    fileName = fileName,
                    language = language,
                    code = code
                )
            )
        }
        return snippets
    }
}
