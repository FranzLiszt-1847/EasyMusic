package com.franz.easymusicplayer.ui.searchResult

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
import com.franz.easymusicplayer.bean.ResultBean
import com.franz.easymusicplayer.bean.SearchKeyBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.bean.SongsResultBean
import com.franz.easymusicplayer.binder.MusicBinder
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.FragmentPlayListResultBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.ui.songList.SongListActivity
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

class PlayListResultFragment : Fragment() {
    private val TAG: String = "PlayListResultFragmentLog"
    private lateinit var binding: FragmentPlayListResultBinding
    private lateinit var adapter: SongsResultAdapter
    private val beanList: MutableList<SongsResultBean> = ArrayList()
    private val UPDATE: Int = 1

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                UPDATE -> {
                    beanList.clear()
                    beanList.addAll(msg.obj as List<SongsResultBean>)
                    adapter.notifyDataSetChanged()
                    binding.loading.hide()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPlayListResultBinding.inflate(inflater)
        initRecycler()
        return binding.root
    }

    private fun initRecycler(){
        binding.songsResultRecycler.layoutManager = LinearLayoutManager(activity)
        adapter = SongsResultAdapter(beanList)
        binding.songsResultRecycler.adapter = adapter

        adapter.setItemClickListener(object : SongsResultAdapter.OnSongsClickListener{
            override fun onClickListener(bean: SongsResultBean) {
                val intent = Intent(activity,SongListActivity::class.java)
                intent.putExtra(CacheKeyParam.songListId,"${bean.id}")
                intent.putExtra(CacheKeyParam.songListName,bean.name)
                intent.putExtra(CacheKeyParam.songListCover,bean.coverImgUrl)
                startActivity(intent)
            }
        })
    }

    /**
     * 获取歌曲搜索结果*/
    private fun getSongsResult(keyWords: String){
        val url = HttpUtil.getSearchResultURL(keyWords,1000,30)

        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, json.toString())
                val results = JSONObject(json.toString()).getJSONObject("result").getJSONArray("playlists")
                val songs = HttpUtil.fromListJson(results.toString(), SongsResultBean::class.java)
                val message = Message()
                message.what = UPDATE
                message.obj = songs
                handler.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, e.toString())
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onEvent(bean: SearchKeyBean){
        binding.loading.show()
        getSongsResult(bean.key)
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