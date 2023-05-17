package com.sharingtechnology.util

import android.graphics.Bitmap

object OnlineUser {
    var account = "" // 账号
    var password = "" // 密码
    var username = "" // 用户名
    var telephone = "" // 电话号码
    var email = "" // 邮箱
    var status = false // 身份
    var balance = 0 // 能力值
    var credit = 0 // 余额
    var userImage: Bitmap? = null // 用户头像
    var wordCloud: Bitmap? = null // 词云
    var indexTable: Bitmap? = null // 指数表

    fun logout() {
        this.account = "" // 账号
        this.password = "" // 密码
        this.username = "" // 用户名
        this.telephone = "" // 电话号码
        this.email = "" // 邮箱
        this.status = false // 身份
        this.balance = 0 // 能力值
        this.credit = 0 // 余额
        this.userImage = null // 用户头像
        this.wordCloud = null // 词云
        this.indexTable = null // 指数表
        WordCloud.usedList = ArrayList<String>()
        WordCloud.usedList.add("程序不是年轻的专利，但是，他属于年轻")
        WordCloud.usedList.add("信念和目标，必须永远洋溢在程序员内心")

    }
}