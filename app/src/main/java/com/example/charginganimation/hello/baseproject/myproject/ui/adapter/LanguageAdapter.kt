package com.example.charginganimation.hello.baseproject.myproject.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.charginganimation.hello.baseproject.myproject.R
import com.example.charginganimation.hello.baseproject.myproject.util.listener.OnLanguageSelected


class LanguageAdapter(
    private val ctx: Context,
    private val languageArray: ArrayList<*>,
    private val onLanguageSelected: OnLanguageSelected
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        Glide.with(ctx).load(languageArray[position]).into(holder.ivFlag)

        holder.tvFlag.text = (languageArray[position].toString())


        holder.itemView.setOnClickListener {
            onLanguageSelected.onLanguageSelected(position)
        }

    }

    override fun getItemCount(): Int {
        return languageArray.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivFlag: ImageView = itemView.findViewById(R.id.ivFlag)
        var tvFlag: TextView = itemView.findViewById(R.id.tvFlag)
    }
}