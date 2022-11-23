package com.franz.easymusicplayer.adapter
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/**
 * desc : 禁止滑动的RecyclerView
 * author : hpy
 * time : 2022/3/4 14:22
 */
class NoSlideRecyclerView (mContext: Context, attrs: AttributeSet) : RecyclerView(mContext,attrs){

    init {
        overScrollMode = OVER_SCROLL_NEVER
    }
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val mExpandSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthSpec, mExpandSpec)
    }
}