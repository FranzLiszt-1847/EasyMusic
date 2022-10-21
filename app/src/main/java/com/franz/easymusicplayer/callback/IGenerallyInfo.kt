package com.franz.easymusicplayer.callback

interface IGenerallyInfo {
    fun onRespond(json: String?)
    fun onFailed(e: String?)
}