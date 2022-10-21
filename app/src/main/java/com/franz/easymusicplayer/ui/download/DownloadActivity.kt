package com.franz.easymusicplayer.ui.download

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseFragmentAdapter
import com.franz.easymusicplayer.databinding.ActivityDownloadBinding
import com.franz.easymusicplayer.databinding.TitleBarBinding
import com.franz.easymusicplayer.utils.StatusBarUtil

class DownloadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadBinding
    private lateinit var titleBar: TitleBarBinding
    private val fragmentsList = arrayListOf<Fragment>()
    private val titlesList = arrayListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarHide(window)
        StatusBarUtil.setStatusBarLightMode(window)
        StatusBarUtil.setStatusBarColor(window,this, R.color.download_bg)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        titleBar = binding.downloadBar
        setContentView(binding.root)
        initBar()
        initViewPager()
    }

    private fun initBar(){
        titleBar.majorTitle.text = "本地下载"
        titleBar.subTitle.visibility = View.INVISIBLE
        titleBar.exit.setOnClickListener { finish() }
    }
    private fun initViewPager(){
        fragmentsList.add(DownloadingFragment())
        fragmentsList.add(DownloadedFragment())

        titlesList.add("未下载")
        titlesList.add("已下载")

        val adapter = BaseFragmentAdapter(supportFragmentManager,fragmentsList,titlesList)
        binding.downloadViewPager.adapter = adapter
        binding.downloadTabTitle.setupWithViewPager(binding.downloadViewPager)
        binding.downloadViewPager.offscreenPageLimit = 2
    }
}