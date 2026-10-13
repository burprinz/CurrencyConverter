package com.example.currencyconverter.util

/** Numeric OCR result and a ready-to-show representation for the camera screen. */
data class DetectedAmount(val value: Double, val display: String)

/**
 * Finds the most likely amount in OCR text, optionally including decimal amounts recovered from
 * visually raised cents (for example, a large `1` followed by a smaller superscript `49`).
 * Price labels and currency markers raise a candidate's score; long identifiers are rejected.
 * If evidence is otherwise equal, the larger candidate wins, matching the screen's original
 * “largest amount” behavior.
 */
fun findLargestAmount(text: String, elevatedCentCandidates: List<String> = emptyList()): DetectedAmount? {
    // Keep OCR lines separate so a date or weight next to a price cannot become its decimal part.
    val lines = text.lineSequence().toList()
    val candidates = lines.flatMapIndexed { index, line ->
        val context = listOfNotNull(lines.getOrNull(index - 1), line, lines.getOrNull(index + 1)).joinToString(" ")
        val textCandidates = amountPattern.findAll(line).map { it.value to false }
        // A geometric reconstruction is strong evidence that the smaller digits are cents.
        val reconstructedCandidates = elevatedCentCandidates.map { it to true }
        (textCandidates + reconstructedCandidates).mapNotNull { (rawCandidate, reconstructed) ->
            val normalized = normalizeRecognizedNumber(rawCandidate) ?: return@mapNotNull null
            val digits = normalized.count(Char::isDigit)
            // Ignore long numbers even when OCR happens to place them beside a currency marker.
            if (digits > 6) return@mapNotNull null
            val value = normalized.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
                ?: return@mapNotNull null
            val hasCurrency = line.any { it == '€' || it == '$' || it == '£' || it == '¥' } ||
                Regex("\\b(EUR|USD|GBP|CHF)\\b", RegexOption.IGNORE_CASE).containsMatchIn(line)
            val hasPriceLabel = Regex("\\b(Preis|price|amount|total)\\b", RegexOption.IGNORE_CASE).containsMatchIn(context)
            val hasDecimal = normalized.contains('.')
            val score = (if (hasPriceLabel) 100 else 0) +
                (if (hasCurrency) 40 else 0) +
                (if (hasDecimal) 20 else 0) +
                (if (reconstructed) 50 else 0) - digits
            ScoredAmount(DetectedAmount(value, formatAmount(value)), score)
        }
    }.toList()

    // Return null instead of inventing a value when OCR found no plausible numeric candidate.
    return candidates.maxWithOrNull(compareBy<ScoredAmount> { it.score }.thenBy { it.amount.value })?.amount
}

/** Internal OCR candidate paired with its relevance score; only the best candidate is returned. */
private data class ScoredAmount(val amount: DetectedAmount, val score: Int)

// The separators include spaces because OCR often turns a faint comma into whitespace (`10 49`).
// The match is deliberately confined to one line to avoid joining unrelated numbers vertically.
private val amountPattern = Regex("""(?<![\p{L}\d])\d+(?:[ \t.,]\d+)*(?![\p{L}\d])""")

/** Converts an OCR number to a dot-decimal form while preserving likely cents and groupings. */
private fun normalizeRecognizedNumber(raw: String): String? {
    val trimmed = raw.trim()
    // When a separator vanished entirely, a short two-digit tail is the common cents pattern.
    val spaceSeparated = trimmed.split(Regex("[ \\t]+"))
    if (spaceSeparated.size == 2 && spaceSeparated.all { part -> part.all(Char::isDigit) } &&
        spaceSeparated.first().length in 1..3 && spaceSeparated.last().length == 2
    ) {
        return "${spaceSeparated.first()}.${spaceSeparated.last()}"
    }

    // Remove spaces around punctuation before deciding whether comma or dot is decimal.
    val value = trimmed.filterNot(Char::isWhitespace)
    val commaIndex = value.lastIndexOf(',')
    val dotIndex = value.lastIndexOf('.')
    if (commaIndex < 0 && dotIndex < 0) return value.filter(Char::isDigit).ifEmpty { null }

    // With both comma and dot present, the final punctuation is decimal; earlier ones group thousands.
    if (commaIndex >= 0 && dotIndex >= 0) {
        val decimalIndex = maxOf(commaIndex, dotIndex)
        val whole = value.substring(0, decimalIndex).filter(Char::isDigit)
        val decimals = value.substring(decimalIndex + 1).filter(Char::isDigit)
        return if (whole.isEmpty() || decimals.isEmpty()) null else "$whole.$decimals"
    }

    val separator = if (commaIndex >= 0) ',' else '.'
    val pieces = value.split(separator)
    // A three-digit suffix usually groups thousands (`1.234`), while one or two digits are cents.
    val isGroupedInteger = pieces.size > 1 && pieces.drop(1).all { it.length == 3 } &&
        pieces.first().length in 1..3 && pieces.first() != "0"
    if (isGroupedInteger) return pieces.joinToString("").filter(Char::isDigit)

    val lastSeparator = value.lastIndexOf(separator)
    val whole = value.substring(0, lastSeparator).filter(Char::isDigit)
    val decimals = value.substring(lastSeparator + 1).filter(Char::isDigit)
    return if (whole.isEmpty() || decimals.isEmpty()) null else "$whole.$decimals"
}
