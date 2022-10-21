package com.franz.easymusicplayer.ui.recently

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.adapter.MusicResultAdapter
import com.franz.easymusicplayer.adapter.SongsResultAdapter
import com.franz.easymusicplayer.base.LazyFragment
import com.franz.easymusicplayer.bean.*
import com.franz.easymusicplayer.binder.MusicBinder
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.FragmentSheetRecentBinding
import com.franz.easymusicplayer.databinding.FragmentSongRecentBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.param.MusicParam
import com.franz.easymusicplayer.ui.songList.SongListActivity
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.ToastUtil
import org.json.JSONArray
import org.json.JSONObject

class SheetRecentFragment : LazyFragment() {
    private val TAG: String = "SheetRecentFragmentLog"
    private lateinit var binding: FragmentSheetRecentBinding
    private lateinit var adapter: SongsResultAdapter
    private val beanList: MutableList<SongsResultBean> = ArrayList()

    private val UPDATE: Int = 0

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                UPDATE -> {
                    beanList.clear()
                    updateRecycler(msg.obj as List<SongRecentBean>)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSheetRecentBinding.inflate(layoutInflater)
        initRecycler()
        return binding.root
    }

    private fun initRecycler(){
        binding.sheetRecentRecycler.layoutManager = LinearLayoutManager(activity)
        adapter = SongsResultAdapter(beanList)
        binding.sheetRecentRecycler.adapter = adapter

        adapter.setItemClickListener(object : SongsResultAdapter.OnSongsClickListener{
            override fun onClickListener(bean: SongsResultBean) {
                val intent = Intent(activity, SongListActivity::class.java)
                intent.putExtra(CacheKeyParam.songListId,"${bean.id}")
                intent.putExtra(CacheKeyParam.songListName,bean.name)
                intent.putExtra(CacheKeyParam.songListCover,bean.coverImgUrl)
                startActivity(intent)
            }
        })
    }

    private fun getRecentInfo(){
        val url = HttpUtil.getRecentlyURL(MusicParam.recentlyList)

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo {
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

    private fun updateRecycler(beans: List<SongRecentBean>){
        for (bean in beans){
            val data = JSONObject(bean.data.toString()).toString()
            beanList.add(HttpUtil.fromJson(data,SongsResultBean::class.java))
        }
        adapter.notifyDataSetChanged()
        binding.loading.hide()
    }

    override fun initLazyFunc() {
        binding.loading.show()
        getRecentInfo()
    }
}