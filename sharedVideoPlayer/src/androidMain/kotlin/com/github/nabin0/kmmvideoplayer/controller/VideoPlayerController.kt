package com.github.nabin0.kmmvideoplayer.controller

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsTrackMetadataEntry
import com.github.nabin0.kmmvideoplayer.data.AudioTrack
import com.github.nabin0.kmmvideoplayer.data.ClosedCaptionForTrackSelector
import com.github.nabin0.kmmvideoplayer.data.VideoItem
import com.github.nabin0.kmmvideoplayer.data.VideoQuality
import kotlinx.coroutines.flow.MutableStateFlow

actual class VideoPlayerController {

    private var exoPlayer: ExoPlayer? = null
    private var currentSelectedVideoQuality = VideoQuality(-1, "Auto", -1)
    private var currentSelectedCC = ClosedCaptionForTrackSelector(-1, "Off", name = "Off")
    private var currentSelectedAudioTrack: AudioTrack? = null

    private var audioTrackGroup: Any? = null
    private var videoTrackGroup: Any? = null
    private var textTrackGroup: Any? = null
    private var trackGroupsList: List<Tracks.Group>? = null


    actual val listOfVideoResolutions: MutableStateFlow<List<VideoQuality>?> =
        MutableStateFlow(null)
    actual val listOfAudioFormats: MutableStateFlow<List<AudioTrack>?> = MutableStateFlow(null)
    actual val listOfCC: MutableStateFlow<List<ClosedCaptionForTrackSelector>?> =
        MutableStateFlow(null)

    actual val mediaDuration: MutableStateFlow<Long> = MutableStateFlow(0L)

    actual val isPlaying: MutableStateFlow<Boolean> =
        MutableStateFlow(exoPlayer?.isPlaying ?: false)

    actual val isBuffering: MutableStateFlow<Boolean> =
        MutableStateFlow(true)

    actual fun currentPosition(): Long = exoPlayer?.currentPosition ?: 0L

    actual fun getCurrentPlaybackSpeed(): Float = currentPlaybackSpeed

    @Composable
    actual fun BuildPlayer(onPlayerCreated: (player: Any) -> Unit) {
        val context = LocalContext.current
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            this.addListener(
                object : Player.Listener {

                    @OptIn(UnstableApi::class)
                    override fun onTracksChanged(tracks: Tracks) {
                        super.onTracksChanged(tracks)
                        listOfVideoResolutions.value = null
                        listOfCC.value = null
                        listOfAudioFormats.value = null


                        trackGroupsList = tracks.groups

                        getAvailableVideoResolutions(tracks)
                        getAvailableSubtitleTracks(tracks)
                        getAvailableAudioTracks(tracks)

                    }


                    override fun onEvents(
                        player: Player,
                        events: Player.Events,
                    ) {
                        super.onEvents(player, events)
                        // Triggers collector when next media item is auto player after finishing first one
                        mediaDuration.value = player.contentDuration
                    }

                    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        if (playbackState == ExoPlayer.STATE_READY && (mediaDuration.value.toInt() == 0)) {
                            mediaDuration.value = contentDuration
                            isBuffering.value = false
                        }


                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                isBuffering.value = true
                            }

                            Player.STATE_READY -> {
                                isBuffering.value = false
                            }

                            Player.STATE_IDLE -> {
                                isBuffering.value = false
                                exoPlayer?.prepare()
                            }

                            Player.STATE_ENDED -> {

                            }
                        }

                    }

                    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                        super.onTimelineChanged(timeline, reason)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        this@VideoPlayerController.isPlaying.value = isPlaying
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        super.onMediaItemTransition(mediaItem, reason)
                        currentSelectedVideoQuality = VideoQuality(-1, "Auto", -1)
                        currentSelectedCC = ClosedCaptionForTrackSelector(-1, "Off", name = null)
                        currentSelectedAudioTrack = null
                    }
                }
            )
        }
        exoPlayer?.let {
            onPlayerCreated(it)

        }
    }


    private fun getAvailableVideoResolutions(tracks: Tracks) {
        try {
            // GET AVAILABLE VIDEO RESOLUTIONS
            val videoResolutions: MutableList<VideoQuality> = mutableListOf()
            for (trackGroup in tracks.groups) {
                // Group level information.
                val trackType = trackGroup.type
                val trackInGroupIsSelected = trackGroup.isSelected
                val trackInGroupIsSupported = trackGroup.isSupported

                if (trackType == C.TRACK_TYPE_VIDEO) {
                    videoTrackGroup = trackGroup
                    for (i in 0 until trackGroup.length) {
                        val isSelected = trackGroup.isTrackSelected(i)
                        val trackFormat = trackGroup.getTrackFormat(i)
                        val trackIndex = i
                        val trackName = "${trackFormat.height} p"
                        val resolutionKey = trackFormat.height
                        if (trackGroup.length == 1)
                            currentSelectedVideoQuality = VideoQuality(-1, "Auto", -1)
                        videoResolutions.add(
                            VideoQuality(
                                trackIndex,
                                trackName,
                                resolutionKey
                            )
                        )
                    }

                    if (videoResolutions.isNotEmpty()) {
                        videoResolutions.add(0, VideoQuality(-1, "Auto", -1))
                        videoResolutions.distinctBy { it.resolutionKey }
                            ?.sortedByDescending { it.resolutionKey }
                            ?.toMutableList()
                            ?.takeIf { it.isNotEmpty() }
                        listOfVideoResolutions.value = videoResolutions
                    } else {
                        listOfVideoResolutions.value = null
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAvailableSubtitleTracks(tracks: Tracks) {
        try {
            val subtitlesList = mutableStateListOf<ClosedCaptionForTrackSelector>()
            subtitlesList.add(
                ClosedCaptionForTrackSelector(
                    -1,
                    "Off",
                    name = "Off"
                )
            )
            for (trackGroupIndex in 0 until tracks.groups.size) {
                val trackGroup = tracks.groups[trackGroupIndex]
                val trackType = trackGroup.type
                if (trackType == C.TRACK_TYPE_TEXT) {
                    textTrackGroup = trackGroup
                    for (i in 0 until trackGroup.length) {
                        val isSelected = trackGroup.isTrackSelected(i)
                        val trackFormat = trackGroup.getTrackFormat(i)
                        if (isSelected) {
                            currentSelectedCC = ClosedCaptionForTrackSelector(
                                index = trackGroupIndex,
                                language = trackFormat.language.toString(),
                                name = null
                            )
                        }

                        subtitlesList.add(
                            ClosedCaptionForTrackSelector(
                                index = trackGroupIndex,
                                language = trackFormat.language.toString(),
                                name = trackFormat.language.toString()
                            )
                        )
                    }
                }
            }
            subtitlesList.distinctBy {
                it.language
            }
            if (subtitlesList.isEmpty())
                listOfCC.value = null
            else
                listOfCC.value = subtitlesList

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(UnstableApi::class)
    private fun getAvailableAudioTracks(tracks: Tracks) {
        try {
            val audioTracks: MutableList<AudioTrack> = mutableListOf()
            for (trackGroupIndex in 0 until tracks.groups.size) {
                val trackGroup = tracks.groups[trackGroupIndex]
                val trackType = trackGroup.type
                val trackInGroupIsSelected = trackGroup.isSelected
                val trackInGroupIsSupported = trackGroup.isSupported
                if (trackType == C.TRACK_TYPE_AUDIO) {
                    audioTrackGroup = trackGroup
                    for (i in 0 until trackGroup.length) {
                        val isSelected = trackGroup.isTrackSelected(i)
                        val trackFormat = trackGroup.getTrackFormat(i)
                        val trackMetadata = trackFormat.metadata?.get(0) as HlsTrackMetadataEntry
                        val isSurround = trackMetadata.groupId == "surround"
                        val isStereo = trackMetadata.groupId == "stereo"

                        trackFormat.language?.let {
                            if (isSelected) {
                                currentSelectedAudioTrack = AudioTrack(
                                    index = trackGroupIndex,
                                    language = "${trackMetadata.name} ${trackMetadata.groupId}-$it",
                                    name = trackFormat.language,
                                    isStereo = isStereo,
                                    isSurround = isSurround,
                                    audioTrackGroupIndex = i
                                )
                            }

                            audioTracks.add(
                                AudioTrack(
                                    index = trackGroupIndex,
                                    language = "${trackMetadata.name} ${trackMetadata.groupId}-$it",
                                    name = trackFormat.language,
                                    isStereo = isStereo,
                                    isSurround = isSurround,
                                    audioTrackGroupIndex = i
                                )
                            )
                        }
                    }
                }
            }

            listOfAudioFormats.value = audioTracks

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    actual fun prepare() {
        exoPlayer?.prepare()
    }

    actual fun play() {
        exoPlayer?.play()
    }

    actual fun pause() {
        exoPlayer?.pause()
    }

    actual fun playWhenReady(boolean: Boolean) {
        exoPlayer?.playWhenReady = boolean
    }

    actual fun setMediaItem(
        videoItem: VideoItem,
    ) {
        exoPlayer?.setMediaItem(
            buildMediaItem(videoItem)
        )
    }

    private fun buildMediaItem(
        videoItem: VideoItem,
    ): MediaItem {
        val mediaItemBuilder = MediaItem.Builder()
            .setUri(videoItem.videoUrl)
            .setMediaId(videoItem.videoUrl.toString())
            .setTag(videoItem.title.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setDisplayTitle(videoItem.title)
                    .build()
            )
        var mediaItem: MediaItem
        try {
            when (Util.inferContentType(Uri.parse(videoItem.videoUrl))) {
                C.CONTENT_TYPE_SS -> mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_SS)
                C.CONTENT_TYPE_DASH -> {
                    mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MPD)
                    videoItem.licenseUrl?.let {
                        mediaItemBuilder.setDrmConfiguration(
                            DrmConfiguration.Builder(C.WIDEVINE_UUID)
                                .setLicenseUri(videoItem.licenseUrl).build()
                        )
                    }
                }

                C.CONTENT_TYPE_HLS -> mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
                C.CONTENT_TYPE_OTHER -> mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MP4)
                else -> {}
            }

            // if (isDRMEnabled) {
            val drmUuid = C.WIDEVINE_UUID
            val requestHeaders = mapOf("X-AxDRM-Message" to videoItem.licenseToken.toString())
            val drmConfiguration = DrmConfiguration.Builder(drmUuid)
                .setLicenseRequestHeaders(requestHeaders)
                .setLicenseUri(videoItem.licenseUrl)
                .build()
            mediaItemBuilder.setDrmConfiguration(drmConfiguration)
            // }

            mediaItemBuilder.setDrmConfiguration(drmConfiguration)
            mediaItem = if (videoItem.listOfClosedCaptions != null) {
                val listOfSubtitleConfiguration = mutableListOf<MediaItem.SubtitleConfiguration>()

                for (cc in videoItem.listOfClosedCaptions) {
                    listOfSubtitleConfiguration.add(
                        MediaItem.SubtitleConfiguration.Builder(Uri.parse(cc.subtitleLink))
                            .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                            .setLanguage(cc.language)
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build()
                    )
                }
                mediaItemBuilder.setSubtitleConfigurations(listOfSubtitleConfiguration).build()
            } else {
                mediaItemBuilder.build()
            }
            return mediaItem
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaItem = mediaItemBuilder.build()
        return mediaItem

    }

    @Composable
    actual fun PlayerView(useDefaultController: Boolean, modifier: Modifier) {
        AndroidView(modifier = modifier, factory = {
            androidx.media3.ui.PlayerView(it)
        }) {
            it.player = exoPlayer
            it.useController = useDefaultController
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT

            )
        }
    }


    actual fun seekTo(millis: Long) {
        exoPlayer?.seekTo(millis)
    }

    @Composable
    actual fun EnableLandscapeScreenMode() {
        val activity = LocalContext.current as Activity
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    @Composable
    actual fun EnablePortraitScreenMode() {
        val activity = LocalContext.current as Activity
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    }

    actual fun releasePlayer() {
        stop()
        exoPlayer?.release()
    }

    actual fun stop() {
        exoPlayer?.stop()
    }

    @Composable
    actual fun HandleActivityLifecycleStageChanges() {
        DisposableEffectWithLifeCycle(
            onResume = { play() },
            onPause = { pause() },
            onDispose = { releasePlayer() })
    }

    actual fun addPlayList(listOfVideos: List<VideoItem>) {
        val listOfMediaItems = mutableListOf<MediaItem>()

        for (videoItem in listOfVideos) {
            listOfMediaItems.add(buildMediaItem(videoItem = videoItem))
        }

        if (listOfMediaItems.isNotEmpty())
            exoPlayer?.addMediaItems(listOfMediaItems)
    }

    actual fun setPlayList(listOfVideos: List<VideoItem>) {
        val listOfMediaItems = mutableListOf<MediaItem>()

        for (videoItem in listOfVideos) {
            listOfMediaItems.add(buildMediaItem(videoItem = videoItem))
        }

        if (listOfMediaItems.isNotEmpty())
            exoPlayer?.setMediaItems(listOfMediaItems)
    }

    actual fun playNextFromPlaylist() {
        if (exoPlayer?.hasNextMediaItem() == true) {
            mediaDuration.value = 0
            exoPlayer?.seekToNextMediaItem()
        }
    }

    actual fun playPreviousFromPlaylist() {
        if (exoPlayer?.hasPreviousMediaItem() == true) {
            mediaDuration.value = 0
            exoPlayer?.seekToPreviousMediaItem()
        }
    }

    private var currentPlaybackSpeed = 1f
    actual fun setPlaybackSpeed(selectedPlaybackSpeed: Float) {
        currentPlaybackSpeed = selectedPlaybackSpeed
        exoPlayer?.setPlaybackSpeed(selectedPlaybackSpeed)
    }

    actual fun setSpecificVideoQuality(videoQuality: VideoQuality) {
        try {
            currentSelectedVideoQuality = videoQuality
            exoPlayer?.let {
                if (videoQuality.index == -1) {
                    it.trackSelectionParameters = it.trackSelectionParameters.buildUpon()
                        .clearOverride((videoTrackGroup as Tracks.Group).mediaTrackGroup).build()
                    return
                }
                it.trackSelectionParameters =
                    it.trackSelectionParameters
                        .buildUpon()
                        .setOverrideForType(
                            TrackSelectionOverride(
                                (videoTrackGroup as Tracks.Group).mediaTrackGroup,
                                videoQuality.index
                            )
                        ).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    actual fun getCurrentVideoStreamingQuality(): VideoQuality {
        return currentSelectedVideoQuality
    }

    @OptIn(UnstableApi::class)
    actual fun setSpecificCC(cc: ClosedCaptionForTrackSelector) {
        try {
            currentSelectedCC = cc
            if (cc.index == -1) {
                setCCEnabled(false)
                return
            }
            val trackGroupTemp = trackGroupsList?.get(cc.index) ?: textTrackGroup
            exoPlayer?.let {
                it.trackSelectionParameters = it.trackSelectionParameters.buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false).build()
                it.trackSelectionParameters =
                    it.trackSelectionParameters
                        .buildUpon()
                        .setOverrideForType(
                            TrackSelectionOverride(
                                (trackGroupTemp as Tracks.Group).mediaTrackGroup,
                                0
                            )
                        ).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun getCurrentCC(): ClosedCaptionForTrackSelector = currentSelectedCC

    actual fun setCCEnabled(enabled: Boolean) {
        exoPlayer?.let {
            it.trackSelectionParameters = it.trackSelectionParameters.buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !enabled).build()
        }
    }

    actual fun setVolumeLevel(volumeLevel: Float) {
        exoPlayer?.volume = volumeLevel
    }

    actual fun setSpecificAudioTrack(audioTrack: AudioTrack) {
        if (audioTrack.index < 0) return
        val trackGroupTemp = trackGroupsList?.get(audioTrack.index) ?: textTrackGroup

        try {
            exoPlayer?.let {
                it.trackSelectionParameters =
                    it.trackSelectionParameters
                        .buildUpon()
                        .setOverrideForType(
                            TrackSelectionOverride(
                                (trackGroupTemp as Tracks.Group).mediaTrackGroup,
                                audioTrack.audioTrackGroupIndex
                            )
                        ).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    actual fun getCurrentSelectedAudioTrack(): AudioTrack? {
        return currentSelectedAudioTrack
    }

}