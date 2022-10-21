package com.franz.easymusicplayer.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater

import android.widget.LinearLayout
import com.franz.easymusicplayer.R
import java.util.jar.Attributes

class MusicPlayerBar(context: Context,attributes: AttributeSet) : LinearLayout(context,attributes) {
    init {
        LayoutInflater.from(context).inflate(R.layout.music_play_layout,this)
    }
}


