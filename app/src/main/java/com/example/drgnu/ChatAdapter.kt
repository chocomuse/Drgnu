package com.example.drgnu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 대화 데이터를 담는 그릇
data class ChatMessage(val text: String, val isUser: Boolean)

class ChatAdapter(private val messages: MutableList<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_USER = 1
        const val VIEW_TYPE_AI = 2
    }

    // 메시지가 유저 것인지 AI 것인지 판단하여 레이아웃 결정
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            // 유저 말풍선 레이아웃 연결
            UserViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false))
        } else {
            // AI 말풍선 레이아웃 연결
            AiViewHolder(inflater.inflate(R.layout.item_chat_ai, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.bind(message.text)
        } else if (holder is AiViewHolder) {
            holder.bind(message.text)
        }
    }

    override fun getItemCount(): Int = messages.size

    // 새 메시지가 올 때마다 리스트를 갱신하는 함수
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // 유저용 뷰홀더
    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text: TextView = view.findViewById(R.id.tv_user_message)
        fun bind(msg: String) {
            text.text = msg
        }
    }

    // AI용 뷰홀더
    class AiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text: TextView = view.findViewById(R.id.tv_ai_message)
        fun bind(msg: String) {
            text.text = msg
        }
    }
}