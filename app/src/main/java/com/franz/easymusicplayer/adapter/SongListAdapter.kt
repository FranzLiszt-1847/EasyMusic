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
import com.franz.easymusicplayer.bean.SongListBean
import com.franz.easymusicplayer.ui.songList.SongListActivity
import com.franz.easymusicplayer.widgets.GlideRoundTransform

class SongListAdapter(beanLIst: List<SongListBean>): RecyclerView.Adapter<SongListAdapter.ViewHolder>(),View.OnClickListener {
    private var beanLIst: List<SongListBean>
    private lateinit var listener: OnSongsClickListener

    init {
        this.beanLIst = beanLIst
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_songlist,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanLIst[position]

        holder.layout.setOnClickListener(this)

        holder.name.text = bean.name


        Glide.with(holder.img)
            .asDrawable()
            .load(bean.coverImgUrl)
            .transform(GlideRoundTransform(BaseApplication.context,10))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(holder.img)

        holder.num.text = "${bean.trackCount}首"

        holder.layout.tag = position
    }

    override fun getItemCount(): Int {
        return beanLIst.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var img: ImageView
         var name: TextView
         var num: TextView
         var layout: ConstraintLayout

        init {
            img = itemView.findViewById(R.id.itemSongListImg)
            name = itemView.findViewById(R.id.itemSongListName)
            num = itemView.findViewById(R.id.itemSongListNum)
            layout = itemView.findViewById(R.id.songListLayout)
        }
    }

    override fun onClick(v: View?) {
        listener?.let {
            val tag: Int = v?.tag as Int
            it.onClickListener(beanLIst[tag])
        }
    }

    fun setItemClickListener(listener: OnSongsClickListener) {
        this.listener = listener
    }

    interface OnSongsClickListener {
        fun onClickListener(bean:SongListBean)
    }
}