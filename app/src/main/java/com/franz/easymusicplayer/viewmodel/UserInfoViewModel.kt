package com.franz.easymusicplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import com.franz.easymusicplayer.bean.AccountInfo
import com.franz.easymusicplayer.bean.AccountInfoBean

object UserInfoViewModel {
    private val user: MutableLiveData<AccountInfoBean> = MutableLiveData<AccountInfoBean>()

    fun getUserInfo(): MutableLiveData<AccountInfoBean> {
        return user
    }

    /**
     * main thread*/
    fun setValue(userInfo: AccountInfoBean){
        user.value = userInfo
    }

    /**
     * sub thread*/
    fun postValue(userInfo: AccountInfoBean){
        user.postValue(userInfo)
    }
}