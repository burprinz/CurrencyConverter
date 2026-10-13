package com.example.currencyconverter.util

/** Numeric OCR result and a ready-to-show representation for the camera screen. */
data class DetectedAmount(val value: Double, val display: String)

/**
 * Picks a likely price from OCR output. Prices are commonly next to a currency symbol
 * or a label such as "Preis"; dates, lot numbers and other long identifiers are ignored.
 * Each candidate gets a relevance score from nearby price words, currency symbols, and decimal
 * punctuation. The winning candidate is the highest-scoring plausible value, with the larger value
 * breaking ties; this avoids blindly selecting a barcode or a product's weight.
 */
fun findLargestAmount(text: String): DetectedAmount? {
    val lines = text.lineSequence().toList()
    val candidates = lines.flatMapIndexed { index, line ->
        val context = listOfNotNull(lines.getOrNull(index - 1), line, lines.getOrNull(index + 1)).joinToString(" ")
        amountPattern.findAll(line).mapNotNull { match ->
            val normalized = normalizeRecognizedNumber(match.value) ?: return@mapNotNull null
            val digits = normalized.count(Char::isDigit)
            // Long digit strings are usually barcodes, dates or lot numbers, not prices.
            if (digits > 6) return@mapNotNull null
            val value = normalized.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
                ?: return@mapNotNull null
            val hasCurrency = line.any { it == '€' || it == '$' || it == '£' || it == '¥' } ||
                Regex("\\b(EUR|USD|GBP|CHF)\\b", RegexOption.IGNORE_CASE).containsMatchIn(line)
            val hasPriceLabel = Regex("\\b(Preis|price|amount|total)\\b", RegexOption.IGNORE_CASE).containsMatchIn(context)
            val hasDecimal = normalized.contains('.')
            val score = (if (hasPriceLabel) 100 else 0) +
                (if (hasCurrency) 40 else 0) +
                (if (hasDecimal) 20 else 0) - digits
            ScoredAmount(DetectedAmount(value, formatAmount(value)), score)
        }
    }.toList()

    return candidates.maxWithOrNull(compareBy<ScoredAmount> { it.score }.thenBy { it.amount.value })?.amount
}

/** Internal OCR candidate paired with its relevance score; only the best candidate is returned. */
private data class ScoredAmount(val amount: DetectedAmount, val score: Int)

// Never span lines: OCR may put unrelated dates, weights and reference numbers nearby.
private val amountPattern = Regex("""(?<![\p{L}\d])\d+(?:[ \t.,]\d+)*(?![\p{L}\d])""")

private fun normalizeRecognizedNumber(raw: String): String? {
    val trimmed = raw.trim()
    // OCR occasionally reads a decimal mark as whitespace: "10 49".
    val spaceSeparated = trimmed.split(Regex("[ \\t]+"))
    if (spaceSeparated.size == 2 && spaceSeparated.all { part -> part.all(Char::isDigit) } &&
        spaceSeparated.first().length in 1..3 && spaceSeparated.last().length == 2
    ) {
        return "${spaceSeparated.first()}.${spaceSeparated.last()}"
    }

    val value = trimmed.filterNot(Char::isWhitespace)
    val commaIndex = value.lastIndexOf(',')
    val dotIndex = value.lastIndexOf('.')
    if (commaIndex < 0 && dotIndex < 0) return value.filter(Char::isDigit).ifEmpty { null }

    // If both separators occur, the rightmost is decimal and the earlier one is grouping punctuation.
    if (commaIndex >= 0 && dotIndex >= 0) {
        val decimalIndex = maxOf(commaIndex, dotIndex)
        val whole = value.substring(0, decimalIndex).filter(Char::isDigit)
        val decimals = value.substring(decimalIndex + 1).filter(Char::isDigit)
        return if (whole.isEmpty() || decimals.isEmpty()) null else "$whole.$decimals"
    }

    val separator = if (commaIndex >= 0) ',' else '.'
    val pieces = value.split(separator)
    // A single 3 digit suffix is usually a thousands separator (e.g. 1.234).
    val isGroupedInteger = pieces.size > 1 && pieces.drop(1).all { it.length == 3 } &&
        pieces.first().length in 1..3 && pieces.first() != "0"
    if (isGroupedInteger) return pieces.joinToString("").filter(Char::isDigit)

    val lastSeparator = value.lastIndexOf(separator)
    val whole = value.substring(0, lastSeparator).filter(Char::isDigit)
    val decimals = value.substring(lastSeparator + 1).filter(Char::isDigit)
    return if (whole.isEmpty() || decimals.isEmpty()) null else "$whole.$decimals"
}
