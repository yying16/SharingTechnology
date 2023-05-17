package com.project.androiddbpart.domain

import android.graphics.Bitmap

class User constructor() {
     var account = "" // 账号
     var password = "" // 密码
     var username = "" // 用户名
     var telephone = "" // 电话号码
     var email = "" // 邮箱
    var flag = false
    var status = false // 身份
    var balance = 0 // 能力值
    var credit = 0// 余额
    var userImage: Bitmap? = null // 用户头像


    constructor(ac:String,psw:String) : this() {
        account = ac
        password = psw
    }

    constructor(acc:String,psw:String,name:String,tel:String,em:String) : this() {
        account = acc
        password = psw
        username = name
        telephone = tel
        email = em
    }

    //用于查找所有用户
    constructor(account:String,password:String,username:String,telephone:String,email:String,balance:Int,credit:Int) : this() {
        this.account = account
        this.password = password
        this.username = username
        this.telephone = telephone
        this.email = email
        this.status = false // 身份
        this.balance = balance // 能力值
        this.credit = credit// 余额
    }

    constructor(f:Boolean) : this() {
        flag = f
    }

    override fun toString(): String {
        return "User(account=$account, username=$username, " +
                "password=$password, telephone=$telephone, email=$email, " +
                "status=$status, balance=$balance, credit=$credit)"
    }

}
