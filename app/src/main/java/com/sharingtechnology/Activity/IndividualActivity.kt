package com.sharingtechnology.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.palette.graphics.Palette
import com.sharingtechnology.R
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.service.WordCloudService
import com.sharingtechnology.util.OnlineUser
import com.sharingtechnology.util.Theme
import com.sharingtechnology.util.WordCloud
import kotlinx.android.synthetic.main.activity_individual.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class IndividualActivity : AppCompatActivity(), View.OnClickListener {
    private val takePhoto = 1
    private val fromAlbum = 2
    lateinit var imageUri: Uri
    lateinit var outputImage: File
    private val db = DatabaseHelper(this, 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.presentTheme) // 设置主题
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual)
        initUser()
        initBitmap()
    }

    override fun onRestart() {
        super.onRestart()
        initUser()
        initBitmap()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.userImage -> {//点击用户头像
                changeUserImage()
            }
            R.id.refresh -> {
                Toast.makeText(this, "重新进入该界面可更新词云......", Toast.LENGTH_SHORT).show()
                refreshWordCloud()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUser() {
        if (OnlineUser.userImage != null) { // 用户头像
            userImage.setImageBitmap(OnlineUser.userImage)
            changeBackground(OnlineUser.userImage!!)
        }
        userName.text = OnlineUser.username // 用户名
        balance.text = "能力值:${OnlineUser.balance}"
        userImage.setOnClickListener(this)
        refresh.setOnClickListener(this)
    }

    private fun initBitmap() {
        drawIndexTable()
        drawWordCloud()
    }

    private fun drawWordCloud() { // 绘制词云
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
        val intent = Intent(this, WordCloudService::class.java)
        intent.putExtra("content", db.getMyWorldCloud())
        startService(intent) // 启动词云分词后台服务
    }

    private fun drawIndexTable() { // 绘制指数表
        val data = db.getIndexData()
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


    //对返回的照片进行处理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) //Intent返回结果处理，显示照片
        drawWordCloud() // 防止词云混乱
        when (requestCode) {//匹配Code
            takePhoto -> {
                if (resultCode == Activity.RESULT_OK) { //✔键
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos) //对bitmap进行格式转发成PNG
                    if (bitmap != null && db.changeUserImage(bitmap)) { // 只有当头像写入数据库之后，才修改界面
                        userImage.setImageBitmap(rotateIfRequired(bitmap))
                        changeBackground(bitmap)
                    }

                }
            }
            fromAlbum -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->//将选择的图片显示
                        val bitmap = contentResolver.openFileDescriptor(uri, "r")?.use {
                            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                        }
                        val baos = ByteArrayOutputStream()
                        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, baos) //对bitmap进行格式转发成PNG
                        if (bitmap != null && db.changeUserImage(bitmap)) { // 只有当头像写入数据库之后，才修改界面
                            userImage.setImageBitmap(bitmap)
                            changeBackground(bitmap)
                        }
                    }
                }
            }
        }
    }

    private fun changeUserImage() { // 修改头像
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.modify_image, null, false)
        val dialog = this.let { AlertDialog.Builder(it, R.style.myCorDialog) }//设置布局
        val dialogBox = dialog.setView(dialogView)?.create() //添加任务窗口
        dialogBox?.window?.setGravity(Gravity.BOTTOM)
        dialogBox?.window?.attributes?.y = 160 // 对话框下边界
        dialogBox?.show()
        //事件监听器
        dialogView.findViewById<Button>(R.id.takeAPicture).setOnClickListener {//拍照
            turnOnTheCamera()
        }
        dialogView.findViewById<Button>(R.id.photoAlbum).setOnClickListener {//相册
            openAlbum()
        }
    }

    private fun openAlbum() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE) //指定只显示图片
        intent.type = "image/*"
        startActivityForResult(intent, fromAlbum)
    }

    private fun turnOnTheCamera() {
        outputImage = File(this.externalCacheDir, "output_image.jpg") //存放照片
        //如果文件存在则进行删除
        if (outputImage.exists()) {
            outputImage.delete()
        }
        //创建新的对象
        outputImage.createNewFile()
        //获取图片uri
        imageUri = FileProvider.getUriForFile(this, "com.sharingtechnology.fileprovider", outputImage)
        //启动相机并获取拍的照片作为活动返回对象
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) //设置相片输出保存的Uri路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)  //指定图片的输出地址
        startActivityForResult(intent, takePhoto) //takePhoto为标记代码
    }


    private fun rotateIfRequired(bitmap: Bitmap): Bitmap { //确保物理设备旋转时获取的照片跟着旋转
        val exif = ExifInterface(outputImage.path) //ExifInterface接口获得图片方向
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) { //按获得方向进行旋转
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
    }

    //旋转图片
    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap { //用角度做参数，设置旋转规则
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat()) //创建经过旋转后的新图片
        val rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle() //将不需要的旧图片对象回收
        return rotateBitmap
    }

    private fun changeBackground(bitmap: Bitmap) { // 修改背景颜色
        val p = Palette.from(bitmap).generate()
        p.darkMutedSwatch?.rgb?.let { background.setBackgroundColor(it) }
        p.lightMutedSwatch?.rgb?.let { userName.setTextColor(it) }
        p.lightVibrantSwatch?.rgb?.let { balance.setTextColor(it) }
    }
}