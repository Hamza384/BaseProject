package com.example.charginganimation.hello.baseproject.myproject.util.fetch_local

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import com.example.charginganimation.hello.baseproject.myproject.R
import com.videoplayer.video.player.music.hd.allformats.R


object LoadingScreen {
    var dialog: Dialog? = null
    fun displayLoadingWithText(
        context: Context?, text: String?, cancelable: Boolean
    ) {
        dialog = Dialog(context!!)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(R.layout.layout_loading_screen)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(cancelable)
        val textView = dialog!!.findViewById<TextView>(R.id.text)
        textView.text = text
        try {
            dialog!!.show()
        } catch (e: Exception) {
            e.message
        }
    }

    fun hideLoading() {
        try {
            if (dialog != null) {
                dialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.message
        }
    }
}