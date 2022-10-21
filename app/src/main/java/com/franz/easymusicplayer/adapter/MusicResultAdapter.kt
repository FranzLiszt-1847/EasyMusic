package com.franz.easymusicplayer.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.ResultBean
import com.franz.easymusicplayer.bean.SongBean
import org.json.JSONArray
import org.json.JSONObject

class MusicResultAdapter(beanLIst: List<ResultBean>): RecyclerView.Adapter<MusicResultAdapter.ViewHolder>(),View.OnClickListener{
    private lateinit var listener: OnSongClickListener
    private lateinit var mvListener: OnMvClickListener
    private lateinit var detailListener: OnDetailInfoClickListener
    private var beanList: List<ResultBean>

    var displayDownload = false

    init {
        this.beanList = beanLIst
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_song,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanList[position]
        holder.layout.setOnClickListener(this)
        holder.mv.setOnClickListener(this)
        holder.detail.setOnClickListener(this)

        if (displayDownload){
            holder.download.visibility = View.VISIBLE
        }else{
            holder.download.visibility = View.GONE
        }

        holder.orderNum.text = "${position+1}"
        holder.song.text = bean.name
        holder.song.setTextColor(BaseApplication.context.getColor(R.color.color_singer))
        val array = JSONArray(bean.ar.toString())
        val mObject = array[0]
        val singerName = JSONObject(mObject.toString()).getString("name")
        holder.singer.text = singerName

        holder.orderNum.visibility = View.INVISIBLE

        holder.layout.tag = position
        holder.mv.tag = position
        holder.detail.tag = position
    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var mv: ImageView
         val detail: ImageView
         var download:ImageView
         var song: TextView
         var singer: TextView
         var orderNum: TextView
         var layout: ConstraintLayout

        init {
            mv = itemView.findViewById(R.id.itemSongMv)
            detail = itemView.findViewById(R.id.itemSongInfo)
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

    interface OnSongClickListener {
        fun onClickListener(pos:Int,bean: ResultBean)
    }

    fun setMvClickListener(mvListener: OnMvClickListener) {
        this.mvListener = mvListener
    }

    interface OnMvClickListener {
        fun onClickListener(pos:Int,bean: ResultBean)
    }

    fun setDetailInfoItemClickListener(detailListener: OnDetailInfoClickListener){
        this.detailListener = detailListener
    }

    interface OnDetailInfoClickListener{
        fun onClickListener(pos:Int,bean: ResultBean)
    }





    override fun onClick(v: View?) {
        when(v?.id){
            R.id.itemSongMv-> {
                val tag: Int = v.tag as Int
                mvListener?.onClickListener(tag,beanList[tag])
            }
            R.id.songDataLayout-> {
                val tag: Int = v.tag as Int
                listener?.onClickListener(tag,beanList[tag])
            }
            R.id.itemSongInfo-> {
                val tag: Int = v.tag as Int
                detailListener?.onClickListener(tag,beanList[tag])
            }
            else -> {}
        }
    }
}