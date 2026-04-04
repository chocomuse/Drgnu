package com.example.drgnu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val chatList: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_USER = 1
    private val TYPE_AI = 2

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].isUser) TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            val view = inflater.inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_ai, parent, false)
            AiViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatList[position]
        if (holder is UserViewHolder) {
            holder.tvMessage.text = chatMessage.content
        } else if (holder is AiViewHolder) {
            holder.tvMessage.text = chatMessage.content
        }
    }

    override fun getItemCount(): Int = chatList.size

    // 🌟 메시지 추가 (메인 스레드에서 호출 권장)
    fun addMessage(chatMessage: ChatMessage) {
        chatList.add(chatMessage)
        notifyItemInserted(chatList.size - 1)
    }

    // 🌟 마지막 메시지(분석 중...) 삭제 (튕김 방지 핵심)
    fun removeLastMessage() {
        if (chatList.isNotEmpty()) {
            val position = chatList.size - 1
            chatList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tv_chat_user_message)
    }

    class AiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tv_chat_ai_message)
    }
}