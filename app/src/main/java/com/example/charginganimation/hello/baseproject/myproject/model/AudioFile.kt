package com.videoplayer.video.player.music.hd.allformats.ui.player.model

import android.net.Uri

data class AudioFile(
    val id: Long,
    val title: String,
    val uri: Uri,
    val album: String,
    val artist: String,
    val duration: Long



) {
    val formattedDuration: String
        get() = formatDuration(duration)

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
