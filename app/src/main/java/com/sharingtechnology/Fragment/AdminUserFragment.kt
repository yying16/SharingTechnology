package com.sharingtechnology.Fragment

import android.annotation.SuppressLint
import android.content.Intent
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
import com.sharingtechnology.R
import com.sharingtechnology.adapter.UserAdapter
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.service.WordCloudService
import com.sharingtechnology.util.Theme
import com.sharingtechnology.util.WordCloud
import kotlinx.android.synthetic.main.activity_individual.*
import kotlinx.android.synthetic.main.fragment_admin_user.*
import kotlin.concurrent.thread

class AdminUserFragment : Fragment() {
    private lateinit var db: DatabaseHelper
    private var adapter: UserAdapter? = null // 任务适配器
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
                swipeRefresh.isRefreshing = false
            }
            UserRecyclerView.adapter = adapter // 修改适配器内容，即修改RecycleView内容
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        swipeRefresh.setOnRefreshListener {
            refreshTasks(view)
        }
    }

    private fun init() {
        //数据库连接
        db = context?.let { DatabaseHelper(it, 1) }!!
        UserRecyclerView.layoutManager = LinearLayoutManager(activity) // 设置布局管理器
        if (adapter == null) {
            adapter = UserAdapter(db.queryAllUser())
        }
        UserRecyclerView.adapter = adapter
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
        dialogView.findViewById<Button>(R.id.search).setOnClickListener { // 查询
            val content = dialogView.findViewById<EditText>(R.id.content).text.toString() // 关键字
            thread {
                Thread.sleep(500)
                val msg = Message()
                adapter = UserAdapter(db.queryAllUser(content))
                msg.what = 0 //刷新标志
                handler.sendMessage(msg) // 发送消息
            }
            frame.dismiss()
        }
    }


}