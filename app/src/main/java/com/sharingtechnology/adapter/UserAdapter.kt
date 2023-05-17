package com.sharingtechnology.adapter

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.project.androiddbpart.domain.User
import com.sharingtechnology.R
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.Theme
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_individual.*
import java.util.*
import kotlin.collections.ArrayList


class UserAdapter(private var userList: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: ImageView = view.findViewById(R.id.userImage) // 详情
        val userName: TextView = view.findViewById(R.id.userName) // 任务标题
        val userCredit: TextView = view.findViewById(R.id.userCredit) // 任务金额
        val userBalance: TextView = view.findViewById(R.id.userBalance) // 任务发布者
        val background: RelativeLayout = view.findViewById(R.id.background)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { //事件监听
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val user = userList[position]
            details(it,user)
            Toast.makeText(it.context, user.username, Toast.LENGTH_SHORT).show()
        }
        return viewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        if(user.userImage!=null){
            holder.userImage.setImageBitmap(user.userImage)
        }else{
            holder.userImage.setImageResource(R.drawable.ic_default_avatar)
        }
        holder.userName.text = user.username
        holder.userCredit.text = "能力值: ${user.credit}"
        holder.userBalance.text = "余额: ${user.balance}"
        holder.background.setBackgroundResource(Theme.getItemBackground())
    }

    override fun getItemCount() = userList.size

    @SuppressLint("CutPasteId")
    private fun details(v:View, user:User){ // 显示用户详情
        val dialogView: View = LayoutInflater.from(v.context).inflate(R.layout.dialog_individual, null, false)
        val dialog = this.let { AlertDialog.Builder(v.context, R.style.changePasswordDialog) }//设置布局
        val frame = dialog.setView(dialogView).create() //详细查看
        //同步用户信息
        val userImage = dialogView.findViewById<CircleImageView>(R.id.userImage)
        val userName = dialogView.findViewById<TextView>(R.id.userName)
        val balance = dialogView.findViewById<TextView>(R.id.balance)
        val background = dialogView.findViewById<RelativeLayout>(R.id.background)
        val bigBackground = dialogView.findViewById<ScrollView>(R.id.bigBackground)
        val indexTable = dialogView.findViewById<ImageView>(R.id.indexTable)
        bigBackground.setBackgroundColor(Theme.getBottomColor())
        if(user.userImage!=null){ // 若已修改头像则同步头像，否则使用默认头像
            userImage.setImageBitmap(user.userImage)
        }
        userName.text = user.username
        balance.text = user.balance.toString()
        // 修改主题颜色
        val p = user.userImage?.let { Palette.from(it).generate() }
        if(p!=null){ // 动态设置背景
            p.darkMutedSwatch?.rgb?.let { background.setBackgroundColor(it) }
            p.lightMutedSwatch?.rgb?.let { userName.setTextColor(it) }
            p.lightVibrantSwatch?.rgb?.let { balance.setTextColor(it) }
        }
        val db = v.context?.let { DatabaseHelper(it, 1) }!!
        drawIndexTable(db,indexTable,user.account)
        frame.show() // 显示对话框
    }

    private fun drawIndexTable(db:DatabaseHelper,indexTable:ImageView,account:String ) { // 绘制指数表
        val data = db.getIndexData(account)
        Log.d("TAG", "drawIndexTable: ${data.size}")
        val image = Bitmap.createBitmap(1116, 600, Bitmap.Config.ARGB_8888)
        val cav = Canvas(image)
        val p = Paint()
        var x = 9F
        var y = 30F
        p.isAntiAlias = true
        for (w in 0 until 16) { // 行
            for (d in 0 until 7) { // 列
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