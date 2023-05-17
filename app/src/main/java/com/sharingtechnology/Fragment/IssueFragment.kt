package com.sharingtechnology.Fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.project.androiddbpart.domain.Task
import com.sharingtechnology.Activity.UserActivity
import com.sharingtechnology.R
import com.sharingtechnology.adapter.TaskAdapter
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.*
import kotlinx.android.synthetic.main.fragment_issue.*
import kotlinx.android.synthetic.main.fragment_issue.TaskRecyclerView
import kotlinx.android.synthetic.main.fragment_task.tabLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class IssueFragment() : Fragment(), View.OnClickListener, TabLayout.OnTabSelectedListener {
    private lateinit var db: DatabaseHelper
    private var adapter: TaskAdapter? = null // 任务适配器
    private var present = 1
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
                swipeRefresh.isRefreshing = false
            }
            TaskRecyclerView.adapter = adapter // 修改适配器内容，即修改RecycleView内容
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        swipeRefresh.setOnRefreshListener {
            refreshTasks(view)
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.fab) {// 悬浮球
            addTask(v)
        }
    }


    override fun onTabSelected(tab: TabLayout.Tab) {
        when (tab.text?.trim()) {
            "正在进行" -> {
                present = 1
            }
            "审核中" -> {
                present = 0
            }
            "未开始" -> {
                present = -1
            }
            "已完成" -> {
                present = 2
            }
        }
        adapter = TaskAdapter(db.queryMyTask(present))
        TaskRecyclerView.adapter = adapter
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    private fun init() {
        //顶部导航栏
        tabLayout.addTab(tabLayout.newTab().setText("正在进行"))
        tabLayout.addTab(tabLayout.newTab().setText("审核中"))
        tabLayout.addTab(tabLayout.newTab().setText("未开始"))
        tabLayout.addTab(tabLayout.newTab().setText("已完成"))
        tabLayout.addOnTabSelectedListener(this)
        fab.setOnClickListener(this)
        db = context?.let { DatabaseHelper(it, 1) }!!
        TaskRecyclerView.layoutManager = LinearLayoutManager(activity)
        if (adapter == null) {
            adapter = TaskAdapter(db.queryMyTask(1))
        }
        TaskRecyclerView.adapter = adapter
        FragmentForSearch.pref = "IssueFragment"
    }

    private fun refreshTasks(v: View) {
        search(v)
    }

    private fun search(v: View) {
        val dialogView: View = LayoutInflater.from(v.context).inflate(R.layout.task_search, null, false)
        val dialog = this.let { AlertDialog.Builder(v.context, Theme.getSearchStyle()) }//设置布局
        val frame = dialog.setView(dialogView).create() //添加任务窗口
        frame.show() // 显示对话框
        swipeRefresh.isRefreshing = false
        dialogView.findViewById<Button>(R.id.search).setOnClickListener { // 修改密码按钮
            val content = dialogView.findViewById<EditText>(R.id.content).text.toString() // 关键字
            thread {
                Thread.sleep(500)
                adapter = TaskAdapter(db.queryMyTask(content, present)) // (条件查找我的任务)
                val msg = Message()
                msg.what = 0 //刷新标志
                handler.sendMessage(msg) // 发送消息
            }
            frame.dismiss()
        }
    }

    @SuppressLint("ResourceType", "SimpleDateFormat", "CutPasteId", "SetTextI18n")
    private fun addTask(v: View) { // 添加任务
        val categoryDialogView: View = LayoutInflater.from(v.context).inflate(R.layout.task_category, null, false)
        val categoryDialogFrame = AlertDialog.Builder(v.context)
        val numberPicker = categoryDialogView.findViewById<NumberPicker>(R.id.numberPicker)
        val taskCategory = arrayOf("设计架构", "前端开发", "后端开发", "Android开发", "IOS开发", "微信小程序开发", "全栈开发", "搭建大数据集群", "大数据计算", "数据挖掘分析", "数据爬取", "项目测试", "GUI程序", "其他")
        val categoryDialog = categoryDialogFrame.setView(categoryDialogView).create() //添加任务窗口
        numberPicker.displayedValues = taskCategory
        numberPicker.minValue = 0
        numberPicker.maxValue = taskCategory.size - 1;
        numberPicker.value = 3
        categoryDialog.show() // 显示对话框
        numberPicker.setOnClickListener {
            categoryDialog.dismiss()
            val dialogView: View = LayoutInflater.from(v.context).inflate(R.layout.task_add, null, false)
            val dialogFrame = this.let { AlertDialog.Builder(v.context, R.style.addTask) }//设置布局
            val dialog = dialogFrame.setView(dialogView).create() //添加任务窗口
            dialog.show() // 显示对话框
            //设置时区为北京时间
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"))
            //获取当前时间
            val st = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date(System.currentTimeMillis())).split("-")
            var year: Int = st[0].toInt()
            var month: Int = st[1].toInt() - 1 // 因为月份是0-11
            var day: Int = st[2].toInt()
            var hour: Int = st[3].toInt()
            var minute: Int = st[4].toInt()
            dialogView.findViewById<Button>(R.id.taskDeadline).setOnClickListener { // 选择任务截至时间
                var t = 3
                if (Theme.presentTheme == Theme.darkTheme) {
                    t = 2
                }
                val dataDialog = activity?.let { // 时间选择器
                    DatePickerDialog(it, t, DatePickerDialog.OnDateSetListener { _, y, m, d ->
                        year = y
                        month = m
                        day = d
                        val timeDialog = activity?.let { it ->
                            TimePickerDialog(it, t, TimePickerDialog.OnTimeSetListener { _, hh, mm ->
                                hour = hh
                                minute = mm
                                dialogView.findViewById<Button>(R.id.taskDeadline).text = Time.format(year, month+1, day, hour, minute)
                            }, hour, minute, true)
                        }
                        timeDialog?.show()
                    }, year, month, day)

                }
                dataDialog?.show()
            }
            dialogView.findViewById<Button>(R.id.addTask).setOnClickListener { // 添加任务
                //数据校验
                val taskTitle = dialogView.findViewById<TextView>(R.id.taskTitle)
                val taskMoney = dialogView.findViewById<TextView>(R.id.taskMoney)
                val taskContent = dialogView.findViewById<TextView>(R.id.taskContent)
                val taskDeadline = dialogView.findViewById<TextView>(R.id.taskDeadline)
                val warnDeadline = dialogView.findViewById<TextView>(R.id.warn_deadline)
                val warnTitle = dialogView.findViewById<TextView>(R.id.warn_title)
                val warnMoney = dialogView.findViewById<TextView>(R.id.warn_money)
                val warnContent = dialogView.findViewById<TextView>(R.id.warn_content)
                var flag = true
                warnTitle.text = ""
                warnMoney.text = ""
                warnContent.text = ""
                if (taskTitle.text.length<2 ||taskTitle.text.length >16) {
                    warnTitle.text = "任务标题必须为2-16个字符"
                    flag = false
                }

                if (!taskMoney.text.matches(Regex("\\d+"))) {
                    warnMoney.text = "请输入任务金额"
                    flag = false
                }
                if (taskMoney.text.toString().toInt() > OnlineUser.credit) {
                    warnMoney.text = "您的余额不足"
                    flag = false
                }


                if (taskContent.text.isEmpty()) {
                    warnContent.text = "任务内容不能为空"
                    flag = false
                }

                if (taskDeadline.text.isEmpty()) {
                    warnDeadline.text = "截至时间不能为空"
                    flag = false
                }

                if (taskDeadline.text.isEmpty()) {
                    warnDeadline.text = "截至时间不能为空"
                    flag = false
                } else if (Time.getMillisecond(taskDeadline.text.toString()) <= 3600000) {
                    warnDeadline.text = "任务宽限时间至少2个小时"
                    flag = false
                }

                if (flag) { // 如果格式无误
                    val pre = Analyse.getPriority(taskMoney.text.toString().toInt(),taskDeadline.text.toString())
                    val ret = db.addTask(Task(pre,taskDeadline.text.toString(), taskTitle.text.toString(), taskCategory[numberPicker.value], taskContent.text.toString(), taskMoney.text.toString().toInt()))
                    if (ret) {//插入成功
                        Toast.makeText(activity, "添加任务成功", Toast.LENGTH_SHORT).show()
                        adapter = TaskAdapter(db.queryMyTask(present))
                        TaskRecyclerView.adapter = adapter
                        if(FragmentForSearch.preActivity!=null){
                            val act = FragmentForSearch.preActivity as UserActivity
                            act.refreshUserData()
                        }
                        dialog.dismiss()
                    } else {
                        Toast.makeText(activity, "添加任务失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
}