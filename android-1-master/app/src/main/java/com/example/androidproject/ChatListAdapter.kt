package com.example.androidproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatListAdapter(private var chatList: List<ChatList>, private val onItemClick: (ChatList) -> Unit) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftTextView: TextView = itemView.findViewById(R.id.leftTextView)
        val rightTextView: TextView = itemView.findViewById(R.id.rightTextView)

        init {
            // 아이템 클릭 시에 호출되는 리스너 설정
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(chatList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatItem = chatList[position]

        // ChatList 클래스에 맞춰서 수정
        holder.leftTextView.text = chatItem.leftMessage
        holder.rightTextView.text = chatItem.rightMessage
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    // 새로운 데이터를 설정하는 함수
    fun setChatList(newChatList: List<ChatList>) {
        chatList = newChatList
        notifyDataSetChanged()
    }
}
