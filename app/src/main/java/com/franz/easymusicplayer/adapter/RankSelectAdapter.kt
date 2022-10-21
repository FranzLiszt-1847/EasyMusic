package com.franz.easymusicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.*
import com.franz.easymusicplayer.widgets.GlideRoundTransform

class RankSelectAdapter( beanList: List<SheetPlayBean>) : RecyclerView.Adapter<RankSelectAdapter.ViewHolder>(), View.OnClickListener {
    private var beanList: List<SheetPlayBean>
    private lateinit var listener: OnSelectRankClickListener

    init {
        this.beanList = beanList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_rank_sub_normal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankSelectAdapter.ViewHolder, position: Int) {
        val bean = beanList[position]

        holder.time.text = bean.updateFrequency.toString()

        Glide.with(holder.cover)
            .asDrawable()
            .load(bean.coverImgUrl)
            .transform(GlideRoundTransform(BaseApplication.context,10))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(holder.cover)
        holder.layout.setOnClickListener(this)
        holder.layout.tag = position

    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var time: TextView
        var layout: ConstraintLayout
        var cover: ImageView

        init {
            time = itemView.findViewById(R.id.selectRankTime)
            layout = itemView.findViewById(R.id.selectRankLayout)
            cover = itemView.findViewById(R.id.selectRankCover)
        }
    }

    fun setItemClickListener(listener: OnSelectRankClickListener) {
        this.listener = listener
    }

    interface OnSelectRankClickListener {
        fun onClickListener(bean:SheetPlayBean)
    }

    override fun onClick(v: View?) {
        listener?.let {
            val tag: Int = v?.tag as Int
            it.onClickListener( beanList[tag])
        }
    }
}