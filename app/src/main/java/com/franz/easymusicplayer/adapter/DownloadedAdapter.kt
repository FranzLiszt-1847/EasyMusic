package com.franz.easymusicplayer.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import cn.we.swipe.helper.WeSwipeHelper
import cn.we.swipe.helper.WeSwipeProxyAdapter
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.bean.DownloadBean

class DownloadedAdapter(beanLIst: List<DownloadBean>): WeSwipeProxyAdapter<DownloadedAdapter.ViewHolder>(),View.OnClickListener{
    private lateinit var listener: OnSongClickListener
    private lateinit var mvListener: OnMvClickListener
    private lateinit var detailListener: OnDetailInfoClickListener
    private lateinit var deleteListener: OnDeleteInfoClickListener
    private var beanList: List<DownloadBean>

    init {
        this.beanList = beanLIst
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_downloaded,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanList[position]

        holder.layout.setOnClickListener(this)
        holder.mv.setOnClickListener(this)
        holder.detail.setOnClickListener(this)
        holder.delete.setOnClickListener(this)


        holder.song.text = bean.songName
        holder.singer.text = bean.singer
        holder.size.text = bean.songSize

        holder.layout.tag = position
        holder.mv.tag = position
        holder.detail.tag = position
        holder.delete.tag = position
    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),WeSwipeHelper.SwipeLayoutTypeCallBack {
         var mv: ImageView
         val detail: ImageView
         var song: TextView
         var singer: TextView
         val delete: TextView
         var size : TextView
         var layout: ConstraintLayout

        init {
            mv = itemView.findViewById(R.id.itemDownloadMv)
            detail = itemView.findViewById(R.id.itemDownloadInfo)
            song = itemView.findViewById(R.id.itemDownloadName)
            size = itemView.findViewById(R.id.itemDownloadSize)
            delete = itemView.findViewById(R.id.itemDownloadDelete)
            singer = itemView.findViewById(R.id.itemDownloadSinger)
            layout = itemView.findViewById(R.id.itemDownloadLayout)
        }

        override fun getSwipeWidth(): Float {
            return delete.width.toFloat()
        }

        override fun needSwipeLayout(): View {
           return layout
        }

        override fun onScreenView(): View {
            return layout
        }
    }

    fun setItemClickListener(listener: OnSongClickListener) {
        this.listener = listener
    }

    interface OnSongClickListener {
        fun onClickListener(pos:Int,bean: DownloadBean)
    }

    fun setMvClickListener(mvListener: OnMvClickListener) {
        this.mvListener = mvListener
    }

    interface OnMvClickListener {
        fun onClickListener(pos:Int,bean: DownloadBean)
    }

    fun setDetailInfoItemClickListener(detailListener: OnDetailInfoClickListener){
        this.detailListener = detailListener
    }

    interface OnDetailInfoClickListener{
        fun onClickListener(pos:Int,bean: DownloadBean)
    }

    fun setDetailInfoItemClickListener(deleteListener: OnDeleteInfoClickListener){
        this.deleteListener = deleteListener
    }

    interface OnDeleteInfoClickListener{
        fun onClickListener(pos:Int,bean: DownloadBean)
    }





    override fun onClick(v: View?) {
        when(v?.id){
            R.id.itemDownloadMv-> {
                val tag: Int = v.tag as Int
                mvListener?.onClickListener(tag,beanList[tag])
            }
            R.id.itemDownloadLayout-> {
                val tag: Int = v.tag as Int
                listener?.onClickListener(tag,beanList[tag])
            }
            R.id.itemDownloadInfo-> {
                val tag: Int = v.tag as Int
                detailListener?.onClickListener(tag,beanList[tag])
            }
            R.id.itemDownloadDelete-> {
                val tag: Int = v.tag as Int
                deleteListener?.onClickListener(tag,beanList[tag])
            }
            else -> {}
        }
    }
}