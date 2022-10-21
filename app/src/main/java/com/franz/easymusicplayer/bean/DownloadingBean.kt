package com.franz.easymusicplayer.bean

data class DownloadingBean(
    val status:Byte,//下载状态
    val speed:String,//当前下载速度
    val percentage:Int,//当前下载百分比
    val error:String,//下载错误信息
    val bean: DownloadBean?,
)
