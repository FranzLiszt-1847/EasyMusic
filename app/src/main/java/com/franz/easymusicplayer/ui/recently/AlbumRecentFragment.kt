package com.franz.easymusicplayer.ui.recently

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.franz.easymusicplayer.adapter.AlbumResultAdapter
import com.franz.easymusicplayer.base.LazyFragment
import com.franz.easymusicplayer.bean.AlbumResultBean
import com.franz.easymusicplayer.bean.SongRecentBean
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.FragmentAlbumRecentBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.param.MusicParam
import com.franz.easymusicplayer.ui.songList.AlbumListActivity
import com.franz.easymusicplayer.utils.HttpUtil
import org.json.JSONObject

class AlbumRecentFragment : LazyFragment() {
    private lateinit var binding: FragmentAlbumRecentBinding
    private lateinit var adapter: AlbumResultAdapter
    private val beanList: MutableList<AlbumResultBean> = ArrayList()
    private val UPDATE: Int = 1
    private val TAG: String = "AlbumRecentFragmentLog"

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
        binding = FragmentAlbumRecentBinding.inflate(layoutInflater)
        initRecycler()
        return binding.root
    }
    private fun initRecycler(){
        binding.albumRecentRecycler.layoutManager = LinearLayoutManager(activity)
        adapter = AlbumResultAdapter(beanList)
        binding.albumRecentRecycler.adapter = adapter

        adapter.setItemClickListener(object : AlbumResultAdapter.OnAlbumClickListener{
            override fun onClickListener(bean: AlbumResultBean) {
                val intent = Intent(activity, AlbumListActivity::class.java)
                intent.putExtra(CacheKeyParam.albumSongs,"${bean.id}")
                startActivity(intent)
            }
        })
    }

    private fun getRecentInfo(){
        val url = HttpUtil.getRecentlyURL(MusicParam.recentlyAlbum)

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
            beanList.add(HttpUtil.fromJson(data, AlbumResultBean::class.java))
        }
        adapter.notifyDataSetChanged()
        binding.loading.hide()
    }
    override fun initLazyFunc() {
        binding.loading.show()
        getRecentInfo()
    }
}