package com.franz.easymusicplayer.base

import androidx.fragment.app.Fragment

abstract class LazyFragment : Fragment() {
    private var isLoaded = false

    private var is_Visible = false

    private var isCallResume = false

    private var isCallHint = false


    override fun onResume() {
        super.onResume()
        isCallResume = true
        if (!isCallHint)
            is_Visible = !isHidden
        isLazy()
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        is_Visible = !isHidden
        isLazy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
        is_Visible = false
        isCallResume = false
        isCallHint = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        is_Visible = isVisibleToUser
        isCallHint = true
        isLazy()
    }

    private fun isLazy() {
        if (!isLoaded && is_Visible && isCallResume) {
            initLazyFunc()
            isLoaded = true
        }
    }

    abstract fun initLazyFunc()
}