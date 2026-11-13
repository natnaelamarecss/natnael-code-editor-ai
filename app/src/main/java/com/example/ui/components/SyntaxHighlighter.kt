package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.theme.*
import java.util.regex.Pattern

object SyntaxHighlighter {

    private val JS_KEYWORDS = listOf(
        "const", "let", "var", "function", "return", "if", "else", "for", "while",
        "class", "extends", "import", "export", "default", "from", "as", "try",
        "catch", "finally", "async", "await", "new", "this", "typeof", "instanceof",
        "switch", "case", "break", "continue", "throw", "true", "false", "null", "undefined"
    )

    private val PY_KEYWORDS = listOf(
        "def", "class", "import", "from", "as", "return", "if", "elif", "else",
        "for", "while", "try", "except", "finally", "with", "yield", "async",
        "await", "lambda", "pass", "break", "continue", "raise", "True", "False",
        "None", "and", "or", "not", "is", "in", "print"
    )

    private val HTML_KEYWORDS = listOf(
        "html", "head", "body", "div", "span", "p", "h1", "h2", "h3", "h4", "h5", "h6",
        "button", "input", "a", "img", "script", "style", "link", "meta", "title",
        "ul", "ol", "li", "table", "tr", "td", "th", "form", "section", "header", "footer"
    )

    fun highlightCode(code: String, language: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            val lang = language.lowercase()
            val keywords = when {
                lang == "python" || lang == "py" -> PY_KEYWORDS
                lang == "html" || lang == "xml" -> HTML_KEYWORDS
                else -> JS_KEYWORDS
            }

            // Highlight keywords
            val keywordPattern = Pattern.compile("\\b(${keywords.joinToString("|")})\\b")
            val kwMatcher = keywordPattern.matcher(code)
            while (kwMatcher.find()) {
                addStyle(
                    SpanStyle(color = SyntaxKeyword),
                    kwMatcher.start(),
                    kwMatcher.end()
                )
            }

            // Highlight numbers
            val numberPattern = Pattern.compile("\\b(\\d+(\\.\\d+)?)\\b")
            val numMatcher = numberPattern.matcher(code)
            while (numMatcher.find()) {
                addStyle(
                    SpanStyle(color = SyntaxNumber),
                    numMatcher.start(),
                    numMatcher.end()
                )
            }

            // Highlight double quote strings
            val dqPattern = Pattern.compile("\".*?\"")
            val dqMatcher = dqPattern.matcher(code)
            while (dqMatcher.find()) {
                addStyle(
                    SpanStyle(color = SyntaxString),
                    dqMatcher.start(),
                    dqMatcher.end()
                )
            }

            // Highlight single quote strings
            val sqPattern = Pattern.compile("'.*?'")
            val sqMatcher = sqPattern.matcher(code)
            while (sqMatcher.find()) {
                addStyle(
                    SpanStyle(color = SyntaxString),
                    sqMatcher.start(),
                    sqMatcher.end()
                )
            }

            // Highlight template literals
            val tlPattern = Pattern.compile("`.*?`", Pattern.DOTALL)
            val tlMatcher = tlPattern.matcher(code)
            while (tlMatcher.find()) {
                addStyle(
                    SpanStyle(color = SyntaxString),
                    tlMatcher.start(),
                    tlMatcher.end()
                )
            }

            // Highlight HTML tags <tag> or </tag>
            if (lang == "html" || lang == "jsx" || lang == "tsx") {
                val tagPattern = Pattern.compile("<\\/?[a-zA-Z0-9_-]+.*?>")
                val tagMatcher = tagPattern.matcher(code)
                while (tagMatcher.find()) {
                    addStyle(
                        SpanStyle(color = SyntaxTag),
                        tagMatcher.start(),
                        tagMatcher.end()
                    )
                }
            }

            // Highlight comments (//... or #...)
            val lineCommentPattern = if (lang == "python" || lang == "py") {
                Pattern.compile("#.*")
            } else {
                Pattern.compile("//.*")
            }
            val lcMatcher = lineCommentPattern.matcher(code)
            while (lcMatcher.find()) {
                addStyle(
                    SpanStyle(color = SyntaxComment),
                    lcMatcher.start(),
                    lcMatcher.end()
                )
            }

            // Highlight block comments /* ... */
            val blockCommentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL)
            val bcMatcher = blockCommentPattern.matcher(code)
            while (bcMatcher.find()) {
                addStyle(
                    SpanStyle(color = SyntaxComment),
                    bcMatcher.start(),
                    bcMatcher.end()
                )
            }
        }
    }
}

class CodeVisualTransformation(private val language: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = SyntaxHighlighter.highlightCode(text.text, language)
        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}
