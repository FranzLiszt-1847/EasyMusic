package com.franz.easymusicplayer.callback

import com.franz.easymusicplayer.bean.DetailInfoBean


interface IDetailInfo {
    fun onRespond(bean: DetailInfoBean)
    fun onFailed(e: String)
}