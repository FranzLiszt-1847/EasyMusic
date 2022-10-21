package com.franz.easymusicplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import com.franz.easymusicplayer.bean.AccountInfo
import com.franz.easymusicplayer.bean.AccountInfoBean
import com.franz.easymusicplayer.bean.DownloadBean

object DownloadViewModel {
    private val download: MutableLiveData<MutableList<DownloadBean>> = MutableLiveData<MutableList<DownloadBean>>()

    fun getDownload(): MutableLiveData<MutableList<DownloadBean>> {
        return download
    }

    /**
     * main thread*/
    fun setDownload(bean: MutableList<DownloadBean>){
        download.value = bean
    }

    /**
     * sub thread*/
    fun postDownload(bean: MutableList<DownloadBean>){
        download.postValue(bean)
    }
}