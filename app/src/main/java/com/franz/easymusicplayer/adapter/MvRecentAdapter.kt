package com.franz.easymusicplayer.adapter

import android.graphics.BitmapFactory
import android.text.TextUtils
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
import com.franz.easymusicplayer.bean.MvRecentBean
import com.franz.easymusicplayer.bean.MvResultBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.bean.SongListBean
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import org.json.JSONArray
import org.json.JSONObject

class MvRecentAdapter(beanLIst: List<MvRecentBean>): RecyclerView.Adapter<MvRecentAdapter.ViewHolder>(),View.OnClickListener{
    private lateinit var listener: OnMvClickListener
    private var beanLIst: List<MvRecentBean>

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

        if (!TextUtils.isEmpty(bean.name))  holder.song.text = bean.name
        else  holder.song.text = bean.title
        holder.duration.text = figureDuration(bean.duration)
        holder.playCount.visibility = View.INVISIBLE

        if (bean.creator != null){
            val name = JSONObject(bean.creator.toString()).getString("nickname")
            holder.singer.text = name
        }

        if (bean.artists != null){
            val array = JSONArray(bean.artists.toString())
            val name = JSONObject(array[0].toString()).getString("name")
            holder.singer.text = name
        }

        Glide.with(holder.mv)
            .asDrawable()
            .load(bean.coverUrl)
            .transform(GlideRoundTransform(BaseApplication.context,10))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(holder.mv)

        holder.layout.tag = position
    }

    override fun getItemCount(): Int {
        return beanLIst.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var mv: ImageView
         var song: TextView
         var singer: TextView
         var playCount: TextView
         var duration: TextView
         var layout: ConstraintLayout

        init {
            mv = itemView.findViewById(R.id.mvResultImg)
            song = itemView.findViewById(R.id.mvResultName)
            singer = itemView.findViewById(R.id.mvResultSinger)
            playCount = itemView.findViewById(R.id.mvResultCount)
            duration = itemView.findViewById(R.id.mvResultDuration)
            layout = itemView.findViewById(R.id.mvResultLayout)
        }
    }

    fun setItemClickListener(listener: OnMvClickListener) {
        this.listener = listener
    }

    interface OnMvClickListener {
        fun onClickListener(bean: MvRecentBean)
    }

    override fun onClick(v: View?) {
        listener?.let {
            val tag: Int = v?.tag as Int
            it.onClickListener(beanLIst[tag])
        }
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