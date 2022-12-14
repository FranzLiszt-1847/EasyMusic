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
     * ???????????????60s?????????*/
    private val runnable = object : Runnable {
        override fun run() {
            codeCount--
            if (codeCount > 0) {
                identifyBinding.identifyCode.text = "$codeCount s"
                identifyBinding.identifyCode.isEnabled = false
                handler.postDelayed(this, 1000)
            }else{
                identifyBinding.identifyCode.text = "???????????????"
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
     * ?????????tabLayout???viewPager???????????????????????????
     * ?????????binding*/
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
            it.add("????????????")
            it.add("????????????")
            it.add("???????????????")
        }
        loginAdapter = BasePagerAdapter(viewsList, titlesList)
        for (title in titlesList) {
            binding.tabTitle.addTab(binding.tabTitle.newTab().setText(title))
        }
        binding.tabTitle.setupWithViewPager(binding.viewPager)
        binding.viewPager.adapter = loginAdapter
    }

    /**
     * ?????????????????????????????????*/
    private fun initTitleBar() {
        titleBarBinding.majorTitle.visibility = View.INVISIBLE
        titleBarBinding.subTitle.visibility = View.INVISIBLE

        titleBarBinding.exit.setOnClickListener { finish() }
    }

    /**
     * ??????????????????*/
    private fun passwordLogin() {
        val phone = passwordBinding.PassUser.text.toString()
        val password = passwordBinding.PassPassword.text.toString()
        val check = passwordBinding.PassPrivacy.isChecked

        val result: Boolean = phoneJudge(phone)
        if (!result) return

        if (TextUtils.isEmpty(password)) {
            ToastUtil.setFailToast(this, "??????????????????!")
            return
        }
        if (!check) {
            ToastUtil.setFailToast(this, "????????????????????????!")
            return
        }

        val url = HttpUtil.getLoginURL(phone, password)
        Log.d(TAG, "URL:$url")

        HttpUtil.getLoginInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, "??????????????????:$json")
                ToastUtil.setSuccessToast(this@LoginActivity, "????????????!")
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
                Log.d(TAG, "??????????????????:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * ?????????????????????*/
    private fun identifyLogin(phone: String, verify: String) {
        val url = HttpUtil.getVerifyLoginURL(phone, verify)
        HttpUtil.getLoginInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, "??????????????????:$json")
                ToastUtil.setSuccessToast(this@LoginActivity, "????????????!")
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
                Log.d(TAG, "??????????????????:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * ???????????????*/
    private fun checkVerifyCode() {
        val phone = identifyBinding.identifyUser.text.toString()
        val check = identifyBinding.identifyPrivacy.isChecked
        val verify = identifyBinding.identifyPassword.text.toString().trim()
        val result: Boolean = phoneJudge(phone)
        if (!result) return


        if (TextUtils.isEmpty(verify)) {
            ToastUtil.setFailToast(this, "?????????????????????")
            return
        }

        if (!check) {
            ToastUtil.setFailToast(this, "????????????????????????!")
            return
        }

        /**
         * ?????????????????????????????????*/
        HttpUtil.getVerifyCodeInfo(HttpUtil.getVerifyResultURL(phone, verify),
            object : IVerifyCodeInfo {
                override fun onRespond(flag: Boolean) {
                    Log.d(TAG, "?????????????????????:$flag")
                    if (flag) identifyLogin(phone, verify)

                }

                override fun onFailed(e: String?) {
                    Log.d(TAG, "fail?????????????????????:$e")
                    ToastUtil.setFailToast(this@LoginActivity, e.toString())
                }
            })
    }

    /**
     * ???????????????????????????????????????*/
    private fun verifyHandle() {
        val phone = identifyBinding.identifyUser.text.toString()
        val result: Boolean = phoneJudge(phone)
        if (!result) return

        HttpUtil.getVerifyCodeInfo(HttpUtil.getVerifyCodeURL(phone), object : IVerifyCodeInfo {
            override fun onRespond(flag: Boolean) {
                Log.d(TAG, "?????????????????????:$flag")
                if (flag) ToastUtil.setSuccessToast(this@LoginActivity, "?????????????????????!")
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "fail?????????????????????:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * ???????????????key*/
    private fun getQRCodeKey(){
        val url = HttpUtil.getQRCodeKeyURL()
        Log.d(TAG, "?????????key URL:$url")

        HttpUtil.getQRCodeKeyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                json?.let {
                    qrCodeKey = json
                    getQRCodeCreate(json)
                    ToastUtil.setSuccessToast(this@LoginActivity, "???????????????key??????!")
                }
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "fail???????????????key??????:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * ?????????????????????*/
    private fun getQRCodeCreate(key: String){
        val url = HttpUtil.getQRCodeCreateURL(key)
        Log.d(TAG, "?????????create URL:$url")

        HttpUtil.getQRCodeCreateInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                json?.let {
                    Log.d(TAG, "base qr code:$json")
                    base64ToBitmap(json)
                    /**
                     * ??????0.5s??????????????????????????????1???????????????*/
                    qrCodeTimer.schedule(timerTask,500,2000)
                }
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "fail???????????????create??????:$e")
                ToastUtil.setFailToast(this@LoginActivity, e.toString())
            }
        })
    }

    /**
     * ???????????????????????????*/
    private fun getQRCodeStatus(key: String){

        val url = HttpUtil.getQRCodeStatusURL(key)

        Log.d(TAG, "?????????status URL:$url")

        HttpUtil.getQRCodeStatusInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                json?.let {
                    Log.d(TAG, "cookie:$json")
                    ToastUtil.setSuccessToast(this@LoginActivity, "????????????!")
                    SPUtil.getInstance().PutData(applicationContext,CacheKeyParam.cookieKey,json)
                    qrCodeTimer.cancel()
                    BaseApplication.cookie = json.toString()
                    startActivity(Intent(this@LoginActivity,HomePageActivity::class.java))
                    finish()
                }
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, "fail?????????status??????:$e")
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
     * ???????????????????????????????????????
     * ??????????????????????????????0?????????????????????????????????????????????*/
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
     * ????????????????????????:??????
     * ??????mode==1??????????????????????????????
     * ??????mode==2?????????????????????????????????*/
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
     * ????????????????????????:??????
     * ??????mode==1??????????????????????????????
     * ??????mode==2?????????????????????????????????*/
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
            ToastUtil.setFailToast(this, "????????????????????????")
            return false
        }
        if (!isStandardPhone(phone)) {
            ToastUtil.setFailToast(this, "????????????????????????????????????")
            return false
        }
        return true
    }

    /**
     * ????????????????????????????????????????????????????????????*/
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
     * ???base64????????????Bitmap*/
    private fun base64ToBitmap(qrCodeUrl: String){
        val decode: ByteArray = Base64.decode(qrCodeUrl.split(",")[1], Base64.DEFAULT)
        val bitmap =  BitmapFactory.decodeByteArray(decode, 0, decode.size)
        runOnUiThread {
            qrCodeBinding.QRCodeImg.setImageBitmap(bitmap)
        }
    }

    /**
     * ??????????????????????????????1???????????????
     * ???????????????10??????????????????????????????????????????*/
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
            ToastUtil.setSuccessToast(this@LoginActivity, "????????????????????????...")
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

