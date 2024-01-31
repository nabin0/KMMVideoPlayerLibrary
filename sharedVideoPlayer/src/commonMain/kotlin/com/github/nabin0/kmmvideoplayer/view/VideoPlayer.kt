package com.github.nabin0.kmmvideoplayer.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.nabin0.kmmvideoplayer.controller.VideoPlayerController
import com.github.nabin0.kmmvideoplayer.data.VideoItem
import com.github.nabin0.kmmvideoplayer.utils.noRippleClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoItem: VideoItem?,
    videoPlayerController: VideoPlayerController,
    listOfVideoUrls: List<VideoItem>?,
    startPlayMuted: Boolean = false,
    setCCEnabled:Boolean = false,

    ) {
    val rememberedPlayerController = remember { videoPlayerController }
    var isControllerCreated by rememberSaveable { mutableStateOf(false) }
    if (!isControllerCreated) {
        rememberedPlayerController.BuildPlayer { }
        videoItem?.let {
            rememberedPlayerController.setMediaItem(it)
        }
        listOfVideoUrls?.let {
            rememberedPlayerController.setPlayList(it)
        }
        if(startPlayMuted){
            rememberedPlayerController.setVolumeLevel(0F)
        }
        rememberedPlayerController.prepare()
        rememberedPlayerController.setCCEnabled(setCCEnabled)
        rememberedPlayerController.playWhenReady(true)
        rememberedPlayerController.HandleActivityLifecycleStageChanges()
        isControllerCreated = true
    }

    VideoPlayerView(modifier = modifier, videoPlayerController = rememberedPlayerController)

}

@Composable
fun VideoPlayerView(modifier: Modifier = Modifier, videoPlayerController: VideoPlayerController) {
    var showOverlay by remember { mutableStateOf(true) }
    var showCustomDialogBoxForVideoControls by remember { mutableStateOf(false) }

    LaunchedEffect(showOverlay) {
        yield()
        delay(5000)
        showOverlay = false
    }


    var enableLandscapeMode by remember { mutableStateOf(false) }
    if (enableLandscapeMode) {
        videoPlayerController.EnableLandscapeScreenMode()
    } else {
        videoPlayerController.EnablePortraitScreenMode()
    }

    val videoViewModifier =
        if (enableLandscapeMode) Modifier.fillMaxSize() else modifier.fillMaxHeight(0.27f)

    Box(modifier = videoViewModifier.background(Color.Black)) {
        videoPlayerController.PlayerView(modifier = Modifier.fillMaxWidth().noRippleClickable {
            //showOverlay = true
        }, useDefaultController = false)

        // ViKitView onclick not working, used as its alternative until fixed
        Box(modifier = Modifier.fillMaxSize().clickable {
            showOverlay = !showOverlay
        })

        VideoPlayerOverlay(
            modifier = Modifier.matchParentSize(),
            videoPlayerController = videoPlayerController,
            showOverLay = showOverlay,
            onClickOverlayToHide = {
                showOverlay = false
            },
            onToggleScreenOrientation = { enableLandscapeMode = !enableLandscapeMode },
            isLandscapeView = enableLandscapeMode,
            onClickShowPlaybackSpeedControls = {
                showCustomDialogBoxForVideoControls = true
            }
        )



        if (showCustomDialogBoxForVideoControls) {
            CustomDialogBox(
                onHideBoxClicked = {
                    showCustomDialogBoxForVideoControls = false
                },
                content = {
                    VideoPreferencesBox(
                        videoPlayerController = videoPlayerController,
                        modifier = Modifier.fillMaxSize()
                    )

                },
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 16.dp).fillMaxSize()
                    .background(Color.Black)
            )
        }
    }
}