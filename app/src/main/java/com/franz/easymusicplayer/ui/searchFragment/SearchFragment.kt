package com.franz.easymusicplayer.ui.searchFragment

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.adapter.HistoryAdapter
import com.franz.easymusicplayer.adapter.KeyResultAdapter
import com.franz.easymusicplayer.adapter.RecommendParentAdapter
import com.franz.easymusicplayer.base.LazyFragment
import com.franz.easymusicplayer.bean.*
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.FragmentSearchBinding
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.searchResult.SearchResultActivity
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.w3c.dom.Text


class SearchFragment : LazyFragment(), SearchView.OnQueryTextListener, View.OnClickListener {
    private lateinit var binding: FragmentSearchBinding
    private val TAG: String = "SearchFragmentLog"
    private val resultList: MutableList<ResultBean> = ArrayList()
    private val historyList: MutableList<HistorySearchBean> = ArrayList()
    private val recommendList: MutableList<RecommendParentBean> = ArrayList()
    private lateinit var recommendAdapter: RecommendParentAdapter
    private lateinit var keyAdapter: KeyResultAdapter
    private lateinit var historyAdapter: HistoryAdapter
    private val UPDATERESULT: Int = 0
    private val RECOMMEND_MUSIC: Int = 1
    private val RECOMMEND_SONGS: Int = 2
    private val RECOMMEND_MV: Int = 3

    private val handle = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                UPDATERESULT -> {
                    resultList.clear()
                    resultList.addAll(msg.obj as List<ResultBean>)
                    keyAdapter.notifyDataSetChanged()
                }

                RECOMMEND_MUSIC->{
                    val bean = msg.obj as List<RecommendBean>
                    recommendList.add(RecommendParentBean(RECOMMEND_MUSIC,bean))
                    recommendAdapter.notifyDataSetChanged()
                    binding.loading.hide()
                }

                RECOMMEND_SONGS->{
                    val bean = msg.obj as List<RecommendBean>
                    recommendList.add(RecommendParentBean(RECOMMEND_SONGS,bean))
                    recommendAdapter.notifyDataSetChanged()
                }

                RECOMMEND_MV->{
                    val bean = msg.obj as List<RecommendBean>
                    recommendList.add(RecommendParentBean(RECOMMEND_MV,bean))
                    recommendAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        initSearchViewStyle()
        initRecycler()
        initListener()
        initAdapterListener()
        updateHistory("")
        return binding.root
    }


    private fun initListener() {
        binding.searchOpenSide.setOnClickListener(this)
        binding.searchBtn.setOnClickListener(this)
        binding.historyImage.setOnClickListener(this)
    }

    /**
     * ??????searchView????????????
     */
    private fun initSearchViewStyle() {
        val editText = binding.searchBar.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        editText.setTextColor(resources.getColor(R.color.black))
        editText.setHintTextColor(resources.getColor(R.color.color_system_default))
        editText.textSize = 14f
        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.searchBar.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
    }

    private fun initRecycler() {
        /**
         * ????????????*/
        binding.searchResultList.layoutManager = LinearLayoutManager(activity)
        keyAdapter = KeyResultAdapter(resultList)
        binding.searchResultList.adapter = keyAdapter

        /**
         * ??????????????????*/
        binding.historyRecycler.layoutManager = GridLayoutManager(activity, 3)
        historyAdapter = HistoryAdapter(historyList)
        binding.historyRecycler.adapter = historyAdapter

        /**
         * ??????*/
        binding.recommendRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        recommendAdapter = RecommendParentAdapter(requireActivity(), recommendList)
        binding.recommendRecycler.adapter = recommendAdapter

        binding.recommendRecycler.isNestedScrollingEnabled = false
        binding.historyRecycler.isNestedScrollingEnabled = false

        binding.searchBar.setOnQueryTextListener(this)
        binding.searchBar.isSubmitButtonEnabled = false
    }

    /**
     * ??????????????????????????????*/
    private fun initAdapterListener(){
        keyAdapter.setItemClickListener(object : KeyResultAdapter.OnKeyResultClickListener{
            override fun onClickListener(name: String) {
                updateHistory(name)
                EventBus.getDefault().postSticky(SearchKeyBean(name))
                startActivity(Intent(activity,SearchResultActivity::class.java))
            }
        })

        historyAdapter.setItemClickListener(object : HistoryAdapter.OnHistoryKeyResultClickListener{
            override fun onClickListener(key: String) {
                updateHistory(key)
                EventBus.getDefault().postSticky(SearchKeyBean(key))
                startActivity(Intent(activity,SearchResultActivity::class.java))
            }
        })
    }



