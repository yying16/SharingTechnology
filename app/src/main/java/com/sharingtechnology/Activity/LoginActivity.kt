package com.sharingtechnology.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.sharingtechnology.R
import com.sharingtechnology.service.WordCloudService
import com.sharingtechnology.helper.DatabaseHelper
import com.sharingtechnology.util.OnlineUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.account
import kotlinx.android.synthetic.main.activity_login.password

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    val db = DatabaseHelper(this, 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    override fun onRestart() {
        super.onRestart()
        init() // 重新初始化
    }


    override fun onClick(v: View) { // 点击事件监听
        when (v.id) {
            R.id.register -> { // "新用户注册"按钮
                startActivity(Intent(this, RegisterActivity::class.java)) // 切换界面到注册界面
                finish()
            }
            R.id.toLogin -> { // "登录"按钮
                if (login()) {
                    Log.d("TAG", "onClick: ${OnlineUser.status}")
                    if (OnlineUser.status) {//管理员
                        startActivity(Intent(this, AdminActivity::class.java)) // 切换界面到注册界面
                    } else {//用户
                        startActivity(Intent(this, UserActivity::class.java)) // 切换界面到注册界面
                        val intent = Intent(this, WordCloudService::class.java)
                        intent.putExtra("content", db.getMyWorldCloud())
                        startService(intent) // 启动词云分词后台服务
                    }
                    Toast.makeText(this, "欢迎${OnlineUser.username}", Toast.LENGTH_SHORT).show()
                    finish()
                }

            }
        }
    }

    //init函数用于项目的初始化操作
    private fun init() {
        // 直接登录
        if (OnlineUser.account != "") {
            if (OnlineUser.status) {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                startActivity(Intent(this, UserActivity::class.java))
            }

            finish()
        }
        Log.d("TAG", "init: ${db.queryAllTask()}")
        supportActionBar?.hide() // 隐藏状态栏
        toLogin.setOnClickListener(this)
        register.setOnClickListener(this)
        db.init()
    }

    private fun login(): Boolean { // 登录
        val accountContent = account.text.toString()
        val passwordContent = password.text.toString()
        val ret = db.login(accountContent, passwordContent)
        if (ret.flag) {
            warn_account.text = ""
            warn_password.text = ""
            return true
        } else {
            warn_account.text = ret.account
            warn_password.text = ret.password
            return false
        }
    }
}