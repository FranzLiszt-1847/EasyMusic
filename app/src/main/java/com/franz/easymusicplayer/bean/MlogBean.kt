package com.franz.easymusicplayer.bean

data class MlogBean(
    var mlogId: String,
    var text: String,
    var cover: String,
    var duration: Long,
    var likedCount: Long,
    var commentCount: Long,
    var playCount: Long,
    var shareCount: Long,
    var singerName: String,
    var songName: String,
    var pubTime: Long,
    var nickname: String,
    var liked: Boolean,
)
