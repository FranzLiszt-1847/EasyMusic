package com.franz.easymusicplayer.ui.logIn

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.base.BasePagerAdapter
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.callback.IVerifyCodeInfo
import com.franz.easymusicplayer.databinding.*
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.ui.HomePageActivity
import com.franz.easymusicplayer.ui.mineFragment.MineFragment
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.SPUtil
import com.franz.easymusicplayer.utils.StatusBarUtil
import com.franz.easymusicplayer.utils.ToastUtil
import org.json.JSONObject
import org.w3c.dom.Text
import java.sql.Time
import java.util.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG: String = "LoginActivityLog"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var titleBarBinding: TitleBarBinding
    private lateinit var loginAdapter: BasePagerAdapter
    private lateinit var passwordBinding: ItemPasswordLoginBinding
    private lateinit var identifyBinding: ItemCodeLoginBinding
    private lateinit var qrCodeBinding: ItemQrcodeLoginBinding
    private val viewsList = arrayListOf<View>()
    private val titlesList = arrayListOf<String>()
    private var codeCount = 60
    private var qrCodeCount = 10
    private lateinit var qrCodeKey: String
    private val qrCodeTimer: Timer = Timer()

    private val handler =  Handler()

    /**
     * 获取验证码60s倒计时*/
    private val runnable = object : Runnable {
        override fun run() {
            codeCount--
            if (codeCount > 0) {
                identifyBinding.identifyCode.text = "$codeCount s"
                identifyBinding.identifyCode.isEnabled = false
                handler.postDelayed(this, 1000)
            }else{
                identifyBinding.identifyCode.text = "获取验证码"
                identifyBinding.identifyCode.isEnabled = true
                codeCount = 60
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarHide(window)
        StatusBarUtil.setStatusBarLightMode(window)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        titleBarBinding = binding.titleBar
        setContentView(binding.root)

        BaseApplication.activityList.add(this)
        initTitleBar()
        initTabLayout()
        initLoginListener()
        initEditListener()
    }

    /**
     * 初始化tabLayout和viewPager，添加两种登录视图
     * 并绑定binding*/
    private fun initTabLayout() {
        val passwordView: View = LayoutInflater.from(this).inflate(R.layout.item_password_login, null)
        val identifyView: View = LayoutInflater.from(this).inflate(R.layout.item_code_login, null)
        val qrCodeView: View = LayoutInflater.from(this).inflate(R.layout.item_qrcode_login, null)

        passwordBinding = ItemPasswordLoginBinding.bind(passwordView)
        identifyBinding = ItemCodeLoginBinding.bind(identifyView)
        qrCodeBinding = ItemQrcodeLoginBinding.bind(qrCodeView)

        viewsList.let {
            it.add(passwordView)
            it.add(identifyView)
            it.add(qrCodeView)
        }
        titlesList.let {
            it.add("密码登录")
            it.add("手机登录")
            it.add("二维码登录")
        }
        loginAdapter = BasePagerAdapter(viewsList, titlesList)
        for (title in titlesList) {
            binding.tabTitle.addTab(binding.tabTitle.newTab().setText(title))
        }
        binding.tabTitle.setupWithViewPager(binding.viewPager)
        binding.viewPager.adapter = loginAdapter
    }

    /**
     * 初始化登录界面的标题栏*/
    private fun initTitleBar() {
        titleBarBinding.majorTitle.visibility = View.INVISIBLE
        titleBarBinding.subTitle.visibility = View.INVISIBLE

        titleBarBinding.exit.setOnClickListener { finish() }
    }

    /**
     * 账号密码登录*/
    private fun passwordLogin() {
        val phone = passwordBinding.PassUser.text.toString()
        val password = passwordBinding.PassPassword.text.toString()
        val check = passwordBinding.PassPrivacy.isChecked

        val result: Boolean = phoneJudge(phone)
        if (!result) return

        if (TextUtils.isEmpty(password)) {
            ToastUtil.setFailToast(this, "密码不能为空!")
            return
        }
        if (!check) {
            ToastUtil.setFailToast(this, "请先勾选隐私权限!")
            return
        }

        val url = HttpUtil.getLoginURL(phone, password)
        Log.d(TAG, "URL:$url")

        HttpUtil.getLoginInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, "登陆成功数据:$json")
                ToastUtil.setSuccessToast(this@LoginActivity, "登陆成功!")
                val cookie: String = JSONObject(json.toString()).getString("cookie")
                val id: String = JSONObject(json.toString()).getJSONObject("account").getLong("id").toString()
                SPUtil.getInstance().PutData(applicationContext,CacheKeyParam.cookieKey,cookie)
                SPUtil.getInstance().PutData(applicationContext,CacheKeyParam.UserId,id)
                BaseApplication.userId = id
                BaseApplication.cookie = cookie
                startActivity(Intent(this@LoginActivity,HomePageActivity::class.java))
                finish()
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "登陆失败提示:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * 手机验证码登陆*/
    private fun identifyLogin(phone: String, verify: String) {
        val url = HttpUtil.getVerifyLoginURL(phone, verify)
        HttpUtil.getLoginInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, "登陆成功数据:$json")
                ToastUtil.setSuccessToast(this@LoginActivity, "登陆成功!")
                val cookie: String = JSONObject(json.toString()).getString("cookie")
                val id: String = JSONObject(json.toString()).getJSONObject("account").getLong("id").toString()
                SPUtil.getInstance().PutData(applicationContext,CacheKeyParam.cookieKey,cookie)
                SPUtil.getInstance().PutData(applicationContext,CacheKeyParam.UserId,id)
                BaseApplication.userId = id
                BaseApplication.cookie = cookie
                startActivity(Intent(this@LoginActivity,HomePageActivity::class.java))
                finish()
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "登陆失败提示:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * 校验验证码*/
    private fun checkVerifyCode() {
        val phone = identifyBinding.identifyUser.text.toString()
        val check = identifyBinding.identifyPrivacy.isChecked
        val verify = identifyBinding.identifyPassword.text.toString().trim()
        val result: Boolean = phoneJudge(phone)
        if (!result) return


        if (TextUtils.isEmpty(verify)) {
            ToastUtil.setFailToast(this, "验证码不能为空")
            return
        }

        if (!check) {
            ToastUtil.setFailToast(this, "请先勾选隐私权限!")
            return
        }

        /**
         * 对填入的验证码进行校验*/
        HttpUtil.getVerifyCodeInfo(HttpUtil.getVerifyResultURL(phone, verify),
            object : IVerifyCodeInfo {
                override fun onRespond(flag: Boolean) {
                    Log.d(TAG, "验证码校验结果:$flag")
                    if (flag) identifyLogin(phone, verify)

                }

                override fun onFailed(e: String?) {
                    Log.d(TAG, "fail验证码校验结果:$e")
                    ToastUtil.setFailToast(this@LoginActivity, e.toString())
                }
            })
    }

    /**
     * 点击获取验证码按钮处理逻辑*/
    private fun verifyHandle() {
        val phone = identifyBinding.identifyUser.text.toString()
        val result: Boolean = phoneJudge(phone)
        if (!result) return

        HttpUtil.getVerifyCodeInfo(HttpUtil.getVerifyCodeURL(phone), object : IVerifyCodeInfo {
            override fun onRespond(flag: Boolean) {
                Log.d(TAG, "获取验证码结果:$flag")
                if (flag) ToastUtil.setSuccessToast(this@LoginActivity, "获取验证码成功!")
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "fail获取验证码结果:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * 获取二维码key*/
    private fun getQRCodeKey(){
        val url = HttpUtil.getQRCodeKeyURL()
        Log.d(TAG, "二维码key URL:$url")

        HttpUtil.getQRCodeKeyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                json?.let {
                    qrCodeKey = json
                    getQRCodeCreate(json)
                    ToastUtil.setSuccessToast(this@LoginActivity, "获取二维码key成功!")
                }
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "fail获取二维码key结果:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * 获取二维码图片*/
    private fun getQRCodeCreate(key: String){
        val url = HttpUtil.getQRCodeCreateURL(key)
        Log.d(TAG, "二维码create URL:$url")

        HttpUtil.getQRCodeCreateInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                json?.let {
                    Log.d(TAG, "base qr code:$json")
                    base64ToBitmap(json)
                    /**
                     * 延迟0.5s执行定时器，然后每隔1秒执行一次*/
                    qrCodeTimer.schedule(timerTask,500,2000)
                }
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "fail获取二维码create结果:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * 检测二维码扫描状态*/
    private fun getQRCodeStatus(key: String){

        val url = HttpUtil.getQRCodeStatusURL(key)

        Log.d(TAG, "二维码status URL:$url")

        HttpUtil.getQRCodeStatusInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                json?.let {
                    Log.d(TAG, "cookie:$json")
                    ToastUtil.setSuccessToast(this@LoginActivity, "扫描成功!")
                    SPUtil.getInstance().PutData(applicationContext,CacheKeyParam.cookieKey,json)
                    qrCodeTimer.cancel()
                    BaseApplication.cookie = json.toString()
                    startActivity(Intent(this@LoginActivity,HomePageActivity::class.java))
                    finish()
                }
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "fail二维码status结果:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }


    private fun initLoginListener() {
        passwordBinding.PassLogin.isEnabled = false
        identifyBinding.identifyLogin.isEnabled = false

        passwordBinding.PassLogin.setOnClickListener(this)
        passwordBinding.passwordGoIn.setOnClickListener(this)
        identifyBinding.identifyLogin.setOnClickListener(this)
        identifyBinding.identifyCode.setOnClickListener(this)
        identifyBinding.identifyGoIn.setOnClickListener(this)
        qrCodeBinding.getQRCode.setOnClickListener(this)

    }

    /**
     * 监听两种模式下的手机输入框
     * 当输入的字符长度大于0，变成高亮模式；反之，灰色模式*/
    private fun initEditListener() {
        passwordBinding.PassUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null || s.isEmpty()) {
                    setNormalBg(1)
                } else {
                    setHighLightBg(1)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        identifyBinding.identifyUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null || s.isEmpty()) {
                    setNormalBg(2)
                } else {
                    setHighLightBg(2)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    /**
     * 设置登录按钮样式:灰色
     * 如果mode==1，则修改密码登录样式
     * 如果mode==2，则修改验证码登录样式*/
    private fun setNormalBg(mode: Int) = when (mode) {
        1 -> {
            passwordBinding.PassLogin.isEnabled = false
            passwordBinding.PassLogin.background = getDrawable(R.drawable.shape_tablayout_no)
        }
        2 -> {
            identifyBinding.identifyLogin.isEnabled = false
            identifyBinding.identifyLogin.background = getDrawable(R.drawable.shape_tablayout_no)
        }
        else -> {}
    }

    /**
     * 设置登录按钮样式:高亮
     * 如果mode==1，则修改密码登录样式
     * 如果mode==2，则修改验证码登录样式*/
    private fun setHighLightBg(mode: Int) = when (mode) {
        1 -> {
            passwordBinding.PassLogin.isEnabled = true
            passwordBinding.PassLogin.background = getDrawable(R.drawable.shape_tablayout_yes)
        }
        2 -> {
            identifyBinding.identifyLogin.isEnabled = true
            identifyBinding.identifyLogin.background = getDrawable(R.drawable.shape_tablayout_yes)
        }
        else -> {}
    }

    private fun phoneJudge(phone: String): Boolean {
        if (TextUtils.isEmpty(phone)) {
            ToastUtil.setFailToast(this, "手机号码不能为空")
            return false
        }
        if (!isStandardPhone(phone)) {
            ToastUtil.setFailToast(this, "手机号码不符合工信部标准")
            return false
        }
        return true
    }

    /**
     * 根据工信部最新标准，判断手机号码是否符合*/
    private fun isStandardPhone(phoneNumber: String?): Boolean {
        val format: String =
            "^(?:(?:\\+|00)86)?1(?:(?:3[\\d])|(?:4[5-79])|(?:5[0-35-9])|(?:6[5-7])|(?:7[0-8])|(?:8[\\d])|(?:9[189]))\\d{8}$"
        phoneNumber?.let {
            val pattern = Pattern.compile(format)
            val matcher = pattern.matcher(phoneNumber)
            return matcher.matches()
        }
        return false
    }

    /**
     * 将base64编码转为Bitmap*/
    private fun base64ToBitmap(qrCodeUrl: String){
        val decode: ByteArray = Base64.decode(qrCodeUrl.split(",")[1], Base64.DEFAULT)
        val bitmap =  BitmapFactory.decodeByteArray(decode, 0, decode.size)
        runOnUiThread {
            qrCodeBinding.QRCodeImg.setImageBitmap(bitmap)
        }
    }

    /**
     * 执行定时器，然后每隔1秒执行一次
     * 推出条件：10秒时间结束，或者返回成功数据*/
    val timerTask: TimerTask = object : TimerTask(){
        override fun run() {
            if (qrCodeCount > 0){
                qrCodeKey?.let {
                    getQRCodeStatus(it)
                }
            }else{
                qrCodeCount = 10
                qrCodeTimer.cancel()
            }
            qrCodeCount--
        }
    }
    override fun onClick(v: View?) = when (v?.id) {
        R.id.PassLogin -> {
            passwordLogin()
        }
        R.id.identifyLogin -> {
            checkVerifyCode()
        }
        R.id.identifyCode -> {
            handler.postDelayed(runnable, 1000)
            verifyHandle()
        }
        R.id.passwordGoIn-> {
            startActivity(Intent(this@LoginActivity,HomePageActivity::class.java))
            finish()
        }
        R.id.identifyGoIn-> {
            startActivity(Intent(this@LoginActivity,HomePageActivity::class.java))
            finish()
        }

        R.id.getQRCode-> {
            ToastUtil.setSuccessToast(this@LoginActivity, "正在获取二维码中...")
            getQRCodeKey()
        }

        else -> {}
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        qrCodeTimer.cancel()
    }
}

