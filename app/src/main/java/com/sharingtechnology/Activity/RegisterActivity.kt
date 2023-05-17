package com.sharingtechnology.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.project.androiddbpart.domain.User
import com.sharingtechnology.R
import com.sharingtechnology.helper.DatabaseHelper
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private val db = DatabaseHelper(this, 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        init()
    }

    override fun onRestart() {
        super.onRestart()
        init() // 重新初始化
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.login -> { // "新用户注册"按钮
                startActivity(Intent(this, LoginActivity::class.java)) // 切换界面到注册界面
            }
            R.id.toRegister -> { //"注册"按钮
                Toast.makeText(this, "toRegister", Toast.LENGTH_SHORT).show()
                register()
            }
        }
    }

    //初始化信息
    private fun init() {
        supportActionBar?.hide() // 隐藏状态栏
        toRegister.setOnClickListener(this)
        login.setOnClickListener(this)
    }

    private fun register() { // 注册
        val accountContent = account.text.toString()
        val passwordContent = password.text.toString()
        val userNameContent = userName.text.toString()
        val telephoneContent = telephone.text.toString()
        val emailContent = email.text.toString()
        val ret = db.register(User(accountContent, passwordContent, userNameContent, telephoneContent, emailContent))
        if (ret.flag) {//注册成功
            warn_account.text = ""
            warn_password.text = ""
            warn_username.text = ""
            warn_telephone.text = ""
            warn_email.text = ""
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(androidx.appcompat.R.anim.abc_tooltip_enter, androidx.appcompat.R.anim.abc_slide_in_bottom);
            finish()
        } else {
            warn_account.text = ret.account
            warn_password.text = ret.password
            warn_username.text = ret.username
            warn_telephone.text = ret.telephone
            warn_email.text = ret.email
        }
    }
}