package com.franz.easymusicplayer.bean

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class MvRecentBean(
    val artists: JsonArray,
    val creator: JsonObject,
    val coverUrl: String,
    val duration: Long,
    val id: String,
    val name: String,
    val title: String
)
