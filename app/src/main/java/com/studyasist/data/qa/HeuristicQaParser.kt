package com.studyasist.data.qa

import com.studyasist.data.local.entity.QuestionType
import com.studyasist.data.repository.QABankRepository
import java.util.regex.Pattern

/**
 * Heuristic parser to extract Q&A pairs from raw OCR text.
 * Detects: numbered questions, MCQ options, fill-in blanks, true/false.
 */
object HeuristicQaParser {

    private val NUMBERED_START = Pattern.compile("^\\s*(\\d+)[.)]\\s+", Pattern.MULTILINE)
    private val Q_NUMBERED = Pattern.compile("(?i)Q\\s*\\d+[.)]?\\s*")
    private val PAREN_NUMBER = Pattern.compile("\\(\\s*\\d+\\s*\\)\\s*")
    private val ROMAN_START = Pattern.compile("^\\s*[IVX]+[.)]\\s+", Pattern.MULTILINE)
    private val MCQ_OPTION = Pattern.compile("^\\s*[a-dA-D][.)]\\s+", Pattern.MULTILINE)
    private val MCQ_OPTION_PAREN = Pattern.compile("\\s*\\([a-dA-D]\\)\\s*")
    private val FILL_BLANK = Pattern.compile("_{2,}|\\[\\s*\\]")
    private val TRUE_FALSE = Pattern.compile("(?i)(true|false)\\s*/\\s*(true|false)")

    fun parse(ocrText: String): List<QABankRepository.ParsedQA> {
        if (ocrText.isBlank()) return emptyList()
        val normalized = normalize(ocrText)
        val blocks = splitIntoBlocks(normalized)
        return blocks.mapNotNull { block -> parseBlock(block) }
    }

    private fun normalize(text: String): String {
        return text
            .replace('\u00a0', ' ')
            .replace(Regex("[-–—]+"), "-")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun splitIntoBlocks(text: String): List<String> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        var current = StringBuilder()
        for (line in lines) {
            if (isQuestionStart(line)) {
                if (current.isNotBlank()) {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
            }
            if (current.isNotEmpty()) current.append(" ")
            current.append(line)
        }
        if (current.isNotBlank()) result.add(current.toString().trim())
        return result
    }

    private fun isQuestionStart(line: String): Boolean {
        if (line.length < 2) return false
        if (NUMBERED_START.matcher(line).lookingAt()) return true
        if (Q_NUMBERED.matcher(line).find()) return true
        if (PAREN_NUMBER.matcher(line).lookingAt()) return true
        if (ROMAN_START.matcher(line).lookingAt()) return true
        if (line.endsWith("?") && line.length in 3..200) return true
        val lower = line.lowercase()
        val questionWords = listOf("who", "what", "when", "where", "why", "how", "solve", "find", "prove", "define", "explain", "list")
        if (questionWords.any { lower.startsWith("$it ") || lower.startsWith("$it?") }) return true
        return false
    }

    private fun parseBlock(block: String): QABankRepository.ParsedQA? {
        val (question, answer, type) = splitQuestionAnswer(block)
        if (question.isBlank()) return null
        return QABankRepository.ParsedQA(
            question = question.trim(),
            answer = answer?.trim() ?: "",
            type = type,
            optionsJson = if (type == QuestionType.MCQ) extractOptions(block) else null,
            metadataJson = null
        )
    }

    private fun splitQuestionAnswer(block: String): Triple<String, String?, QuestionType> {
        val type = detectQuestionType(block)
        val answerMarker = when (type) {
            QuestionType.TRUE_FALSE -> "(?i)\\b(?:answer|ans\\.?)\\s*[:.]?\\s*(true|false)"
            QuestionType.MCQ -> "(?i)\\b(?:answer|ans\\.?|correct)\\s*[:.]?\\s*([a-dA-D])"
            else -> "(?i)\\b(?:answer|ans\\.?)\\s*[:.]?\\s*"
        }
        val answerRegex = answerMarker.toRegex()
        val answerMatch = answerRegex.find(block)
        if (answerMatch != null) {
            val answer = when (type) {
                QuestionType.TRUE_FALSE, QuestionType.MCQ -> answerMatch.groupValues.getOrNull(1) ?: ""
                else -> block.substring(answerMatch.range.last + 1).trim()
            }
            val question = block.substring(0, answerMatch.range.first).trim()
            return Triple(question, answer, type)
        }
        if (block.contains("?")) {
            val qEnd = block.indexOf('?') + 1
            val question = block.substring(0, qEnd).trim()
            val rest = block.substring(qEnd).trim()
            val answer = rest.takeIf { it.isNotBlank() }?.take(500)
            return Triple(question, answer, type)
        }
        return Triple(block, null, type)
    }

    private fun detectQuestionType(block: String): QuestionType {
        if (TRUE_FALSE.matcher(block).find()) return QuestionType.TRUE_FALSE
        if (FILL_BLANK.matcher(block).find()) return QuestionType.FILL_BLANK
        val optionMatcher = MCQ_OPTION.matcher(block)
        var optionCount = 0
        while (optionMatcher.find() && optionCount < 3) optionCount++
        if (optionCount >= 2) return QuestionType.MCQ
        if (block.length > 300) return QuestionType.ESSAY
        if (block.length > 80) return QuestionType.SHORT
        if (Regex("\\d+(\\.\\d+)?").containsMatchIn(block)) return QuestionType.NUMERIC
        return QuestionType.SHORT
    }

    private fun extractOptions(block: String): String? {
        val options = mutableListOf<String>()
        val optionPattern = Regex("(?m)^\\s*([a-dA-D])[.)]\\s*(.+?)(?=^\\s*[a-dA-D][.)]|\\b(?:answer|ans)\\b|$)", RegexOption.DOT_MATCHES_ALL)
        optionPattern.findAll(block).forEach { match ->
            val text = match.groupValues.getOrNull(2)?.trim() ?: return@forEach
            if (text.isNotBlank()) options.add(text)
        }
        if (options.size >= 2) {
            return options.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" }.let { "[$it]" }
        }
        return null
    }
}
