package com.sharingtechnology.helper


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.util.Log
import androidx.core.database.getBlobOrNull
import com.project.androiddbpart.domain.Task
import com.project.androiddbpart.domain.User
import com.sharingtechnology.domain.Record
import com.sharingtechnology.util.BlobConverter
import com.sharingtechnology.util.OnlineUser
import com.sharingtechnology.util.Time
import com.sharingtechnology.util.Time.getIndexTableDate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// 负责数据库表的增删改查
class DatabaseHelper(context: Context, version: Int) : SQLiteOpenHelper(context, "data.db", null, version) {

    // t_user
    private var sql1 = "create table if not exists t_user(" +
            "account nvarchar(30) primary key not null," + // 账号
            "password nvarchar(30)  not null," + // 密码
            "username nvarchar(30) not null," + // 用户名
            "telephone nvarchar(30) not null," + // 电话号码
            "email nvarchar(30) not null," + // 邮箱
            "status int default 0," + // 身份(1代表管理员 0代表普通用户)
            "balance int default 0," + // 能力值 默认为0
            "credit int default 0," + // 余额 默认为0
            "user_image blob" + // 用户头像
            ")"

    // t_task
    private var sql2 = "create table if not exists t_task(" +
            "task_id integer primary key autoincrement not null ," + // 任务id
            "task_priority double default 0," + // 任务优先级
            "release_time nvarchar(30) not null," + // 发布时间
            "deadline nvarchar(30) not null," + // 截止时间
            "task_title nvarchar(30) not null," + // 任务标题
            "task_content text not null," + // 任务内容
            "task_category nvarchar(30) not null," + // 任务分类
            "money int ," + // 任务悬赏度
            "sender nvarchar(30)," + // 发送者账号
            "state int default 0," + // 任务完成状态(0代表审核状态 1代表完成,-1表示未完成)
            "constraint task_fk foreign key(sender) references t_user(account)" +
            ")"

    private var sql3 = "create table if not exists t_record(" +
            "account nvarchar(30)," + // 接受者账号 (t_user外键account)
            "task_id nvarchar(30)," + // 任务id (t_task外键task_id)
            "start_time nvarchar(30) not null," + // 接受时
            "end_time nvarchar(30)," + // 完成时间
            "state boolean default 0," + //完成状态(0代表未完成 1代表完成)
            "constraint record_pk primary key(account, task_id)," +
            "constraint record_fk1 foreign key(account) references t_user(account)," +
            "constraint record_fk2 foreign key(task_id) references t_task(task_id)" +
            ")"


