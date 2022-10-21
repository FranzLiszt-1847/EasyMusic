package com.franz.easymusicplayer.ui.recently

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.adapter.MusicResultAdapter
import com.franz.easymusicplayer.adapter.SongsAdapter
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.base.LazyFragment
import com.franz.easymusicplayer.bean.*
import com.franz.easymusicplayer.binder.MusicBinder
import com.franz.easymusicplayer.callback.IDetailInfo
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.FragmentSongRecentBinding
import com.franz.easymusicplayer.databinding.PopSongDetailBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.param.MusicParam
import com.franz.easymusicplayer.ui.player.MLogActivity
import com.franz.easymusicplayer.utils.DetailInfo
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.SystemParamUtil
import com.franz.easymusicplayer.utils.ToastUtil
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject

class SongRecentFragment : LazyFragment() {
    private lateinit var binding: FragmentSongRecentBinding
    private val TAG: String = "SongRecentFragmentLog"
    private lateinit var adapter: MusicResultAdapter
    private val beanList: MutableList<ResultBean> = ArrayList()

    private val UPDATE: Int = 0
    private val SONGURL: Int = 1
    private val SONGDETAIL: Int = 3

    private lateinit var detailBinding: PopSongDetailBinding
    private lateinit var detailView: View
    private lateinit var detailPop: PopupWindow

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                UPDATE -> {
                    beanList.clear()
                    updateRecycler(msg.obj as List<SongRecentBean>)
                }

                SONGURL->{
                    MusicBinder.prepare(msg.obj as SongBean)
                    ToastUtil.setSuccessToast(activity!!, "歌曲信息获取完成")
                }
                SONGDETAIL -> updateSongDetail(msg.obj as DetailInfoBean)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSongRecentBinding.inflate(layoutInflater)
        initRecycler()
        initPopWindow()
        return binding.root
    }


    private fun initRecycler(){
        binding.songRecentRecycler.layoutManager = LinearLayoutManager(activity)
        adapter = MusicResultAdapter(beanList)
        binding.songRecentRecycler.adapter = adapter

        adapter.setItemClickListener(object : MusicResultAdapter.OnSongClickListener{
            override fun onClickListener(pos: Int, bean: ResultBean) {
                MusicBinder.curSongsCur = 0
                val array = JSONArray(bean.ar.toString())
                val mObject = JSONObject(array[0].toString())
                val singerId = mObject.getString("id")
                val singerName = mObject.getString("name")
                val coverImg = JSONObject(bean.al.toString()).getString("picUrl")
                var songBean = SongBean(bean.id.toInt(),bean.name,bean.id.toLong(),singerName,singerId.toLong(),coverImg)
                getSongUrl(songBean)
            }
        })

        adapter.setMvClickListener(object: MusicResultAdapter.OnMvClickListener{
            override fun onClickListener(pos: Int, bean: ResultBean) {
                val intent = Intent(activity, MLogActivity::class.java)
                intent.putExtra(CacheKeyParam.mlogId, "${bean.id}")
                startActivity(intent)
            }
        })

        /**
         * 根据歌曲id获取歌曲详情*/
        adapter.setDetailInfoItemClickListener(object : MusicResultAdapter.OnDetailInfoClickListener{
            override fun onClickListener(pos: Int, bean: ResultBean) {
                getSongDetail(bean.id.toString())
            }
        })
    }

    /**
     * 初始化歌曲详细pop*/
    private fun initPopWindow(){
        detailView = LayoutInflater.from(activity).inflate(R.layout.pop_song_detail,null,false)
        detailBinding = PopSongDetailBinding.bind(detailView)

        detailPop = PopupWindow(detailView, SystemParamUtil.width-200,ViewGroup.LayoutParams.WRAP_CONTENT)
        //detailPop.contentView = detailView

        detailPop.isFocusable = true
        detailPop.isTouchable = true
        detailPop.isOutsideTouchable = true
        detailPop.animationStyle = R.style.PopupWindowAnim

    }

    private fun getRecentInfo(){
        val url = HttpUtil.getRecentlyURL(MusicParam.recentlySong)

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG, json.toString())
                val results = JSONObject(json.toString()).getJSONObject("data").getJSONArray("list")
                val songs = HttpUtil.fromListJson(results.toString(), SongRecentBean::class.java)
                val message = Message()
                message.what = UPDATE
                message.obj = songs
                handler.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
            }
        })
    }

    /**
     * 获取歌曲url并播放*/
    private fun getSongUrl(bean: SongBean){
        val url = HttpUtil.getSongURL(bean.songId.toString())
        Log.d(TAG,"歌曲url=$url")
        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG,"歌曲数据=${json.toString()}")
                val array = JSONObject(json.toString()).getJSONArray("data")
                val songURL = JSONObject(array[0].toString()).getString("url")
                bean.songUrl = songURL
                val mes = Message()
                mes.what = SONGURL
                mes.obj = bean
                handler.sendMessage(mes)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
                ToastUtil.setFailToast(activity!!, e.toString())
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
                Log.d(TAG, e.toString())
                ToastUtil.setFailToast(activity!!, e.toString())
            }
        })
    }

    private fun updateRecycler(beans: List<SongRecentBean>){
        for (bean in beans){
            val data = JSONObject(bean.data.toString()).toString()
            beanList.add(HttpUtil.fromJson(data,ResultBean::class.java))
        }
        adapter.notifyDataSetChanged()
        binding.loading.hide()
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

    private fun detailShow(){
        detailPop.showAtLocation(detailView, Gravity.CENTER,0,0)
    }

    override fun initLazyFunc() {
        binding.loading.show()
        getRecentInfo()
    }
}