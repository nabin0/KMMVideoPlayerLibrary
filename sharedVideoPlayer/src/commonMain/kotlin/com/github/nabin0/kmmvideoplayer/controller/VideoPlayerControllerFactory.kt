package com.github.nabin0.kmmvideoplayer.controller

import androidx.compose.runtime.Composable

expect class VideoPlayerControllerFactory {
    fun createVideoPlayer(): VideoPlayerController
}