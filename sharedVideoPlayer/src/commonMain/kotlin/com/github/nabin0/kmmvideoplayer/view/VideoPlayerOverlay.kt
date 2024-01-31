package com.github.nabin0.kmmvideoplayer.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nabin0.kmmvideoplayer.controller.VideoPlayerController
import com.github.nabin0.kmmvideoplayer.data.AudioTrack
import com.github.nabin0.kmmvideoplayer.data.ClosedCaptionForTrackSelector
import com.github.nabin0.kmmvideoplayer.data.VideoQuality
import com.github.nabin0.kmmvideoplayer.utils.convertMillisToReadableTime
import com.github.nabin0.kmmvideoplayer.utils.noRippleClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

@Composable
fun BoxScope.VideoPlayerOverlay(
    modifier: Modifier = Modifier,
    videoPlayerController: VideoPlayerController,
    showOverLay: Boolean = false,
    onClickOverlayToHide: () -> Unit,
    onToggleScreenOrientation: () -> Unit,
    isLandscapeView: Boolean,
    onClickShowPlaybackSpeedControls: () -> Unit,
) {
    var currentPosition by remember { mutableStateOf<Long?>(null) }
    val videoDuration by videoPlayerController.mediaDuration.collectAsState()
    val isPlaying by videoPlayerController.isPlaying.collectAsState()
    val isBuffering by videoPlayerController.isBuffering.collectAsState()

    if (isBuffering && videoDuration <= 0) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center).size(50.dp),
            color = Color.White
        )
    }

    if (showOverLay) {
        LaunchedEffect(Unit) {
            while (true) {
                yield()
                delay(100)
                currentPosition = videoPlayerController.currentPosition()
            }
        }

        Box(
            modifier = modifier.background(color = Color.Black.copy(alpha = 0.4f)).fillMaxWidth()
                .noRippleClickable {
                    onClickOverlayToHide()
                }) {

            VideoTopRowControls(onClickShowPlaybackSpeedControls = onClickShowPlaybackSpeedControls)

            PlayerControlButtons(
                isPaused = !isPlaying,
                showProgressIndicator = isBuffering && videoDuration > 0,
                pause = { videoPlayerController.pause() },
                resume = { videoPlayerController.play() },
                seekForward = { videoPlayerController.seekTo(currentPosition?.plus(it) ?: 0) },
                seekBackward = { videoPlayerController.seekTo(currentPosition?.minus(it) ?: 0) },
                skipToNextMediaItem = { videoPlayerController.playNextFromPlaylist() },
                skipToPreviousMediaItem = { videoPlayerController.playPreviousFromPlaylist() }
            )

            VideoCurrentProgressData(
                onToggleScreenOrientation = onToggleScreenOrientation,
                isLandscapeView = isLandscapeView,
                totalDuration = videoDuration.toFloat(),
                currentPosition = currentPosition?.toFloat() ?: 0F,
                onProgressValueChanged = { videoPlayerController.seekTo(millis = it.toLong()) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun BoxScope.VideoTopRowControls(
    modifier: Modifier = Modifier,
    onClickShowPlaybackSpeedControls: () -> Unit,
) {
    Row(modifier = modifier.align(Alignment.TopEnd)) {
        val playerControlIcon: @Composable (imageVector: ImageVector, size: Dp) -> Unit =
            @Composable { imageVector, size ->
                Icon(
                    imageVector = imageVector,
                    tint = Color.White,
                    modifier = Modifier.size(size),
                    contentDescription = "",
                )
            }

        val modifier =
            Modifier.padding(top = 16.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0f), shape = CircleShape)
                .size(24.dp)
        Spacer(Modifier.weight(2f))
        IconButton(modifier = modifier, onClick = {
            onClickShowPlaybackSpeedControls()
        }) {
            playerControlIcon(Icons.Rounded.Settings, 24.dp)
        }

    }
}

@Composable
fun BoxScope.PlayerControlButtons(
    isPaused: Boolean,
    showProgressIndicator: Boolean,
    pause: () -> Unit,
    resume: () -> Unit,
    skipToPreviousMediaItem: () -> Unit,
    skipToNextMediaItem: () -> Unit,
    seekForward: (millis: Long) -> Unit,
    seekBackward: (millis: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val seekAmount = 10000L
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier.align(Alignment.Center).fillMaxWidth()
    ) {
        val playerControlIcon: @Composable (imageVector: ImageVector, size: Dp) -> Unit =
            @Composable { imageVector, size ->
                Icon(
                    imageVector = imageVector,
                    tint = Color.White,
                    modifier = Modifier.size(size),
                    contentDescription = "",
                )
            }

        val modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)

        IconButton(modifier = modifier, onClick = {
            skipToPreviousMediaItem()
        }) {
            playerControlIcon(Icons.Rounded.SkipPrevious, 35.dp)
        }

        IconButton(modifier = modifier, onClick = {
            seekBackward(seekAmount)
        }) {
            playerControlIcon(Icons.Rounded.Replay10, 35.dp)
        }

        if (showProgressIndicator) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                color = Color.White
            )
        } else {
            if (isPaused) {
                IconButton(modifier = modifier, onClick = { resume() }) {
                    playerControlIcon(Icons.Rounded.PlayArrow, 50.dp)
                }
            } else {
                IconButton(modifier = modifier, onClick = { pause() }) {
                    playerControlIcon(Icons.Rounded.Pause, 50.dp)
                }
            }
        }

        IconButton(modifier = modifier, onClick = { seekForward(seekAmount) }) {
            playerControlIcon(Icons.Rounded.Forward10, 35.dp)
        }

        IconButton(modifier = modifier, onClick = {
            skipToNextMediaItem()
        }) {
            playerControlIcon(Icons.Rounded.SkipNext, 35.dp)
        }
    }
}


