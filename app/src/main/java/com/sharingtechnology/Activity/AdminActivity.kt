package com.sharingtechnology.Activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sharingtechnology.Fragment.*
import com.sharingtechnology.R
import com.sharingtechnology.util.FragmentForSearch
import com.sharingtechnology.util.OnlineUser
import kotlinx.android.synthetic.main.activity_admin.*
import java.util.*

class AdminActivity : AppCompatActivity() {
    private var isExit = false // 双击退出程序
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        supportActionBar?.show()
        init()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.powerOff) {
            OnlineUser.logout()
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_bottom)
        }
        return true
    }

    override fun onBackPressed() { // 重写返回键
        exitBy2Click() // 双击退出程序
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_admin, menu)
        return true
    }

    private fun init() {
        setSupportActionBar(toolBar)
        Bottom_Navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.task -> {
                    FragmentForSearch.pref = "AdminTaskFragment"
                    replaceFragment(AdminTaskFragment()) // 更换碎片
                }
                R.id.user -> {
                    FragmentForSearch.pref = "AdminUserFragment"
                    replaceFragment(AdminUserFragment()) // 更换碎片
                }
                R.id.data -> {
                    FragmentForSearch.pref = "AdminDataFragment"
                    replaceFragment(AdminDataFragment()) // 更换碎片
                }
            }
            true
        }
        Bottom_Navigation.setBackgroundColor(Color.parseColor("#FFFFFF"))
        FragmentForSearch.pref = "AdminTaskFragment0"
        replaceFragment(AdminTaskFragment()) // 更换碎片
    }

    private fun replaceFragment(fragment: Fragment) { // 切换碎片
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_layout, fragment)
        transaction.commit()
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