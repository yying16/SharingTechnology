package com.sharingtechnology.domain

import com.project.androiddbpart.domain.Task
import com.project.androiddbpart.domain.User

class Record constructor() {
    var user : User? = null
    var task : Task? = null
    var startTime = ""
    var endTime = ""
    var state = false

    constructor(
        user: User,
        task: Task,
        startTime: String,
        endTime: String,
        state: Boolean): this() {
        this.user = user
        this.task = task
        this.startTime = startTime
        this.endTime = endTime
        this.state = state
    }
}