@Composable
fun VideoProgressBar(
    totalDuration: Float,
    currentPosition: Float,
    onProgressValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (totalDuration <= 0) return
    var selectedValue by remember { mutableStateOf(0f) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isInteracting = isPressed || isDragged

    val sliderValue by derivedStateOf {
        if (isInteracting) {
            selectedValue
        } else {
            currentPosition
        }
    }

    Slider(
        modifier = modifier.padding(0.dp).height(10.dp),
        value = sliderValue,
        onValueChange = {
            selectedValue = it
        },
        onValueChangeFinished = {
            onProgressValueChanged(selectedValue)
        },
        interactionSource = interactionSource,
        valueRange = 0f..totalDuration,
        colors = SliderDefaults.colors(
            thumbColor = Color.Red,
            activeTrackColor = Color.Red,
            inactiveTrackColor = Color.White.copy(alpha = 0.7f)
        )
    )
}

@Composable
fun BoxScope.VideoCurrentProgressData(
    onToggleScreenOrientation: () -> Unit,
    isLandscapeView: Boolean,
    totalDuration: Float,
    currentPosition: Float,
    onProgressValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (totalDuration <= 0) return

    Column(
        modifier = modifier.align(Alignment.BottomStart).fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom
    ) {
        val textStyle =
            TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${convertMillisToReadableTime(currentPosition.toLong())}",
                    style = textStyle, maxLines = 1
                )
                Text(
                    text = "/ ${convertMillisToReadableTime(totalDuration.toLong())}",
                    style = textStyle.copy(color = Color.White.copy(alpha = 0.85f)), maxLines = 1
                )
            }

            Box(modifier = Modifier.padding(end = 8.dp)) {
                val playerControlIcon: @Composable (imageVector: ImageVector, size: Dp) -> Unit =
                    @Composable { imageVector, size ->
                        Icon(
                            imageVector = imageVector,
                            tint = Color.White,
                            modifier = Modifier.size(size),
                            contentDescription = "",
                        )
                    }
                if (isLandscapeView) {
                    IconButton(modifier = Modifier.size(24.dp).padding(bottom = 0.dp), onClick = {
                        onToggleScreenOrientation()
                    }) {
                        playerControlIcon(Icons.Rounded.FullscreenExit, 24.dp)
                    }
                } else {
                    IconButton(modifier = Modifier.size(24.dp).padding(bottom = 0.dp), onClick = {
                        onToggleScreenOrientation()
                    }) {
                        playerControlIcon(Icons.Rounded.Fullscreen, 24.dp)
                    }
                }
            }

        }
        VideoProgressBar(
            totalDuration = totalDuration,
            currentPosition = currentPosition,
            onProgressValueChanged = onProgressValueChanged,
            modifier = Modifier.padding(0.dp).fillMaxWidth()
        )

    }
}


@Composable
fun CustomDialogBox(
    onHideBoxClicked: () -> Unit, content: @Composable() () -> Unit,
    modifier: Modifier = Modifier.background(Color.Black),
) {
    Column(modifier = modifier.padding(10.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Rounded.Close,
                null,
                modifier = Modifier.align(Alignment.TopEnd).clickable {
                    onHideBoxClicked()
                },
                tint = Color.White
            )
        }
        content()
    }
}

