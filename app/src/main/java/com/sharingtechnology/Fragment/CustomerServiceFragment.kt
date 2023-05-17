package com.sharingtechnology.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sharingtechnology.R
import kotlinx.android.synthetic.main.fragment_customer_service.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class CustomerServiceFragment : Fragment() {
    private val msgList = ArrayList<Msg>()
    private var adapter: MsgAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_customer_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        adapter = MsgAdapter(msgList)
        recyclerView.adapter = adapter
        YY("在").start() //默认发送信息，激活机器人打招呼
        send.setOnClickListener { // 点击发送按钮
            val content = inputText.text.toString()
            if (content.isNotEmpty()) {
                setMsg(content, Msg.TYPE_SENT)
                inputText.setText("")
                YY(content).start()
            }else{
                Toast.makeText(activity, "Please enter the content.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setMsg(content:String,type:Int){ // 发送信息
        val msg = Msg(content, type)
        msgList.add(msg)
        adapter?.notifyItemChanged(msgList.size - 1)
        recyclerView.scrollToPosition(msgList.size - 1)
    }

    inner class YY(private val content:String) :Thread(){ // 机器人线程
        override fun run() {
            super.run()
            sleep(500)
            try {
                val client = OkHttpClient()
                val key = "d3293691ecbb5cfe034da7dbf6d3a17a" //天行机器人api_key
                val request = Request.Builder().url("https://api.tianapi.com/robot/index?key=$key&question=$content").build() //get请求
                val response = client.newCall(request).execute() //获取新的请求
                val responseData = response.body?.string()
                val result = JSONObject(responseData).getJSONArray("newslist").getJSONObject(0).getString("reply")
                Log.d("data", "onViewCreated: $result")
                activity?.runOnUiThread{
                    setMsg(result, Msg.TYPE_RECEIVED)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class MsgAdapter(private val msgList: List<Msg>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class LeftViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
            var leftMsg: TextView = view.findViewById(R.id.LeftMsg)
        }
        inner class RightViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
            var rightMsg: TextView = view.findViewById(R.id.RightMsg)
        }
        override fun getItemViewType(position: Int): Int {
            val msg = msgList[position]
            return msg.type
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            if (viewType == Msg.TYPE_RECEIVED) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_left_item, parent, false)
                LeftViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_right_item, parent, false)
                RightViewHolder(view)
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val msg = msgList[position]
            when (holder) {
                is LeftViewHolder -> holder.leftMsg.text = msg.content
                is RightViewHolder -> holder.rightMsg.text = msg.content
                else -> throw IllegalArgumentException()
            }
        }

        override fun getItemCount() = msgList.size

    }
    class Msg(val content:String,val type:Int) { // 消息类
        companion object{
            const val TYPE_RECEIVED = 0
            const val TYPE_SENT = 1
        }
    }
}
