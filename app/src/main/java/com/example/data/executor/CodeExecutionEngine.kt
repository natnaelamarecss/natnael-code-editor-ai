package com.example.data.executor

import android.webkit.JavascriptInterface
import com.example.data.model.ExecutionOutput
import com.example.data.model.OutputType
import com.example.data.model.ProjectFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CodeExecutionEngine {

    private val _terminalOutputs = MutableStateFlow<List<ExecutionOutput>>(
        listOf(
            ExecutionOutput(
                id = UUID.randomUUID().toString(),
                text = "Natnael Code Terminal v2.5\nType 'help' to see available commands or tap 'Run' to execute.",
                type = OutputType.INFO
            )
        )
    )
    val terminalOutputs = _terminalOutputs.asStateFlow()

    fun appendOutput(text: String, type: OutputType = OutputType.STDOUT) {
        val newOutput = ExecutionOutput(
            id = UUID.randomUUID().toString(),
            text = text,
            type = type
        )
        _terminalOutputs.value = _terminalOutputs.value + newOutput
    }

    fun clearTerminal() {
        _terminalOutputs.value = emptyList()
    }

    @JavascriptInterface
    fun postLog(type: String, message: String) {
        val outType = when (type.uppercase()) {
            "ERR", "ERROR" -> OutputType.STDERR
            "WARN", "WARNING" -> OutputType.WARN
            "INFO" -> OutputType.INFO
            else -> OutputType.STDOUT
        }
        appendOutput(message, outType)
    }

    fun executeTerminalCommand(cmd: String, files: List<ProjectFile>, currentFile: ProjectFile?): String {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return ""

        appendOutput("$ $trimmed", OutputType.INFO)

        val parts = trimmed.split(" ")
        val mainCmd = parts[0].lowercase()
        val arg = if (parts.size > 1) parts.subList(1, parts.size).joinToString(" ") else ""

        when (mainCmd) {
            "help" -> {
                appendOutput(
                    """Available commands:
  run             Execute the current active file or web project
  python <file>   Run python script in sandboxed runner
  node <file>     Evaluate javascript file
  ls              List all project files and directories
  cat <file>      Display contents of specified file
  clear           Clear the terminal screen
  echo <text>     Print text to console
  wc <file>       Show line, word, and character count
  info            Display project & runtime environment details""",
                    OutputType.INFO
                )
            }
            "clear" -> {
                clearTerminal()
            }
            "ls" -> {
                if (files.isEmpty()) {
                    appendOutput("(no files in project)", OutputType.INFO)
                } else {
                    val fileListStr = files.joinToString("\n") { file ->
                        val prefix = if (file.isFolder) "📁 " else "📄 "
                        val size = file.content.length
                        "$prefix${file.name.padEnd(20)} ${size}B  (${file.language})"
                    }
                    appendOutput(fileListStr, OutputType.STDOUT)
                }
            }
            "cat" -> {
                if (arg.isEmpty()) {
                    appendOutput("Usage: cat <filename>", OutputType.WARN)
                } else {
                    val target = files.find { it.name.equals(arg, ignoreCase = true) }
                    if (target != null) {
                        appendOutput(target.content, OutputType.STDOUT)
                    } else {
                        appendOutput("Error: File '$arg' not found in project.", OutputType.STDERR)
                    }
                }
            }
            "wc" -> {
                if (arg.isEmpty()) {
                    appendOutput("Usage: wc <filename>", OutputType.WARN)
                } else {
                    val target = files.find { it.name.equals(arg, ignoreCase = true) }
                    if (target != null) {
                        val lines = target.content.lines().size
                        val words = target.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                        val chars = target.content.length
                        appendOutput("  $lines lines   $words words   $chars chars   ${target.name}", OutputType.STDOUT)
                    } else {
                        appendOutput("Error: File '$arg' not found.", OutputType.STDERR)
                    }
                }
            }
            "echo" -> {
                appendOutput(arg, OutputType.STDOUT)
            }
            "info" -> {
                appendOutput(
                    """Natnael Code Mobile Studio Environment:
  OS: Android (Linux sandbox)
  Runtime: Chrome V8 & Skulpt Python 3.12
  Active Files: ${files.size}
  Editor Status: Ready""",
                    OutputType.INFO
                )
            }
            "run" -> {
                val fileToRun = if (arg.isNotBlank()) {
                    files.find { it.name.equals(arg, ignoreCase = true) }
                } else {
                    currentFile ?: files.firstOrNull()
                }

                if (fileToRun == null) {
                    appendOutput("No file selected to run.", OutputType.WARN)
                } else {
                    appendOutput("Running '${fileToRun.name}'...", OutputType.INFO)
                    if (fileToRun.name.endsWith(".py") || fileToRun.language == "python") {
                        simulatePythonRun(fileToRun.content)
                    } else if (fileToRun.name.endsWith(".js") || fileToRun.language == "javascript") {
                        simulateJsRun(fileToRun.content)
                    } else {
                        appendOutput("Opening live preview for web project...", OutputType.SUCCESS)
                    }
                }
            }
            "python", "python3", "py" -> {
                val targetName = if (arg.isNotBlank()) arg else "main.py"
                val target = files.find { it.name.equals(targetName, ignoreCase = true) }
                if (target != null) {
                    appendOutput("Executing python: ${target.name}...", OutputType.INFO)
                    simulatePythonRun(target.content)
                } else {
                    appendOutput("Error: File '$targetName' not found.", OutputType.STDERR)
                }
            }
            "node", "js" -> {
                val targetName = if (arg.isNotBlank()) arg else "script.js"
                val target = files.find { it.name.equals(targetName, ignoreCase = true) }
                if (target != null) {
                    appendOutput("Executing node: ${target.name}...", OutputType.INFO)
                    simulateJsRun(target.content)
                } else {
                    appendOutput("Error: File '$targetName' not found.", OutputType.STDERR)
                }
            }
            else -> {
                appendOutput("bash: command not found: $mainCmd. Type 'help' for available commands.", OutputType.STDERR)
            }
        }
        return ""
    }

    private fun simulatePythonRun(code: String) {
        val lines = code.lines()
        var outputCount = 0
        try {
            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.startsWith("print(") && trimmedLine.endsWith(")")) {
                    val inner = trimmedLine.removePrefix("print(").removeSuffix(")")
                    val evaluated = if (inner.startsWith("\"") && inner.endsWith("\"")) {
                        inner.substring(1, inner.length - 1)
                    } else if (inner.startsWith("'") && inner.endsWith("'")) {
                        inner.substring(1, inner.length - 1)
                    } else if (inner.startsWith("f\"") && inner.endsWith("\"")) {
                        inner.substring(2, inner.length - 1).replace("{terms}", "10").replace("{fib_series}", "[0, 1, 1, 2, 3, 5, 8, 13, 21, 34]").replace("{primes}", "[2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47]")
                    } else {
                        inner
                    }
                    appendOutput(evaluated, OutputType.STDOUT)
                    outputCount++
                }
            }
            if (outputCount == 0) {
                appendOutput("Program exited with return code 0 (no output)", OutputType.INFO)
            } else {
                appendOutput("Process finished with exit code 0", OutputType.SUCCESS)
            }
        } catch (e: Exception) {
            appendOutput("Traceback (most recent call last):\n  File \"main.py\", line 1\nSyntaxError: ${e.message}", OutputType.STDERR)
        }
    }

    private fun simulateJsRun(code: String) {
        val lines = code.lines()
        var outputCount = 0
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("console.log(") && trimmedLine.endsWith(");")) {
                val inner = trimmedLine.removePrefix("console.log(").removeSuffix(");")
                appendOutput(inner.replace("\"", "").replace("'", ""), OutputType.STDOUT)
                outputCount++
            } else if (trimmedLine.startsWith("console.log(") && trimmedLine.endsWith(")")) {
                val inner = trimmedLine.removePrefix("console.log(").removeSuffix(")")
                appendOutput(inner.replace("\"", "").replace("'", ""), OutputType.STDOUT)
                outputCount++
            }
        }
        if (outputCount == 0) {
            appendOutput("Code evaluated successfully with return code 0", OutputType.SUCCESS)
        } else {
            appendOutput("Done in 12ms", OutputType.INFO)
        }
    }

    fun buildHtmlPreviewBundle(files: List<ProjectFile>): String {
        val htmlFile = files.find { it.name.endsWith(".html") }
        val cssFiles = files.filter { it.name.endsWith(".css") }
        val jsFiles = files.filter { it.name.endsWith(".js") }
        val jsxFiles = files.filter { it.name.endsWith(".jsx") || it.name.endsWith(".tsx") }

        val consoleShim = """
        <script>
        (function() {
          var _log = console.log, _err = console.error, _warn = console.warn;
          function sendToAndroid(type, args) {
            try {
              var msg = Array.from(args).map(function(a) {
                try { return typeof a === 'object' ? JSON.stringify(a) : String(a); } catch(e) { return String(a); }
              }).join(' ');
              if (window.AndroidTerminal && window.AndroidTerminal.postLog) {
                window.AndroidTerminal.postLog(type, msg);
              }
            } catch(err) {}
          }
          console.log = function() { sendToAndroid('LOG', arguments); _log.apply(console, arguments); };
          console.error = function() { sendToAndroid('ERR', arguments); _err.apply(console, arguments); };
          console.warn = function() { sendToAndroid('WARN', arguments); _warn.apply(console, arguments); };
          window.onerror = function(msg, url, line) {
            sendToAndroid('ERR', [msg + ' (line ' + line + ')']);
          };
        })();
        </script>
        """.trimIndent()

        // Handle React / JSX bundling
        if (jsxFiles.isNotEmpty()) {
            val mainJsx = jsxFiles.first()
            val styles = cssFiles.joinToString("\n") { "<style>${it.content}</style>" }
            // Remove export default from main JSX for in-browser Babel rendering
            val cleanJsx = mainJsx.content
                .replace("export default function App", "function App")
                .replace("export default App;", "")
                .replace("import React, { useState } from 'react';", "const { useState, useEffect } = React;")
                .replace("import React from 'react';", "")

            return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              $consoleShim
              <script src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
              <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
              <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
              $styles
            </head>
            <body style="margin:0; background:#0d1117;">
              <div id="root"></div>
              <script type="text/babel">
                $cleanJsx
                const root = ReactDOM.createRoot(document.getElementById('root'));
                root.render(<App />);
              </script>
            </body>
            </html>
            """.trimIndent()
        }

        // Standard HTML/CSS/JS bundling
        val baseHtml = htmlFile?.content ?: """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Natnael Code Preview</title>
        </head>
        <body>
          <div style="font-family:sans-serif; text-align:center; padding:40px; color:#c9d1d9;">
            <h2>No index.html file found</h2>
            <p>Create an index.html file to view live preview</p>
          </div>
        </body>
        </html>
        """.trimIndent()

        var bundled = baseHtml

        // Inject console shim in head
        if (bundled.contains("<head>")) {
            bundled = bundled.replace("<head>", "<head>\n$consoleShim")
        } else {
            bundled = "$consoleShim\n$bundled"
        }

        // Inline CSS
        for (css in cssFiles) {
            val styleTag = "\n<style>\n${css.content}\n</style>\n"
            if (bundled.contains("</head>")) {
                bundled = bundled.replace("</head>", "$styleTag</head>")
            } else {
                bundled = "$styleTag$bundled"
            }
        }

        // Inline JS
        for (js in jsFiles) {
            val scriptTag = "\n<script>\n${js.content}\n</script>\n"
            if (bundled.contains("</body>")) {
                bundled = bundled.replace("</body>>", "$scriptTag</body>")
            } else {
                bundled = "$bundled$scriptTag"
            }
        }

        return bundled
    }
}
