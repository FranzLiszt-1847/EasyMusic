package com.franz.easymusicplayer.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.MlogBean
import com.franz.easymusicplayer.bean.MvResultBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.bean.SongListBean
import com.franz.easymusicplayer.widgets.GlideRoundTransform

class MLogAdapter(beanLIst: List<MlogBean>): RecyclerView.Adapter<MLogAdapter.ViewHolder>(),View.OnClickListener{
    private lateinit var listener: OnMLogClickListener
    private var beanLIst: List<MlogBean>

    init {
        this.beanLIst = beanLIst
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_mv_result,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanLIst[position]
        holder.layout.setOnClickListener(this)

        holder.song.text = bean.text
        holder.singer.text = bean.nickname
        holder.playCount.text = figurePlayCount(bean.playCount)
        holder.duration.text = figureDuration(bean.duration)

        Glide.with(holder.mlog)
            .asDrawable()
            .load(bean.cover)
            .transform(GlideRoundTransform(BaseApplication.context,10))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(holder.mlog)

        holder.layout.tag = position
    }

    override fun getItemCount(): Int {
        return beanLIst.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var mlog: ImageView
         var song: TextView
         var singer: TextView
         var playCount: TextView
         var duration: TextView
         var layout: ConstraintLayout

        init {
            mlog = itemView.findViewById(R.id.mvResultImg)
            song = itemView.findViewById(R.id.mvResultName)
            singer = itemView.findViewById(R.id.mvResultSinger)
            playCount = itemView.findViewById(R.id.mvResultCount)
            duration = itemView.findViewById(R.id.mvResultDuration)
            layout = itemView.findViewById(R.id.mvResultLayout)
        }
    }

    fun setItemClickListener(listener: OnMLogClickListener) {
        this.listener = listener
    }

    interface OnMLogClickListener {
        fun onClickListener(pos:Int,bean: MlogBean)
    }

    override fun onClick(v: View?) {
        listener?.let {
            val tag: Int = v?.tag as Int
            it.onClickListener(tag,beanLIst[tag])
        }
    }

    /**
     * 计算播放量*/
    private fun figurePlayCount(count: Long): String{
        return if (count < 10000) "$count 播放"
        else if (count < 100000000) "${count/10000}万播放"
        else "${count/100000000}亿播放"
    }
    /**
     * 计算视频时长*/
    private fun figureDuration(count: Long): String{
        //208080
        return if (count < 10000) {
            val t = count/1000
            if (t < 10) "00:0${count/1000}"
            else "00:${count/1000}"
        }
        else {
            val t = count*1.0/1000/60
            val min = t.toInt()
            val second = ((t-min)*100).toInt()
            var result: String
            if (min < 10) result = "0$min:"
            else result = "$min"
            if (second < 10) result += "0$second"
            else result += "$second"
            result
        }
    }
}