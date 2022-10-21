package com.franz.easymusicplayer.param

class MusicParam {
    companion object{
        /**
         * 所有网址都接在baseURL后面*/
        @JvmStatic
        val baseURL: String = "http://111.67.192.132:3000"
        @JvmStatic
        val loginURL: String = "/login/cellphone"//密码、验证码登陆接口
        @JvmStatic
        val sendIdentify: String = "/captcha/sent"//发送短信验证码
        @JvmStatic
        val verifyResult:String = "/captcha/verify"//验证短信验证码
        @JvmStatic
        val userAllSongList = "/user/playlist"//获取用户所有的歌单，包括创建的和收藏
        @JvmStatic
        val qrCodeKey = "/login/qr/key"//获取二维码key
        @JvmStatic
        val qrCodeCreate = "/login/qr/create"//通过二维码key生成二维码图片,base64图片
        @JvmStatic
        val qrCodeStatus = "/login/qr/check"//检测二维码扫描状态
        @JvmStatic
        val accountInformation = "/user/account"//根据cookie获取账户信息
        @JvmStatic
        val subcountInformation = "/user/subcount"//获取用户信息 , 歌单，收藏，mv, dj 数量
        @JvmStatic
        val accountLoveSong = "/likelist"//获取用户喜欢的音乐列表
        @JvmStatic
        val songListData = "/playlist/track/all"//获取歌单内所有歌曲数据
        @JvmStatic
        val songUrl = "/song/url/v1"//获取歌曲链接
        @JvmStatic
        val searchResult = "/cloudsearch"//获取用户搜索的模糊关键字列表
        @JvmStatic
        val recommendMusic = "/personalized/newsong"//获取推荐歌曲
        @JvmStatic
        val recommendSongs = "/personalized"//获取推荐歌单
        @JvmStatic
        val recommendMv = "/personalized/mv"//获取推荐mv
        @JvmStatic
        val albumSongs = "/album"//获取专辑内所有歌曲
        @JvmStatic
        val mvVideoUrl = "/mv/url"//获取mv视频地址
        @JvmStatic
        val mvVideoInfo = "/mv/detail"//获取mv点赞收藏等内容
        @JvmStatic
        val recentlySong = "/record/recent/song"//最近播放的歌曲
        @JvmStatic
        val recentlyList = "/record/recent/playlist"//最近播放的歌单
        @JvmStatic
        val recentlyAlbum = "/record/recent/album"//最近播放的专辑
        @JvmStatic
        val recentlyMv = "/record/recent/video"//最近播放的视频
        @JvmStatic
        val lyric = "/lyric"//获取歌曲歌词
        @JvmStatic
        val banner = "/banner"//获取banner
        @JvmStatic
        val topRank = "/toplist/detail"//获取所有排行榜
        @JvmStatic
        val mvURL = "/mlog/music/rcmd"//根据歌曲id获取mvid
        @JvmStatic
        val mlogURL = "/mlog/url"//获取mlog视频url
        @JvmStatic
        val songDetailInfo = "/song/detail"//获取歌曲详细信息
        @JvmStatic
        val userLevel = "/user/level"//获取用户等级
        @JvmStatic
        val userDetail = "/user/detail"//获取用户详情，包括等级，粉丝，关注等
        @JvmStatic
        val downloadUrl = "/song/download/url"//获取音乐下载url,因为某些账号不是vip,无法获取无损获超清品质音乐，通过此链接可获取
    }
}