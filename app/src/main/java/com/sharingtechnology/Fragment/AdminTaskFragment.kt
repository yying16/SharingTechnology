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
import com.sharingtechnology.R
import com.sharingtechnology.adapter.TaskAdapter
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.FragmentForSearch
import com.sharingtechnology.util.Theme
import kotlin.concurrent.thread
import kotlinx.android.synthetic.main.fragment_admin_task.*

class AdminTaskFragment : Fragment(), TabLayout.OnTabSelectedListener {
    private lateinit var db: DatabaseHelper
    var adapter: TaskAdapter? = null // 任务适配器
    private var present = 0
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
        return inflater.inflate(R.layout.fragment_admin_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        swipeRefresh.setOnRefreshListener { // 下拉查找
            refreshTasks(view)
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        FragmentForSearch.pref = "AdminTaskFragment"+tab.position
        present = -tab.position
        adapter = TaskAdapter(db.queryAllTask(present))
        TaskRecyclerView.adapter = adapter
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    private fun refreshTasks(v: View) {
        search(v)
    }

    fun checkTask(){ // 用于适配器调用刷新
        adapter = TaskAdapter(db.queryAllTask(present))
        TaskRecyclerView.adapter = adapter
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
                adapter = TaskAdapter(db.queryCheckTask(content,present))
                val msg = Message()
                msg.what = 0 //刷新标志
                handler.sendMessage(msg) // 发送消息
            }
            frame.dismiss()
        }
    }

    private fun init() {
        //顶部导航栏
        tabLayout.addTab(tabLayout.newTab().setText("待审核"))
        tabLayout.addTab(tabLayout.newTab().setText("已审核"))
        tabLayout.addOnTabSelectedListener(this)
        db = context?.let { DatabaseHelper(it, 1) }!!
        TaskRecyclerView.layoutManager = LinearLayoutManager(activity)
        if (adapter == null) {
            adapter = TaskAdapter(db.queryAllTask(0))
        }
        TaskRecyclerView.adapter = adapter
        FragmentForSearch.preFragment = this
    }

}