package com.sharingtechnology.util

import android.annotation.SuppressLint
import com.project.androiddbpart.domain.Task
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Time {
    var taskIndex: Task? = null
    fun format(yyyy: Int, mm: Int, dd: Int, HH: Int, MM: Int): String // 日期格式化
            = String.format("%02d-%02d-%02d %02d:%02d", yyyy, mm, dd, HH, MM)

    fun getPresentTime(): String =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now())

    @SuppressLint("SimpleDateFormat")
    fun getMillisecond(date:String):Long{ // 将时间段转换为距离当前时间的毫秒数（时间戳）
        try{
            val temp = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date)
            return temp.time - System.currentTimeMillis()
        }catch (e:Exception){
            return 0
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getIndexTableDate():String{
        try{
            val now = Date()
            val date = Date(now.time - 9676800000L)
            date.hours = 0
            date.minutes = 0
            return SimpleDateFormat("yyyy-MM-dd HH:mm").format(date)
        }catch (e:Exception){
            return ""
        }
    }
    fun subtract(a:String,b:String):Long{
        val aTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(a).time
        val bTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(a).time
        return (aTime - bTime)
    }

    fun getTime(a:String):Long{ // 输入截至时间，返回毫秒数
        val aTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(a).time
        val bTime = Date().time
        return (aTime - bTime)
    }
}