package org.github.raspopov.extensions

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

enum class FormatPatterns(val pattern: String)
{
    HH_MM("HH:mm"),
    DD_MM_YYYY_HH_MM("dd.MM.yy HH:mm"),
}

fun LocalDateTime.format(pattern: FormatPatterns): String{
    @OptIn(FormatStringsInDatetimeFormats::class)
    val dateTimeFormat = LocalDateTime.Format {
        byUnicodePattern(pattern.pattern)
    }
    return this.format(dateTimeFormat)
}