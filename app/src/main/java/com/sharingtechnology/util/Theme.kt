package com.sharingtechnology.util

import android.graphics.Color
import com.sharingtechnology.R

object Theme {
    var presentTheme = R.style.AppTheme
    const val darkTheme = R.style.DarkTheme
    const val lightTheme = R.style.AppTheme

    fun setTheme(theme:Int){ // 设置主题
        presentTheme = theme
    }

    /*动态修改主题*/
    fun getBottomColor():Int{ // 底部导航栏颜色
        return if(presentTheme==darkTheme){
            Color.parseColor("#303030")
        }else{
            Color.parseColor("#FFFFFF")
        }
    }

    fun getFontColor():Int{ // 状态栏字体颜色
        return if(presentTheme==darkTheme){
            Color.parseColor("#BEBEBE")
        }else{
            Color.parseColor("#7675b9")
        }
    }

    fun getWordCloudColor():Int{ // 词云背景颜色
        return if(presentTheme==darkTheme){
            Color.parseColor("#292725")
        }else{
            Color.parseColor("#F2F2F2")
        }
    }

    fun getItemBackground():Int{
        return if(presentTheme==darkTheme){
            R.drawable.list_border_dark
        }else{
            R.drawable.list_border
        }
    }

    fun getSearchStyle():Int{
        return if(presentTheme==darkTheme){
            R.style.searchDialogDark
        }else{
            R.style.searchDialog
        }
    }
}