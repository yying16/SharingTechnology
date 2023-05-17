package com.sharingtechnology.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import cn.hutool.core.date.DateUtil
import com.sharingtechnology.R
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.Time
import kotlinx.android.synthetic.main.fragment_countdown.*


class CountdownFragment : Fragment() {
    private lateinit var db: DatabaseHelper
    private var t = Timer(60000)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_countdown, container, false)
    }

    override fun onResume() {
        super.onResume()
        val task = Time.taskIndex
        if (task != null) { // 有做时间记录
            t.cancel()
            t = Timer(Time.getMillisecond(task.deadline))
            chooseTask.text = task.taskTitle
            t.start()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //连接数据库
        db = context?.let { DatabaseHelper(it, 1) }!!
        super.onViewCreated(view, savedInstanceState)
        chooseTask.setOnClickListener { // 选择任务
            val categoryDialogView: View = LayoutInflater.from(it.context).inflate(R.layout.task_category, null, false)
            val categoryDialogFrame = this.let { AlertDialog.Builder(view.context, R.style.numberPickerDialog) }
            val numberPicker = categoryDialogView.findViewById<NumberPicker>(R.id.numberPicker)
            val list = db.queryMyRecord(false)
            val taskList = ArrayList<String>()
            for (record in list) {
                val title = record.task?.taskTitle.toString()
                taskList.add(title)
            }
            val taskCategory = taskList.toTypedArray()
            if (taskCategory.isNotEmpty()) {
                val categoryDialog = categoryDialogFrame.setView(categoryDialogView).create() //添加任务窗口
                numberPicker.displayedValues = taskCategory
                numberPicker.minValue = 0;
                numberPicker.maxValue = taskCategory.size - 1;
                numberPicker.value = 4
                categoryDialog.show() // 显示对话框
                numberPicker.setOnClickListener {
                    val deadline = list[numberPicker.value].task?.deadline
                    if (deadline != null) {
                        t.cancel()
                        t = Timer(Time.getMillisecond(deadline)) // 设置倒计时
                        t.start()
                    }
                    chooseTask.text = taskCategory[numberPicker.value]
                    categoryDialog.dismiss()
                }
            } else {
                Toast.makeText(it.context, "目前暂无未完成任务", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class Timer(time: Long) : CountDownTimer(time, 500) {
        override fun onTick(residualTime: Long) {
            val value = (residualTime / 1000).toInt() // 单位转换为 秒
            countdown.text = DateUtil.secondToTime(value) // 更新时间
        }

        override fun onFinish() {
            Toast.makeText(activity, "时间到了", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        t.cancel()
    }
}