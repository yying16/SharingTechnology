package com.sharingtechnology.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.project.androiddbpart.domain.Task
import com.sharingtechnology.Activity.UserActivity
import com.sharingtechnology.Fragment.RecordFragment
import com.sharingtechnology.R
import com.sharingtechnology.domain.Record
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.FragmentForSearch
import com.sharingtechnology.util.Theme
import com.sharingtechnology.util.Time

class RecordAdapter(private var recordList: ArrayList<Record>) : RecyclerView.Adapter<RecordAdapter.ViewHolder>() {
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

        viewHolder.taskButton.setOnClickListener{
            val position = viewHolder.adapterPosition
            val task = recordList[position].task
            if (task != null) {
                details(it,task)
            }

        }
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val record = recordList[position]
            val task = recordList[position].task
            Toast.makeText(it.context, task?.taskTitle, Toast.LENGTH_SHORT).show()
            if (task != null) {
                commitTask(it, task, record)
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
        val task = recordList[position].task
        holder.taskTitle.text = task?.taskTitle
        holder.taskMoney.text = task?.money.toString()
        holder.taskSender.text = task?.senderAccount
        holder.taskDeadline.text = task?.deadline
        holder.background.setBackgroundResource(Theme.getItemBackground())
        if(task!=null){
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
    }
    override fun getItemCount() = recordList.size


    // 用户提交一个任务
    private fun commitTask(v: View, task: Task, record: Record) {
        val db = DatabaseHelper(v.context, 1)
        val f = FragmentForSearch.preFragment as RecordFragment
        if(!f.state){ // 未完成
            val confirm = AlertDialog.Builder(v.context).setMessage("确认完成并提交该任务？").setTitle("确认信息")
            confirm.setPositiveButton(R.string.confirm) { dialog, _ ->
                if (db.finishTask(task) && db.updateRecord(record)) { // 如果提交成功
                    dialog.dismiss()
                    Toast.makeText(v.context, "提交成功,完成任务!", Toast.LENGTH_SHORT).show()
                    f.refresh()
                    if(Time.taskIndex?.taskId==task.taskId){
                        Time.taskIndex = null // 注销记录
                    }
                    if(FragmentForSearch.preActivity!=null){
                        val act = FragmentForSearch.preActivity as UserActivity
                        act.refreshUserData()
                    }
                }
                else {
                    dialog.dismiss()
                    Toast.makeText(v.context, "内部出错了,请稍后重试......", Toast.LENGTH_SHORT).show()
                }
            }
            confirm.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss() // 关闭提示信息
                Toast.makeText(v.context, "已取消提交", Toast.LENGTH_SHORT).show()
            }
            confirm.show()
        }
    }


}