@Composable
fun VideoPreferencesBox(
    videoPlayerController: VideoPlayerController,
    modifier: Modifier = Modifier,
) {
    val videoQualitiesList by videoPlayerController.listOfVideoResolutions.collectAsState()
    val audioTracksList by videoPlayerController.listOfAudioFormats.collectAsState()

    var selectedPreferenceOptionIndex by remember { mutableStateOf(0) }
    val ccList by videoPlayerController.listOfCC.collectAsState()
    val preferenceOptions =
        remember { listOf("PlaybackSpeed", "Video Quality", "Closed Caption", "Audio Tracks") }

    Row(modifier = Modifier.fillMaxWidth().background(Color.Black)) {
        Surface(modifier = Modifier.weight(1f)) {
            LazyColumn(modifier = Modifier.fillMaxWidth().background(Color.Black)) {
                itemsIndexed(items = preferenceOptions) { index, item ->
                    Row(
                        modifier = Modifier.clickable {
                            selectedPreferenceOptionIndex = index
                        }.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val textStyle = if (selectedPreferenceOptionIndex == index)
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Green
                            )
                        else TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = item,
                            style = textStyle.copy(textAlign = TextAlign.Start),
                        )
                    }
                }

            }
        }
        Spacer(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Red))
        Surface(modifier = Modifier.weight(1f)) {
            if (selectedPreferenceOptionIndex == 0) {
                PlaybackSpeedList(
                    currentPlaybackSpeed = videoPlayerController.getCurrentPlaybackSpeed(),
                    onPlaybackSpeedSelected = {
                        videoPlayerController.setPlaybackSpeed(it)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (selectedPreferenceOptionIndex == 1) {
                if (videoQualitiesList != null) {
                    VideoResolutionDialogBox(
                        videoQualitiesList = videoQualitiesList!!,
                        currentSelectedQuality = videoPlayerController.getCurrentVideoStreamingQuality(),
                        onItemSelected = {
                            videoPlayerController.setSpecificVideoQuality(it)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (selectedPreferenceOptionIndex == 2) {
                if (!ccList.isNullOrEmpty()) {
                    CCSelectorDialogBox(
                        ccList = ccList!!,
                        currentSelectedCC = videoPlayerController.getCurrentCC(),
                        onItemSelected = {
                            videoPlayerController.setSpecificCC(it)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                }
            } else if (selectedPreferenceOptionIndex == 3) {
                if (!audioTracksList.isNullOrEmpty()) {
                    AudioTrackSelectorBox(
                        audioTrackList = audioTracksList!!,
                        modifier = Modifier.fillMaxWidth(),
                        selectedAudioTrack = videoPlayerController.getCurrentSelectedAudioTrack(),
                        onItemSelected = { videoPlayerController.setSpecificAudioTrack(it) },
                    )

                }
            }
        }
    }
}

@Composable
fun PlaybackSpeedList(
    currentPlaybackSpeed: Float,
    onPlaybackSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var localCurrentPlaybackSpeed by remember { mutableStateOf(currentPlaybackSpeed) }
    val listOfPlaybackSpeeds = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(Color.Black),
    ) {
        itemsIndexed(items = listOfPlaybackSpeeds) { index, item ->
            Row(
                modifier = Modifier.clickable {
                    onPlaybackSpeedSelected(item)
                    localCurrentPlaybackSpeed = item
                }.fillMaxWidth().padding(1.dp),
            ) {
                val textStyle = if (localCurrentPlaybackSpeed == item)
                    TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Green)
                else TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${item}x",
                    style = textStyle.copy(textAlign = TextAlign.Start),
                    modifier = Modifier.weight(1f)
                )

            }
        }
    }
}

@Composable
fun VideoResolutionDialogBox(
    videoQualitiesList: List<VideoQuality>,
    currentSelectedQuality: VideoQuality,
    onItemSelected: (VideoQuality) -> Unit,
    modifier: Modifier = Modifier,
) {
    var localVideoSelectedQuality by remember { mutableStateOf(currentSelectedQuality) }
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(items = videoQualitiesList) { index, item ->
            Row(
                modifier = Modifier.clickable {
                    onItemSelected(item)
                    localVideoSelectedQuality = item
                }.fillMaxWidth().padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val textStyle = if (localVideoSelectedQuality.index == item.index)
                    TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Green)
                else TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.value,
                    style = textStyle.copy(textAlign = TextAlign.Start),
                    modifier = Modifier.weight(1f)
                )

            }
        }
    }
}


@Composable
fun CCSelectorDialogBox(
    ccList: List<ClosedCaptionForTrackSelector>,
    currentSelectedCC: ClosedCaptionForTrackSelector,
    onItemSelected: (ClosedCaptionForTrackSelector) -> Unit,
    modifier: Modifier = Modifier,
) {
    var localVideoSelectedQuality by remember { mutableStateOf(currentSelectedCC) }
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(items = ccList) { index, item ->
            Row(
                modifier = Modifier.clickable {
                    onItemSelected(item)
                    localVideoSelectedQuality = item
                }.fillMaxWidth().padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val textStyle = if (localVideoSelectedQuality.index == item.index)
                    TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Green)
                else TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.language,
                    style = textStyle.copy(textAlign = TextAlign.Start),
                    modifier = Modifier.weight(1f)
                )

            }
        }
    }
}


@Composable
fun AudioTrackSelectorBox(
    audioTrackList: List<AudioTrack>,
    selectedAudioTrack: AudioTrack?,
    onItemSelected: (AudioTrack) -> Unit,
    modifier: Modifier = Modifier,
) {
    var localSelectedAudioTrack by remember { mutableStateOf(selectedAudioTrack) }
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(items = audioTrackList) { index, item ->
            Row(
                modifier = Modifier.clickable {
                    onItemSelected(item)
                    localSelectedAudioTrack = item
                }.fillMaxWidth().padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val textStyle =
                    if ((localSelectedAudioTrack?.index == item.index)
                        && (localSelectedAudioTrack?.audioTrackGroupIndex == item.audioTrackGroupIndex)
                    )
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Green
                        )
                    else TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.language,
                    style = textStyle.copy(textAlign = TextAlign.Start),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }
        }
    }
}