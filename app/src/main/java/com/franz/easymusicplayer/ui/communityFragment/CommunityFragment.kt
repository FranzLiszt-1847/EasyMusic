package com.franz.easymusicplayer.ui.communityFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.franz.easymusicplayer.base.LazyFragment
import com.franz.easymusicplayer.databinding.FragmentCommunityBinding
import com.franz.easymusicplayer.databinding.FragmentMineBinding


class CommunityFragment : LazyFragment() {
    var binding: FragmentCommunityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCommunityBinding.inflate(layoutInflater)
        return binding!!.root
    }

    /**
     * 网络请求放这里*/
    override fun initLazyFunc() {

    }

}