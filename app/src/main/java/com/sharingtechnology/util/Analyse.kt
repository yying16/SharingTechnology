package com.sharingtechnology.util

import android.graphics.Color
import android.util.Log
import com.project.androiddbpart.domain.Task

object Analyse {
    val colors = arrayOf(
        Color.rgb(32, 205, 205),
        Color.rgb(114, 188, 223),
        Color.rgb(255, 123, 124),
        Color.rgb(57, 135, 200),
        Color.rgb(62, 53, 64),
        Color.rgb(88, 97, 115),
        Color.rgb(217, 215, 204),
        Color.rgb(191, 168, 147),
        Color.rgb(140, 117, 112),
        Color.rgb(68, 87, 102),
        Color.rgb(82, 111, 132),
        Color.rgb(209, 225, 233),
        Color.rgb(1, 52, 64),
        Color.rgb(62, 132, 140),
        Color.rgb(139, 166, 147),
        Color.rgb(255, 255, 245))
    val taskCategory = arrayOf("设计架构", "前端开发", "后端开发", "Android开发", "IOS开发", "微信小程序开发", "全栈开发", "搭建大数据集群", "大数据计算", "数据挖掘分析", "数据爬取", "项目测试", "GUI程序", "其他")

    //计算任务优先级
    fun getPriority(money:Int,ddl:String): Double { // 计算任务的优先级
        try{
            val d = Time.getMillisecond(ddl)*0.0000000006
            return (OnlineUser.credit * 0.001 + OnlineUser.balance * 0.1 + money * 0.001+d)*0.1
        }catch (e:Exception){
            return 10.0
        }
    }




}