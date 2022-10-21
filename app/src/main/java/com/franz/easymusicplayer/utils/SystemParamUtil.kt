package com.franz.easymusicplayer.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Point
import com.franz.easymusicplayer.base.BaseApplication


object SystemParamUtil {
    var width: Int = 0
    var height: Int = 0

    fun getSystemParam(activity: Activity){
        val manager = activity.window.windowManager
        val point = Point()
        manager.defaultDisplay.getRealSize(point)
        this.width = point.x
        this.height = point.y
    }

}