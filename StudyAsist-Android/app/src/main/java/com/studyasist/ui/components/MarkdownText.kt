package com.studyasist.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Renders markdown-like text (bold, italic, headers, lists) in Compose.
 * Supports: **bold**, *italic*, # H1, ## H2, ### H3, - bullet, 1. numbered.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val spanStyle = MaterialTheme.typography.bodyMedium.toSpanStyle()
    val boldStyle = spanStyle.copy(fontWeight = FontWeight.Bold)
    val italicStyle = spanStyle.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    val h1Style = MaterialTheme.typography.headlineMedium.toSpanStyle()
    val h2Style = MaterialTheme.typography.titleLarge.toSpanStyle()
    val h3Style = MaterialTheme.typography.titleMedium.toSpanStyle()

    val annotatedString = buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { lineIdx, line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    withStyle(h3Style) { append(trimmed.removePrefix("### ")) }
                    append("\n")
                }
                trimmed.startsWith("## ") -> {
                    withStyle(h2Style) { append(trimmed.removePrefix("## ")) }
                    append("\n")
                }
                trimmed.startsWith("# ") -> {
                    withStyle(h1Style) { append(trimmed.removePrefix("# ")) }
                    append("\n")
                }
                trimmed.startsWith("- ") || trimmed.startsWith("• ") -> {
                    append("  • ")
                    parseInlineFormatting(trimmed.removePrefix("- ").removePrefix("• "), boldStyle, italicStyle)
                    append("\n")
                }
                Regex("^\\d+\\.\\s").containsMatchIn(trimmed) -> {
                    val match = Regex("^(\\d+)\\.\\s").find(trimmed)
                    val rest = if (match != null) trimmed.substring(match.range.last + 1) else trimmed
                    append("  ${match?.groupValues?.get(1) ?: "1"}. ")
                    parseInlineFormatting(rest, boldStyle, italicStyle)
                    append("\n")
                }
                else -> {
                    parseInlineFormatting(trimmed, boldStyle, italicStyle)
                    append("\n")
                }
            }
        }
    }
    Text(text = annotatedString, modifier = modifier)
}

private fun AnnotatedString.Builder.parseInlineFormatting(
    text: String,
    boldStyle: androidx.compose.ui.text.SpanStyle,
    italicStyle: androidx.compose.ui.text.SpanStyle
) {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(boldStyle) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            text.startsWith("*", i) && (i + 1 >= text.length || text[i + 1] != '*') -> {
                val end = text.indexOf("*", i + 1)
                if (end != -1 && (end == i + 1 || text[end - 1] != '*')) {
                    withStyle(italicStyle) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            text.startsWith("__", i) -> {
                val end = text.indexOf("__", i + 2)
                if (end != -1) {
                    withStyle(boldStyle) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            text.startsWith("_", i) && (i + 1 >= text.length || text[i + 1] != '_') -> {
                val end = text.indexOf("_", i + 1)
                if (end != -1 && (end == i + 1 || text[end - 1] != '_')) {
                    withStyle(italicStyle) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}
