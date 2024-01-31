package com.github.nabin0.kmmvideoplayer.controller

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class VideoPlayerControllerFactory {

    actual fun createVideoPlayer(): VideoPlayerController {
        return VideoPlayerController()
    }
}