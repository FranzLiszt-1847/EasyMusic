package com.franz.easymusicplayer.ui.findingFragment

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.franz.easymusicplayer.adapter.FBannerAdapter
import com.franz.easymusicplayer.adapter.RankOfficialAdapter
import com.franz.easymusicplayer.adapter.RankSelectAdapter
import com.franz.easymusicplayer.base.LazyFragment
import com.franz.easymusicplayer.bean.BannerBean
import com.franz.easymusicplayer.bean.DrawerBean
import com.franz.easymusicplayer.bean.SheetPlayBean
import com.franz.easymusicplayer.bean.SongListBean
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.FragmentFindingBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.ui.songList.SongListActivity
import com.franz.easymusicplayer.utils.HttpUtil
import com.youth.banner.indicator.CircleIndicator
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject


class FindingFragment : LazyFragment() {
    private lateinit var binding: FragmentFindingBinding
    private val bannerBeanList: MutableList<BannerBean> = ArrayList()

    private val officialBeanList: MutableList<SheetPlayBean> = ArrayList()
    private lateinit var officialAdapter: RankOfficialAdapter

    private val selectBeanList: MutableList<SheetPlayBean> = ArrayList()
    private lateinit var selectAdapter: RankSelectAdapter

    private val TAG: String = "FindingFragmentLog"
    private val UPDATE_BANNER: Int = 1
    private val UPDATE_RANK: Int = 2

    private val handler = object : Handler(Looper.myLooper()!!){
        override fun handleMessage(msg: Message) {
            when(msg.what){
                UPDATE_BANNER-> {
                    bannerBeanList.addAll(msg.obj as List<BannerBean>)
                    binding.findingBanner.setDatas(bannerBeanList)
                }

                UPDATE_RANK->{
                    updateRank(msg.obj as List<SheetPlayBean>)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFindingBinding.inflate(layoutInflater)
        initBanner()
        initRecycler()
        return binding.root
    }

    private fun initBanner(){
        binding.findingBanner
            .isAutoLoop(true)
            .setLoopTime(3000)
            .setScrollTime(800)
            .addBannerLifecycleObserver(this)
            .setIndicator(CircleIndicator(context))
            .setAdapter(FBannerAdapter(bannerBeanList))
            .setBannerRound(10f)
            .start()

    }

    private fun initRecycler(){
        binding.officialRecycler.layoutManager = LinearLayoutManager(activity)
        officialAdapter = RankOfficialAdapter(officialBeanList)
        binding.officialRecycler.adapter = officialAdapter

        binding.selectRecycler.layoutManager = GridLayoutManager(activity,3)
        selectAdapter = RankSelectAdapter(selectBeanList)
        binding.selectRecycler.adapter = selectAdapter

        officialAdapter.setItemClickListener(object : RankOfficialAdapter.OnOfficialRankClickListener{
            override fun onClickListener(bean:SheetPlayBean) {
                val intent: Intent = Intent(activity, SongListActivity::class.java)
                intent.putExtra(CacheKeyParam.songListId,"${bean.id}")
                intent.putExtra(CacheKeyParam.songListName,bean.name)
                intent.putExtra(CacheKeyParam.songListCover,bean.coverImgUrl)
                startActivity(intent)
            }
        })

        selectAdapter.setItemClickListener(object : RankSelectAdapter.OnSelectRankClickListener{
            override fun onClickListener(bean:SheetPlayBean) {
                val intent: Intent = Intent(activity, SongListActivity::class.java)
                intent.putExtra(CacheKeyParam.songListId,"${bean.id}")
                intent.putExtra(CacheKeyParam.songListName,bean.name)
                intent.putExtra(CacheKeyParam.songListCover,bean.coverImgUrl)
                startActivity(intent)
            }
        })

        binding.findingOpenSide.setOnClickListener {
            EventBus.getDefault().postSticky(DrawerBean(true))
        }
    }

    /**
     * 获取banner信息*/
    private fun getBannerInfo(){
        val url = HttpUtil.getBannerURL()

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG,json.toString())
                val array = JSONObject(json.toString()).getJSONArray("banners")
                val result = HttpUtil.fromListJson(array.toString(),BannerBean::class.java)
                val message = Message()
                message.what = UPDATE_BANNER
                message.obj = result
                handler.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
            }
        })
    }

    /**
     * 获取排行榜信息*/
    private fun getRankInfo(){
        val url = HttpUtil.getTopRankURL()

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG,json.toString())
                val array = JSONObject(json.toString()).getJSONArray("list")
                val result = HttpUtil.fromListJson(array.toString(),SheetPlayBean::class.java)
                val message = Message()
                message.what = UPDATE_RANK
                message.obj = result
                handler.sendMessage(message)

            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
            }
        })
    }

    private fun updateRank(rankList: List<SheetPlayBean>){
        for (rank in rankList){
            if (rank.tracks.size() == 0){
                //精选榜
                selectBeanList.add(rank)
            }else{
                //官网榜
                officialBeanList.add(rank)
            }
        }

        officialAdapter.notifyDataSetChanged()
        selectAdapter.notifyDataSetChanged()
        binding.loading.hide()
    }
    /**
     * 网络请求放这里*/
    override fun initLazyFunc() {
        binding.loading.show()
        getBannerInfo()
        getRankInfo()
    }
}