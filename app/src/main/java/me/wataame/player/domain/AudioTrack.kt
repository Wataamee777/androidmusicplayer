package me.wataame.player.domain

import android.net.Uri

data class AudioTrack(
    val uri: Uri,
    val absolutePath: String,
    val parentPath: String,
    val fileName: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val artwork: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean = other is AudioTrack && uri == other.uri
    override fun hashCode(): Int = uri.hashCode()
}