    // v_record
    private var sql4 = "create view v_record as " +
            "select " +
            "u.account u_account,u.username u_username,u.balance u_balance,u.credit u_credit," +
            "s.account s_account,s.username s_username,s.balance s_balance,s.credit s_credit," +
            "t.task_id task_id,t.task_priority task_priority,t.release_time release_time," +
            "t.deadline deadline,t.task_title task_title," +
            "t.task_content task_content,t.task_category task_category,t.money money," +
            "r.start_time start_time,r.end_time end_time,r.state state " +
            "from t_record r," +
            "t_task t," +
            "t_user u, t_user s " +
            "where r.account = u.account " +
            "and r.task_id = t.task_id " +
            "and t.sender = s.account"

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL(sql1)
        p0?.execSQL(sql2)
        p0?.execSQL(sql3)
        p0?.execSQL(sql4)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        Log.d("[$p0][DATABASE_VERSION_UPDATE]", "VERSION_$p1 --> VERSION_$p2")
        p0?.execSQL("drop table if exists t_user")
        p0?.execSQL("drop table if exists t_task")
        p0?.execSQL("drop table if exists t_record")
        p0?.execSQL("drop view if exists v_record")
        onCreate(p0)
    }


    fun init() {//初始胡数据库
        register(User("1712131536", "wsadzxc123", "迎迎", "13128438703", "1712131536@qq.com"))
        register(User("2704145572", "wsadzxc123", "YYing", "13128438703", "2704145572@qq.com"))
        register(User("1478963250", "wsadzxc123", "yingying", "13128438703", "1478963250@qq.com"))
    }

    /*-------------------------------user----------------------------------*/
    @SuppressLint("Range", "Recycle")
    fun login(account: String, password: String): User { // 登录验证
        try {
            val temp1 = writableDatabase.query("t_user", null, "account = ? ", arrayOf(account), null, null, null)
            if (temp1.count > 0) { // 账号存在
                val cursor = writableDatabase.query("t_user", null, "account = ? and password = ? ", arrayOf(account, password), null, null, null)
                return if (cursor.count > 0) { // 密码正确
                    if (cursor.moveToFirst()) {
                        //将用户信息填写到OnlineUser上
                        OnlineUser.account = cursor.getString(cursor.getColumnIndex("account"))
                        OnlineUser.password = cursor.getString(cursor.getColumnIndex("password"))
                        OnlineUser.username = cursor.getString(cursor.getColumnIndex("username"))
                        OnlineUser.telephone = cursor.getString(cursor.getColumnIndex("telephone"))
                        OnlineUser.status = cursor.getInt(cursor.getColumnIndex("status")) == 1
                        OnlineUser.email = cursor.getString(cursor.getColumnIndex("email"))
                        OnlineUser.balance = cursor.getInt(cursor.getColumnIndex("balance"))
                        OnlineUser.credit = cursor.getInt(cursor.getColumnIndex("credit"))
                        if (cursor.getBlob(cursor.getColumnIndex("user_image")) != null) {
                            Log.d("TAG", "login: ${cursor.getBlob(cursor.getColumnIndex("user_image"))}")
                            OnlineUser.userImage = BlobConverter.avatarDecoder(cursor.getBlob(cursor.getColumnIndex("user_image")))
                        }
                    }
                    User(true)
                } else {// 密码错误
                    User("", "账号或密码错误")
                }

            } else {
                return User("账号不存在", "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return User("", "账号或密码错误")
        }
    }

    fun register(user: User): User { // 注册账号
        Log.d("TAG", "register: ${user.toString()}")
        var flag = true // 数据格式
        var ret = User()
        if (user.account.isEmpty()) {
            flag = false
            ret.account = "账号不能为空"
        } else if ((writableDatabase.query("t_user", null, "account = ? ", arrayOf(user.account), null, null, null)).count > 0) {
            flag = false
            ret.account = "账号已注册，请点击登录接口直接登录"
        } else if (!user.account.matches(Regex("^\\w{4,16}\$"))) {
            flag = false
            ret.account = "账号长度应为4-18个字符,且只能由字母和数字组成"
        }

        if (user.password.isEmpty()) {
            flag = false
            ret.password = "密码不能为空"
        } else if (!user.password.matches(Regex("^\\w{4,16}\$"))) {
            flag = false
            ret.password = "密码长度应为4-18个字符,且只能由字母和数字组成"
        }
        if (user.username.isEmpty()) {
            flag = false
            ret.username = "用户名不能为空"
        } else if (!user.username.matches(Regex("^\\w{2,8}\$"))) {
            flag = false
            ret.username = "用户名长度应为2-8个字符,且只能由字母和数字组成"
        }
        if (!user.telephone.matches(Regex("^1[356789]\\d{9}\$"))) {
            flag = false
            ret.telephone = "电话号码格式不正确"
        }
        if (!user.email.matches(Regex("^\\w+@\\w+(\\.\\w+)$"))) {
            flag = false
            ret.email = "邮箱格式不正确"
        }
        if (flag) { // 数据格式正确
            val value = ContentValues().apply {
                put("account", user.account)
                put("password", user.password)
                put("username", user.username)
                put("telephone", user.telephone)
                put("email", user.email)
            }
            writableDatabase.insert("t_user", null, value)
            return User(true)
        } else {
            return ret
        }
    }

    fun changeUserImage(bitmap: Bitmap): Boolean { // 修改头像
        return try {
            val value = ContentValues()
            value.put("user_image", BlobConverter.avatarEncoder(bitmap))
            writableDatabase.update("t_user", value, "account = ?", arrayOf(OnlineUser.account))
            OnlineUser.userImage = bitmap
            Log.d("TAG", "changeUserImage: true")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun changePassword(newPassword: String): Boolean { // 用户修改密码
        try {
            val value = ContentValues()
            value.put("password", newPassword)
            writableDatabase.update("t_user", value, "account = ?", arrayOf(OnlineUser.account))
        } catch (e: Exception) {
            return false
        }
        OnlineUser.password = newPassword
        return true
    }

    @SuppressLint("Range")
    fun queryAllUser(): ArrayList<User> {
        try {
            val cursor = writableDatabase.query("t_user", null, "status = ?", arrayOf("0"), null, null, null)
            val list = ArrayList<User>()
            while (cursor.moveToNext()) {
                val u = User(
                    cursor.getString(cursor.getColumnIndex("account")),
                    cursor.getString(cursor.getColumnIndex("password")),
                    cursor.getString(cursor.getColumnIndex("username")),
                    cursor.getString(cursor.getColumnIndex("telephone")),
                    cursor.getString(cursor.getColumnIndex("email")),
                    cursor.getInt(cursor.getColumnIndex("balance")),
                    cursor.getInt(cursor.getColumnIndex("credit"))
                )
                if (cursor.getBlobOrNull(cursor.getColumnIndex("user_image")) != null) {
                    u.userImage = BlobConverter.avatarDecoder(cursor.getBlob(cursor.getColumnIndex("user_image")))
                }
                list.add(u)
            }
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList<User>()
        }
    }




    /*---------------------------Task--------------------------------*/
    // 添加一个任务
    fun addTask(task: Task): Boolean {
        try {
            val contentValues = ContentValues().apply {
                put("task_priority", task.priority)
                put("release_time", Time.getPresentTime())
                put("deadline", task.deadline)
                put("task_title", task.taskTitle)
                put("task_category", task.taskCategory)
                put("task_content", task.taskContent)
                put("money", task.money)
                put("sender", OnlineUser.account)
                put("state", task.state)
            }
            val value = ContentValues()
            value.put("credit", OnlineUser.credit - task.money)
            writableDatabase.insert("t_task", null, contentValues)
            writableDatabase.update("t_user", value, "account = ?", arrayOf(OnlineUser.account))
            OnlineUser.credit = OnlineUser.credit - task.money
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


    @SuppressLint("Range")
    fun queryMyTask(state: Int): ArrayList<Task> {
        try {
            val wd = this.writableDatabase
            val list = ArrayList<Task>()
            val csr = wd.query("t_task", null, "sender = ? and state = ?", arrayOf(OnlineUser.account, state.toString()), null, null, null)
            while (csr.moveToNext()) {
                list.add(
                    Task(
                        csr.getInt(csr.getColumnIndex("task_id")),
                        csr.getDouble(csr.getColumnIndex("task_priority")),
                        csr.getString(csr.getColumnIndex("release_time")),
                        csr.getString(csr.getColumnIndex("deadline")),
                        csr.getString(csr.getColumnIndex("task_title")),
                        csr.getString(csr.getColumnIndex("task_category")),
                        csr.getString(csr.getColumnIndex("task_content")),
                        csr.getInt(csr.getColumnIndex("money")),
                        csr.getString(csr.getColumnIndex("sender")),
                        csr.getInt(csr.getColumnIndex("state"))
                    )
                )
            }
            csr.close()
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList<Task>()
        }
    }

    @SuppressLint("Range")
    fun queryAllTask(): ArrayList<Task> {
        try {
            val wd = this.writableDatabase
            val list = ArrayList<Task>()
            val csr = wd.query("t_task", null, null, null, null, null, null)
            while (csr.moveToNext()) {
                val t = Task(
                    csr.getInt(csr.getColumnIndex("task_id")),
                    csr.getDouble(csr.getColumnIndex("task_priority")),
                    csr.getString(csr.getColumnIndex("release_time")),
                    csr.getString(csr.getColumnIndex("deadline")),
                    csr.getString(csr.getColumnIndex("task_title")),
                    csr.getString(csr.getColumnIndex("task_category")),
                    csr.getString(csr.getColumnIndex("task_content")),
                    csr.getInt(csr.getColumnIndex("money")),
                    csr.getString(csr.getColumnIndex("sender")),
                    csr.getInt(csr.getColumnIndex("state"))
                )
                val acc = csr.getString(csr.getColumnIndex("sender"))
                val cr = wd.query("t_user",null,"account = ?", arrayOf(acc),null,null,null)
                if(cr.moveToFirst()){
                    val balance = cr.getInt(cr.getColumnIndex("balance"))
                    val credit = cr.getInt(cr.getColumnIndex("credit"))
                    t.senderBalance = balance
                    t.senderCredit = credit
                    list.add(t)
                }
                cr.close()
            }
            csr.close()
            wd.close()
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList<Task>()
        }
    }


    @SuppressLint("Range")
    fun queryAllTask(state: Int): ArrayList<Task> {
        try {
            val wd = this.writableDatabase
            val list = ArrayList<Task>()
            val csr = wd.query("t_task", null, "state = ?", arrayOf(state.toString()), null, null, null)
            while (csr.moveToNext()) {
                list.add(
                    Task(
                        csr.getInt(csr.getColumnIndex("task_id")),
                        csr.getDouble(csr.getColumnIndex("task_priority")),
                        csr.getString(csr.getColumnIndex("release_time")),
                        csr.getString(csr.getColumnIndex("deadline")),
                        csr.getString(csr.getColumnIndex("task_title")),
                        csr.getString(csr.getColumnIndex("task_category")),
                        csr.getString(csr.getColumnIndex("task_content")),
                        csr.getInt(csr.getColumnIndex("money")),
                        csr.getString(csr.getColumnIndex("sender")),
                        csr.getInt(csr.getColumnIndex("state"))
                    )
                )
            }
            csr.close()
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList<Task>()
        }
    }

    // 方法重写 (content 表示关键字)
    @SuppressLint("Range") // 关键字查询采用 模糊查询
    fun queryTask(content: String): ArrayList<Task> {
        val wd = this.writableDatabase
        val list = ArrayList<Task>()

        val csr: Cursor = if (content.trim().isEmpty()) wd.query(
            "t_task",
            null, null, null, null,
            null, null
        ) else wd.query(
            "t_task", null,
            "(task_title like ? or task_content like ? or task_category like ?)",
            arrayOf("%$content%", "%$content%", "%$content%"), null, null, null
        )

        while (csr.moveToNext()) {
            list.add(
                Task(
                    csr.getInt(csr.getColumnIndex("task_id")),
                    csr.getDouble(csr.getColumnIndex("task_priority")),
                    csr.getString(csr.getColumnIndex("release_time")),
                    csr.getString(csr.getColumnIndex("deadline")),
                    csr.getString(csr.getColumnIndex("task_title")),
                    csr.getString(csr.getColumnIndex("task_category")),
                    csr.getString(csr.getColumnIndex("task_content")),
                    csr.getInt(csr.getColumnIndex("money")),
                    csr.getString(csr.getColumnIndex("sender")),
                    csr.getInt(csr.getColumnIndex("state"))
                )
            )
        }
        csr.close()
        return list
    }

    @SuppressLint("Range")
    fun queryMyTask(content: String, state: Int): ArrayList<Task> {
        try {
            val wd = this.writableDatabase
            val list = ArrayList<Task>()

            val csr = if (content.isEmpty()) wd.query(
                "t_task", null, "sender = ? and state = ?",
                arrayOf(OnlineUser.account, state.toString()), null, null, null
            )
            else wd.query(
                "t_task", null, "sender = ? and state = ? and " +
                        "(task_title like ? or task_content like ? or task_category like ?)",
                arrayOf(OnlineUser.account, state.toString(), "%$content%", "%$content%","%$content%"),
                null, null, null
            )

            while (csr.moveToNext()) {
                list.add(
                    Task(
                        csr.getInt(csr.getColumnIndex("task_id")),
                        csr.getDouble(csr.getColumnIndex("task_priority")),
                        csr.getString(csr.getColumnIndex("release_time")),
                        csr.getString(csr.getColumnIndex("deadline")),
                        csr.getString(csr.getColumnIndex("task_title")),
                        csr.getString(csr.getColumnIndex("task_category")),
                        csr.getString(csr.getColumnIndex("task_content")),
                        csr.getInt(csr.getColumnIndex("money")),
                        csr.getString(csr.getColumnIndex("sender")),
                        csr.getInt(csr.getColumnIndex("state"))
                    )
                )
            }
            csr.close()
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList<Task>()
        }
    }

    // type = 0 -> 综合
    // type = 1 -> 悬赏
    // type = 2 -> 截至时间
    @SuppressLint("Range")
    fun queryAllTask(state: Int, type: Int = 0, asc: Boolean = true): ArrayList<Task> {
        try {
            val wd = this.writableDatabase
            val list = ArrayList<Task>()
            val orderBy = when (type) {
                0 -> {"task_priority desc"}
                1 -> when (asc) {
                    true -> "money"
                    else -> "money desc" // false
                }
                2 -> when (asc) {
                    true -> "deadline"
                    else -> "deadline desc" // false
                }
                else -> null // (后续补充) task_priority
            }

            val csr = wd.query(
                "t_task", null, "state = ?", arrayOf(state.toString()),
                null, null, orderBy
            )
            while (csr.moveToNext()) {
                list.add(
                    Task(
                        csr.getInt(csr.getColumnIndex("task_id")),
                        csr.getDouble(csr.getColumnIndex("task_priority")),
                        csr.getString(csr.getColumnIndex("release_time")),
                        csr.getString(csr.getColumnIndex("deadline")),
                        csr.getString(csr.getColumnIndex("task_title")),
                        csr.getString(csr.getColumnIndex("task_category")),
                        csr.getString(csr.getColumnIndex("task_content")),
                        csr.getInt(csr.getColumnIndex("money")),
                        csr.getString(csr.getColumnIndex("sender")),
                        csr.getInt(csr.getColumnIndex("state"))
                    )
                )
            }
            csr.close()
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList<Task>()
        }
    }

    /*-----------------------record----------------------------*/

    fun addRecord(task: Task): Boolean {
        val account = OnlineUser.account
        val startTime = Time.getPresentTime()

        try {
            val wd = this.writableDatabase
            val cv1 = ContentValues().apply {
                put("account", account)
                put("task_id", task.taskId)
                put("start_time", startTime)
                put("end_time", "")
                put("state", 0)
            }
            val cv2 = ContentValues().apply {
                put("state", 1)
            }
            wd.apply {
                insert("t_record", null, cv1)
                update("t_task", cv2, "task_id = ?", arrayOf(task.taskId.toString()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    // 用户查询自己所有的接受任务的记录(查询 v_record 视图) (state 代表任务完成状态) 包含方法重写
    @SuppressLint("Range")
    fun queryMyRecord(state: Boolean): ArrayList<Record> {
        val list = ArrayList<Record>()
        val wd = this.writableDatabase
        var s = "0"
        if (state)
            s = "1"
        val csr = wd.query(
            "v_record", null, "u_account = ? and state = ?",
            arrayOf(OnlineUser.account, s), null, null, null
        )

        return try {
            while (csr.moveToNext()) {
                val user = User()
                val task = Task()

                csr.apply {
                    user.account = getString(getColumnIndex("u_account"))
                    user.username = getString(getColumnIndex("u_username"))
                    user.balance = getInt(getColumnIndex("u_balance"))
                    user.credit = getInt(getColumnIndex("u_credit"))
                    task.taskId = getInt(getColumnIndex("task_id"))
                    task.priority = getDouble(getColumnIndex("task_priority"))
                    task.releaseTime = getString(getColumnIndex("release_time"))
                    task.deadline = getString(getColumnIndex("deadline"))
                    task.taskTitle = getString(getColumnIndex("task_title"))
                    task.taskCategory = getString(getColumnIndex("task_category"))
                    task.taskContent = getString(getColumnIndex("task_content"))
                    task.senderAccount = getString(getColumnIndex("s_account"))
                    task.senderName = getString(getColumnIndex("s_username"))
                    task.money = getInt(getColumnIndex("money"))
                }

                list.add(
                    Record(
                        user,
                        task,
                        csr.getString(csr.getColumnIndex("start_time")),
                        csr.getString(csr.getColumnIndex("end_time")),
                        csr.getColumnIndex("state") == 1
                    )
                )
            }
            csr.close()
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        }
    }

    @SuppressLint("Range") //(content关键字条件查找任务)
    fun queryMyRecord(content: String, state: Boolean): ArrayList<Record> {
        var s = 0
        if(state){
            s = 1
        }
        val list = ArrayList<Record>()
        val wd = this.writableDatabase
        val csr = if (content.trim().isEmpty()) wd.query(
            "v_record", null,
            "u_account like ? and state = ?", arrayOf(OnlineUser.account, s.toString()),
            null, null, null
        )
        else wd.query(
            "v_record", null, "u_account = ? and state = ? " +
                    "and (task_title like ? or task_content like ? or task_category like ?)",
            arrayOf(OnlineUser.account, s.toString(), "%$content%", "%$content%", "%$content%"), null, null, null
        )

        return try {
            while (csr.moveToNext()) {
                val user = User()
                val task = Task()
                csr.apply {
                    user.account = getString(getColumnIndex("u_account"))
                    user.username = getString(getColumnIndex("u_username"))
                    user.balance = getInt(getColumnIndex("u_balance"))
                    user.credit = getInt(getColumnIndex("u_credit"))
                    task.taskId = getInt(getColumnIndex("task_id"))
                    task.priority = getDouble(getColumnIndex("task_priority"))
                    task.releaseTime = getString(getColumnIndex("release_time"))
                    task.deadline = getString(getColumnIndex("deadline"))
                    task.taskTitle = getString(getColumnIndex("task_title"))
                    task.taskCategory = getString(getColumnIndex("task_category"))
                    task.taskContent = getString(getColumnIndex("task_content"))
                    task.senderAccount = getString(getColumnIndex("s_account"))
                    task.senderName = getString(getColumnIndex("s_username"))
                    task.money = getInt(getColumnIndex("money"))
                }

                list.add(
                    Record(
                        user,
                        task,
                        csr.getString(csr.getColumnIndex("start_time")),
                        csr.getString(csr.getColumnIndex("end_time")),
                        csr.getColumnIndex("state") == 1
                    )
                )
            }
            csr.close()
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        }
    }

    // 用户完成任务 (在 t_task 里更新 state 的值) 在逻辑中会一起使用 finishTask 和 updateRecord
    fun finishTask(task: Task): Boolean {
        val cv1 = ContentValues().apply {
            put("state", 2) // 任务状态发生变化
        }
        val cv2 = ContentValues().apply {
            put("balance", OnlineUser.balance + 1) // 用户能力值提升（后续修改）
            put("credit", OnlineUser.credit + task.money) // 用户积分增加
        }
        try {
            val res1 = writableDatabase.update(
                "t_task", cv1,
                "task_id = ?", arrayOf(task.taskId.toString())
            )
            // 完成任务的用户 其一些数值会发生变化
            val res2 = writableDatabase.update(
                "t_user", cv2,
                "account = ?", arrayOf(OnlineUser.account)
            )
            OnlineUser.credit = OnlineUser.credit + task.money
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    // 更新记录 (更新t_record and v_record) 在逻辑中将配合 finishTask 一起使用
    fun updateRecord(record: Record): Boolean {
        val endTime = Time.getPresentTime()
        val cv = ContentValues().apply {
            put("end_time", endTime)
            put("state", 1)
        }
        try {
            writableDatabase.apply {
                update(
                    "t_record", cv, "account = ? and task_id = ?",
                    arrayOf(record.user?.account, record.task?.taskId.toString())
                )
                execSQL("drop view if exists v_record")
                execSQL(sql4)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /*------------------------数据处理---------------------*/
    fun getFinishedTaskNumber(category:String):Int{
        val cursor2 = writableDatabase.query("v_record",null,"task_category = ? and state = 1", arrayOf(category),null,null,null)
        return cursor2.count
    }

    fun getTaskNumber(category:String):Int{
        val cursor1 = writableDatabase.query("v_record",null,"task_category = ?", arrayOf(category),null,null,null)
        return cursor1.count
    }


    /*--------------------------数据可视化--------------------------*/
    // 词云数据 (找出当前 OnlineUser 的所有发放的任务和所有接受的任务)
    // 拼接这些任务的 标题 和 内容 数据
    @SuppressLint("Range")
    fun getMyWorldCloud(): String {
        var res = ""
        val curUserAccount = OnlineUser.account

        val csr: Cursor = writableDatabase.query(
            "v_record", null,
            "u_account = ? or s_account = ?", arrayOf(curUserAccount, curUserAccount),
            null, null, null
        )

        try {
            while (csr.moveToNext()) {
                val taskTitle = csr.getString(csr.getColumnIndex("task_title"))
                val taskContent = csr.getString(csr.getColumnIndex("task_content"))
                res += "$taskTitle,$taskContent\n"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
        return res
    }

    // 按任务类别返回任务总数
    fun getTaskNumberFinishedCategory(taskCategory: String): Int {
        try{
            val cursor = writableDatabase.query("v_record",null, "task_category = ? and state = ?",arrayOf(taskCategory,"1"),null,null,null)
            return cursor.count
        }catch (e:Exception){
            e.printStackTrace()
            return 0
        }
    }

    // 按任务类别返回完成率
    fun getTaskNumberReleasedCategory(taskCategory: String): Int {
        try{
            val cursor = writableDatabase.query("t_task",null, "task_category = ?",arrayOf(taskCategory),null,null,null)
            return cursor.count
        }catch (e:Exception){
            e.printStackTrace()
            return 0
        }
    }

    //指数表数据来源
    @SuppressLint("Range", "SimpleDateFormat", "Recycle")
    fun getIndexData(): IntArray {
        try{
            val start = getIndexTableDate()
            val cursor = writableDatabase.query("t_record",null,"account = ? and datetime(start_time) > datetime(?)", arrayOf(OnlineUser.account, start),null,null,null)
            val ret = IntArray(112)
            val day = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(start).time
            while (cursor.moveToNext()) {
                val startTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(cursor.getString(cursor.getColumnIndex("start_time"))).time
                var endTime = Date().time
                if(cursor.getString(cursor.getColumnIndex("end_time")).isNotEmpty()){
                    endTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(cursor.getString(cursor.getColumnIndex("end_time"))).time
                }
                for (i in 0..111) {
                    val dd = day + i * 86400000L
                    if (dd in (startTime + 1) until endTime) {
                        ret[i]++
                    }
                }
            }
            return ret
        }catch (e:Exception){
            e.printStackTrace()
            return IntArray(112)
        }
    }
    //指数表数据来源
    @SuppressLint("Range", "SimpleDateFormat", "Recycle")
    fun getIndexData(account:String): IntArray {
        try{
            val start = getIndexTableDate()
            val cursor = writableDatabase.query("t_record",null,"account = ? and datetime(start_time) > datetime(?)", arrayOf(account, start),null,null,null)
            val ret = IntArray(112)
            val day = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(start).time
            while (cursor.moveToNext()) {
                val startTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(cursor.getString(cursor.getColumnIndex("start_time"))).time
                var endTime = Date().time
                if(cursor.getString(cursor.getColumnIndex("end_time")).isNotEmpty()){
                    endTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(cursor.getString(cursor.getColumnIndex("end_time"))).time
                }
                for (i in 0..111) {
                    val dd = day + i * 86400000L
                    if (dd in (startTime + 1) until endTime) {
                        ret[i]++
                    }
                }
            }
            return ret
        }catch (e:Exception){
            e.printStackTrace()
            return IntArray(112)
        }
    }


    /*-------------------------管理员使用---------------------------*/
    // 审核任务(管理员特有权限)
    fun checkTask(task: Task): Boolean {
        // 底层逻辑就是 update 将 Task 中的 state 字段进行修改 (0 -> -1)
        // 前提是 state 只能等于 0(待审核) 不能是 -1(未完成) 或 1(已完成)
        if (task.state != 0) return false
        val wd = this.writableDatabase
        val cv = ContentValues().apply {
            put("state", -1)
        }

        return try {
            val res = wd.update(
                "t_task", cv, "task_id = ?",
                arrayOf(task.taskId.toString())
            )
            res != 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 方法重写 (参数为关键字content) (按用户名称或账号查找所有用户) (管理员特权)
    @SuppressLint("Range")
    fun queryAllUser(content: String): ArrayList<User> {
        try {
            val cursor = writableDatabase.query("t_user", null,
                if (content.trim().isEmpty()) null else "account like ? or username like ?",
                if (content.trim().isEmpty()) null else arrayOf("%$content%", "%$content%"),
                null, null, null)
            val list = ArrayList<User>()
            while (cursor.moveToNext()) {
                val u = User(
                    cursor.getString(cursor.getColumnIndex("account")),
                    cursor.getString(cursor.getColumnIndex("password")),
                    cursor.getString(cursor.getColumnIndex("username")),
                    cursor.getString(cursor.getColumnIndex("telephone")),
                    cursor.getString(cursor.getColumnIndex("email")),
                    cursor.getInt(cursor.getColumnIndex("balance")),
                    cursor.getInt(cursor.getColumnIndex("credit"))
                )
                if (cursor.getBlobOrNull(cursor.getColumnIndex("user_image")) != null) {
                    u.userImage = BlobConverter.avatarDecoder(cursor.getBlob(cursor.getColumnIndex("user_image")))
                }
                list.add(u)
            }
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList<User>()
        }
    }

    @SuppressLint("Range")
    fun queryCheckTask(content: String, state: Int): ArrayList<Task> {
        try {
            val wd = this.writableDatabase
            val list = ArrayList<Task>()

            val csr = if (content.isEmpty()) wd.query(
                "t_task", null, "state = ?",
                arrayOf( state.toString()), null, null, null
            )
            else wd.query(
                "t_task", null, " state = ? and " +
                        "(task_title like ? or task_content like ? or task_category like ?)",
                arrayOf(state.toString(), "%$content%", "%$content%","%$content%"),
                null, null, null
            )

            while (csr.moveToNext()) {
                list.add(
                    Task(
                        csr.getInt(csr.getColumnIndex("task_id")),
                        csr.getDouble(csr.getColumnIndex("task_priority")),
                        csr.getString(csr.getColumnIndex("release_time")),
                        csr.getString(csr.getColumnIndex("deadline")),
                        csr.getString(csr.getColumnIndex("task_title")),
                        csr.getString(csr.getColumnIndex("task_category")),
                        csr.getString(csr.getColumnIndex("task_content")),
                        csr.getInt(csr.getColumnIndex("money")),
                        csr.getString(csr.getColumnIndex("sender")),
                        csr.getInt(csr.getColumnIndex("state"))
                    )
                )
            }
            csr.close()
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList<Task>()
        }
    }


}