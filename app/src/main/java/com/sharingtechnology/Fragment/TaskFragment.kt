package com.sharingtechnology.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.project.androiddbpart.domain.Task
import com.sharingtechnology.R
import com.sharingtechnology.adapter.TaskAdapter
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.FragmentForSearch
import com.sharingtechnology.util.Theme
import kotlinx.android.synthetic.main.fragment_task.TaskRecyclerView
import kotlinx.android.synthetic.main.fragment_task.swipeRefresh
import kotlinx.android.synthetic.main.fragment_task.tabLayout
import kotlin.concurrent.thread

class TaskFragment() : Fragment(), TabLayout.OnTabSelectedListener {
    private lateinit var db: DatabaseHelper
    private val taskList = ArrayList<Task>()
    private var adapter= TaskAdapter(taskList) // 任务适配器
    private lateinit var comprehensive: TabLayout.Tab // 顶部导航栏————综合
    private lateinit var price: TabLayout.Tab  // 顶部导航栏————悬赏
    private lateinit var deadline: TabLayout.Tab  // 顶部导航栏————截至时间
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if(msg.what == 0){
                swipeRefresh.isRefreshing=false
            }
            TaskRecyclerView.adapter = adapter // 修改适配器内容，即修改RecycleView内容
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        swipeRefresh.setOnRefreshListener { // 下拉查找
            refreshTasks(view)
        }
    }


    override fun onTabSelected(tab: TabLayout.Tab?) {
        when(tab?.position){
            0 ->{
                comprehensive.text = "综合"
                orderTask(0,false)
            }
            1 ->{
                price.text = "悬赏 V "
                orderTask(1,false)
            }
            2 ->{
                deadline.text = "截至时间 V "
                orderTask(2,false)
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        when(tab?.position){
            0 ->{
                comprehensive.text = "综合"
            }
            1 ->{
                price.text = "悬赏   "
            }
            2 ->{
                deadline.text = "截至时间   "
            }
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        var str = " V "
        if(tab?.text.toString().endsWith(" V ")){
            str = " Λ "
        }
        when(tab?.position){
            0 ->{
                comprehensive.text = "综合"
            }
            1 ->{
                price.text = "悬赏$str"
                if(str==" Λ "){
                    orderTask(1,true)
                }else{
                    orderTask(1,false)
                }
            }
            2 ->{
                deadline.text = "截至时间$str"
                if(str==" Λ "){
                    orderTask(2,true)
                }else{
                    orderTask(2,false)
                }
            }
        }
    }

    private fun init() { // 初始化
        //顶部导航栏
        comprehensive = tabLayout.newTab().setText("综合   ")
        price = tabLayout.newTab().setText("悬赏   ")
        deadline = tabLayout.newTab().setText("截至时间   ")
        tabLayout.addTab(comprehensive)
        tabLayout.addTab(price)
        tabLayout.addTab(deadline)
        tabLayout.addOnTabSelectedListener(this)
        db = context?.let { DatabaseHelper(it,1) }!!
        adapter = TaskAdapter(db.queryAllTask(-1,0,false))
        TaskRecyclerView.layoutManager = LinearLayoutManager(activity)
        TaskRecyclerView.adapter = adapter
        FragmentForSearch.preFragment = this
        FragmentForSearch.pref = "TaskFragment"
    }
    private fun refreshTasks(v:View) {
        search(v)
    }

     fun refresh(){
        adapter = TaskAdapter(db.queryAllTask(-1))
        TaskRecyclerView.adapter = adapter
    }

    private fun orderTask(type:Int,asc:Boolean){ //任务排序
        adapter = TaskAdapter(db.queryAllTask(-1,type,asc))
        TaskRecyclerView.adapter = adapter
    }

    private fun search(v:View){
        val dialogView: View = LayoutInflater.from(v.context).inflate(R.layout.task_search, null, false)
        val dialog = this.let { AlertDialog.Builder(v.context, Theme.getSearchStyle()) }//设置布局
        val frame = dialog.setView(dialogView).create() //添加任务窗口
        frame.show() // 显示对话框
        swipeRefresh.isRefreshing=false
        dialogView.findViewById<Button>(R.id.search).setOnClickListener { // 查询
            val content = dialogView.findViewById<EditText>(R.id.content).text.toString() // 关键字
            thread {
                Thread.sleep(500)
                adapter = TaskAdapter(db.queryTask(content)) // (条件查找任务)
                val msg = Message()
                msg.what = 0 //刷新标志
                handler.sendMessage(msg) // 发送消息
            }
            tabLayout.getTabAt(0)?.select() // 跳转到综合
            frame.dismiss()
        }
    }
}