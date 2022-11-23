package com.franz.easymusicplayer.ui.mineFragment

import android.content.Context
import android.content.Intent
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.adapter.SongListAdapter
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.base.BasePagerAdapter
import com.franz.easymusicplayer.base.LazyFragment
import com.franz.easymusicplayer.bean.*
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.*
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.ui.download.DownloadActivity
import com.franz.easymusicplayer.ui.playSheet.PlaySheetActivity
import com.franz.easymusicplayer.ui.recently.RecentlyActivity
import com.franz.easymusicplayer.ui.songList.SongListActivity
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.SPUtil
import com.franz.easymusicplayer.utils.ToastUtil
import com.franz.easymusicplayer.viewmodel.UserInfoViewModel
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

import kotlin.collections.ArrayList


class MineFragment : LazyFragment(),View.OnClickListener{
    private lateinit var binding: FragmentMineBinding
    private val viewsList = arrayListOf<View>()
    private val titlesList = arrayListOf<String>()

    private lateinit var createSongBinding : ItemMineCreateSonglistBinding
    private lateinit var favoriteSongBinding : ItemMineFavoriteSonglistBinding
    private lateinit var createAdapter: SongListAdapter
    private lateinit var favoriteAdapter: SongListAdapter
    private val createBeanList: MutableList<SongListBean> = ArrayList()
    private val favoriteBeanList: MutableList<SongListBean> = ArrayList()

    /**
     * 喜欢的歌单数据*/
    private lateinit var loveSongPlay: SongListBean

    private val TAG: String = "MineFragmentLog"
    private val ACCOUNTINFO: Int = 1
    private val SONGLIST: Int = 2

    private lateinit var songPlayList: List<SongListBean>

