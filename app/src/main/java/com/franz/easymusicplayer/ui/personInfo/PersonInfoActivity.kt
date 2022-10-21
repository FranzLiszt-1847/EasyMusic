package com.franz.easymusicplayer.ui.personInfo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.bean.ProvinceBean
import com.franz.easymusicplayer.databinding.ActivityPersonInfoBinding
import com.franz.easymusicplayer.utils.StatusBarUtil
import com.franz.easymusicplayer.utils.XMLParserUtil
import com.franz.easymusicplayer.viewmodel.UserInfoViewModel
import java.text.SimpleDateFormat
import java.util.*

class PersonInfoActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPersonInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarHide(window)
        binding = ActivityPersonInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPersonInfo()
    }

    private fun initPersonInfo(){
        UserInfoViewModel.getUserInfo().observe(this,Observer{
            Log.d("tag",it.toString())
            binding.personName.text = it.nickname//名称
            binding.personFollows.text = "${it.follows}"//关注
            binding.personFans.text = "${it.followeds}"//粉丝
            binding.personInfo.text = it.signature//简介
            binding.personLevel.text = "${it.level}"//等级
            binding.personGender.text = //性别
                if (it.gender == 1) "男"
                else "女"

            binding.personRegister.text = millToDate(it.createTime)//注册时间
            binding.personAge.text = figureBirthday(it.birthday)//生日

            val region = XMLParserUtil.getCityNameByCode(it.province,it.city,"-")//地区
            binding.personLocate.text = region


            if (!this.isFinishing){
                Glide.with(binding.personCover)
                    .asDrawable()
                    .load(it.backgroundUrl)
                    .placeholder(R.drawable.icon_default_songs)
                    .dontAnimate()
                    .into(binding.personCover)

                Glide.with(binding.personImg)
                    .asDrawable()
                    .load(it.avatarUrl)
                    .placeholder(R.drawable.icon_default_songs)
                    .dontAnimate()
                    .into(binding.personImg)
            }
        })
    }

    private fun figureBirthday(day: Long): String{
        val birthday = millToDate(day)
        val current = millToDate(System.currentTimeMillis())
        val birthday_year = birthday.substring(0,4).toInt()
        val birthday_month = birthday.substring(5,7).toInt()
        val current_year = current.substring(0,4).toInt()
        val current_month = current.substring(5,7).toInt()
        var age = 0
        if (birthday_year == 0)return "$birthday_month 个月"

        if (current_year >= birthday_year)age = current_year - birthday_year
        if (current_month < birthday_month)age--
        return "$age 岁"

    }
    private fun millToDate(time: Long): String{
        val date = Date(time)
        val format = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        return format.format(date)
    }
}