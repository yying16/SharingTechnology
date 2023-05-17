package com.sharingtechnology.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.project.androiddbpart.domain.Task
import com.sharingtechnology.Fragment.AdminTaskFragment
import com.sharingtechnology.Fragment.TaskFragment
import com.sharingtechnology.R
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.FragmentForSearch
import com.sharingtechnology.util.OnlineUser
import com.sharingtechnology.util.Theme


class TaskAdapter(private var taskList: ArrayList<Task>) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskButton: Button = view.findViewById(R.id.taskButton) // 详情
        val taskTitle: TextView = view.findViewById(R.id.taskTitle) // 任务标题
        val taskMoney: TextView = view.findViewById(R.id.taskMoney) // 任务金额
        val taskSender: TextView = view.findViewById(R.id.taskSender) // 任务发布者
        val taskDeadline: TextView = view.findViewById(R.id.taskDeadline) // 任务截至时间
        val taskImage: ImageView = view.findViewById(R.id.taskImage)
        val background: LinearLayout = view.findViewById(R.id.background)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { //事件监听
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        val viewHolder = ViewHolder(view)

        viewHolder.taskButton.setOnClickListener {
            val position = viewHolder.adapterPosition
            val task = taskList[position]
            details(it, task)

        }
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val task = taskList[position]
            when (FragmentForSearch.pref) {
                "TaskFragment" -> { // 查看所有任务界面
                    acceptTask(it, task)
                }
                "IssueFragment" -> { // 发布任务界面
                    Toast.makeText(it.context, task.taskTitle, Toast.LENGTH_SHORT).show()
                }
                "AdminTaskFragment0" -> { // 用户审核任务——未审核
                    checkTask(it, task)
                }
            }
        }
        return viewHolder
    }

    private fun details(v: View, task: Task) { // 详情按钮（弹出对话框）
        val dialogView: View = LayoutInflater.from(v.context).inflate(R.layout.task_details, null, false)
        val dialog = AlertDialog.Builder(v.context)
        val frame = dialog.setView(dialogView).create() //添加任务窗口
        frame.show() // 显示对话框
        dialogView.findViewById<TextView>(R.id.taskTitle).text = task.taskTitle
        dialogView.findViewById<TextView>(R.id.taskCategory).text = task.taskCategory
        dialogView.findViewById<TextView>(R.id.taskContent).text = task.taskContent
        dialogView.findViewById<TextView>(R.id.taskDeadline).text = task.deadline
        dialogView.findViewById<TextView>(R.id.taskSender).text = task.senderAccount
        dialogView.findViewById<TextView>(R.id.taskMoney).text = task.money.toString()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskTitle.text = task.taskTitle
        holder.taskMoney.text = task.money.toString()
        holder.taskSender.text = task.senderAccount
        holder.taskDeadline.text = task.deadline
        holder.background.setBackgroundResource(Theme.getItemBackground())
        when (task.taskCategory.trim()) { // 设置任务图标
            "设计架构" -> {
                holder.taskImage.setImageResource(R.drawable.task_structure)
            }
            "前端开发" -> {
                holder.taskImage.setImageResource(R.drawable.task_web)
            }
            "后端开发" -> {
                holder.taskImage.setImageResource(R.drawable.task_java)
            }
            "Android开发" -> {
                holder.taskImage.setImageResource(R.drawable.task_android)
            }
            "IOS开发" -> {
                holder.taskImage.setImageResource(R.drawable.task_ios)
            }
            "微信小程序开发" -> {
                holder.taskImage.setImageResource(R.drawable.task_wxapp)
            }
            "全栈开发" -> {
                holder.taskImage.setImageResource(R.drawable.task_full_stack)
            }
            "搭建大数据集群" -> {
                holder.taskImage.setImageResource(R.drawable.task_big_data)
            }
            "大数据计算" -> {
                holder.taskImage.setImageResource(R.drawable.task_window)
            }
            "数据挖掘分析" -> {
                holder.taskImage.setImageResource(R.drawable.task_data_analysis)
            }
            "数据爬取" -> {
                holder.taskImage.setImageResource(R.drawable.task_web_crawler)
            }
            "项目测试" -> {
                holder.taskImage.setImageResource(R.drawable.task_test)
            }
            "GUI程序" -> {
                holder.taskImage.setImageResource(R.drawable.task_gui)
            }
            "其他" -> {
                holder.taskImage.setImageResource(R.drawable.task_other)
            }
        }
        holder.taskImage.background.alpha = 0
    }

    override fun getItemCount() = taskList.size


    private fun checkTask(v: View, task: Task) { // 审核任务
        val db = DatabaseHelper(v.context, 1)
        val confirm = AlertDialog.Builder(v.context).setMessage("确认通过该任务的审核吗？").setTitle("确认信息")
        confirm.setPositiveButton(R.string.confirm) { dialog, _ ->
            if (db.checkTask(task)) { // 如果修改成功
                dialog.dismiss() // 关闭提示信息
                Toast.makeText(v.context, "审核通过！", Toast.LENGTH_SHORT).show()
                val f = FragmentForSearch.preFragment as AdminTaskFragment
                f.checkTask()
            }
        }
        confirm.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss() // 关闭提示信息
            Toast.makeText(v.context, "已取消", Toast.LENGTH_SHORT).show()
        }
        confirm.show()
    }

    private fun acceptTask(v: View, task: Task) {
        val db = DatabaseHelper(v.context, 1)
        val confirm = AlertDialog.Builder(v.context).setMessage("确定接受这个任务？").setTitle("确认信息")
        confirm.setPositiveButton(R.string.confirm) { dialog, _ ->
            if(task.senderAccount==OnlineUser.account){
                Toast.makeText(v.context, "不能接受自己发布的任务！", Toast.LENGTH_SHORT).show()
            }else{
                if (db.addRecord(task)) { // 如果修改成功
                    dialog.dismiss() // 关闭提示信息
                    Toast.makeText(v.context, "记得及时完成哦！", Toast.LENGTH_SHORT).show()
                    val f = FragmentForSearch.preFragment as TaskFragment
                    f.refresh()
                }
            }

        }
        confirm.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss() // 关闭提示信息
            Toast.makeText(v.context, "已取消", Toast.LENGTH_SHORT).show()
        }
        confirm.show()
    }
}