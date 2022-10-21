package com.franz.easymusicplayer.param

class CacheKeyParam {
    companion object{
        @JvmStatic
        val cookieKey: String = "CookieKey"//用于存储登录返回的cookie
        @JvmStatic
        val songListId: String = "songListId"//用于Intent之间传输歌单id
        @JvmStatic
        val songListName: String = "songListName"//用于Intent之间传输歌单名称
        @JvmStatic
        val songListCover: String = "songListCover"//用于Intent之间传输歌单封面
        @JvmStatic
        val UserId: String = "UserId"//用于Intent之间传输用户id
        @JvmStatic
        val QRCode: String = "QRCode"//用于二维码登录时，传输标志位
        @JvmStatic
        val searchKey: String = "searchKey"//用于传输搜索关键词
        @JvmStatic
        val albumSongs: String = "albumSongs"//用于传输专辑id
        @JvmStatic
        val mvId: String = "mvId"//用于视频id
        @JvmStatic
        val mvName: String = "mvName"//用于mv名称
        @JvmStatic
        val recordId: String = "recordId"//用于存储当前播放的歌曲信息，下次重新进来时，可展现上次播放记录
        @JvmStatic
        val recordBean: String = "recordBean"//用于mv名称
        @JvmStatic
        val mlogId: String = "mlogId"//用于mlog传输
        @JvmStatic
        val mlogName: String = "mlogName"//用于mlog名称
    }
}