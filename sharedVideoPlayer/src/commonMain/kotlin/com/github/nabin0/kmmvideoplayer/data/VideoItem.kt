package com.github.nabin0.kmmvideoplayer.data

data class VideoItem(
    val id: Int,
    val videoUrl: String,
    val title: String? = "Unknown",
    val videoDescription: String? = "Unknown",
    val licenseUrl: String? = null,
    val listOfClosedCaptions: List<ClosedCaption>? = null,
    val isDrmEnabled:Boolean?,
    val licenseToken: String?,
    val certificateUrl: String?
)
