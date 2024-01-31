package com.github.nabin0.kmmvideoplayer.data

data class AudioTrack(
    val index: Int,
    val language: String,
    val name: String?,
    val isStereo: Boolean = false,
    val isSurround: Boolean = false,
    val audioTrackGroupIndex:Int
)
