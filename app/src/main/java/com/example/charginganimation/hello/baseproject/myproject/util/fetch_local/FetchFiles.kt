package com.example.charginganimation.hello.baseproject.myproject.util.fetch_local

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.charginganimation.hello.baseproject.myproject.model.FileDirectoryModel
import com.example.charginganimation.hello.baseproject.myproject.util.konstant.Konstant.cacheMusicDirArrayList
import com.example.charginganimation.hello.baseproject.myproject.util.konstant.Konstant.cacheVideoDirArrayList
import com.example.charginganimation.hello.baseproject.myproject.util.konstant.Konstant.cacheVideoInfoList
import com.videoplayer.video.player.music.hd.allformats.ui.player.model.AudioFile
import com.videoplayer.video.player.music.hd.allformats.ui.player.model.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

object FetchFiles {


    suspend fun getVideoDirectories(context: Context?): ArrayList<FileDirectoryModel> =
        withContext(Dispatchers.IO) {
            val contentResolver: ContentResolver = context!!.contentResolver
            val projection = arrayOf(
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA
            )
            val sortOrder = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC"

            val cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder
            )

            val directories = HashMap<String, FileDirectoryModel>()

            cursor?.use { cursor ->
                val nameIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex)
                    val path = cursor.getString(pathIndex)
                    val directoryPath = File(path).parent

                    if (directoryPath != null) {
                        val file = File(path)
                        val fileSize = file.length()

                        if (directories.containsKey(directoryPath)) {
                            val directory = directories[directoryPath]!!
                            directory.fileCount++
                            directory.folderSize += fileSize
                        } else {
                            val directory = FileDirectoryModel(
                                name = name,
                                path = directoryPath,
                                fileCount = 1,
                                folderSize = fileSize
                            )
                            directories[directoryPath] = directory
                        }
                    }
                }
            }
            cacheVideoDirArrayList = ArrayList(directories.values)
            return@withContext ArrayList(directories.values)
        }


    private fun getDirectorySize(directoryPath: String): Long {
        var totalSize: Long = 0
        val directory = File(directoryPath)

        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()

            files?.forEach { file ->
                if (file.isFile) {
                    totalSize += file.length()
                }
            }
        }

        return totalSize
    }


    suspend fun getAllAudioSongs(context: Context?): ArrayList<AudioFile> =
        withContext(Dispatchers.IO) {
            val audioSongs = ArrayList<AudioFile>()
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            val songLibraryUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val cursor = context!!.contentResolver.query(
                songLibraryUri, projection, selection, null, sortOrder
            )

            cursor?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val album = cursor.getString(albumColumn)
                    val artist = cursor.getString(artistColumn)
                    val duration = cursor.getLong(durationColumn)
                    val contentUri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val audioSong = AudioFile(id, title, contentUri, album, artist, duration)
                    audioSongs.add(audioSong)
                }
            }
            cacheMusicDirArrayList = audioSongs
            return@withContext audioSongs
        }


    fun getAllVideosFromDirectory(context: Context, directoryPath: String): ArrayList<VideoInfo> {
        val videos = ArrayList<VideoInfo>()
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Video.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("$directoryPath%")
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)




            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val path = it.getString(pathColumn)
                val size = it.getLong(sizeColumn)
                val dateAddedSeconds = it.getLong(dateAddedColumn)
                val dateAdded = Date(dateAddedSeconds * 1000)

                val thumbnailUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )

                videos.add(
                    VideoInfo(
                        id, name, path, thumbnailUri, size, dateAdded
                    )
                )
            }
        }

        cacheVideoInfoList = videos
        return videos
    }


}