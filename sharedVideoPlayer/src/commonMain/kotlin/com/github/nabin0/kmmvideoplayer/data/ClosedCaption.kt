package com.github.nabin0.kmmvideoplayer.data

data class ClosedCaption(
    val subtitleLink: String,
    val language: String
)

data class ClosedCaptionForTrackSelector(
    val index: Int,
    val language: String,
    val name: String?
)
