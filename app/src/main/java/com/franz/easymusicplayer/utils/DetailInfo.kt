package com.franz.easymusicplayer.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.DetailInfoBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.callback.IDetailInfo
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.ui.HomePageActivity
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DetailInfo {
    /**
     * 根据歌曲id获取歌曲详情*/
     fun getSongDetail(songId: String,callback: IDetailInfo){
        val url = HttpUtil.getSongDetailURL(songId)

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                updateSongDetail(json.toString(),callback)
            }

            override fun onFailed(e: String?) {
                callback.onFailed(e.toString())
            }
        })
    }

    private fun updateSongDetail(json: String,callback: IDetailInfo){
        val array = JSONObject(json.toString()).getJSONArray("songs")
        if (array.length() == 0){
            callback.onFailed("歌单解析错误!")
            return
        }
        val firstInfo = JSONObject(array[0].toString())
        val single = firstInfo.getInt("single")//0:有专辑信息或者是DJ节目,1: 未知专辑
        val djId = firstInfo.get("djId")//0:不是DJ节目,其他：是DJ节目，表示DJ ID
        if (single != 0 || djId != 0) {
            callback.onFailed("该歌曲没有专辑信息!")
            return
        }
        val songName = firstInfo.getString("name")//歌曲名字
        val singer = JSONObject(firstInfo.getJSONArray("ar")[0].toString()).getString("name")//歌手名称
        val album = firstInfo.getJSONObject("al").getString("name").toString()//专辑名称
        val cover = firstInfo.getJSONObject("al").getString("picUrl").toString()//专辑封面
        val publishTime = firstInfo.getLong("publishTime").toLong()//发行时间
        val time = millToDate(publishTime)
        /**
         *
        0: 免费或无版权
        1: VIP 歌曲
        4: 购买专辑
        8: 非会员可免费播放低音质，会员可播放高音质及下载
        fee 为 1 或 8 的歌曲均可单独购买 2 元单曲*/
        val fee = JSONObject(JSONObject(json.toString()).getJSONArray("privileges")[0].toString()).getInt("fee").toInt()

        val type = when(fee){
            0-> "免费"
            1-> "VIP歌曲"
            4-> "需购买"
            8-> "限免"
            else-> "免费"
        }

        val bean: DetailInfoBean = DetailInfoBean(songName,singer,album,cover,time,type)

        callback.onRespond(bean)
    }

    private fun millToDate(time: Long): String{
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }
}