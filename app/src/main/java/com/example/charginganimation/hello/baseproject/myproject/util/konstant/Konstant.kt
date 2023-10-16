package com.example.charginganimation.hello.baseproject.myproject.util.konstant

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.example.charginganimation.hello.baseproject.myproject.R
import com.example.charginganimation.hello.baseproject.myproject.model.FileDirectoryModel
import com.videoplayer.video.player.music.hd.allformats.ui.player.model.AudioFile
import com.videoplayer.video.player.music.hd.allformats.ui.player.model.VideoInfo

object Konstant {

    var cacheVideoDirArrayList = ArrayList<FileDirectoryModel>()
    var cacheMusicDirArrayList = ArrayList<AudioFile>()
    var cacheVideoInfoList = ArrayList<VideoInfo>()


    fun showToast(context: Context, s: String) {
        val toast = Toast.makeText(context, s, Toast.LENGTH_SHORT)
        toast.setGravity(GravityCompat.END, 0, 0)
        toast.show()
    }

    fun moreApps(context: Context, app_link: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$app_link")
                )
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$app_link")
                )
            )
        }
    }

    fun rate(context: Context, appLink: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("market://details?id=$appLink")
                )
            )
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appLink")
                )
            )
        }
    }


    @Throws(java.lang.Exception::class)
    fun shareApp(context: Context) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.app_name))
        var sAux =
            "Let me recommend you this application${context.resources.getString(R.string.app_name)}".trimIndent()
        sAux = "${sAux}https://play.google.com/store/apps/details?id=${context.packageName}"
        i.putExtra(Intent.EXTRA_TEXT, sAux)
        context.startActivity(Intent.createChooser(i, "choose one"))
    }

}