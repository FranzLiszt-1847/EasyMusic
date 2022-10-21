package com.franz.easymusicplayer.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.bean.HistorySearchBean
import com.franz.easymusicplayer.bean.RecommendBean
import com.franz.easymusicplayer.bean.RecommendParentBean
import com.franz.easymusicplayer.bean.SearchKeyBean
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.searchResult.SearchResultActivity
import com.franz.easymusicplayer.utils.SystemParamUtil
import org.greenrobot.eventbus.EventBus

class RecommendParentAdapter(context: Activity, beanList: List<RecommendParentBean>): RecyclerView.Adapter<RecommendParentAdapter.ViewHolder>() {
    private var beanList: List<RecommendParentBean>
    private var context: Activity
    private var width: Int

    init {
        this.beanList = beanList
        this.context = context
        this.width = ((SystemParamUtil.width*1.0) / 1.5).toInt()
        Log.d("SystemWidth","system=${SystemParamUtil.width} -- current=$width")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_recommend_parent,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanList[position]

        when (bean.type) {
            1 -> holder.name.text = "推荐歌曲"
            2 -> holder.name.text = "推荐歌单"
            3 -> holder.name.text = "推荐MV"
            else-> holder.name.text = "无匹配项"
        }

        holder.line.layoutParams = LinearLayout.LayoutParams(width-20,1)

        holder.subRecycler.layoutManager = LinearLayoutManager(context)
        val adapter = RecommendSubAdapter(bean.beanList)
        holder.subRecycler.adapter = adapter
        holder.subRecycler.layoutParams = LinearLayout.LayoutParams(width,LinearLayout.LayoutParams.MATCH_PARENT)

        adapter.setItemClickListener(object : RecommendSubAdapter.OnRecommendResultClickListener{
            override fun onClickListener(key: String) {
                /**
                 * 跳转到搜索结果页*/
                initRecord(key)
                EventBus.getDefault().postSticky(SearchKeyBean(key))
                context.startActivity(Intent(context, SearchResultActivity::class.java))
            }
        })
    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
          var name: TextView
          var subRecycler: RecyclerView
          var line: View

        init {
            name = itemView.findViewById(R.id.parentRecommendTitle)
            subRecycler = itemView.findViewById(R.id.subRecommendRecycler)
            line = itemView.findViewById(R.id.recommendLine)
        }
    }
    private fun initRecord(key: String) {
        val dao = PlayListDataBase.getDBInstance().historyDao()
        val bean: HistorySearchBean = dao.findByKey(key)
        if (bean != null) {
            dao.delete(bean)
        }
        dao.insert(HistorySearchBean(key))
    }
}