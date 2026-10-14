package com.example.currencyconverter.util

/** Numeric OCR result and a ready-to-show representation for the camera screen. */
data class DetectedAmount(val value: Double, val display: String)

/** A number reconstructed from separate OCR boxes, with nearby label text for ranking. */
data class ReconstructedAmount(val text: String, val context: String, val confidence: Int)

/**
 * Finds the most likely amount in OCR text, optionally including decimal amounts recovered from
 * visually raised cents (for example, a large `1` followed by a smaller superscript `49`).
 * Price labels and currency markers raise a candidate's score; long identifiers are rejected.
 * If evidence is otherwise equal, the larger candidate wins, matching the screen's original
 * “largest amount” behavior.
 */
fun findLargestAmount(text: String, elevatedCentCandidates: List<ReconstructedAmount> = emptyList()): DetectedAmount? {
    // Keep OCR lines separate so a date or weight next to a price cannot become its decimal part.
    val lines = text.lineSequence().toList()
    val candidates = lines.flatMapIndexed { index, line ->
        // Nearby lines can carry a € sign or a word such as "Preis" even if the number is alone.
        val context = listOfNotNull(lines.getOrNull(index - 1), line, lines.getOrNull(index + 1)).joinToString(" ")
        // Normal OCR is parsed per line; reconstructed candidates are added only once below.
        val textCandidates = amountPattern.findAll(line).map { Candidate(it.value, context, 0) }
        // Reconstructed candidates are scored once, with their own geometry and label context.
        val candidatesForLine = if (index == 0) textCandidates + elevatedCentCandidates.map {
            Candidate(it.text, it.context, it.confidence)
        } else textCandidates
        candidatesForLine.mapNotNull { candidate ->
            // Normalize first so validation and scoring use one consistent decimal representation.
            val rawCandidate = candidate.text
            val normalized = normalizeRecognizedNumber(rawCandidate) ?: return@mapNotNull null
            val digits = normalized.count(Char::isDigit)
            // Ignore long numbers even when OCR happens to place them beside a currency marker.
            if (digits > 6) return@mapNotNull null
            val value = normalized.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
                ?: return@mapNotNull null
            val hasCurrency = candidate.context.any { it == '€' || it == '$' || it == '£' || it == '¥' } ||
                Regex("\\b(EUR|USD|GBP|CHF)\\b", RegexOption.IGNORE_CASE).containsMatchIn(candidate.context)
            val hasPriceLabel = Regex("\\b(Preis|price|amount|total)\\b", RegexOption.IGNORE_CASE).containsMatchIn(candidate.context)
            val hasDecimal = normalized.contains('.')
            // Price labels and currency symbols carry more weight than digit count or punctuation alone.
            val score = (if (hasPriceLabel) 100 else 0) +
                (if (hasCurrency) 40 else 0) +
                (if (hasDecimal) 20 else 0) +
                candidate.confidence - digits
            ScoredAmount(DetectedAmount(value, formatAmount(value)), score)
        }
    }.toList()

    // Return null instead of inventing a value when OCR found no plausible numeric candidate.
    return candidates.maxWithOrNull(compareBy<ScoredAmount> { it.score }.thenBy { it.amount.value })?.amount
}

/** Parsed amount and ranking score; the score itself is only used to choose a result. */
private data class ScoredAmount(val amount: DetectedAmount, val score: Int)

/** Raw OCR candidate before normalization, paired with nearby evidence and optional geometry confidence. */
private data class Candidate(val text: String, val context: String, val confidence: Int)

/**
 * Keeps live OCR output steady when adjacent frames disagree about faint punctuation. A changed
 * reading must repeat before replacing the display; when both readings have the same digits,
 * prefer the decimal interpretation because price labels commonly lose their comma/dot in OCR.
 */
class AmountStabilizer(private val confirmationWindowMs: Long = 1_500L) {
    private var displayed: DetectedAmount? = null
    private var pending: DetectedAmount? = null
    private var pendingHits = 0
    private var pendingAt = 0L

    /** Returns the stable value to display, or null until a changed reading is confirmed. */
    fun update(candidate: DetectedAmount, nowMillis: Long): DetectedAmount {
        val current = displayed
        // The first reading is useful immediately; a stable repeat also clears any stale pending value.
        if (current == null || sameAmount(current, candidate)) {
            displayed = candidate
            clearPending()
            return candidate
        }

        if (sameDigitsDifferentDecimal(current, candidate) || samePriceDecimalPair(current, candidate)) {
            // If OCR has just recovered punctuation, accept it immediately and keep it over integer noise.
            if (candidate.display.contains('.') || !current.display.contains('.')) displayed = candidate
            clearPending()
            return displayed ?: candidate
        }

        // Require a second observation inside a short window before accepting a completely different number.
        if (pending?.display == candidate.display && nowMillis - pendingAt <= confirmationWindowMs) {
            pendingHits++
        } else {
            pending = candidate
            pendingHits = 1
            pendingAt = nowMillis
        }
        if (pendingHits >= 2) {
            displayed = candidate
            clearPending()
        }
        return displayed ?: candidate
    }

    /** Forget prior readings when switching between live preview and a still photo. */
    fun reset() {
        displayed = null
        clearPending()
    }

    /** Numeric equality avoids treating harmless differences in display formatting as a new reading. */
    private fun sameAmount(first: DetectedAmount, second: DetectedAmount) = first.value == second.value

    /** Detects OCR adding/removing punctuation while leaving the same digit sequence. */
    private fun sameDigitsDifferentDecimal(first: DetectedAmount, second: DetectedAmount): Boolean {
        val firstDigits = first.display.filter(Char::isDigit)
        val secondDigits = second.display.filter(Char::isDigit)
        return firstDigits.isNotEmpty() && firstDigits == secondDigits && first.display != second.display
    }

    /** Also catches formatting such as `1049` versus `10.49` (the formatter drops `.00`). */
    private fun samePriceDecimalPair(first: DetectedAmount, second: DetectedAmount): Boolean {
        // The general amount formatter trims trailing zeroes, so `10.50` is displayed as `10.5`.
        fun isPriceDecimal(amount: DetectedAmount) = amount.display.substringAfter('.', "").length in 1..2
        return (isPriceDecimal(first) && second.display.none { it == '.' } && second.value == first.value * 100.0) ||
            (isPriceDecimal(second) && first.display.none { it == '.' } && first.value == second.value * 100.0)
    }

    /** Clears the confirmation candidate after acceptance or when changing camera modes. */
    private fun clearPending() {
        pending = null
        pendingHits = 0
        pendingAt = 0L
    }
}

// The separators include spaces because OCR often turns a faint comma into whitespace (`10 49`).
// The match is deliberately confined to one line to avoid joining unrelated numbers vertically.
private val amountPattern = Regex("""(?<![\p{L}\d])\d+(?:[ \t.,]\d+)*(?![\p{L}\d])""")

/** Converts an OCR number to a dot-decimal form while preserving likely cents and groupings. */
private fun normalizeRecognizedNumber(raw: String): String? {
    // Trim outer OCR padding first; internal spaces may still signal a faint missing separator.
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
    // A bare digit string stays an integer; cents are inferred only if OCR left separator evidence.
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

    // Use the final punctuation as the decimal mark and reject incomplete values such as `12,`.
    val lastSeparator = value.lastIndexOf(separator)
    val whole = value.substring(0, lastSeparator).filter(Char::isDigit)
    val decimals = value.substring(lastSeparator + 1).filter(Char::isDigit)
    return if (whole.isEmpty() || decimals.isEmpty()) null else "$whole.$decimals"
}
