package com.franz.easymusicplayer.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class BaseFragmentAdapter: FragmentPagerAdapter {
    private lateinit var fragments: List<Fragment>
    private lateinit var titles: List<String>

    constructor(fm: FragmentManager, fragments: List<Fragment>, titles: List<String>):super(fm){
        this.fragments = fragments
        this.titles = titles
    }

    override fun getCount(): Int = fragments.size

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getPageTitle(position: Int): CharSequence? =  titles[position].toString()
}