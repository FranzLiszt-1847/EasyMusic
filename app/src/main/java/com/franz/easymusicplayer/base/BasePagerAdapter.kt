package com.franz.easymusicplayer.base


import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.franz.easymusicplayer.R


class BasePagerAdapter(viewList: List<View>, titleList: List<String>) : PagerAdapter() {
    private var titleList: List<String>
    private var viewList: List<View>

    init {
        this.titleList = titleList
        this.viewList = viewList
    }

    override fun getCount(): Int = viewList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewList[position])
        return viewList[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) =
        container.removeView(viewList[position])

    override fun getPageTitle(position: Int): CharSequence? = titleList[position]
}