package com.franz.easymusicplayer.callback

interface IVerifyCodeInfo {
    fun onRespond(flag: Boolean)
    fun onFailed(e: String?)
}