package com.franz.easymusicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.bean.SongBean

class SongsAdapter(beanLIst: List<SongBean>): RecyclerView.Adapter<SongsAdapter.ViewHolder>(),View.OnClickListener{
    private lateinit var listener: OnSongClickListener
    private lateinit var mvListener: OnMvClickListener
    private lateinit var moreListener: OnMoreInfoClickListener
    private lateinit var downloadListener: OnDownloadClickListener

    /**
     * 是否显示多选框*/
    var displaySelect = false

    private var beanLIst: List<SongBean>

    init {
        this.beanLIst = beanLIst
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_song,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanLIst[position]
        holder.layout.setOnClickListener(this)
        holder.mv.setOnClickListener(this)
        holder.more.setOnClickListener(this)
        holder.download.setOnClickListener(this)

        if (displaySelect)
            holder.select.visibility = View.VISIBLE
        else
            holder.select.visibility = View.GONE


        holder.orderNum.text = "${position+1}"
        holder.song.text = bean.songName
        holder.singer.text = bean.singerName

        holder.layout.tag = position
        holder.mv.tag = position
        holder.more.tag = position
        holder.download.tag = position
    }

    override fun onBindViewHolder(holder: SongsAdapter.ViewHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        val flag = beanLIst[position].isSelect
        if (flag){
           holder.select.setImageResource(R.drawable.icon_select_yes)
        }else{
            holder.select.setImageResource(R.drawable.icon_select_not)
        }
    }

    override fun getItemCount(): Int {
        return beanLIst.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var mv: ImageView
         var more: ImageView
         var download: ImageView
         var select:ImageView
         var song: TextView
         var singer: TextView
         var orderNum: TextView
         var layout: ConstraintLayout

        init {
            mv = itemView.findViewById(R.id.itemSongMv)
            more = itemView.findViewById(R.id.itemSongInfo)
            select = itemView.findViewById(R.id.itemSongsSelect)
            download = itemView.findViewById(R.id.itemSongDownload)
            song = itemView.findViewById(R.id.itemSongName)
            singer = itemView.findViewById(R.id.itemSingerName)
            orderNum = itemView.findViewById(R.id.itemOrderNum)
            layout = itemView.findViewById(R.id.songDataLayout)
        }
    }

    fun setItemClickListener(listener: OnSongClickListener) {
        this.listener = listener
    }

    fun setMvItemClickListener(mvListener: OnMvClickListener){
        this.mvListener = mvListener
    }

    fun setMoreInfoItemClickListener(moreListener: OnMoreInfoClickListener){
        this.moreListener = moreListener
    }

    interface OnSongClickListener {
        fun onClickListener(pos:Int,bean: SongBean)
    }

    interface OnMvClickListener{
        fun onClickListener(pos:Int,bean: SongBean)
    }

    interface OnMoreInfoClickListener{
        fun onClickListener(pos:Int,bean: SongBean)
    }
    fun setDownloadItemClickListener(downloadListener: OnDownloadClickListener){
        this.downloadListener = downloadListener
    }

    interface OnDownloadClickListener{
        fun onClickListener(pos:Int,bean: SongBean)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.itemSongMv-> {
                val tag: Int = v.tag as Int
                mvListener?.onClickListener(tag,beanLIst[tag])
            }
            R.id.songDataLayout-> {
                val tag: Int = v.tag as Int
                listener?.onClickListener(tag,beanLIst[tag])
            }

            R.id.itemSongInfo-> {
                val tag: Int = v.tag as Int
                moreListener?.onClickListener(tag,beanLIst[tag])
            }
            R.id.itemSongDownload-> {
                val tag: Int = v.tag as Int
                downloadListener?.onClickListener(tag,beanLIst[tag])
            }
            else -> {}
        }
    }
}