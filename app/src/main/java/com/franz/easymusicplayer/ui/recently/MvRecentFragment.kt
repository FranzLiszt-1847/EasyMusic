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
import com.franz.easymusicplayer.adapter.MvAdapter
import com.franz.easymusicplayer.adapter.MvRecentAdapter
import com.franz.easymusicplayer.base.LazyFragment
import com.franz.easymusicplayer.bean.AlbumResultBean
import com.franz.easymusicplayer.bean.MvRecentBean
import com.franz.easymusicplayer.bean.MvResultBean
import com.franz.easymusicplayer.bean.SongRecentBean
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.FragmentMvRecentBinding
import com.franz.easymusicplayer.databinding.FragmentMvResultBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.param.MusicParam
import com.franz.easymusicplayer.ui.player.VideoPlayerActivity
import com.franz.easymusicplayer.utils.HttpUtil
import org.json.JSONObject


class MvRecentFragment : LazyFragment() {
    private lateinit var binding: FragmentMvRecentBinding
    private lateinit var adapter: MvRecentAdapter
    private val beanList: MutableList<MvRecentBean> = ArrayList()
    private val UPDATE: Int = 1
    private val TAG: String = "MvRecentFragmentLog"

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
        binding = FragmentMvRecentBinding.inflate(layoutInflater)
        initRecycler()
        return binding.root
    }

    private fun initRecycler(){
        binding.mvRecentRecycler.layoutManager = LinearLayoutManager(activity)
        adapter = MvRecentAdapter(beanList)
        binding.mvRecentRecycler.adapter = adapter

        adapter.setItemClickListener(object : MvRecentAdapter.OnMvClickListener{
            override fun onClickListener(bean: MvRecentBean) {
                val intent = Intent(activity, VideoPlayerActivity::class.java)
                intent.putExtra(CacheKeyParam.mvId, bean.id)
                intent.putExtra(CacheKeyParam.mvName,bean.name)
                startActivity(intent)
            }
        })
    }

    private fun getRecentInfo(){
        val url = HttpUtil.getRecentlyURL(MusicParam.recentlyMv)

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
            if (bean.resourceType == "MLOG")continue
            val data = JSONObject(bean.data.toString()).toString()
            beanList.add(HttpUtil.fromJson(data, MvRecentBean::class.java))
        }
        adapter.notifyDataSetChanged()
        binding.loading.hide()
    }
    override fun initLazyFunc() {
        binding.loading.show()
        getRecentInfo()
    }
}