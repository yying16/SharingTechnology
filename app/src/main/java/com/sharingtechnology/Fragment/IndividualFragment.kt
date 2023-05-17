package com.sharingtechnology.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.palette.graphics.Palette
import com.sharingtechnology.R
import com.sharingtechnology.service.WordCloudService
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.OnlineUser
import com.sharingtechnology.util.Theme
import com.sharingtechnology.util.WordCloud
import kotlinx.android.synthetic.main.activity_individual.*
import java.util.*

class IndividualFragment : Fragment() {
    private lateinit var db: DatabaseHelper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_individual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = context?.let { DatabaseHelper(it, 1) }!!
        refresh.setOnClickListener {
            Toast.makeText(activity, "重新进入该界面可更新词云......", Toast.LENGTH_SHORT).show()
            refreshWordCloud()
        }
        initUser()
        initBitmap()
    }

    @SuppressLint("SetTextI18n")
    private fun initUser() {
        if (OnlineUser.userImage != null) { // 用户头像
            userImage.setImageBitmap(OnlineUser.userImage)
            changeBackground(OnlineUser.userImage!!)
        }
        userName.text = OnlineUser.username // 用户名
        balance.text = "能力值:${OnlineUser.balance}"
    }

    private fun changeBackground(bitmap: Bitmap) { // 修改背景颜色
        val p = Palette.from(bitmap).generate()
        p.darkMutedSwatch?.rgb?.let { background.setBackgroundColor(it) }
        p.lightMutedSwatch?.rgb?.let { userName.setTextColor(it) }
        p.lightVibrantSwatch?.rgb?.let { balance.setTextColor(it) }
    }

    private fun initBitmap() {
        drawIndexTable()
        drawWordCloud()
    }

    private fun drawWordCloud() {
        if (WordCloud.usedList.size <= 1) {
            WordCloud.usedList.add("程序员")
            WordCloud.usedList.add("程序开发")
        }
        wordCloud.setWords(WordCloud.usedList)
        // 防止无法更新词云（因为上一行已经将词云的词库设置好了，若还是未更新词云，则需要重新刷新
        if (WordCloud.usedList.size != WordCloud.list.size) {
            refreshWordCloud()
        }
    }

    private fun refreshWordCloud() {// 刷新词云
        val intent = Intent(activity, WordCloudService::class.java)
        intent.putExtra("content", db.getMyWorldCloud())
        activity?.startService(intent) // 启动词云分词后台服务
    }

    private fun drawIndexTable() {
        val data = db.getIndexData()
        val image = Bitmap.createBitmap(1116, 600, Bitmap.Config.ARGB_8888)
        val cav = Canvas(image)
        val p = Paint()
        var x = 9F
        var y = 30F
        p.isAntiAlias = true
        for (w in 0 until 16) {
            for (d in 0 until 7) {
                val temp = data[w * 7 + d]
                if (temp >= 7) {
                    p.color = Color.parseColor("#006633")
                } else if (temp >= 5) {
                    p.color = Color.parseColor("#00994c")
                } else if (temp >= 3) {
                    p.color = Color.parseColor("#00e672")
                } else if (temp >= 2) {
                    p.color = Color.parseColor("#19ff8c")
                } else if (temp >= 1) {
                    p.color = Color.parseColor("#66ffb2")
                } else {
                    p.color = Color.parseColor("#b3ffd9")
                }
                cav.drawRoundRect(RectF(x, y, x + 58F, y + 58F), 6F, 6F, p)
                y += 69F
            }
            x += 69F
            y = 30F
        }
        p.color = Theme.getFontColor()
        p.textSize = 54F
        val m = Date().month
        cav.drawText("${m - 2} 月", 100F, 580F, p)
        cav.drawText("${m - 1} 月", 420F, 580F, p)
        cav.drawText("$m 月", 760F, 580F, p)
        indexTable.setImageBitmap(image)
    }


}