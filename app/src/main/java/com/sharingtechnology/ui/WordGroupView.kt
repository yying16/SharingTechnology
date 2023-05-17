package com.sharingtechnology.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.sharingtechnology.util.Theme
import java.util.ArrayList

class WordGroupView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    //文字绘制画笔
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var background = Color.parseColor("#F2F2F2")
    //辅助线绘制画笔
    private val drawPointList: MutableList<DrawPoint> = ArrayList()
    private var keyWords: List<String> = ArrayList()
    private val colors = intArrayOf(
        Color.parseColor("#009f9d"),
        Color.parseColor("#cd4545"),
        Color.parseColor("#7ed3b2"),
        Color.parseColor("#d195f9"),
        Color.parseColor("#222831"),
        Color.parseColor("#b55400")
    )

    //文字展示最小可用宽高
    private val mMinTextWidth: Int
    private val mMinTextHeight: Int

    //最小可展示文字大小
    private val mMinFontSize: Int
    private lateinit var mPoints: Array<IntArray>
    private var mMaxXPointCount = 0
    private var mMaxYPointCount = 0

    //设置词条最大长度和最小长度
    private var mMaxLength = 0
    private var mMinLength = 0
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mMaxXPointCount = w / mMinTextWidth
        mMaxYPointCount = h / mMinTextHeight
        mPoints = Array(mMaxXPointCount) { IntArray(mMaxYPointCount) }
        for (i in mPoints.indices) {
            for (j in 0 until mPoints[i].size) {
                mPoints[i][j] = 0
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(background)
        //绘制辅助线
        //widget=4表示单个文字占用空间为4x4,其他类似
        for (i in 4 downTo 1) {
            buildPoints(mPoints, getFullContent(i), i, 0)
        }

        for (point in drawPointList) {
            point.draw(canvas)
        }
    }

    private fun getFullContent(widget: Int): Array<String?> {
        val maxCount = mMaxXPointCount * mMaxYPointCount * widget * widget
        val currMaxItemCount = (Math.random() * maxCount).toInt()
        var maxItems = arrayOfNulls<String>(currMaxItemCount)
        for (i in maxItems.indices) {
            maxItems[i] = keyWords[(Math.random() * keyWords.size).toInt()]
        }
        if (maxItems.isEmpty()) {
            maxItems = arrayOf(
                keyWords[0]
            )
        }
        return maxItems
    }

    private fun buildPoints(points: Array<IntArray>, maxItems: Array<String?>, widget: Int, index: Int) {
        if (index > maxItems.size - 1 || capacityOut(points, widget, maxItems[index])) {
            return
        }
        val randomPointX = (Math.random() * mMaxXPointCount).toInt()
        val randomPointY = (Math.random() * mMaxYPointCount).toInt()
        var isBuildSuc = false
        if (isCanFull(points, randomPointX, randomPointY)
            && isCanDraw(points, randomPointX, randomPointY, maxItems[index], widget)
        ) {
            for (i in randomPointX until randomPointX + maxItems[index]!!.length * widget) {
                for (j in randomPointY until randomPointY + widget) {
                    points[i][j] = 1
                }
            }
            drawPointList.add(DrawPoint(randomPointX, randomPointY, maxItems[index], widget))
            isBuildSuc = true
        } else {
            buildPoints(points, maxItems, widget, index)
        }
        if (isBuildSuc) {
            buildPoints(points, maxItems, widget, index + 1)
        }
    }

    //超出可容纳范围
    private fun capacityOut(points: Array<IntArray>, widget: Int, maxItem: String?): Boolean {
        var isCapacityOut = true
        for (i in points.indices) {
            for (j in 0 until points[i].size) {
                if (isCanDraw(points, i, j, maxItem, widget)) {
                    isCapacityOut = false
                    break
                }
            }
        }
        return isCapacityOut
    }


    // 判断是否可以绘制

    private fun isCanDraw(points: Array<IntArray>, randomPointX: Int, randomPointY: Int, maxItem: String?, widget: Int): Boolean {
        var isCanFull = false
        if (isOutHorizontal(randomPointX + maxItem!!.length * widget)
            && isOutVertical(randomPointY + widget)
        ) {
            isCanFull = true
            for (i in randomPointX until randomPointX + maxItem.length * widget) {
                for (j in randomPointY until randomPointY + widget) {
                    if (!isCanFull(points, i, j)) {
                        isCanFull = false
                        break
                    }
                }
            }
        }
        return isCanFull
    }

    private fun isOutHorizontal(pointX: Int): Boolean {
        return pointX <= mMaxXPointCount
    }

    private fun isOutVertical(pointY: Int): Boolean {
        return pointY <= mMaxYPointCount
    }

    private fun isCanFull(point: Array<IntArray>, pointX: Int, pointY: Int): Boolean {
        return if (pointX < mMaxXPointCount && pointY < mMaxYPointCount) {
            point[pointX][pointY] == 0
        } else false
    }

    fun random() {
        for (i in mPoints.indices) {
            for (j in 0 until mPoints[i].size) {
                mPoints[i][j] = 0
            }
        }
        drawPointList.clear()
        invalidate()
    }

    inner class DrawPoint internal constructor(private val pointX: Int, private val pointY: Int, var label: String?, var widget: Int) {
        fun draw(canvas: Canvas) {
            mTextPaint.textSize = (mMinFontSize * widget - 8 * widget).toFloat()
            mTextPaint.color = colors[(Math.random() * colors.size).toInt()]
            Log.e(TAG, "$widget-$label-$pointX-$pointY")
            canvas.drawText(label!!, (pointX * mMinTextWidth + 4 * widget).toFloat(), ((pointY + widget) * mMinTextHeight - 4 * widget).toFloat(), mTextPaint)
            mTextPaint.style = Paint.Style.FILL
        }
    }

    fun setWords(words: List<String>) {
        if(Theme.presentTheme==Theme.darkTheme){
            background = Color.parseColor("#404040")
        }else{
            background = Color.parseColor("#F2F2F2")
        }
        keyWords = words
        for (i in keyWords.indices) {
            if (i == 0) {
                mMaxLength = keyWords[i].length
                mMinLength = keyWords[i].length
                continue
            }
            if (keyWords[i].length > mMaxLength) {
                mMaxLength = keyWords[i].length
            }
            if (keyWords[i].length < mMinLength) {
                mMinLength = keyWords[i].length
            }
        }
        drawPointList.clear()
        invalidate()
    }

    companion object {
        private val TAG = WordGroupView::class.java.simpleName
    }

    init {
        mMinFontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 10f,
            context.resources.displayMetrics
        ).toInt()
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = mMinFontSize.toFloat()
        val textBound = Rect()
        mTextPaint.getTextBounds("正", 0, 1, textBound)
        mMinTextHeight = textBound.width()
        mMinTextWidth = mMinTextHeight
        Log.e(TAG, "$mMinTextWidth-$mMinTextHeight")
    }
}