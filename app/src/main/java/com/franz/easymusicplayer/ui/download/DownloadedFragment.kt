package com.franz.easymusicplayer.ui.download

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
import cn.we.swipe.helper.WeSwipe
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.adapter.DownloadedAdapter
import com.franz.easymusicplayer.adapter.DownloadingAdapter
import com.franz.easymusicplayer.adapter.MusicResultAdapter
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.*
import com.franz.easymusicplayer.binder.MusicBinder
import com.franz.easymusicplayer.callback.IDetailInfo
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.FragmentDownloadedBinding
import com.franz.easymusicplayer.databinding.PopSongDetailBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.player.MLogActivity
import com.franz.easymusicplayer.utils.*
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

class DownloadedFragment : Fragment() {
    private lateinit var binding:FragmentDownloadedBinding

    private lateinit var adapter:DownloadedAdapter
    private val downloadList: MutableList<DownloadBean> = ArrayList<DownloadBean>()

    private lateinit var detailBinding: PopSongDetailBinding
    private lateinit var detailView: View
    private lateinit var detailPop: PopupWindow

    private val SONGDETAIL: Int = 3

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                SONGDETAIL -> updateSongDetail(msg.obj as DetailInfoBean)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =  FragmentDownloadedBinding.inflate(layoutInflater)
        initRecycler()
        initData()
        initPopWindow()
        return binding.root
    }

    private fun initRecycler(){
        binding.downloadRecycler.layoutManager = LinearLayoutManager(requireActivity())
        adapter = DownloadedAdapter(downloadList)
        binding.downloadRecycler.adapter = adapter
        WeSwipe.attach(binding.downloadRecycler)

        /**
         * 歌曲播放*/
        adapter.setItemClickListener(object : DownloadedAdapter.OnSongClickListener{
            override fun onClickListener(pos: Int, bean: DownloadBean) {
                MusicBinder.curSongsCur = 0
                var songBean = SongBean(bean.songId.toInt(),bean.songName,bean.songId,bean.singer,bean.songId,bean.cover)
                songBean.songUrl = bean.path
                MusicBinder.prepare(songBean)
                ToastUtil.setSuccessToast(activity!!, "歌曲信息获取完成")
            }
        })

        /**
         * mlog播放*/
        adapter.setMvClickListener(object: DownloadedAdapter.OnMvClickListener{
            override fun onClickListener(pos: Int, bean: DownloadBean) {
                val intent = Intent(activity, MLogActivity::class.java)
                intent.putExtra(CacheKeyParam.mlogId, "${bean.songId}")
                startActivity(intent)
            }
        })

        /**
         * 歌曲详情*/
        adapter.setDetailInfoItemClickListener(object : DownloadedAdapter.OnDetailInfoClickListener{
            override fun onClickListener(pos: Int, bean: DownloadBean) {
                getSongDetail(bean.songId.toString())
            }
        })

        adapter.setDetailInfoItemClickListener(object :DownloadedAdapter.OnDeleteInfoClickListener{
            override fun onClickListener(pos: Int, bean: DownloadBean) {
                PlayListDataBase.getDBInstance().downloadDao().delete(bean)//删除数据库
                FileUtils.getInstance().deleteDirectory(bean)//删除本地文件
                downloadList.remove(bean)//删除列表
                adapter.notifyDataSetChanged()
                ToastUtil.setSuccessToast(requireActivity(),"删除成功!")
            }
        })
    }

    /**
     * 初始化recycler数据*/
    private fun initData(){
        val list = PlayListDataBase.getDBInstance().downloadDao().getAll()
        if (list == null || list.size == 0)return
        for (i in 0 until list.size){
            if (list[i].complete){
                downloadList.add(list[i])
            }
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 初始化歌曲详细pop*/
    private fun initPopWindow(){
        detailView = LayoutInflater.from(activity).inflate(R.layout.pop_song_detail,null,false)
        detailBinding = PopSongDetailBinding.bind(detailView)

        detailPop = PopupWindow(detailView, SystemParamUtil.width-200,ViewGroup.LayoutParams.WRAP_CONTENT)

        detailPop.isFocusable = true
        detailPop.isTouchable = true
        detailPop.isOutsideTouchable = true
        detailPop.animationStyle = R.style.PopupWindowAnim

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
                ToastUtil.setFailToast(activity!!, e.toString())
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

    private fun detailShow(){
        detailPop.showAtLocation(detailView, Gravity.CENTER,0,0)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(bean: DownloadCompleteBean){
        downloadList.clear()
        initData()
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}