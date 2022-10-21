package com.franz.easymusicplayer.`interface`

interface IMusic {
    /**
     * 准备播放，传入播放地址*/
    fun prepare(path: String)
    /**
     * 开始播放*/
    fun play()
    /**
     * 暂停播放*/
    fun stop()
    /**
     * 读取播放列表下一首歌曲*/
    fun next()
    /**
     * 读取播放列表上一首歌曲*/
    fun previous()
    /**
     * 当前是否正在播放*/
    fun isPlaying(): Boolean
    /**
     * 歌曲总时长*/
    fun duration(): Long
    /**
     * 当前播放进度*/
    fun position(): Long
    /**
     * 改变进度条*/
    fun seek(pos: Long): Long
}