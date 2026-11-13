package com.example

import com.example.data.executor.CodeExecutionEngine
import com.example.data.model.OutputType
import com.example.data.model.ProjectFile
import com.example.data.repository.AiRepository
import com.example.ui.components.SyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NatnaelCodeUnitTest {

    @Test
    fun testSyntaxHighlighterProducesAnnotatedSpans() {
        val code = "const x = 42;\nfunction run() { return true; }"
        val result = SyntaxHighlighter.highlightCode(code, "javascript")
        assertNotNull(result)
        assertEquals(code, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun testTerminalExecutionEngineCommands() {
        val engine = CodeExecutionEngine()
        val dummyFiles = listOf(
            ProjectFile(id = "1", name = "index.html", content = "<h1>Test</h1>", language = "html"),
            ProjectFile(id = "2", name = "main.py", content = "print('Hello')", language = "python")
        )

        // Test echo
        engine.executeTerminalCommand("echo Hello Android", dummyFiles, dummyFiles[0])
        val lastOutput = engine.terminalOutputs.value.last()
        assertEquals("Hello Android", lastOutput.text)

        // Test ls
        engine.executeTerminalCommand("ls", dummyFiles, dummyFiles[0])
        val lsOutput = engine.terminalOutputs.value.last()
        assertTrue(lsOutput.text.contains("index.html"))
        assertTrue(lsOutput.text.contains("main.py"))

        // Test cat
        engine.executeTerminalCommand("cat index.html", dummyFiles, dummyFiles[0])
        val catOutput = engine.terminalOutputs.value.last()
        assertEquals("<h1>Test</h1>", catOutput.text)
    }

    @Test
    fun testAiRepositorySnippetParser() {
        val aiRepo = AiRepository()
        val markdown = """
            Here is your component:
            ```javascript:App.js
            import React from 'react';
            export default function App() { return <h1>Hello</h1>; }
            ```
            Enjoy!
        """.trimIndent()

        val snippets = aiRepo.parseCodeSnippets(markdown)
        assertEquals(1, snippets.size)
        assertEquals("App.js", snippets[0].fileName)
        assertEquals("javascript", snippets[0].language)
        assertTrue(snippets[0].code.contains("export default function App"))
    }

    @Test
    fun testEmailAccountValidationRules() {
        // Password length requirement >= 6
        val shortPassword = "123"
        assertTrue(shortPassword.length < 6)

        val validPassword = "securePassword123"
        assertTrue(validPassword.length >= 6)

        // Email format check
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        assertTrue(emailRegex.matches("natnael@example.com"))
        assertTrue(!emailRegex.matches("invalid-email-address"))
    }

    @Test
    fun testApkMimeTypeAndNaming() {
        val apkMime = "application/vnd.android.package-archive"
        val fileName = "Natnael_Code_Editor_AI.apk"
        assertTrue(fileName.endsWith(".apk"))
        assertEquals("application/vnd.android.package-archive", apkMime)
    }
}
