package com.github.nabin0.kmmvideoplayer.controller

actual class VideoPlayerControllerFactory {
    actual fun createVideoPlayer(): VideoPlayerController {
        return VideoPlayerController()
    }
}