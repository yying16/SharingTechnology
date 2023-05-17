package com.sharingtechnology.util

import android.annotation.SuppressLint
import android.app.Activity
import androidx.fragment.app.Fragment
import com.sharingtechnology.Fragment.AdminTaskFragment

@SuppressLint("StaticFieldLeak")
object FragmentForSearch {
    var pref = "TaskFragment"
    var preFragment: Fragment? = null
    var preActivity:Activity? = null
    override fun toString(): String {
        return pref
    }
}