package me.wataame.player.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import me.wataame.player.domain.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MetadataExtractor(private val context: Context) {
    suspend fun extract(file: File): AudioTrack = withContext(Dispatchers.IO) {
        val uri = Uri.fromFile(file)
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            AudioTrack(
                uri = uri,
                absolutePath = file.absolutePath,
                parentPath = file.parentFile?.absolutePath.orEmpty(),
                fileName = file.name,
                title = title,
                artist = artist,
                durationMs = duration,
                artwork = retriever.embeddedPicture,
            )
        } finally {
            retriever.release()
        }
    }
}