    private val handler = object : Handler(Looper.myLooper()!!){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                ACCOUNTINFO-> {

                }

                SONGLIST-> {
                    songPlayList = msg.obj as List<SongListBean>
                    updateUserSongList()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMineBinding.inflate(layoutInflater)
        initListener()
        initTabLayout()
        return binding.root
    }

    private fun initListener(){
        binding.likeLayout.setOnClickListener(this)
        binding.openSideNavi.setOnClickListener(this)
        binding.searchFunc.setOnClickListener(this)
        binding.minePlayList.setOnClickListener(this)
        binding.mineRecently.setOnClickListener(this)
        binding.mineDownload.setOnClickListener(this)
    }
    /**
     * 初始化创建歌单和收藏歌单tab和适配器*/
    private fun initTabLayout(){

        val createListView: View = LayoutInflater.from(activity).inflate(R.layout.item_mine_create_songlist, null)
        val favoriteListView: View = LayoutInflater.from(activity).inflate(R.layout.item_mine_favorite_songlist, null)

        val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)

        createSongBinding = ItemMineCreateSonglistBinding.bind(createListView)
        favoriteSongBinding = ItemMineFavoriteSonglistBinding.bind(favoriteListView)

//        createSongBinding.createRecycler.isNestedScrollingEnabled = false
//        favoriteSongBinding.favoriteRecycler.isNestedScrollingEnabled = false


        viewsList.let {
            it.add(createListView)
            it.add(favoriteListView)
        }
        titlesList.let {
            it.add("创建歌单")
            it.add("收藏歌单")
        }


        val adapter = BasePagerAdapter(viewsList, titlesList)
        for (title in titlesList) {
            binding.mineTabTitle.addTab(binding.mineTabTitle.newTab().setText(title))
        }
        binding.mineTabTitle.setupWithViewPager(binding.mineViewPager)
        binding.mineViewPager.adapter = adapter

        binding.mineViewPager.offscreenPageLimit = adapter.count

        val createManager: LinearLayoutManager = LinearLayoutManager(activity)
        createManager.isSmoothScrollbarEnabled = true
        createManager.isAutoMeasureEnabled = true
        createSongBinding.createRecycler.layoutManager = createManager
        createAdapter = SongListAdapter(createBeanList)
        createSongBinding.createRecycler.adapter = createAdapter

        val favoriteManager: LinearLayoutManager = LinearLayoutManager(activity)
        favoriteManager.isSmoothScrollbarEnabled = true
        favoriteManager.isAutoMeasureEnabled = true
        favoriteSongBinding.favoriteRecycler.layoutManager = favoriteManager
        favoriteAdapter = SongListAdapter(favoriteBeanList)
        favoriteSongBinding.favoriteRecycler.adapter = favoriteAdapter


        createAdapter.setItemClickListener(object: SongListAdapter.OnSongsClickListener{
            override fun onClickListener(bean:SongListBean) {
                if (createBeanList.size == 0) return

                val intent: Intent = Intent(activity,SongListActivity::class.java)
                intent.putExtra(CacheKeyParam.songListId,bean.id.toString())
                intent.putExtra(CacheKeyParam.songListName,bean.name)
                intent.putExtra(CacheKeyParam.songListCover,bean.coverImgUrl)
                startActivity(intent)
            }
        })

        favoriteAdapter.setItemClickListener(object: SongListAdapter.OnSongsClickListener{
            override fun onClickListener(bean:SongListBean) {
                if (favoriteBeanList.size == 0) return

                val intent: Intent = Intent(activity,SongListActivity::class.java)
                intent.putExtra(CacheKeyParam.songListId,bean.id.toString())
                intent.putExtra(CacheKeyParam.songListName,bean.name)
                intent.putExtra(CacheKeyParam.songListCover,bean.coverImgUrl)
                startActivity(intent)
            }
        })


        binding.mineViewPager.initIndexList(adapter.count)
        binding.mineViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                binding.mineViewPager.updateHeight(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })

    }

    /**
     * 更新用户信息,UI*/
    private fun updateAccountInfo(){
        UserInfoViewModel.getUserInfo().observe(this, Observer {
            Log.d(TAG,"${it.level}")

            Glide.with(binding.userImage)
                .asDrawable()
                .load(it.avatarUrl)
                .placeholder(R.drawable.icon_default_user_img)
                .dontAnimate()
                .into(binding.userImage)

            binding.userName.text = it.nickname

           // EventBus.getDefault().postSticky(SimpleAccountInfo(it.avatarUrl,it.nickname))
        })
    }

    /**
     * 更新用户喜欢、创建和收藏的歌单*/
    private fun updateUserSongList(){
        /**
         * 喜欢的歌单名称由用户名称+喜欢的音乐组成*/
        //孙正义的网易云Music账号喜欢的音乐
        val loveListName = "${UserInfoViewModel.getUserInfo().value?.nickname}喜欢的音乐"

        for (songBean in songPlayList ){
            val jsonObject = JSONObject(songBean.creator.toString())
            val id = jsonObject.getString("userId")

            if (songBean.name.equals(loveListName)){
                loveSongPlay = songBean

                Glide.with( binding.likeSons)
                    .asDrawable()
                    .load(songBean.coverImgUrl)
                    .transform(GlideRoundTransform(context,10))
                    .placeholder(R.drawable.icon_my_like)
                    .dontAnimate()
                    .into( binding.likeSons)

                binding.likeSonsNum.text = "${songBean.trackCount}首"

                if (TextUtils.isEmpty(BaseApplication.userId))BaseApplication.userId = id
                if (BaseApplication.userId != id)BaseApplication.userId = id

            } else {
                BaseApplication.userId?.let {
                    if (BaseApplication.userId == id) createBeanList.add(songBean)
                    else favoriteBeanList.add(songBean)
                }
            }
        }

        createSongBinding.createListNum.text = "创建歌单(${createBeanList.size}个)"
        favoriteSongBinding.favoriteListNum.text = "收藏歌单(${favoriteBeanList.size}个)"

        createAdapter.notifyDataSetChanged()
        favoriteAdapter.notifyDataSetChanged()
    }

    /**
     * 判断是否存在缓冲cookie，然后决定是否获取用户信息*/
    private fun initAccountInfo(){
        if (TextUtils.isEmpty(BaseApplication.userId)){
            SPUtil.getInstance().PutData(context,CacheKeyParam.cookieKey,"")
            ToastUtil.setFailToast(requireActivity(), "请重新登录(warn:QR code login has been abandoned)")
            return
        }

        val url = HttpUtil.getUserDetailURL(BaseApplication.userId)

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG,json.toString())
                val level = JSONObject(json.toString()).getInt("level")
                val profile: JSONObject = JSONObject(json.toString()).getJSONObject("profile")
                val userInfo = HttpUtil.fromJson(profile.toString(),AccountInfoBean::class.java)
                userInfo.level = level

                UserInfoViewModel.postValue(userInfo)
                getUserSongList(userInfo.userId.toString())
            }

            override fun onFailed(e: String?) {
               Log.d(TAG,e.toString())
                /**
                 * 如果返回错误信息，将缓冲cookie清空，防止后续进入持续bug*/
                SPUtil.getInstance().PutData(context,CacheKeyParam.cookieKey,"")
                ToastUtil.setFailToast(activity!!, "请重新登录,${e.toString()}")
            }
        })
    }

    /**
     * 获取用户所有歌单信息*/
    private fun getUserSongList(userID: String){
        val url = HttpUtil.getUserAllSongListURL(userID)
        Log.d(TAG,"url=$url")

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG,json.toString())
                val playlist = JSONObject(json.toString()).getJSONArray("playlist")
                val songList = HttpUtil.fromListJson(playlist.toString(),SongListBean::class.java)
                val message = Message()
                message.what = SONGLIST
                message.obj = songList
                handler.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
                ToastUtil.setFailToast(activity!!, e.toString())
            }

        })
    }

    private fun getAccountInfo(){
        val cookie: String = SPUtil.getInstance().GetData(context,CacheKeyParam.cookieKey,"") as String
        if (TextUtils.isEmpty(cookie))return

        Log.d(TAG,"cookie=$cookie")
        val url = HttpUtil.getSubAccountInfoURL(cookie)

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG,json.toString())
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
                ToastUtil.setFailToast(activity!!, "请重新登录,${e.toString()}")
            }
        })
    }

    /**
     * 获取用户等级*/
    private fun getUserLevel(){
        val url = HttpUtil.getUserLevelURL()

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG,json.toString())
                val level = JSONObject(json.toString()).getJSONObject("data").getInt("level")
