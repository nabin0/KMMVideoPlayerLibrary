package com.github.nabin0.kmmvideoplayer.controller

import io.mockk.unmockkAll
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class VideoPlayerControllerFactoryTest {

    private lateinit var videoPlayerControllerFactory: VideoPlayerControllerFactory

    @BeforeTest
    fun `initial setup`() {
        videoPlayerControllerFactory = VideoPlayerControllerFactory()
    }

    @AfterTest
    fun cleanup(){
        unmockkAll()
    }

    @Test
    fun `createVideoPlayer should return instance of videoPlayerController`() {
        val result = videoPlayerControllerFactory.createVideoPlayer()
        assertTrue { result != null && result is VideoPlayerController }
    }
}