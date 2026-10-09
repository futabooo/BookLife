package com.futabooo.android.booklife.ui.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Read-date helpers. Dates are exchanged with bookmeter as `yyyy/M/d` and shown as `yyyy/MM/dd`.
 * The Material3 date picker works with UTC-midnight millis, so everything here is in UTC.
 */
internal object ReadDates {

    private fun format(pattern: String) = SimpleDateFormat(pattern, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }

    /** UTC-midnight millis of today's local date. */
    fun todayMillis(now: Calendar = Calendar.getInstance()): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis

    /** `yyyy/M/d` (also accepts zero-padded values) -> millis, or null when it does not parse. */
    fun parse(text: String?): Long? {
        if (text.isNullOrBlank()) return null
        return try {
            format("yyyy/M/d").parse(text.trim())?.time
        } catch (e: java.text.ParseException) {
            null
        }
    }

    /** `yyyy/MM/dd`, shown to the user. */
    fun display(millis: Long): String = format("yyyy/MM/dd").format(millis)

    /** `yyyy/M/d`, sent to the server. */
    fun submit(millis: Long): String = format("yyyy/M/d").format(millis)
}
