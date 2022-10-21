package com.franz.easymusicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.HistorySearchBean
import com.franz.easymusicplayer.bean.ResultBean
import com.franz.easymusicplayer.bean.SongListBean
import com.franz.easymusicplayer.ui.songList.SongListActivity
import com.franz.easymusicplayer.widgets.GlideRoundTransform

class HistoryAdapter(beanList: List<HistorySearchBean>): RecyclerView.Adapter<HistoryAdapter.ViewHolder>(),View.OnClickListener {
    private var beanList: List<HistorySearchBean>
    private lateinit var listener: OnHistoryKeyResultClickListener

    init {
        this.beanList = beanList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_history,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanList[position]

        holder.key.setOnClickListener(this)

        holder.key.text = bean.key

        holder.key.tag = position
    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var key: TextView

        init {
            key = itemView.findViewById(R.id.itemHistory)
        }
    }

    override fun onClick(v: View?) {
        listener?.let {
            val tag: Int = v?.tag as Int
            it.onClickListener(beanList[tag].key.toString())
        }
    }

    fun setItemClickListener(listener: OnHistoryKeyResultClickListener) {
        this.listener = listener
    }

    interface OnHistoryKeyResultClickListener {
        fun onClickListener(key: String)
    }
}