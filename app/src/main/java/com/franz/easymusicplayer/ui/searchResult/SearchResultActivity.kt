package com.franz.easymusicplayer.ui.searchResult

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseFragmentAdapter
import com.franz.easymusicplayer.bean.DrawerBean
import com.franz.easymusicplayer.bean.HistorySearchBean
import com.franz.easymusicplayer.bean.SearchKeyBean
import com.franz.easymusicplayer.databinding.ActivitySearchResultBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.utils.StatusBarUtil
import com.franz.easymusicplayer.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SearchResultActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySearchResultBinding
    private val fragmentsList = arrayListOf<Fragment>()
    private val titlesList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarHide(window)
        StatusBarUtil.setStatusBarLightMode(window)
        StatusBarUtil.setStatusBarColor(window, this, R.color.color_system_bg)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        initViewPager()
        initSearchViewStyle()
        initListener()
        setContentView(binding.root)
    }


    private fun initViewPager() {
        fragmentsList.add(MusicResultFragment())
        fragmentsList.add(PlayListResultFragment())
        fragmentsList.add(AlbumResultFragment())
        fragmentsList.add(MvResultFragment())

        titlesList.add("歌曲")
        titlesList.add("歌单")
        titlesList.add("专辑")
        titlesList.add("视频")

        val adapter = BaseFragmentAdapter(supportFragmentManager, fragmentsList, titlesList)
        binding.searchResultViewPager.adapter = adapter
        binding.searchResultTabTitle.setupWithViewPager(binding.searchResultViewPager)
        binding.searchResultViewPager.offscreenPageLimit = 4
    }

    /**
     * 更改searchView字体颜色
     */
    private fun initSearchViewStyle() {
        val editText =
            binding.searchBar.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        editText.setTextColor(resources.getColor(R.color.black))
        editText.setHintTextColor(resources.getColor(R.color.color_system_default))
        editText.textSize = 14f
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.searchBar.setSearchableInfo(searchManager.getSearchableInfo(componentName))
    }

    private fun initRecord(key: String) {
        val dao = PlayListDataBase.getDBInstance().historyDao()
        val bean: HistorySearchBean = dao.findByKey(key)
        if (bean != null) {
            dao.delete(bean)
        }
        dao.insert(HistorySearchBean(key))
    }


    private fun initListener() {
        binding.exitSearchResult.setOnClickListener(this)
        binding.searchResultBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) = when (v?.id) {
        R.id.exitSearchResult -> finish()
        R.id.searchResultBtn -> {
            val key = binding.searchBar.query.toString()
            if (TextUtils.isEmpty(key)) {
                ToastUtil.setFailToast(this@SearchResultActivity, "输入结果不能为空!")
            } else {
                initRecord(key)
                ToastUtil.setSuccessToast(this@SearchResultActivity, "搜索成功!")
                EventBus.getDefault().postSticky(SearchKeyBean(key))
            }
        }
        else -> {}
    }
}