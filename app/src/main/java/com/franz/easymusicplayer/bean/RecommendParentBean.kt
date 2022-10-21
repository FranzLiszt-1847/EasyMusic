package com.franz.easymusicplayer.bean

class RecommendParentBean(type: Int,beanList: List<RecommendBean>) {
    var type: Int
    var beanList: List<RecommendBean>
    init {
        this.type = type
        this.beanList = beanList
    }
}