package com.franz.easymusicplayer.bean

import com.google.gson.JsonObject


data class SongRecentBean(
    val banned: Boolean,
    val data: JsonObject,
    val playTime: Long,
    val resourceId: String,
    val resourceType: String
)