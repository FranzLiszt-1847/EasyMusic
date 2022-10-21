package com.franz.easymusicplayer.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.BannerBean
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import com.youth.banner.adapter.BannerAdapter

class FBannerAdapter(beanList: MutableList<BannerBean>) : BannerAdapter<BannerBean, FBannerAdapter.ViewHolder>(beanList) {
    private lateinit var beanList: MutableList<BannerBean>
    init {
        this.beanList = beanList
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
       val imageView = ImageView(parent?.context)
        imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        return ViewHolder(imageView)
    }

    override fun onBindView(holder: ViewHolder?, data: BannerBean?, position: Int, size: Int) {
        val bean = beanList[position]

            Glide.with(holder!!.imageView)
                .asDrawable()
                .load(bean.pic)
                .transform(GlideRoundTransform(BaseApplication.context, 10))
                .placeholder(R.drawable.icon_default_songs)
                .dontAnimate()
                .into(holder.imageView)

    }

    class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView
    }
}