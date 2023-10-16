package com.videoplayer.video.player.music.hd.allformats.ui.player.model

import android.net.Uri
import java.util.Date

data class VideoInfo(
    val id: Long,
    val name: String,
    val path: String,
    val thumbUri: Uri?,
    val size: Long,
    val dateAdded: Date
) {
    val formattedDuration: String
        get() = formatDuration(size)

    private fun formatDuration(duration: Long): String {
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = duration / (1000 * 60 * 60)

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            else -> String.format("00:%02d", seconds)
        }
    }
}

