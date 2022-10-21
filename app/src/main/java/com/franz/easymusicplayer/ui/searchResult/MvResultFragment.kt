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
import com.franz.easymusicplayer.adapter.MvAdapter
import com.franz.easymusicplayer.adapter.SongsResultAdapter
import com.franz.easymusicplayer.base.LazyFragment
import com.franz.easymusicplayer.bean.MvResultBean
import com.franz.easymusicplayer.bean.SearchKeyBean
import com.franz.easymusicplayer.bean.SongsResultBean
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.FragmentAlbumResultBinding
import com.franz.easymusicplayer.databinding.FragmentMvResultBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.ui.player.VideoPlayerActivity
import com.franz.easymusicplayer.ui.songList.SongListActivity
import com.franz.easymusicplayer.utils.HttpUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class MvResultFragment : Fragment() {
    private val TAG: String = "MvResultFragmentLog"
    private lateinit var binding: FragmentMvResultBinding
    private lateinit var adapter: MvAdapter
    private val beanList: MutableList<MvResultBean> = ArrayList()
    private val UPDATE: Int = 1

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                UPDATE -> {
                    beanList.clear()
                    beanList.addAll(msg.obj as List<MvResultBean>)
                    adapter.notifyDataSetChanged()
                    binding.loading.hide()
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMvResultBinding.inflate(inflater)
        initRecycler()
        return binding.root
    }

    private fun initRecycler(){
        binding.mvResultRecycler.layoutManager = LinearLayoutManager(activity)
        adapter = MvAdapter(beanList)
        binding.mvResultRecycler.adapter = adapter

        adapter.setItemClickListener(object : MvAdapter.OnMvClickListener{
            override fun onClickListener(bean: MvResultBean) {
                val intent = Intent(activity,VideoPlayerActivity::class.java)
                intent.putExtra(CacheKeyParam.mvId, bean.id)
                intent.putExtra(CacheKeyParam.mvName,bean.name)
                startActivity(intent)
            }
        })
    }

    /**
     * 获取歌曲搜索结果*/
    private fun getSongsResult(keyWords: String){
        val url = HttpUtil.getSearchResultURL(keyWords,1004,30)

        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, json.toString())
                val results = JSONObject(json.toString()).getJSONObject("result").getJSONArray("mvs")
                val songs = HttpUtil.fromListJson(results.toString(), MvResultBean::class.java)
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