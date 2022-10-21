package com.franz.easymusicplayer.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.base.BasePagerAdapter
import com.franz.easymusicplayer.bean.DrawerBean
import com.franz.easymusicplayer.bean.PlayProgressBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.binder.DownloadBinder
import com.franz.easymusicplayer.binder.MusicBinder
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.*

import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.communityFragment.CommunityFragment
import com.franz.easymusicplayer.ui.findingFragment.FindingFragment
import com.franz.easymusicplayer.ui.mineFragment.MineFragment
import com.franz.easymusicplayer.ui.personInfo.PersonInfoActivity
import com.franz.easymusicplayer.ui.searchFragment.SearchFragment
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.SPUtil
import com.franz.easymusicplayer.utils.StatusBarUtil
import com.franz.easymusicplayer.utils.SystemParamUtil
import com.franz.easymusicplayer.viewmodel.UserInfoViewModel
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import jp.wasabeef.glide.transformations.BlurTransformation
import me.wcy.lrcview.LrcView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class HomePageActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener,View.OnClickListener,ViewPager.OnPageChangeListener {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var sideNaviBinding: SideNaviHeaderBinding
    private lateinit var musicPlayerBinding: MusicPlayLayoutBinding
    private lateinit var musicDetailBinding: PlayDetailLayoutBinding
    private lateinit var coverBinding : ItemPlayCoverBinding
    private lateinit var lyricBinding : ItemPlayLyricBinding


    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentTransaction: FragmentTransaction

    private var currentFragment: Fragment? = null
    private var searchFragment: SearchFragment? = null
    private var communityFragment: CommunityFragment? = null
    private var mineFragment: MineFragment? = null
    private var findingFragment: FindingFragment? = null

    private val viewsList = arrayListOf<View>()
    private val titlesList = arrayListOf<String>()

    private val TAG: String = "HomePageActivityLog"
    private val UPDATEINFO: Int = 1
    private var oldPos: Int = 0

    private val second: Long = 1000
    private val min: Long = 1000 * 60

    /**
     * 动态申请权限*/
    private var Permission: Array<String> = arrayOf<String>(
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val handler = object : Handler(Looper.myLooper()!!){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                UPDATEINFO-> {
                    updateDetailProgress(msg.obj as PlayProgressBean)
                }
            }
        }
    }

    companion object{
        @JvmStatic
        lateinit var MA: Activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatusBarUtil.setStatusBarHide(window)
        StatusBarUtil.setStatusBarLightMode(window)
        StatusBarUtil.setStatusBarColor(window,this,R.color.color_system_bg)

        binding = ActivityHomePageBinding.inflate(layoutInflater)
        sideNaviBinding = SideNaviHeaderBinding.bind(binding.sideNavi.getHeaderView(0))
        musicPlayerBinding = binding.musicPlayer
        musicDetailBinding = binding.musicDetail
        setContentView(binding.root)

        requestPermissions()

        MA = this
        BaseApplication.activityList.add(this)

        initBottomNavi()
        initFragment(savedInstanceState)
        initListener()
        initViewPager()
        initIndicator()
        initLyric()
        getCacheMusic()
        initNavListener()
        updateSideInfo()
        resumeDownload()

        /**
         * 初始化*/
        SystemParamUtil.getSystemParam(this)
    }

    /**
     * 获取上次播放的歌曲信息，
     * 例如播放到偏爱时，退出app,加入缓存，下次进入app，展示记录*/
    private fun getCacheMusic(){
        val order = SPUtil.getInstance().GetData(this,CacheKeyParam.recordId,-1) as Int
        val jsonBean = SPUtil.getInstance().GetData(this,CacheKeyParam.recordBean,"")
        if (order == -1)return
        MusicBinder.curSongsCur = order - 1
        val jsonObject = JSONObject(jsonBean.toString())
        val bean:SongBean = HttpUtil.fromJson(jsonObject.toString(),SongBean::class.java)
        updateDetailInfo(bean)
    }
    private fun initBottomNavi() {
        binding.bottomNavi.itemIconTintList = null
        binding.bottomNavi.setOnItemSelectedListener(this)
    }

    /**
     * 初始化中间的view,包括图片和歌词两部分*/
    private fun initViewPager(){
        val coverView: View = LayoutInflater.from(this).inflate(R.layout.item_play_cover, null)
        val lyricView: View = LayoutInflater.from(this).inflate(R.layout.item_play_lyric, null)

        coverBinding = ItemPlayCoverBinding.bind(coverView)
        lyricBinding = ItemPlayLyricBinding.bind(lyricView)

        viewsList.add(coverView)
        viewsList.add(lyricView)
        titlesList.add("封面")
        titlesList.add("歌词")

        val adapter = BasePagerAdapter(viewsList, titlesList)
        musicDetailBinding.playViewPager.adapter = adapter

        musicDetailBinding.playViewPager.addOnPageChangeListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.bottom_navi_mine -> {
            if (mineFragment == null) {
                mineFragment = MineFragment()
            }
            switchContent(currentFragment, mineFragment)
            true
        }
        R.id.bottom_navi_search -> {
            if (searchFragment == null) {
                searchFragment = SearchFragment()
            }
            switchContent(currentFragment, searchFragment)
            true
        }
        R.id.bottom_navi_finding -> {
            if (findingFragment == null) {
                findingFragment = FindingFragment()
            }
            switchContent(currentFragment, findingFragment)
            true
        }
        R.id.bottom_navi_community -> {
            if (communityFragment == null) {
                communityFragment = CommunityFragment()
            }
            switchContent(currentFragment, communityFragment)
            true
        }
        else -> {
            false
        }
    }

    /**
     * 侧边栏各项点击事件*/
    private fun initNavListener(){
        /**
         * 顶部header点击事件*/
        sideNaviBinding.sideNavUserImg.setOnClickListener {
            startActivity(Intent(this,PersonInfoActivity::class.java))
        }

        /**
         * 底部content点击事件*/
        binding.sideNavi.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when(item.itemId){

                }
                return false
            }
        })
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        //判断activity是否重建，如果不是，则不需要重新建立fragment.
        if (savedInstanceState == null) {
            fragmentManager = supportFragmentManager
            fragmentTransaction = fragmentManager.beginTransaction()
            if (mineFragment == null) {
                mineFragment = MineFragment()
            }
            currentFragment = mineFragment
            fragmentTransaction.replace(R.id.contentFragment, mineFragment!!)
                .commit() //fragment parent layout id
            /**
             * 动态设置默认启动fragment*/
            binding.bottomNavi.selectedItemId = binding.bottomNavi.menu[2].itemId
        }
    }


    private fun switchContent(from: Fragment?, to: Fragment?) {
        if (from == null || to == null) return
        if (currentFragment != to) {
            currentFragment = to
            fragmentManager = supportFragmentManager
            fragmentTransaction = fragmentManager.beginTransaction()
            if (!to.isAdded)
                fragmentTransaction.hide(from).add(R.id.contentFragment, to).commit()
            else
                fragmentTransaction.hide(from).show(to).commit()
        }
    }

    /**
     * 初始化指示器个数 */
    private fun initIndicator() {
        for (i in 1..viewsList.size) {
            val view = View(this)
            view.setBackgroundResource(R.drawable.selector_indicator)
            view.isEnabled = false
            val params = LinearLayout.LayoutParams(30, 30)
            params.rightMargin = 15
            params.leftMargin = 15
            musicDetailBinding.indicatorLayout.addView(view, params)
        }
        /**
         * 默认第0页面与指示器绑定 */
        musicDetailBinding.indicatorLayout.getChildAt(oldPos).isEnabled = true
    }

    /**
     * 随着viewpager切换，同步更新底部指示器 */
    private fun updateIndicator(newPos: Int) {
        musicDetailBinding.indicatorLayout.getChildAt(oldPos).isEnabled = false
        musicDetailBinding.indicatorLayout.getChildAt(newPos).isEnabled = true
        oldPos = newPos
    }

    /**
     * 打开或者关闭侧边栏*/
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
     fun onEvent(bean: DrawerBean){
        if (bean.isOpen)
            binding.drawerLayout.openDrawer(GravityCompat.START)
        else
            binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    /**
     * 更新侧边栏用户信息*/
//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
//    fun onEvent(bean: SimpleAccountInfo){
//        Glide.with( sideNaviBinding.sideNavUserImg)
//            .asDrawable()
//            .load(bean.userImg)
//            .placeholder(R.drawable.icon_default_user_img)
//            .dontAnimate()
//            .into( sideNaviBinding.sideNavUserImg)
//
//        sideNaviBinding.sideNavUserName.text = bean.userName
//    }

    /**
     * * 更新侧边栏用户信息*/
    private fun updateSideInfo(){
        UserInfoViewModel.getUserInfo().observe(this, Observer{
            Glide.with( sideNaviBinding.sideNavUserImg)
                .asDrawable()
                .load(it.avatarUrl)
                .placeholder(R.drawable.icon_default_user_img)
                .dontAnimate()
                .into( sideNaviBinding.sideNavUserImg)

            sideNaviBinding.sideNavUserName.text = it.nickname
        })
    }

    /**
     * 更新当前播放歌曲信息和状态*/
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(bean: SongBean){
        updateDetailInfo(bean)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(bean: PlayProgressBean){
        if (bean.isStart)
            start()
        else
            pause()
    }

    /**
     * 更新音乐详情信息*/
    private fun updateDetailInfo(bean: SongBean){
        if (this.isFinishing)return

        Glide.with(musicPlayerBinding.songsCover)
            .asDrawable()
            .load(bean.albumCover)
            .transform(GlideRoundTransform(BaseApplication.context, 10))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(musicPlayerBinding.songsCover)

        musicPlayerBinding.songsName.text = bean.songName
        musicPlayerBinding.singerName.text = bean.singerName

        Glide.with(coverBinding.playCover)
            .asDrawable()
            .load(bean.albumCover)
            .transform(GlideRoundTransform(BaseApplication.context, 10))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(coverBinding.playCover)

        Glide.with(musicDetailBinding.bigCover)
            .asDrawable()
            .load(bean.albumCover)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(50)))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(musicDetailBinding.bigCover)

        musicDetailBinding.detailSongName.text = bean.songName
        musicDetailBinding.detailSingerName.text = bean.singerName

        /**
         * 获取系统歌词*/
        getMusicLyric(bean.songId.toString())
    }
    private fun updateDetailProgress(bean: PlayProgressBean){
        lyricBinding.mLrcView.updateTime(bean.current)
        musicDetailBinding.startTime.text = millToDate(bean.current)
        musicDetailBinding.endTime.text = millToDate(bean.duration)
        val pre = (bean.current*1.0) / (bean.duration) * 100
        musicDetailBinding.musicProgress.progress = pre.toInt()

        if (bean.isStart){
            start()
        }else{
           pause()
        }
    }

    private fun initListener(){
        musicPlayerBinding.playSongs.setOnClickListener(this)
        musicPlayerBinding.nextSongs.setOnClickListener(this)
        musicPlayerBinding.previousSongs.setOnClickListener(this)
        musicPlayerBinding.songsCover.setOnClickListener(this)
        musicPlayerBinding.songInfoLayout.setOnClickListener(this)
        musicDetailBinding.startPlay.setOnClickListener(this)
        musicDetailBinding.detailNext.setOnClickListener(this)
        musicDetailBinding.detailPrevious.setOnClickListener(this)
        musicPlayerBinding.songsCover.setOnClickListener(this)
        musicDetailBinding.detailExit.setOnClickListener(this)


        /**
         * 音乐详情滑动监听*/
        binding.slidUpLayout.isTouchEnabled = false
        binding.slidUpLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener{
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                //Log.d(TAG,"slideOffset= $slideOffset")
            }

            override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
                Log.d(TAG,"previousState= $previousState ,newState = $newState ")
                when(newState){
                    SlidingUpPanelLayout.PanelState.COLLAPSED-> {
                        //折叠
                        musicPlayerBinding.miniPlayLayout.visibility = View.VISIBLE
                        binding.bottomNavi.visibility = View.VISIBLE
                    }

                    SlidingUpPanelLayout.PanelState.DRAGGING-> {
                        //拖拽中
                        musicPlayerBinding.miniPlayLayout.visibility = View.INVISIBLE
                        binding.bottomNavi.visibility = View.INVISIBLE
                    }
                    SlidingUpPanelLayout.PanelState.EXPANDED-> {
                        //展开
                        musicPlayerBinding.miniPlayLayout.visibility = View.GONE
                        binding.bottomNavi.visibility = View.GONE
                    }
                }
            }
        })

        /**
         * 播放歌曲实时进度回调*/
        MusicBinder.setProgressListener(object : MusicBinder.ProgressStatus{
            override fun getCurrentProgress(isStart: Boolean, current: Long, duration: Long) {
                val message = Message()
                message.what = UPDATEINFO
                message.obj = PlayProgressBean(isStart,current,duration)
                handler.sendMessage(message)
            }
        })

        musicDetailBinding.musicProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d(TAG,"progress= $progress")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d(TAG,"progress= ${seekBar?.progress}")
                val progress: Long = (MusicBinder.duration() * seekBar?.progress!! / 100.0).toLong()
                MusicBinder.seek(progress)
            }
        })
    }

    override fun onClick(v: View?) = when(v?.id){
        R.id.playSongs-> {
            if (MusicBinder.isPlay){
                MusicBinder.pause()
                pause()
            }
            else{
                MusicBinder.play()
                start()
            }
        }

        R.id.startPlay-> {
            if (MusicBinder.isPlay){
                MusicBinder.pause()
                pause()
            }
            else{
                MusicBinder.play()
                start()
            }
        }

        R.id.nextSongs-> MusicBinder.next(MusicBinder.curSongsCur + 1)
        R.id.previousSongs-> MusicBinder.previous(MusicBinder.curSongsCur - 1)

        R.id.detailPrevious-> MusicBinder.previous(MusicBinder.curSongsCur - 1)
        R.id.detailNext-> MusicBinder.next(MusicBinder.curSongsCur + 1)
        R.id.songsCover-> controlPanel()
        R.id.detailExit-> controlPanel()
        else-> {}
    }

     private fun controlPanel(){
        if (binding.slidUpLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED){
            /**
             * 如果是展开则关闭*/
            binding.slidUpLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }else{
            /**
             * 反之，开启*/
            binding.slidUpLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
    }

    private fun start(){
        musicDetailBinding.startPlay.setImageResource(R.drawable.icon_start_play)
        musicPlayerBinding.playSongs.setImageResource(R.drawable.icon_start_play_black)
    }

    private fun pause(){
        musicDetailBinding.startPlay.setImageResource(R.drawable.icon_pause_play)
        musicPlayerBinding.playSongs.setImageResource(R.drawable.icon_pause_play_black)
    }
    /**
     * 将毫秒级进度转化为00:00格式*/
    private fun figureProgress(progress: Long): String{
        val builder = StringBuilder()
        val m = progress / min
        val s = (progress - m*min) / second

        if (m >= 10)
            builder.append("$m:")
       else
            builder.append("0$m:")

        if (s >= 10)
            builder.append("$s")
        else
            builder.append("0$s")
        return builder.toString()
    }


    private fun millToDate(time: Long): String{
        val date = Date(time)
        val format = SimpleDateFormat("mm:ss", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC+8");
        return format.format(date)
    }

    /**
     * 获取歌曲歌词*/
    private fun getMusicLyric(id: String){
        val url = HttpUtil.getLyricURL(id)

        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG,json.toString())
                val lyc = JSONObject(json.toString()).getJSONObject("lrc").getString("lyric").toString()
                loadLyric(lyc)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG,e.toString())
            }

        })
    }

    /**
     * 加载歌词*/
    private fun loadLyric(lyc: String){
        lyricBinding.mLrcView.loadLrc(lyc)
    }

    /**
     * 滑动歌词，同步歌曲*/
    private fun initLyric(){
        lyricBinding.mLrcView.setDraggable(true,object : LrcView.OnPlayClickListener{
            override fun onPlayClick(view: LrcView?, time: Long): Boolean {
                MusicBinder.seek(time)
                return true
            }
        })

    }
    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        musicDetailBinding.indicatorLayout.getChildAt(oldPos).isEnabled = false
        musicDetailBinding.indicatorLayout.getChildAt(position).isEnabled = true
        oldPos = position
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (str in Permission) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(Permission, 200)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("DownloadService", "permission success")
            } else {
                Log.d("DownloadService", "permission fail")
            }
        }
    }

    /**
     * 如果存在没有下载完的，强制退出后，断电重新下载*/
    private fun resumeDownload(){
        val list = PlayListDataBase.getDBInstance().downloadDao().getAll()
        if (list.size == 0)return
        for (i in list.indices){
            if (!list[i].complete){
                DownloadBinder.downloadList.add(list[i])
            }
        }
        DownloadBinder.download()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        for (activity in BaseApplication.activityList)
            activity.finish()
    }

}