    /**
     * ???????????????????????????????????????9??????????????????????????????
     * ??????????????????????????????
     * ????????????????????????????????????????????????????????????????????????*/
    private fun updateHistory(key: String) {
        val dao = PlayListDataBase.getDBInstance().historyDao()
        var all = dao.getAll()

        if (!TextUtils.isEmpty(key)){
            val bean: HistorySearchBean = dao.findByKey(key)
            if (bean != null){
                dao.delete(bean)
                Log.d(TAG,"????????????=${bean.key}")
            }
            dao.insert(HistorySearchBean(key))
            all = dao.getAll()
        }

        all.reverse()
        historyList.clear()
        if (all.size < 9) {
            historyList.addAll(all)
        } else {
            var count = 0
            for (item in all) {
                count++
                if (count > 9) break
                historyList.add(item)
            }
        }
        historyAdapter.notifyDataSetChanged()
    }

    private fun getSearchResult(keyWords: String) {
        val url = HttpUtil.getSearchResultURL(keyWords,1,10)

        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, json.toString())
                val results =
                    JSONObject(json.toString()).getJSONObject("result").getJSONArray("songs")
                val songs = HttpUtil.fromListJson(results.toString(), ResultBean::class.java)
                val message = Message()
                message.what = UPDATERESULT
                message.obj = songs
                handle.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, e.toString())
            }
        })
    }

    private fun getRecommendContent() {
        getRecommendMusic()
        getRecommendSongs()
        getRecommendMV()
    }

    /**
     * ??????????????????*/
    private fun getRecommendMusic() {
        val url = HttpUtil.getRecommendMusicURL()
        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                val result = JSONObject(json.toString()).getJSONArray("result")
                val beanList = HttpUtil.fromListJson(result.toString(), RecommendBean::class.java)
                val message = Message()
                message.what = RECOMMEND_MUSIC
                message.obj = beanList
                handle.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
                ToastUtil.setFailToast(requireActivity(),"????????????????????????!")
            }
        })
    }

    /**
     * ??????????????????*/
    private fun getRecommendSongs() {
        val url = HttpUtil.getRecommendSongsURL()
        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                val result = JSONObject(json.toString()).getJSONArray("result")
                val beanList = HttpUtil.fromListJson(result.toString(), RecommendBean::class.java)
                val message = Message()
                message.what = RECOMMEND_SONGS
                message.obj = beanList
                handle.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
                ToastUtil.setFailToast(requireActivity(),"????????????????????????!")
            }
        })
    }

    /**
     * ????????????MV*/
    private fun getRecommendMV() {
        val url = HttpUtil.getRecommendMvURL()
        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                val result = JSONObject(json.toString()).getJSONArray("result")
                val beanList = HttpUtil.fromListJson(result.toString(), RecommendBean::class.java)
                val message = Message()
                message.what = RECOMMEND_MV
                message.obj = beanList
                handle.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
                ToastUtil.setFailToast(requireActivity(),"????????????????????????!")
            }
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(newText: String?): Boolean {
        if (TextUtils.isEmpty(newText)) {
            binding.searchResultList.visibility = View.GONE
        } else {
            binding.searchResultList.visibility = View.VISIBLE
            binding.searchResultList.dispatchDisplayHint(View.INVISIBLE)
            getSearchResult(newText.toString())
        }
        return true
    }

    override fun onClick(v: View?) = when (v?.id) {
        R.id.searchOpenSide -> EventBus.getDefault().postSticky(DrawerBean(true))
        R.id.searchBtn -> {
            val key = binding.searchBar.query.toString()
            if (TextUtils.isEmpty(key)) {
                ToastUtil.setFailToast(requireActivity(), "????????????????????????!")
            } else {
                updateHistory(key)
                ToastUtil.setSuccessToast(requireActivity(), "????????????!")
                EventBus.getDefault().postSticky(SearchKeyBean(key))
                startActivity(Intent(activity,SearchResultActivity::class.java))
            }
        }
        R.id.historyImage -> {
            val dao = PlayListDataBase.getDBInstance().historyDao()
            dao.deleteAll()
            historyList.clear()
            historyAdapter.notifyDataSetChanged()
            ToastUtil.setSuccessToast(requireActivity(), "????????????!")
        }
        else -> {}
    }

    /**
     * ?????????????????????*/
    override fun initLazyFunc() {
        binding.loading.show()
        getRecommendContent()
    }

}