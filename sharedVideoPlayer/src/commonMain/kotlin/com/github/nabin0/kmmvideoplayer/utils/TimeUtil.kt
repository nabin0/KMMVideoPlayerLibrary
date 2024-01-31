package com.github.nabin0.kmmvideoplayer.utils

fun convertMillisToReadableTime(millis: Long): ReadableTime {
    var availableSeconds = millis / 1000;
    val seconds = availableSeconds % 60
    availableSeconds -= seconds
    val minutes = availableSeconds / 60
    availableSeconds -= minutes * 60
    val hours = availableSeconds / (60 * 60)

    return ReadableTime(
        seconds = seconds,
        minutes = minutes,
        hours = hours
    )
}

class ReadableTime(val seconds: Long, val minutes: Long, val hours: Long) {
    override fun toString(): String {
        val time = listOf(minutes, seconds)
        // val timeWithHour = listOf(hours, minutes, seconds)
        return time.joinToString(":") { it.toString().padStart(2, '0') }
    }
}