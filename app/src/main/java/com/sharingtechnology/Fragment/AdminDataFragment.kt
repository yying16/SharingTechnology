package com.sharingtechnology.Fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.sharingtechnology.R
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.Analyse
import kotlinx.android.synthetic.main.fragment_admin_data.*


class AdminDataFragment : Fragment() {
    private lateinit var db: DatabaseHelper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = context?.let { DatabaseHelper(it, 1) }!!
        pipTaskReleased()
        type.setOnClickListener{
            val categoryDialogView: View = LayoutInflater.from(it.context).inflate(R.layout.task_category, null, false)
            val categoryDialogFrame = AlertDialog.Builder(it.context)
            val numberPicker = categoryDialogView.findViewById<NumberPicker>(R.id.numberPicker)
            val choose = arrayOf("任务发布率","任务完成率")
            val categoryDialog = categoryDialogFrame.setView(categoryDialogView).create() //选择
            numberPicker.displayedValues = choose
            numberPicker.minValue = 0
            numberPicker.maxValue = 1
            numberPicker.value = 0
            categoryDialog.show() // 显示对话框
            numberPicker.setOnClickListener {
                if(numberPicker.value==0){ // 任务发布率
                    pipTaskReleased()
                    type.text = "任务发布率"
                }else{ // 任务完成率
                    pipTaskFinished()
                    type.text = "任务完成率"
                }
                categoryDialog.dismiss()
            }
        }
    }

    private fun pipTaskReleased(){
        val arr = ArrayList<Int>()
        val array = ArrayList<Float>()
        var sum = 0.0F
        for (i in 0 until Analyse.taskCategory.size) {
            val t = db.getTaskNumberReleasedCategory(Analyse.taskCategory[i])
            arr.add(t)
            sum += t
        }
        for (i in 0 until arr.size){
            array.add((arr[i] / sum)-(arr[i] / sum)%0.01F)
        }
        drawPipChart("任务发布率",Analyse.taskCategory,array)
    }

    private fun pipTaskFinished(){
        val arr = ArrayList<Int>()
        val array = ArrayList<Float>()
        var sum = 0.0F
        for (i in 0 until Analyse.taskCategory.size) {
            val t = db.getTaskNumberFinishedCategory(Analyse.taskCategory[i])
            arr.add(t)
            sum += t
        }
        for (i in 0 until arr.size){
            array.add((arr[i] / sum)-(arr[i] / sum)%0.01F)
        }
        drawPipChart("任务完成率",Analyse.taskCategory,array)
    }



    private fun drawPipChart(title:String,str: Array<String>, v: ArrayList<Float>) { // 绘制饼图
        spread_pie_chart.setHoleColorTransparent(true)
        spread_pie_chart.holeRadius = 40F
        spread_pie_chart.setDescription("")
        spread_pie_chart.isDrawHoleEnabled = true
        spread_pie_chart.rotationAngle = 90F // 初始化角度
        spread_pie_chart.isRotationEnabled = true // 可以手动旋转
        spread_pie_chart.setUsePercentValues(true)
        spread_pie_chart.setDrawSliceText(false) // 不显示文字
        spread_pie_chart.setDrawCenterText(true) //饼状图中间可以添加文字
        spread_pie_chart.setCenterText(title)
        spread_pie_chart.setCenterTextColor(Color.GRAY)
        spread_pie_chart.data = getPieData(str, v)
        val mLegend: Legend = spread_pie_chart.legend //设置比例图
        mLegend.position = Legend.LegendPosition.RIGHT_OF_CHART //坐右边显示
        mLegend.textSize = 10F
        mLegend.xEntrySpace = 10f
        mLegend.yEntrySpace = 5f
        mLegend.textColor = Color.GRAY
        spread_pie_chart.animateXY(1000, 1000)
    }

    private fun getPieData(str: Array<String>, v: ArrayList<Float>): PieData { // 获取饼图数据
        val count = v.size
        val xValues = ArrayList<String>() //用来表示每个饼块上的内容
        for (i in 0 until count) {
            xValues.add(str[i])
        }
        val yValue = ArrayList<Entry>() //用来表示封装每个饼块的实际数据
        val qs: MutableList<Float> = ArrayList()
        for (i in 0 until count) {
            qs.add(v[i])
            yValue.add(Entry(qs[i], i))
        }
        val pieDataSet = PieDataSet(yValue, "统计数据")
        pieDataSet.sliceSpace = 0f
        val colors = ArrayList<Int>()
        //饼图颜色
        for (i in 0 until count) {
            colors.add(Analyse.colors[i])
        }
        pieDataSet.colors = colors //设置颜色
        pieDataSet.valueTextSize = 8f
        pieDataSet.valueTextColor = Color.WHITE
        val metrics = resources.displayMetrics
        val px = 5 * (metrics.densityDpi / 160f)
        pieDataSet.selectionShift = px //选中态多出的长度
        return PieData(xValues, pieDataSet)
    }

}