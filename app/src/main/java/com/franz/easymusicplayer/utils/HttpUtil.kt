package com.franz.easymusicplayer.utils

import android.util.Log
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.callback.IVerifyCodeInfo
import com.franz.easymusicplayer.callback.MusicInfo
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.param.MusicParam
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

object HttpUtil {

    private val TAG: String = "HttpUtilLog"

    fun getMusicInfo(url: String, callback: MusicInfo) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                callback.onRespond(response.body()?.string())
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onFailed(e)
            }
        })
    }

    /**
     * 登录接口
     * 可使用手机验证码登录和密码登录*/
    fun getLoginInfo(url: String, callback: IGenerallyInfo) {
        thread {
            val build: Headers.Builder = Headers.Builder()
            build.add("cookie","__remember_me=true; NMTID=sb")
            val headers = build.build()
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .headers(headers)
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val json = response.body()?.string()
                    val jsonObject = JSONObject(json.toString())
                    if (!jsonObject.has("code")){
                        callback.onFailed("接口异常，请重试!")
                        return
                    }
                    val code = jsonObject.getInt("code")

                    Log.d(TAG, "登陆数据:${json.toString()}")

                    if (code == 200){
                        callback.onRespond(json.toString())
                    }else{
                        val message = jsonObject.getString("message")
                        callback.onFailed(message)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailed(e.message)
                }
            })
        }
    }

    /**
     * 获取验证码和校验验证码*/
    fun getVerifyCodeInfo(url: String, callback: IVerifyCodeInfo) {
        thread {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val json = response.body()?.string()
                    val jsonObject = JSONObject(json.toString())
                    val code = jsonObject.getInt("code")

                    Log.d(TAG, "验证码数据:${json.toString()}")

                    if (code == 200) {
                        val result = jsonObject.getBoolean("data")
                        callback.onRespond(result)
                    }else{
                        val message = jsonObject.getString("message")
                        callback.onFailed(message)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailed(e.message)
                }
            })
        }
    }

    /**
     * 获取二维码key*/
    fun getQRCodeKeyInfo(url: String, callback: IGenerallyInfo) {
        thread {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val json = response.body()?.string()
                    val jsonObject = JSONObject(json.toString())
                    if (!jsonObject.has("code")){
                        callback.onFailed("接口异常，请重试!")
                        return
                    }

                    val code = jsonObject.getInt("code")

                    Log.d(TAG, "二维码key数据:${json.toString()}")

                    if (code == 200) {
                        val result = jsonObject.getJSONObject("data")
                        val inCode = result.getInt("code")
                        if (inCode == 200){
                            val key = result.getString("unikey")
                            callback.onRespond(key)
                        }else{
                            val message = jsonObject.getString("message")
                            callback.onFailed(message)
                        }
                    }else{
                        val message = jsonObject.getString("message")
                        callback.onFailed(message)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailed(e.message)
                }
            })
        }
    }

    /**
     * 获取二维码图片*/
    fun getQRCodeCreateInfo(url: String, callback: IGenerallyInfo) {
        thread {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val json = response.body()?.string()
                    val jsonObject = JSONObject(json.toString())
                    val code = jsonObject.getInt("code")

                    Log.d(TAG, "二维码Create数据:${json.toString()}")

                    if (code == 200) {
                        val result = jsonObject.getJSONObject("data")
                        val qrImg = result.getString("qrimg")
                        callback.onRespond(qrImg)
                    }else{
                        val message = jsonObject.getString("message")
                        callback.onFailed(message)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailed(e.message)
                }
            })
        }
    }

    /**
     * 获取二维码被扫的状态*/
    fun getQRCodeStatusInfo(url: String, callback: IGenerallyInfo) {
        thread {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val json = response.body()?.string()
                    val jsonObject = JSONObject(json.toString())
                    val code = jsonObject.getInt("code")

                    Log.d(TAG, "二维码Status数据:${json.toString()}")

                    when(code){
                        800->callback.onFailed("二维码过期")
                        801->callback.onFailed("等待扫码")
                        802->callback.onFailed("待确认")
                        803->{
                            val cookie = jsonObject.getString("cookie")
                            callback.onRespond(cookie)
                        }
                        else->callback.onFailed("异常授权信息")
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailed(e.message)
                }
            })
        }
    }

    /**
     * 通用接口*/
    fun getGenerallyInfo(url: String, callback: IGenerallyInfo) {
        thread {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val json = response.body()?.string()
                        val jsonObject = JSONObject(json.toString())
                        val code = jsonObject.getInt("code")

                        Log.d(TAG, "通用接口数据数据:${json.toString()}")

                        if (code == 200){
                            callback.onRespond(json.toString())
                        }else{
                            callback.onFailed("通用接口信息错误")
                        }
                    }catch (e: JSONException){
                        callback.onFailed(e.message)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailed(e.message)
                }
            })
        }
    }

    /**
     * 密码登陆*/
    fun getLoginURL(phone: String, password: String): String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.loginURL)!!.newBuilder()
        httpUrl.addQueryParameter("phone", phone)
        httpUrl.addQueryParameter("password", password)
        return httpUrl.build().toString()
    }

    /**
     * 验证码登陆*/
    fun getVerifyLoginURL(phone: String, captcha: String): String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.loginURL)!!.newBuilder()
        httpUrl.addQueryParameter("phone", phone)
        httpUrl.addQueryParameter("captcha", captcha)
        return httpUrl.build().toString()
    }

    /**
     * 获取验证码*/
    fun getVerifyCodeURL(phone: String): String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.sendIdentify)!!.newBuilder()
        httpUrl.addQueryParameter("phone", phone)
        return httpUrl.build().toString()
    }

    /**
     * 验证验证码结果*/
    fun getVerifyResultURL(phone: String, captcha: String): String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.verifyResult)!!.newBuilder()
        httpUrl.addQueryParameter("phone", phone)
        httpUrl.addQueryParameter("captcha", captcha)
        return httpUrl.build().toString()
    }

    /**
     * 获取用户所有的歌单，包括创建的和收藏*/
    fun getUserAllSongListURL(uid: String): String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.userAllSongList)!!.newBuilder()
        httpUrl.addQueryParameter("uid", uid)
        httpUrl.addQueryParameter("limit", "30")
        httpUrl.addQueryParameter("offset", "0")
        return httpUrl.build().toString()
    }

    /**
     * 获取二维码登录的key*/
    fun getQRCodeKeyURL():String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.qrCodeKey)!!.newBuilder()
        httpUrl.addQueryParameter("timerstamp", System.currentTimeMillis().toString())
        return httpUrl.build().toString()
    }
    /**
     * 获取二维码图片*/
    fun getQRCodeCreateURL(key: String):String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.qrCodeCreate)!!.newBuilder()
        httpUrl.addQueryParameter("key", key)
        httpUrl.addQueryParameter("qrimg", "true")
        httpUrl.addQueryParameter("timerstamp", System.currentTimeMillis().toString())
        return httpUrl.build().toString()
    }

    /**
     * 获取二维码登录的key*/
    fun getQRCodeStatusURL(key: String):String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.qrCodeStatus)!!.newBuilder()
        httpUrl.addQueryParameter("key", key)
        httpUrl.addQueryParameter("timerstamp", System.currentTimeMillis().toString())
        return httpUrl.build().toString()
    }

    /**
     * 获取账户信息*/
    fun getAccountInformationURL(cookie: String):String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.accountInformation)!!.newBuilder()
        httpUrl.addQueryParameter("cookie", cookie)
        return httpUrl.build().toString()
    }
    /**
     * 获取账户子信息*/
    fun getSubAccountInfoURL(cookie: String):String {
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.subcountInformation)!!.newBuilder()
        httpUrl.addQueryParameter("cookie", cookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取歌单内所有歌曲数据*/
    fun getSongListURL(id: String,limit:Int):String {
        val cookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.songListData)!!.newBuilder()
        httpUrl.addQueryParameter("id", id)
        httpUrl.addQueryParameter("cookie", cookie)
        if (limit != 0) httpUrl.addQueryParameter("limit", "$limit")
        return httpUrl.build().toString()
    }

    /**
     * 获取歌曲链接
     * 播放音质等级, 分为 standard => 标准, exhigh=>极高, lossless=>无损, hires=>Hi-Res*/
    fun getSongURL(id: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.songUrl)!!.newBuilder()
        httpUrl.addQueryParameter("id", id)
        httpUrl.addQueryParameter("level", "lossless")
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 调用此接口 , 传入搜索关键词可以搜索该音乐 / 专辑 / 歌手 / 歌单 / 用户 , 关键词可以多个 , 以空格隔开 , 如 " 周杰伦 搁浅 "( 不需要登录 )*/
    fun getSearchResultURL(keywords: String,type: Int,limit: Int):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.searchResult)!!.newBuilder()
        httpUrl.addQueryParameter("keywords", keywords)
        httpUrl.addQueryParameter("type", "$type")
        httpUrl.addQueryParameter("limit", "$limit")
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取推荐歌曲*/
    fun getRecommendMusicURL():String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.recommendMusic)!!.newBuilder()
        httpUrl.addQueryParameter("limit", "${10}")
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }
    /**
     * 获取推荐歌单*/
    fun getRecommendSongsURL():String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.recommendSongs)!!.newBuilder()
        httpUrl.addQueryParameter("limit", "${10}")
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }
    /**
     * 获取推荐MV*/
    fun getRecommendMvURL():String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.recommendMv)!!.newBuilder()
        httpUrl.addQueryParameter("limit", "${10}")
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取专辑*/
    fun getAlbumSongsURL(id: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.albumSongs)!!.newBuilder()
        httpUrl.addQueryParameter("id", id)
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取MV视频地址*/
    fun getMvURL(id: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.mvVideoUrl)!!.newBuilder()
        httpUrl.addQueryParameter("id", id)
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取MV视频相关视频*/
    fun getMvInfoURL(id: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.mvVideoInfo)!!.newBuilder()
        httpUrl.addQueryParameter("mvid", id)
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取最近播放的数据*/
    fun getRecentlyURL(subURL: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + subURL)!!.newBuilder()
        httpUrl.addQueryParameter("limit", "${100}")
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取歌曲歌词*/
    fun getLyricURL(id: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.lyric)!!.newBuilder()
        httpUrl.addQueryParameter("id", id)
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取banner*/
    fun getBannerURL():String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.banner)!!.newBuilder()
        httpUrl.addQueryParameter("type", "${2}")
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取排行榜*/
    fun getTopRankURL():String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.topRank)!!.newBuilder()
        httpUrl.addQueryParameter("limit", "${10}")
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 根据歌曲id获取mv视频id*/
    fun getMvInfoAsIdURL(songID: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.mvURL)!!.newBuilder()
        httpUrl.addQueryParameter("songid", songID)
        httpUrl.addQueryParameter("limit", "${10}")
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 根据歌曲id获取mlog视频id*/
    fun getMLogURL(id: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.mlogURL)!!.newBuilder()
        httpUrl.addQueryParameter("id", id)
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取歌曲详细信息*/
    fun getSongDetailURL(songId: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.songDetailInfo)!!.newBuilder()
        httpUrl.addQueryParameter("ids", songId)
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取用户等级信息*/
    fun getUserLevelURL():String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.userLevel)!!.newBuilder()
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取用户详情信息*/
    fun getUserDetailURL(userID: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.userDetail)!!.newBuilder()
        httpUrl.addQueryParameter("uid", userID)
        httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
     * 获取下载url
     * 默认最大质量音源*/
    fun getDownloadURL(songID: String):String {
        val mCookie: String = SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.cookieKey,"") as String
        val httpUrl = HttpUrl.parse(MusicParam.baseURL + MusicParam.downloadUrl)!!.newBuilder()
        httpUrl.addQueryParameter("id", songID)
        //httpUrl.addQueryParameter("cookie", mCookie)
        return httpUrl.build().toString()
    }

    /**
    * resolve jsonObject*/
    fun <T> fromJson(json: String,c: Class<T>): T{
        val gson =  Gson();
        return  gson.fromJson(json,c);
    }
    /**
     * resolve jsonArray*/
    fun <T> fromListJson(json: String,c: Class<T>): List<T>{
        val list  =  ArrayList<T>();
        val gson =  Gson();
        val array: JsonArray =  JsonParser().parse(json).asJsonArray;
        for (element: JsonElement in array) {
            list.add(gson.fromJson(element,c));
        }
        return list;
    }
}