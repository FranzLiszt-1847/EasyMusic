package com.franz.easymusicplayer.utils

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.callback.IGenerallyInfo

object ToastUtil {
    private val layoutList = listOf<Int>(R.layout.toast__success_tips,R.layout.toast_fail_tips)
    private val idList = listOf<Int>(R.id.SuccessTips,R.id.FailTips)
    private var toast: Toast? = null

    private fun initLayout(context: Context,layout: Int,id: Int,tips: String){
        toast = Toast(context)
        val view: View = LayoutInflater.from(context).inflate(layout,null)
        val textView = view.findViewById<TextView>(id)
        textView.text = tips
        toast?.view = view
        toast!!.duration = Toast.LENGTH_SHORT
        toast?.setGravity(Gravity.TOP,0,0)
    }

    /**
     * 成功toast样式*/
    fun setSuccessToast(context: Activity,tips: String){
        context.runOnUiThread {
            initLayout(context, layoutList[0], idList[0],tips)
            toast?.show()
        }
    }

    /**
     * 失败toast样式*/
    fun setFailToast(context: Activity, tips: String){
        context.runOnUiThread{
            initLayout(context, layoutList[1], idList[1],tips)
            toast?.show()
        }
    }
}