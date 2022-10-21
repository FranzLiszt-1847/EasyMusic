package com.franz.easymusicplayer.ui.playSheet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.adapter.SongsAdapter
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.DetailInfoBean
import com.franz.easymusicplayer.bean.DownloadBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.binder.DownloadBinder
import com.franz.easymusicplayer.binder.MusicBinder
import com.franz.easymusicplayer.callback.IDetailInfo
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.ActivityPlaySheetBinding
import com.franz.easymusicplayer.databinding.PopSongDetailBinding
import com.franz.easymusicplayer.databinding.TitleBarBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.player.MLogActivity
import com.franz.easymusicplayer.utils.*
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import org.json.JSONObject

class PlaySheetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaySheetBinding
    private lateinit var barBinding: TitleBarBinding
    private lateinit var adapter: SongsAdapter
    private val beanList: MutableList<SongBean> = ArrayList()

    private val SONGURL: Int =  1
    private val UPDATE: Int =  2
    private val SONGDETAIL: Int = 3

    private lateinit var detailBinding: PopSongDetailBinding
    private lateinit var detailView: View
    private lateinit var detailPop: PopupWindow

    private val handler = object : Handler(Looper.myLooper()!!){
        override fun handleMessage(msg: Message) {
            when(msg.what){
                SONGURL->  updatePlayInfo(msg.obj as SongBean)
                UPDATE-> {
                    beanList.addAll(msg.obj as MutableList<SongBean>)
                    adapter.notifyDataSetChanged()
                }
                SONGDETAIL -> updateSongDetail(msg.obj as DetailInfoBean)
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarHide(window)
        StatusBarUtil.setStatusBarLightMode(window)
        StatusBarUtil.setStatusBarColor(window,this,R.color.white)
        binding = ActivityPlaySheetBinding.inflate(layoutInflater)
        barBinding = binding.sheetTitleBar
        setContentView(binding.root)
        initBar()
        initRecycler()
        initData()
        initPopWindow()
    }

    /**
     * 初始化标题栏*/
    private fun initBar(){
        barBinding.majorTitle.text = "播放列表"
        barBinding.subTitle.visibility = View.INVISIBLE
        barBinding.exit.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                finish()
            }
        })
    }

    private fun initRecycler(){
        binding.sheetRecycler.layoutManager = LinearLayoutManager(this)
        adapter = SongsAdapter(beanList)
        binding.sheetRecycler.adapter = adapter

        adapter.setItemClickListener(object : SongsAdapter.OnSongClickListener{
            override fun onClickListener(pos: Int, bean: SongBean) {
                /**
                 * 修改歌曲在列表中位置*/
                MusicBinder.curSongsCur = pos
                getSongUrl(bean)
                ToastUtil.setSuccessToast(this@PlaySheetActivity, "歌曲正在加载中...")
            }
        })

        adapter.setMvItemClickListener(object : SongsAdapter.OnMvClickListener{
            override fun onClickListener(pos: Int, bean: SongBean) {
                val intent = Intent(this@PlaySheetActivity, MLogActivity::class.java)
                intent.putExtra(CacheKeyParam.mlogId, "${bean.songId}")
                startActivity(intent)
            }
        })

        /**
         * 根据歌曲id获取歌曲详情*/
        adapter.setMoreInfoItemClickListener(object : SongsAdapter.OnMoreInfoClickListener{
            override fun onClickListener(pos: Int, bean: SongBean) {
                getSongDetail(bean.songId.toString())
            }
        })

        /**
         * 歌曲下载*/
        adapter.setDownloadItemClickListener(object :SongsAdapter.OnDownloadClickListener{
            override fun onClickListener(pos: Int, bean: SongBean) {
                val flag = isExistIdentity(bean)
                if (flag){
                    ToastUtil.setFailToast(this@PlaySheetActivity,"已存在相同下载项，请勿重复添加!")
                }else{
                    DownloadBinder.download()
                    ToastUtil.setSuccessToast(this@PlaySheetActivity,"已添加到下载队列中!")
                }
            }
        })
    }

    /**
     * 初始化歌曲详细pop*/
    private fun initPopWindow(){
        detailView = LayoutInflater.from(this).inflate(R.layout.pop_song_detail,null,false)
        detailBinding = PopSongDetailBinding.bind(detailView)

        detailPop = PopupWindow(detailView, SystemParamUtil.width-200, ViewGroup.LayoutParams.WRAP_CONTENT)
        //detailPop.contentView = detailView

        detailPop.isFocusable = true
        detailPop.isTouchable = true
        detailPop.isOutsideTouchable = true
        detailPop.animationStyle = R.style.PopupWindowAnim

    }

    private fun initData(){
        val dao = PlayListDataBase.getDBInstance().playListDao()
        val list = dao.getAll()

        if (list.size == 0){
            ToastUtil.setFailToast(this@PlaySheetActivity, "播放列表为空!")
            return
        }

        val mes = Message()
        mes.what = UPDATE
        mes.obj = list
        handler.sendMessage(mes)
    }

    /**
     * 获取歌曲url并播放*/
    private fun getSongUrl(bean: SongBean) {
        val url = HttpUtil.getSongURL(bean.songId.toString())
        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                val array = JSONObject(json.toString()).getJSONArray("data")
                val songURL = JSONObject(array[0].toString()).getString("url")
                bean.songUrl = songURL
                val mes = Message()
                mes.what = SONGURL
                mes.obj = bean
                handler.sendMessage(mes)
            }

            override fun onFailed(e: String?) {
                ToastUtil.setFailToast(this@PlaySheetActivity, e.toString())
            }
        })
    }

    /**
     * 根据歌曲id获取歌曲详情*/
    private fun getSongDetail(songId: String){
        val detailInfo = DetailInfo()
        detailInfo.getSongDetail(songId,object : IDetailInfo {
            override fun onRespond(bean: DetailInfoBean) {
                val message = Message()
                message.what = SONGDETAIL
                message.obj = bean
                handler.sendMessage(message)
            }

            override fun onFailed(e: String) {
                ToastUtil.setFailToast(this@PlaySheetActivity, e.toString())
            }
        })
    }

    private fun updateSongDetail(bean: DetailInfoBean){
        detailBinding.songDetailSong.text = bean.songName
        detailBinding.songDetailSinger.text = bean.singer
        detailBinding.songDetailAlbum.text = bean.album
        Glide.with(detailBinding.songDetailCover)
            .asDrawable()
            .load(bean.cover)
            .transform(GlideRoundTransform(BaseApplication.context,10))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(detailBinding.songDetailCover)
        detailBinding.songDetailTime.text = bean.time
        detailBinding.songDetailType.text = bean.type
        detailShow()
    }

    /**
     * 播放歌曲，并更新歌曲信息*/
    private fun updatePlayInfo(bean: SongBean) {
        MusicBinder.prepare(bean)
        ToastUtil.setSuccessToast(this@PlaySheetActivity, "歌曲信息获取完成")
    }

    private fun detailShow(){
        detailPop.showAtLocation(detailView, Gravity.CENTER,0,0)
    }

    private fun isExistIdentity(bean: SongBean): Boolean{
        val dao = PlayListDataBase.getDBInstance().downloadDao()
        val key = "${bean.songId}${bean.songName}"
        val isExist: DownloadBean = dao.findByKey(key)
        /**
         * 如果不为空，则代表存在，则不再进行重复下载*/
        if (isExist != null) return true

        val downloadBean = DownloadBean(key,bean.songName,bean.songId,bean.singerName,"",bean.albumCover,"","",0,0,false,false,"")

        /*update room*/
        dao.insert(downloadBean)

        /*update binder*/
        DownloadBinder.downloadList.add(downloadBean)
        return false
    }
}