package com.franz.easymusicplayer.ui.recently

import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseFragmentAdapter
import com.franz.easymusicplayer.databinding.ActivityRecentlyBinding
import com.franz.easymusicplayer.databinding.TitleBarBinding
import com.franz.easymusicplayer.ui.searchResult.AlbumResultFragment
import com.franz.easymusicplayer.ui.searchResult.MusicResultFragment
import com.franz.easymusicplayer.ui.searchResult.MvResultFragment
import com.franz.easymusicplayer.ui.searchResult.PlayListResultFragment
import com.franz.easymusicplayer.utils.StatusBarUtil

class RecentlyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecentlyBinding
    private lateinit var titleBar: TitleBarBinding
    private val fragmentsList = arrayListOf<Fragment>()
    private val titlesList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarHide(window)
        StatusBarUtil.setStatusBarLightMode(window)
        binding = ActivityRecentlyBinding.inflate(layoutInflater)
        titleBar = binding.recentBar
        setContentView(binding.root)
        initBar()
        initViewPager()
    }

    private fun initBar(){
        titleBar.majorTitle.text = "最近播放"
        titleBar.subTitle.visibility = View.INVISIBLE
        titleBar.exit.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                finish()
            }
        })
    }
    private fun initViewPager(){
        fragmentsList.add(SongRecentFragment())
        fragmentsList.add(SheetRecentFragment())
        fragmentsList.add(AlbumRecentFragment())
        fragmentsList.add(MvRecentFragment())

        titlesList.add("歌曲")
        titlesList.add("歌单")
        titlesList.add("专辑")
        titlesList.add("视频")

        val adapter = BaseFragmentAdapter(supportFragmentManager,fragmentsList,titlesList)
        binding.recentViewPager.adapter = adapter
        binding.recentTabTitle.setupWithViewPager(binding.recentViewPager)
        binding.recentViewPager.offscreenPageLimit = 4
    }

}