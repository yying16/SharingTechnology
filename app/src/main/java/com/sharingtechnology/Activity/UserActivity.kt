package com.sharingtechnology.Activity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.sharingtechnology.Fragment.*
import com.sharingtechnology.R
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.FragmentForSearch
import com.sharingtechnology.util.OnlineUser
import com.sharingtechnology.util.Theme
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.nav_header.view.*
import java.util.*
class UserActivity : AppCompatActivity(), DrawerLayout.DrawerListener, NavigationView.OnNavigationItemSelectedListener {

    private val db = DatabaseHelper(this, 1)
    private var isExit = false // 双击退出程序
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.presentTheme) // 设置主题
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        init()
    }

    override fun onRestart() {
        super.onRestart()
        refreshUserData()
    }

    override fun onResume() {
        super.onResume()
        refreshUserData()
    }

    override fun onBackPressed() { // 重写返回键
        exitBy2Click() // 双击退出程序
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // 设置标题栏样式
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    //抽屉监听器
    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        if (slideOffset > 0) { //打开抽屉时则让主界面往后退（完成过渡）
            fragment_layout.elevation = drawerLayout.elevation - 100
        } else {
            fragment_layout.elevation = drawerLayout.elevation + 100
        }
    }

    override fun onDrawerOpened(drawerView: View) {
    }

    override fun onDrawerClosed(drawerView: View) {
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { // 抽屉按钮点击事件
        drawerLayout.closeDrawers() // 关闭抽屉栏
        when (item.itemId) {
            R.id.navTask -> { // 任务界面
                supportActionBar?.title = "SharingTechnology"
                replaceFragment(MainFragment())
            }

            R.id.navRecord -> {
                FragmentForSearch.pref = "RecordFragment"
                supportActionBar?.title = "我的记录"
                replaceFragment(RecordFragment())
            }

            R.id.navCountdown -> { // 倒计时
                FragmentForSearch.pref = "CountdownFragment"
                supportActionBar?.title = "倒计时"
                replaceFragment(CountdownFragment())
            }

            R.id.navChangePassword -> { // 修改密码
                changePassword()
            }

            R.id.navRobot -> { // robot
                FragmentForSearch.pref = "CustomerServiceFragment"
                supportActionBar?.title = "客服机器人"
                replaceFragment(CustomerServiceFragment())
            }
            R.id.theme -> { // 更换主题
                if (Theme.presentTheme == Theme.lightTheme) {
                    Theme.setTheme(Theme.darkTheme)
                } else {
                    Theme.setTheme(Theme.lightTheme)
                }
                startActivity(Intent(this, UserActivity::class.java))
                finish()
            }
            R.id.powerOff -> { // 退出登录
                OnlineUser.logout()
                finish()
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_bottom)
            }
        }
        return true
    }

    //导航栏
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return true
    }

    private fun init() { // 初始化
        FragmentForSearch.preActivity = this
        setSupportActionBar(toolBar)
        supportActionBar?.let { // 设置导航栏
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_drawerlist)
        }
        navView.setCheckedItem(R.id.navTask)
        navView.setNavigationItemSelectedListener { // 抽屉栏设置
            drawerLayout.closeDrawers()
            true
        }
        val headerLayout = navView.getHeaderView(0)//设置抽屉按钮
        //初始化侧边框用户信息
        refreshUserData()
        headerLayout.userImage.setOnClickListener {  // 用户头像
            startActivity(Intent(this, IndividualActivity::class.java))
        }
        navView.setNavigationItemSelectedListener(this)//抽屉选项添加事件监听
        drawerLayout.addDrawerListener(this)
        replaceFragment(MainFragment())
    }

    fun refreshUserData() { // 初始化用户信息
        val headerLayout = navView.getHeaderView(0)//设置抽屉按钮
        //初始化侧边框用户信息
        if (OnlineUser.userImage != null) { // 用户头像
            headerLayout.userImage.setImageBitmap(OnlineUser.userImage)
        }
        headerLayout.userName.text = OnlineUser.username // 用户名
        headerLayout.userName.setTextColor(Theme.getFontColor())
        headerLayout.telephone.text = OnlineUser.telephone
        headerLayout.email.text = OnlineUser.email
        headerLayout.balance.text = OnlineUser.balance.toString()
        headerLayout.credit.text = "￥${OnlineUser.credit}"
    }

    private fun replaceFragment(fragment: Fragment) { // 切换碎片
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_layout, fragment)
        transaction.commit()
    }

    private fun changePassword() {
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.change_password, null, false)
        val dialog = this.let { AlertDialog.Builder(it, R.style.changePasswordDialog) }//设置布局
        val frame = dialog.setView(dialogView).create() //添加任务窗口
        frame.show() // 显示对话框
        dialogView.findViewById<Button>(R.id.changePassword).setOnClickListener { // 修改密码按钮
            val warnOld = dialogView.findViewById<TextView>(R.id.warn_old)
            val warnConfirm = dialogView.findViewById<TextView>(R.id.warn_confirm)
            val oldPsw = dialogView.findViewById<EditText>(R.id.oldPassword).text.toString()
            val newPsw = dialogView.findViewById<EditText>(R.id.newPassword).text.toString()
            val conPsw = dialogView.findViewById<EditText>(R.id.confirmPassword).text.toString()
            var flag = true
            warnOld.text = ""
            warnConfirm.text = ""
            if (oldPsw != OnlineUser.password) { // 原密码
                warnOld.text = "原密码输入错误"
                flag = false
            }
            if (newPsw != conPsw) {
                warnConfirm.text = "两次密码输入不相同"
                flag = false
            }
            if (flag) {
                val confirm = AlertDialog.Builder(this).setMessage("确认修改密码？").setTitle("提示信息")
                confirm.setPositiveButton(R.string.confirm) { dialog, _ ->
                    if (db.changePassword(newPsw)) { // 如果修改成功
                        dialog.dismiss() // 关闭提示信息
                        frame.dismiss() // 关闭修改密码对话框
                        Toast.makeText(this, "密码修改成功！", Toast.LENGTH_SHORT).show()
                    }
                }
                confirm.setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss() // 关闭提示信息
                }
                confirm.show()
            }
        }
    }

    private fun exitBy2Click() {
        var tExit: Timer? = null
        if (!isExit) {
            isExit = true // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show()
            tExit = Timer()
            tExit.schedule(object : TimerTask() {
                override fun run() {
                    isExit = false // 取消退出
                }
            }, 2000) // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            finish()
        }
    }
}