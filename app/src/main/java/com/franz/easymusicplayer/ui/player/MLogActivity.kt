package com.franz.easymusicplayer.ui.player

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.adapter.MLogAdapter
import com.franz.easymusicplayer.adapter.SongsAdapter
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.MlogBean
import com.franz.easymusicplayer.bean.MvInfoBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.binder.MusicBinder
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.ActivityMlogBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.StatusBarUtil
import com.franz.easymusicplayer.utils.ToastUtil
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMlogBinding
    private lateinit var orientationUtils: OrientationUtils
    private val mlogList: MutableList<MlogBean> = ArrayList()
    private lateinit var adapter: MLogAdapter
    private val TAG: String = "MLogActivityLog"
    private val UPDATEPlayer: Int = 1
    private val UPDATEInfos: Int = 2
    private val UPDATECurrent: Int = 3
    private var currentMlog = 0

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) = when (msg.what) {
            UPDATEPlayer -> {
                startPlay(msg.obj as String)
                binding.mlogLoading.hide()
            }

            UPDATEInfos->{
                updateRecyclerData(msg.obj as String)
            }

            UPDATECurrent->{
                adapter.notifyDataSetChanged()
                getMLogUrl(mlogList[currentMlog].mlogId)
                updateInfo(mlogList[currentMlog])
            }
            else -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarHide(window)
        StatusBarUtil.setStatusBarColor(window, this, R.color.black)
        binding = ActivityMlogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mlogLoading.show()
        turnOffMusic()
        initPlayer()
        getMlogID()
        initRecycler()
    }
    private fun turnOffMusic(){
        if (MusicBinder.isPlay) {
            MusicBinder.pause()
        }
    }
    private fun getMlogID() {
        val intent = intent
        val mlogID = intent.getStringExtra(CacheKeyParam.mlogId).toString()
        if (TextUtils.isEmpty(mlogID)) {
            ToastUtil.setFailToast(this@MLogActivity, "??????id????????????!")
            return
        }

        getMvIdInfo(mlogID)
    }

    private fun initRecycler(){
        binding.relateMlog.layoutManager = LinearLayoutManager(this)
        adapter = MLogAdapter(mlogList)
        binding.relateMlog.adapter = adapter

        adapter.setItemClickListener(object : MLogAdapter.OnMLogClickListener{
            override fun onClickListener(pos: Int, bean: MlogBean) {
                if (currentMlog != pos){
                    binding.mlogLoading.show()
                    currentMlog = pos
                    val message = Message()
                    message.what = UPDATECurrent
                    handler.sendMessage(message)
                }
            }
        })
    }

    private fun initPlayer() {
        orientationUtils = OrientationUtils(this, binding.mLogPlayer)
        val builder = GSYVideoOptionBuilder()
        builder.setUrl(null)
            .setVideoTitle(null) //movie title
            .setIsTouchWiget(false) //????????????????????????
            .setRotateViewAuto(false) //????????????????????????
            .setLockLand(false) //?????????????????????????????????false??????????????????setRotateViewAuto??????
            .setAutoFullWithSize(true) //?????????????????????????????????????????????????????????????????????
            .setShowFullAnimation(true) //????????????
            .setNeedLockFull(false) //????????????????????????????????????
            .setCacheWithPlay(true) //??????????????????m3u8?????????
            .setReleaseWhenLossAudio(false) //?????????????????????????????????
            .setSeekRatio(8f)
            .setSeekOnStart(0)
            .setIsTouchWigetFull(true) //??????????????????????????????????????????????????????
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onPrepared(url: String, vararg objects: Any) {
                    super.onPrepared(url, *objects)
                    orientationUtils.isEnable = true
                }

                override fun onQuitFullscreen(url: String, vararg objects: Any) {
                    super.onQuitFullscreen(url, *objects)
                    orientationUtils.backToProtVideo()
                }
            })

        binding.mLogPlayer.backButton.setOnClickListener(View.OnClickListener {
            if (orientationUtils != null && GSYVideoManager.isFullState(this)) {
                orientationUtils.backToProtVideo()
                GSYVideoManager.backFromWindowFull(this)
            } else {
                finish()
            }
        })

        binding.mLogPlayer.fullscreen.visibility = View.VISIBLE
        binding.mLogPlayer.fullscreen.setOnClickListener {
            orientationUtils.resolveByClick() //LANDSCAPE
            binding.mLogPlayer.startWindowFullscreen(this, false, true)
        }
    }

    /**
     * ????????????id??????mvID*/
    private fun getMvIdInfo(songId: String){
        val url = HttpUtil.getMvInfoAsIdURL(songId)

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                val message = Message()
                message.obj = json.toString()
                message.what = UPDATEInfos
                handler.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, e.toString())
                ToastUtil.setFailToast(this@MLogActivity, e.toString())
            }
        })
    }

    /**
     * ????????????????????????*/
    private fun getMLogUrl(id: String) {
        val url = HttpUtil.getMLogURL(id).toString()

        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, json.toString())
                val mLogURL = JSONObject(json.toString()).getJSONObject("data").getJSONObject("resource").getJSONObject("content").getJSONObject("video").getJSONObject("urlInfo").getString("url")
                val message = Message()
                message.what = UPDATEPlayer
                message.obj = mLogURL.toString()
                handler.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, e.toString())
                ToastUtil.setFailToast(this@MLogActivity, e.toString())
            }
        })
    }

    private fun updateRecyclerData(json: String){
        if (TextUtils.isEmpty(json))return
        val array = JSONObject(json.toString()).getJSONObject("data").getJSONArray("feeds")
        if (array.length() == 0){
            ToastUtil.setFailToast(this,"???????????????mlog??????")
            return
        }
        for (mlog in 0 until array.length()) {
            val resource = JSONObject(array[mlog].toString()).getJSONObject("resource")
            val mlogBaseData = JSONObject(resource.toString()).getJSONObject("mlogBaseData")
            val mlogId = mlogBaseData.getString("id").toString()
            val text = mlogBaseData.getString("text").toString()
            val pubTime = mlogBaseData.getLong("pubTime")
            val duration = mlogBaseData.getLong("duration")
            val cover = mlogBaseData.getJSONObject("video").getString("frameUrl").toString()

            val mlogExtVO = resource.getJSONObject("mlogExtVO")
            val likedCount = mlogExtVO.getLong("likedCount")
            val commentCount = mlogExtVO.getLong("commentCount")
            val playCount = mlogExtVO.getLong("playCount")
            val shareCount = mlogExtVO.getLong("shareCount")
            val liked = mlogExtVO.getBoolean("liked")

            val artists = mlogExtVO.getJSONObject("song").getJSONArray("artists").get(0).toString()
            val singerName = JSONObject(artists.toString()).getString("artistName").toString()
            val songName = mlogExtVO.getJSONObject("song").getString("name").toString()

            val nickname = resource.getJSONObject("userProfile").getString("nickname").toString()

            val mlogBean = MlogBean(mlogId, text, cover, duration, likedCount, commentCount, playCount, shareCount, singerName, songName, pubTime, nickname, liked)
            mlogList.add(mlogBean)
        }

        val message = Message()
        message.what = UPDATECurrent
        handler.sendMessage(message)
    }

    /**
     * ??????????????????*/
    private fun remainDigit(num: Double):String {
        val df = DecimalFormat("#.0")
        return df.format(num)
    }


    private fun startPlay(url: String) {
        binding.mLogPlayer.setUp(url, false, "")
        binding.mLogPlayer.startPlayLogic()
    }

    private fun updateInfo(bean: MlogBean) {
        binding.mlogLoveText.visibility = View.VISIBLE
        binding.mlogCommentText.visibility = View.VISIBLE
        binding.mlogShareText.visibility = View.VISIBLE
        binding.mlogFavoriteText.visibility = View.VISIBLE

        binding.mlogDescribe.text = bean.text
        binding.mlogInfo.text = "${bean.singerName}-${bean.songName}"

        binding.mlogFavoriteText.text = "??????"

        if (bean.likedCount < 10000) binding.mlogLoveText.text = "${bean.likedCount}"
        else binding.mlogLoveText.text = "${remainDigit(bean.likedCount*1.0 / 10000)}W"

        if (bean.shareCount < 10000) binding.mlogShareText.text = "${bean.shareCount}"
        else binding.mlogShareText.text = "${remainDigit(bean.shareCount*1.0 / 10000)}W"

        if (bean.commentCount < 10000) binding.mlogCommentText.text = "${bean.commentCount}"
        else binding.mlogCommentText.text = "${remainDigit(bean.commentCount*1.0 / 10000)}W"

        binding.mlogPlayCount.text = figurePlayCount(bean.playCount)
        binding.mlogPublishTime.text = millToDate(bean.pubTime)
    }

    /**
     * ???????????????*/
    private fun figurePlayCount(count: Long): String{
        return if (count < 10000) "$count ??????"
        else if (count < 100000000) "${count/10000}?????????"
        else "${count/100000000}?????????"
    }

    private fun millToDate(time: Long): String{
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }
    override fun onResume() {
        super.onResume()
        binding.mLogPlayer.currentPlayer.onVideoResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mLogPlayer.currentPlayer.onVideoPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        binding.mLogPlayer.currentPlayer.release()
    }
}