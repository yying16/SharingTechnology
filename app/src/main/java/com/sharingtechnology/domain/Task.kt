package com.project.androiddbpart.domain

import com.sharingtechnology.util.OnlineUser

class Task() {
    var taskId = 0 // 任务id
    var priority = 0.0 // 任务优先级
    var releaseTime = "" // 任务发布时间
    var deadline = "" // 任务截止时间
    var taskTitle = "" // 任务标题
    var taskCategory = "" // 任务标题
    var taskContent = "" // 任务内容
    var money = 0 // 任务报酬(悬赏度)
    var senderAccount = "" // 发送者账号
    var senderName = "" // 发送者昵称
    var state = 0 // 任务完成状态(0代表待审核，-1代表未完成，1代表完成)
    var senderBalance = 0
    var senderCredit = 0
    constructor(taskId: Int, priority: Double, releaseTime: String, deadline: String, taskTitle: String, taskCategory: String, taskContent: String, money: Int,senderAccount:String,state:Int) : this() {
        this.taskId = taskId
        this.priority = priority
        this.releaseTime = releaseTime
        this.deadline = deadline
        this.taskTitle = taskTitle
        this.taskCategory = taskCategory
        this.taskContent = taskContent
        this.money = money
        this.senderAccount = senderAccount
        this.state = state
    }


    constructor(taskId: Int, priority: Double, releaseTime: String, deadline: String, taskTitle: String, taskCategory: String, taskContent: String, money:Int) : this() {
        this.taskId = taskId
        this.priority = priority
        this.releaseTime = releaseTime
        this.deadline = deadline
        this.taskTitle = taskTitle
        this.taskCategory = taskCategory
        this.taskContent = taskContent
        this.money = money
        this.senderAccount = OnlineUser.account
        this.senderName = OnlineUser.username
    }

    constructor( deadline: String, taskTitle: String, taskCategory: String, taskContent: String, money: Int) : this() {
        this.releaseTime = releaseTime
        this.deadline = deadline
        this.taskTitle = taskTitle
        this.taskCategory = taskCategory
        this.taskContent = taskContent
        this.money = money
        this.senderAccount = OnlineUser.account
        this.senderName = OnlineUser.username
    }

    constructor(priority: Double,deadline: String, taskTitle: String, taskCategory: String, taskContent: String, money: Int) : this() {
        this.priority = priority
        this.releaseTime = releaseTime
        this.deadline = deadline
        this.taskTitle = taskTitle
        this.taskCategory = taskCategory
        this.taskContent = taskContent
        this.money = money
        this.senderAccount = OnlineUser.account
        this.senderName = OnlineUser.username
    }

    override fun toString(): String {
        return "Task[taskId=$taskId, priority=$priority, releaseTime=$releaseTime, " +
                "taskCategory=$taskCategory,deadline=$deadline, taskTitle=$taskTitle, " +
                "taskContent=$taskContent, money=$money, sender(account)=$senderAccount, state=$state]"
    }
}