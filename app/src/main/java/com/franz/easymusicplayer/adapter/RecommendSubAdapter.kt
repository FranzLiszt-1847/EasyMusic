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
import com.franz.easymusicplayer.bean.RecommendBean
import com.franz.easymusicplayer.bean.ResultBean
import com.franz.easymusicplayer.bean.SongListBean
import com.franz.easymusicplayer.ui.songList.SongListActivity
import com.franz.easymusicplayer.widgets.GlideRoundTransform

class RecommendSubAdapter(beanList: List<RecommendBean>): RecyclerView.Adapter<RecommendSubAdapter.ViewHolder>(),View.OnClickListener {
    private var beanList: List<RecommendBean>
    private lateinit var listener: OnRecommendResultClickListener

    init {
        this.beanList = beanList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_recommend_sub,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanList[position]

        holder.key.setOnClickListener(this)

        if (position < 3){
            holder.order.setTextColor(BaseApplication.context.getColor(R.color.color_system_select))
        }
        holder.order.text = "${position + 1}"
        holder.key.text = bean.name

        holder.key.tag = position
    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var key: TextView
         var order: TextView

        init {
            key = itemView.findViewById(R.id.subRecommendContent)
            order = itemView.findViewById(R.id.subRecommendOrder)
        }
    }

    override fun onClick(v: View?) {
        listener?.let {
            val tag: Int = v?.tag as Int
            it.onClickListener(beanList[tag].name)
        }
    }

    fun setItemClickListener(listener: OnRecommendResultClickListener) {
        this.listener = listener
    }

    interface OnRecommendResultClickListener {
        fun onClickListener(key: String)
    }
}