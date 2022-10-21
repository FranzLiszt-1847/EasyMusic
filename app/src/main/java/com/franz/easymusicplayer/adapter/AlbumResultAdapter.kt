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
import com.franz.easymusicplayer.bean.AlbumResultBean
import com.franz.easymusicplayer.bean.SongsResultBean
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import org.json.JSONObject

class AlbumResultAdapter(beanList: List<AlbumResultBean>): RecyclerView.Adapter<AlbumResultAdapter.ViewHolder>(),View.OnClickListener {
    private var beanList: List<AlbumResultBean>
    private lateinit var listener: OnAlbumClickListener

    init {
        this.beanList = beanList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_songs_result,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanList[position]

        holder.layout.setOnClickListener(this)

        holder.name.text = bean.name

        Glide.with(holder.img)
            .asDrawable()
            .load(bean.picUrl)
            .transform(GlideRoundTransform(BaseApplication.context,10))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(holder.img)

        val singerName = JSONObject(bean.artist.toString()).getString("name")
        holder.singer.text = singerName

        holder.layout.tag = position
    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var img: ImageView
         var name: TextView
         var singer: TextView
         var layout: ConstraintLayout

        init {
            img = itemView.findViewById(R.id.songsResultImg)
            name = itemView.findViewById(R.id.songsResultName)
            singer = itemView.findViewById(R.id.songsResultSinger)
            layout = itemView.findViewById(R.id.songsResultLayout)
        }
    }

    override fun onClick(v: View?) {
        listener?.let {
            val tag: Int = v?.tag as Int
            it.onClickListener(beanList[tag])
        }
    }

    fun setItemClickListener(listener: OnAlbumClickListener) {
        this.listener = listener
    }

    interface OnAlbumClickListener {
        fun onClickListener(bean: AlbumResultBean)
    }
}