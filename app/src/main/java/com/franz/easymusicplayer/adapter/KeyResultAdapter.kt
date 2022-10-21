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
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.songList.SongListActivity
import com.franz.easymusicplayer.widgets.GlideRoundTransform

class KeyResultAdapter(beanLIst: List<ResultBean>): RecyclerView.Adapter<KeyResultAdapter.ViewHolder>(),View.OnClickListener {
    private var beanLIst: List<ResultBean>
    private lateinit var listener: OnKeyResultClickListener

    init {
        this.beanLIst = beanLIst
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanLIst[position]

        holder.name.setOnClickListener(this)

        holder.name.text = bean.name

        holder.name.tag = position
    }

    override fun getItemCount(): Int {
        return beanLIst.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var name: TextView

        init {
            name = itemView.findViewById(R.id.searchResult)
        }
    }

    override fun onClick(v: View?) {
        listener?.let {
            val tag: Int = v?.tag as Int
            it.onClickListener(beanLIst[tag].name.toString())
        }
    }

    fun setItemClickListener(listener: OnKeyResultClickListener) {
        this.listener = listener
    }

    interface OnKeyResultClickListener {
        fun onClickListener(name:String)
    }

}