//                val user = UserInfoViewModel.getUserInfo().value
//                user?.level = level
//                user?.let { UserInfoViewModel.postValue(it) }
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
                ToastUtil.setFailToast(activity!!, "请重新登录,${e.toString()}")
            }
        })
    }

    /**
     * 网络请求放这里*/
    override fun initLazyFunc() {
        initAccountInfo()
        //getUserLevel()
        updateAccountInfo()
    }

    override fun onClick(v: View?) = when(v?.id){
        R.id.likeLayout-> {
            loveSongPlay?.let {
                Log.d(TAG,"歌单id=${it.id}")
                val intent: Intent = Intent(activity,SongListActivity::class.java)
                intent.putExtra(CacheKeyParam.songListId,it.id.toString())
                intent.putExtra(CacheKeyParam.songListName,it.name)
                intent.putExtra(CacheKeyParam.songListCover,it.coverImgUrl)
                startActivity(intent)
            }
        }

        R.id.openSideNavi->{
           EventBus.getDefault().postSticky(DrawerBean(true))
        }

        R.id.searchFunc-> {
            //SPUtil.getInstance().PutData(context, CacheKeyParam.cookieKey, "")
        }
        R.id.minePlayList-> startActivity(Intent(activity,PlaySheetActivity::class.java))
        R.id.mineRecently-> startActivity(Intent(activity,RecentlyActivity::class.java))
        R.id.mineDownload-> startActivity(Intent(activity,DownloadActivity::class.java))
        else-> {}
    }
}

