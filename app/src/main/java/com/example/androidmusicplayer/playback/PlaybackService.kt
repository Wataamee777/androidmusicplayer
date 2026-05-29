package com.example.androidmusicplayer.playback

import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(SEEK_STEP_MS)
            .setSeekForwardIncrementMs(SEEK_STEP_MS)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .build()

        AudioSessionRepository.update(player.audioSessionId)

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(SessionCallback())
            .setCustomLayout(notificationButtons())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun notificationButtons(): List<CommandButton> = listOf(
        CommandButton.Builder(CommandButton.ICON_REWIND)
            .setDisplayName("10秒戻る")
            .setSessionCommand(SessionCommand(ACTION_SEEK_BACK_10, Bundle.EMPTY))
            .build(),
        CommandButton.Builder(CommandButton.ICON_FAST_FORWARD)
            .setDisplayName("10秒送り")
            .setSessionCommand(SessionCommand(ACTION_SEEK_FORWARD_10, Bundle.EMPTY))
            .build(),
    )

    private inner class SessionCallback : MediaSession.Callback {
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_SEEK_BACK_10 -> player.seekTo((player.currentPosition - SEEK_STEP_MS).coerceAtLeast(0L))
                ACTION_SEEK_FORWARD_10 -> player.seekTo((player.currentPosition + SEEK_STEP_MS).coerceAtMost(player.duration.coerceAtLeast(0L)))
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    companion object {
        const val ACTION_SEEK_BACK_10 = "com.example.androidmusicplayer.SEEK_BACK_10"
        const val ACTION_SEEK_FORWARD_10 = "com.example.androidmusicplayer.SEEK_FORWARD_10"
        private const val SEEK_STEP_MS = 10_000L
    }
}
