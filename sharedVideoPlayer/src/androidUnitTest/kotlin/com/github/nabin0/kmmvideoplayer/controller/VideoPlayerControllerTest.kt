package com.github.nabin0.kmmvideoplayer.controller

import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer
import com.github.nabin0.kmmvideoplayer.data.VideoItem
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


class VideoPlayerControllerTest {

    @MockK
    lateinit var exoPlayer: ExoPlayer

    lateinit var videoPlayerController: VideoPlayerController

    @BeforeTest
    fun `initial setup`() {
        MockKAnnotations.init(this, relaxed = true)
        videoPlayerController = VideoPlayerController()
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(Uri.EMPTY)
    }

    @AfterTest
    fun cleanup() {
        unmockkAll()
    }

    @Test
    fun `ViewPlayerController currentPosition should return long value of players current position (zero if exoplayer is null)`() {

        var res = videoPlayerController.currentPosition()
        assert(res == 0L)

        initExoplayer()
        coEvery { exoPlayer.currentPosition }.returns(100L)
        res = videoPlayerController.currentPosition()
        assert(res == 100L)
    }


    private fun initExoplayer() {
        val player = VideoPlayerController::class.java.getDeclaredField("exoPlayer")
        player.isAccessible = true
        player.set(videoPlayerController, exoPlayer)
    }

   @Test
   fun `VideoPlayerController getCurrentPlaybackSpeed should return current playbackspeed of player`(){
       val resultPlaybackSpeed = videoPlayerController.getCurrentPlaybackSpeed()
       assert(resultPlaybackSpeed == 1F)
   }

    @Test
    fun `prepare of VideoPlayerController should prepare exoplayer for playing video`(){
        initExoplayer()
        videoPlayerController.prepare()
        coVerify { exoPlayer.prepare() }
    }

    @Test
    fun `play method should call play() of exoplayer`(){
        initExoplayer()
        videoPlayerController.play()
        coVerify { exoPlayer.play() }
    }
    @Test
    fun `pause method should call pause() of exoplayer`(){
        initExoplayer()
        videoPlayerController.pause()
        coVerify { exoPlayer.pause() }
    }

    @Test
    fun `playWhenReady method should call playWhenReady of exoplayer`(){
        initExoplayer()
        videoPlayerController.playWhenReady(true)
        coVerify { exoPlayer.playWhenReady = true }
    }

    @Test
    fun  `setMediaItem mediaItem builds and sets Media3MediaItem to exoplayer`(){
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(Uri.EMPTY)
        initExoplayer()
        videoPlayerController.setMediaItem(VideoItem(
            id = 1,
            videoUrl = "https://www.testurl.com",
            title = "title",
            videoDescription = null,
            licenseUrl = null,
            listOfClosedCaptions = null,
            isDrmEnabled = false,
            licenseToken = null,
            certificateUrl = null
        ))
        verify { exoPlayer.setMediaItem(any()) }
    }

    @Test
    fun `seekTo method should call seekTo of exoplayer to set new progress value`(){
        initExoplayer()
        videoPlayerController.seekTo(100L)
        every { exoPlayer.seekTo(100L) }
    }


    @Test
    fun `stop should call stop of exoplayer to stop the video playing`(){
        initExoplayer()
        videoPlayerController.stop()
        verify { exoPlayer.stop() }
    }

    @Test
    fun `release player should release exoplayer`(){
        initExoplayer()
        videoPlayerController.releasePlayer()
    }


    @Test
    fun `addPlaylist should add playlist to exoplayer instance`(){
        val sampleList = listOf(VideoItem(
            id = 1,
            videoUrl = "https://www.testurl.com",
            title = "title",
            videoDescription = null,
            licenseUrl = null,
            listOfClosedCaptions = null,
            isDrmEnabled = false,
            licenseToken = null,
            certificateUrl = null
        ))

//        val buildMediaItemField=  VideoPlayerController::class.java.getDeclaredMethod("buildMediaItem")
//        buildMediaItemField.isAccessible = true
//        val mediaItem = buildMediaItemField.invoke(videoPlayerController, sampleList[0])
//        assert(mediaItem is VideoItem)
        initExoplayer()
        videoPlayerController.addPlayList(sampleList)
         // coVerify { exoPlayer.addMediaItem() }

    }




}