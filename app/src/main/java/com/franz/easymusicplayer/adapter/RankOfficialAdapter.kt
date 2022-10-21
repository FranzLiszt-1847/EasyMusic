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
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import org.json.JSONArray
import org.json.JSONObject

class RankOfficialAdapter(beanList: List<SheetPlayBean>): RecyclerView.Adapter<RankOfficialAdapter.ViewHolder>(), View.OnClickListener{
    private var beanList: List<SheetPlayBean>
    private lateinit var listener: OnOfficialRankClickListener

    init {
        this.beanList = beanList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_rank_sub_track,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanList[position]

        holder.rankName.text = bean.name
        holder.updateTime.text = bean.updateFrequency.toString()

        val array = JSONArray(bean.tracks.toString())
        val tracks = HttpUtil.fromListJson(array.toString(),TrackBean::class.java)
        if (tracks != null && tracks.size > 0) {

            holder.oneSongName.text = tracks[0].first
            holder.oneSinger.text = tracks[0].second
            holder.twoSongName.text = tracks[1].first
            holder.twoSinger.text = tracks[1].second
            holder.threeSongName.text = tracks[2].first
            holder.threeSinger.text = tracks[2].second
        }

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
        var rankName: TextView
        var updateTime: TextView
        var oneSongName: TextView
        var oneSinger: TextView
        var twoSongName: TextView
        var twoSinger: TextView
        var threeSongName: TextView
        var threeSinger: TextView
        var cover: ImageView
        val layout: ConstraintLayout

        init {
            rankName = itemView.findViewById(R.id.officialRankTitle)
            updateTime = itemView.findViewById(R.id.officialRankTime)
            cover = itemView.findViewById(R.id.officialCover)
            oneSinger = itemView.findViewById(R.id.oneSinger)
            oneSongName = itemView.findViewById(R.id.oneSongName)
            twoSinger = itemView.findViewById(R.id.twoSinger)
            twoSongName = itemView.findViewById(R.id.twoSongName)
            threeSinger = itemView.findViewById(R.id.threeSinger)
            threeSongName = itemView.findViewById(R.id.threeSongName)
            layout = itemView.findViewById(R.id.officialRankLayout)
        }
    }


    fun setItemClickListener(listener: OnOfficialRankClickListener) {
        this.listener = listener
    }

    interface OnOfficialRankClickListener {
        fun onClickListener(bean:SheetPlayBean)
    }

    override fun onClick(v: View?) {
        listener?.let {
            val tag: Int = v?.tag as Int
            it.onClickListener(beanList[tag])
        }
    }
}