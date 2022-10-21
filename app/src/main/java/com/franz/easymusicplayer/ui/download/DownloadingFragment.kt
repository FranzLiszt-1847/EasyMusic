package com.franz.easymusicplayer.ui.download

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.franz.easymusicplayer.adapter.DownloadingAdapter
import com.franz.easymusicplayer.bean.*
import com.franz.easymusicplayer.databinding.FragmentDownloadingBinding
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.HomePageActivity
import com.franz.easymusicplayer.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class DownloadingFragment : Fragment() {
    private lateinit var binding:FragmentDownloadingBinding
    private lateinit var adapter: DownloadingAdapter
    private val downloadList: MutableList<DownloadBean> = ArrayList<DownloadBean>()

    private val pending_start:Byte = 6
    private val running:Byte = 3
    private val completed:Byte = -3
    private val failed:Byte = -1
    private val error:Byte = 111

    private var currentItem: Int = -2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDownloadingBinding.inflate(layoutInflater)
        initRecycler()
        initData()
        return binding.root
    }

    private fun initRecycler(){
        binding.downloadingRecycler.layoutManager = LinearLayoutManager(requireActivity())
        adapter = DownloadingAdapter(downloadList,requireContext())
        binding.downloadingRecycler.adapter = adapter

        /**
         * 下载项点击时事件*/
        adapter.setItemClickListener { view, pos ->

        }
    }

    /**
     * 初始化recycler数据*/
    private fun initData(){
        val list = PlayListDataBase.getDBInstance().downloadDao().getAll()
        if (list == null || list.size == 0)return
        for (i in 0 until list.size){
            /**
             * 初始化未完成的*/
            if (!list[i].complete){
                downloadList.add(list[i])
            }
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 下载回调监听*/
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(bean: DownloadingBean){
        update(bean)
    }

    /**
     * currentItem等于-1代表没有相同项
     * currentItem等于-2代表状态改变，比如下载完成,初始化-2*/
    private fun update(bean: DownloadingBean){
        when(bean.status){
            pending_start->{}
            running->{
                updateItem(bean,1)
            }
            completed->{
                if (currentItem != -1 && currentItem != -2){
                    adapter.deleteCompletedItem(currentItem)
                    currentItem = -2
                    EventBus.getDefault().postSticky(DownloadCompleteBean(true))
                }
            }
            failed->{updateItem(bean,3)}
            error->{
                ToastUtil.setFailToast(HomePageActivity.MA,bean.error)
            }
        }
    }

    private fun updateItem(bean: DownloadingBean,status: Int){
        when (currentItem) {
            -2 -> {
                searchItem(bean)
            }
            -1 -> {
                //没有相同项
            }
            else -> {
                downloadList[currentItem].progress = bean.percentage
                downloadList[currentItem].speed = bean.speed
                adapter.notifyItemChanged(currentItem,status)
            }
        }
    }

    /**
     * 查找当前下载项在列表中的位置*/
    private fun searchItem(bean: DownloadingBean){
        if (downloadList.size == 0 || bean == null){
            currentItem = -1
            return
        }
        for (i in 0 until downloadList.size){
            val search = "${bean.bean?.songId}${bean.bean?.songName}"
            val require = "${downloadList[i].songId}${downloadList[i].songName}"
            if (search == require){
                currentItem = i
                return
            }
        }
        currentItem = -1
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