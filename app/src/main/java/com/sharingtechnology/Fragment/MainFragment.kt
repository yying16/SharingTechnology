package com.sharingtechnology.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sharingtechnology.R
import com.sharingtechnology.util.FragmentForSearch
import com.sharingtechnology.util.Theme
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment() : Fragment(){


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //底部导航栏跳转设置
        Bottom_Navigation.setOnNavigationItemSelectedListener{
                when (it.itemId) {
                    R.id.task -> {
                        FragmentForSearch.pref = "TaskFragment"
                        replaceFragment(TaskFragment()) // 更换碎片
                    }
                    R.id.issue -> {
                        FragmentForSearch.pref = "IssueFragment"
                        replaceFragment(IssueFragment()) // 更换碎片
                    }
                    R.id.personalCenter -> {
                        FragmentForSearch.pref = "IndividualFragment"
                        replaceFragment(IndividualFragment()) // 更换碎片
                    }
                }
                true
            }
        Bottom_Navigation.setBackgroundColor(Theme.getBottomColor())
        replaceFragment(TaskFragment()) // 更换碎片
    }

    private fun replaceFragment(fragment: Fragment) { // 更换碎片
        val fragmentManager = childFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_layout, fragment)
        transaction.commit()
    }

}