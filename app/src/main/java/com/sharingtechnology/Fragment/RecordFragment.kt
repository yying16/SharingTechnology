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
import com.sharingtechnology.adapter.RecordAdapter
import com.sharingtechnology.domain.Record
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.FragmentForSearch
import com.sharingtechnology.util.Theme
import kotlinx.android.synthetic.main.fragment_record.*
import kotlinx.android.synthetic.main.fragment_task.swipeRefresh
import kotlinx.android.synthetic.main.fragment_task.tabLayout
import kotlin.concurrent.thread

class RecordFragment : Fragment(),TabLayout.OnTabSelectedListener  {
    private lateinit var db: DatabaseHelper
    private val recordList = ArrayList<Record>()
    private var adapter= RecordAdapter(recordList) // 任务适配器
    private lateinit var notCompleted: TabLayout.Tab // 顶部导航栏————未完成
    private lateinit var completed: TabLayout.Tab  // 顶部导航栏————已完成
    var state = false
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if(msg.what == 0){
                swipeRefresh.isRefreshing=false
            }
            RecordRecyclerView.adapter = adapter // 修改适配器内容，即修改RecycleView内容
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
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
        state = tab?.position==1 // 修改状态
        adapter = RecordAdapter(db.queryMyRecord(state))
        RecordRecyclerView.adapter = adapter
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    private fun init() { // 初始化
        //顶部导航栏
        notCompleted = tabLayout.newTab().setText("未完成")
        completed = tabLayout.newTab().setText("已完成")
        tabLayout.addTab(notCompleted)
        tabLayout.addTab(completed)
        tabLayout.addOnTabSelectedListener(this)
        //连接helper
        db = context?.let { DatabaseHelper(it,1) }!!
        adapter = RecordAdapter(db.queryMyRecord(state))
        RecordRecyclerView.layoutManager = LinearLayoutManager(activity)
        RecordRecyclerView.adapter = adapter
        FragmentForSearch.preFragment = this // 记录最顶界面

    }
    private fun refreshTasks(v:View) { // 搜索刷新
        search(v)
    }

    fun refresh(){ // 刷新界面
        adapter = RecordAdapter(db.queryMyRecord(state))
        RecordRecyclerView.adapter = adapter
    }

    private fun search(v:View){
        val dialogView: View = LayoutInflater.from(v.context).inflate(R.layout.task_search, null, false)
        val dialog = this.let { AlertDialog.Builder(v.context, Theme.getSearchStyle()) }//设置布局
        val frame = dialog.setView(dialogView).create() //添加任务窗口
        frame.show() // 显示对话框
        swipeRefresh.isRefreshing=false
        dialogView.findViewById<Button>(R.id.search).setOnClickListener { // 修改密码按钮
            val content = dialogView.findViewById<EditText>(R.id.content).text.toString() // 关键字
            thread {
                Thread.sleep(500)
                adapter = RecordAdapter(db.queryMyRecord(content,state)) // (条件查找任务)
                val msg = Message()
                msg.what = 0 //刷新标志
                handler.sendMessage(msg) // 发送消息
            }
            if(state){
                tabLayout.getTabAt(1)?.select() // 跳转到综合
            }else{
                tabLayout.getTabAt(0)?.select() // 跳转到综合
            }
            frame.dismiss()
        }
    }
}