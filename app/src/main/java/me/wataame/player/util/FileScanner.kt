package me.wataame.player.util

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import me.wataame.player.domain.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private val supportedAudioExtensions = setOf("mp3", "m4a", "aac", "flac", "wav", "ogg", "opus", "wma", "mid", "midi", "afc", "aiff", "aif", "aifc")

class FileScanner(context: Context) {
    private val extractor = MetadataExtractor(context.applicationContext)

    suspend fun scanFolder(folderPath: String): List<AudioTrack> = withContext(Dispatchers.IO) {
        val root = File(folderPath).canonicalFile
        require(root.isDirectory) { "Not a directory: $folderPath" }
        collectAudioFilesDepthFirst(root).let { files ->
            coroutineScope { files.map { async { extractor.extract(it) } }.awaitAll() }
        }
    }

    suspend fun buildQueueFromSelectedTrack(selectedPath: String): List<MediaItem> {
        val selected = File(selectedPath).canonicalFile
        val selectedFolder = selected.parentFile?.canonicalFile ?: error("No parent folder: $selectedPath")
        val tracks = scanFolder(selectedFolder.absolutePath)
        val selectedIndex = tracks.indexOfFirst { File(it.absolutePath).canonicalFile == selected }
        val ordered = if (selectedIndex <= 0) tracks else tracks.drop(selectedIndex) + tracks.take(selectedIndex)
        return ordered.map { it.toMediaItem() }
    }

    private fun collectAudioFilesDepthFirst(root: File): List<File> {
        val filesHere = root.listFiles().orEmpty()
            .filter { it.isFile && it.extension.lowercase(Locale.ROOT) in supportedAudioExtensions }
            .sortedBy { it.name.lowercase(Locale.ROOT) }
        val childTracks = root.listFiles().orEmpty()
            .filter { it.isDirectory && !it.isHidden }
            .sortedBy { it.name.lowercase(Locale.ROOT) }
            .flatMap { collectAudioFilesDepthFirst(it) }
        return filesHere + childTracks
    }
}

fun AudioTrack.toMediaItem(): MediaItem = MediaItem.Builder()
    .setUri(uri)
    .setMediaId(absolutePath)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkData(artwork, 3) // ★数値の「3」に書き換えます
            .build(),
    )
    .build()
