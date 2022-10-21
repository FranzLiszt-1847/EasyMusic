package com.franz.easymusicplayer.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.MvInfoBean
import com.franz.easymusicplayer.binder.MusicBinder
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.ActivityVideoPlayerBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.StatusBarUtil
import com.franz.easymusicplayer.utils.ToastUtil
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import org.json.JSONObject
import java.text.DecimalFormat

class VideoPlayerActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityVideoPlayerBinding
    private lateinit var orientationUtils: OrientationUtils
    private lateinit var mvName: String
    private val TAG: String = "VideoPlayerActivityLog"
    private val UPDATEURL: Int = 1
    private val UPDATEINFO: Int = 2

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) = when (msg.what) {
            UPDATEURL -> {
                startPlay(msg.obj as String, mvName)
            }

            UPDATEINFO -> {
                val data = JSONObject(msg.obj as String).getJSONObject("data")
                val bean = HttpUtil.fromJson(data.toString(),MvInfoBean::class.java)
                updateInfo(bean)
            }
            else -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarHide(window)
        StatusBarUtil.setStatusBarColor(window, this, R.color.black)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        turnOffMusic()
        initPlayer()
        getMvID()
        init()
    }

    private fun turnOffMusic(){
        if (MusicBinder.isPlay) {
            MusicBinder.pause()
        }
    }
    private fun getMvID() {
        val intent = intent
        val mvID = intent.getStringExtra(CacheKeyParam.mvId).toString()
        if (TextUtils.isEmpty(mvID)) {
            ToastUtil.setFailToast(this@VideoPlayerActivity, "视频id不能为空!")
            return
        }
        mvName = intent.getStringExtra(CacheKeyParam.mvName).toString()
        /**
         * mv*/
        getVideoUrl(mvID)
        getVideoInfo(mvID)
    }

    private fun initPlayer() {
        orientationUtils = OrientationUtils(this, binding.iVideoPlayer)
        val builder = GSYVideoOptionBuilder()
        builder.setUrl(null)
            .setVideoTitle(null) //movie title
            .setIsTouchWiget(false) //小屏时不触摸滑动
            .setRotateViewAuto(false) //是否开启自动旋转
            .setLockLand(false) //一全屏就锁屏横屏，默认false竖屏，可配合setRotateViewAuto使用
            .setAutoFullWithSize(true) //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
            .setShowFullAnimation(true) //全屏动画
            .setNeedLockFull(false) //是否需要全屏锁定屏幕功能
            .setCacheWithPlay(true) //是否边缓存，m3u8等无效
            .setReleaseWhenLossAudio(false) //音频焦点冲突时是否释放
            .setSeekRatio(8f)
            .setSeekOnStart(0)
            .setIsTouchWigetFull(true) //是否可以全屏滑动界面改变进度，声音等
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

        binding.iVideoPlayer.backButton.setOnClickListener(View.OnClickListener {
            if (orientationUtils != null && GSYVideoManager.isFullState(this)) {
                orientationUtils.backToProtVideo()
                GSYVideoManager.backFromWindowFull(this)
            } else {
                finish()
            }
        })
    }

    /**
     * 获取视频播放地址*/
    private fun getVideoUrl(id: String) {
        val url = HttpUtil.getMvURL(id).toString()

        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, json.toString())
                val mvURL = JSONObject(json.toString()).getJSONObject("data").getString("url")
                val message = Message()
                message.what = UPDATEURL
                message.obj = mvURL.toString()
                handler.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, e.toString())
                ToastUtil.setFailToast(this@VideoPlayerActivity, e.toString())
            }
        })
    }

    /**
     * 获取视频相关信息*/
    private fun getVideoInfo(id: String) {
        val url = HttpUtil.getMvInfoURL(id).toString()

        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, json.toString())
                val message = Message()
                message.what = UPDATEINFO
                message.obj = json.toString()
                handler.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, e.toString())
                ToastUtil.setFailToast(this@VideoPlayerActivity, e.toString())
            }
        })
    }

    /**
     * 保留一位小数*/
    private fun remainDigit(num: Double):String {
        val df = DecimalFormat("#.0")
        return df.format(num)
    }


    private fun startPlay(url: String, title: String) {
        binding.iVideoPlayer.setUp(url, false, title)
        binding.iVideoPlayer.startPlayLogic()
    }

    private fun updateInfo(bean: MvInfoBean) {
        binding.authorName.text = bean.artistName
        binding.authorSong.text = bean.name
        binding.videoFavoriteNum.text = "收藏"

        if (bean.subCount < 10000) binding.videoLikesNum.text = "${bean.subCount}"
        else binding.videoLikesNum.text = "${remainDigit(bean.subCount*1.0 / 10000)}W"

        if (bean.shareCount < 10000) binding.videoSharesNum.text = "${bean.shareCount}"
        else binding.videoSharesNum.text = "${remainDigit(bean.shareCount*1.0 / 10000)}W"

        if (bean.commentCount < 10000) binding.videoCommentNum.text = "${bean.commentCount}"
        else binding.videoCommentNum.text = "${remainDigit(bean.commentCount*1.0 / 10000)}W"

        if (!this.isFinishing) {
            Glide.with(binding.authorImg)
                .asDrawable()
                .load(bean.cover)
                .transform(GlideRoundTransform(BaseApplication.context, 10))
                .placeholder(R.drawable.icon_default_songs)
                .dontAnimate()
                .into(binding.authorImg)
        }

    }

    private fun init() {
        binding.fullScreen.setOnClickListener(this)
        binding.videoExit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fullScreen -> {
                orientationUtils.resolveByClick() //LANDSCAPE
                binding.iVideoPlayer.startWindowFullscreen(this, false, true)
            }
            R.id.videoExit-> {
                finish()
            }
            else -> {
                null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.iVideoPlayer.currentPlayer.onVideoResume()
    }

    override fun onPause() {
        super.onPause()
        binding.iVideoPlayer.currentPlayer.onVideoPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        binding.iVideoPlayer.currentPlayer.release()
    }
}