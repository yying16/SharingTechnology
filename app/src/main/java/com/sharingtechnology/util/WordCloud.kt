package com.sharingtechnology.util

import com.huaban.analysis.jieba.JiebaSegmenter

object WordCloud { // 管理词云
    //因为更新存在一定的滞后性，所以每次先用usedList顶替上去，待list更新完毕后将list替代usedList
    var list = ArrayList<String>() // 待更新的分词
    var usedList = ArrayList<String>() // 已使用的分词
    init {
        usedList.add("程序员")
        usedList.add("程序开发")
    }
    fun refresh(content:String){ // 更新list并将list替代usedList
        val symbol = "~!@#$%^&*()（）-=——+_+-=, 。。、,./《》？<>?,。、‘；;':\"“：】,【【】<>《》{}[]微信qqQQ附件价格登录操作目前执行完成大中小一二三四五六七八九十1234567890\\"
        val segmenter = JiebaSegmenter() // jieba分词
        val words = segmenter.sentenceProcess(content)
        this.list = ArrayList<String>()
        for (word in words){
            if(symbol.contains(word.trim())||word.trim().length<3||word.matches(Regex("\\s"))){
                continue
            }
            list.add(word)
        }
        if(list.size>2){
            usedList = list // 同步更新
        }

    }
}