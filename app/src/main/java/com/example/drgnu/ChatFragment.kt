package com.example.drgnu

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ChatFragment : Fragment() {

    private lateinit var rvChat: RecyclerView
    private lateinit var btnRecord: Button
    private lateinit var layoutEmptyPopup: View
    private lateinit var pillWait: TextView
    private lateinit var pillReady: TextView

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var speechRecognizer: SpeechRecognizer

    // MainActivity의 보안 무시 클라이언트 사용
    private val client = MainActivity.getUnsafeOkHttpClient()
    private var isRecording = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // 1. UI 요소 연결
        rvChat = view.findViewById(R.id.rv_chat)
        btnRecord = view.findViewById(R.id.btn_chat_record)
        layoutEmptyPopup = view.findViewById(R.id.layout_empty_popup)
        pillWait = view.findViewById(R.id.pill_wait)
        pillReady = view.findViewById(R.id.pill_ready)

        // ⭐ 중앙 내용 영역에만 블러 효과 적용 (Android 12+)
        applyBlurEffectToContainer(view.findViewById(R.id.rv_chat))

        // 어댑터 설정
        chatAdapter = ChatAdapter(mutableListOf())
        rvChat.layoutManager = LinearLayoutManager(context)
        rvChat.adapter = chatAdapter

        setupSpeechRecognizer()

        btnRecord.setOnClickListener {
            if (isRecording) stopRecordingLogic() else startRecordingLogic()
        }

        return view
    }

    // ⭐ 중앙 내용 영역에만 블러 효과를 적용하는 함수
    private fun applyBlurEffectToContainer(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 강한 블러 효과로 유리 느낌 극대화
            view.setRenderEffect(RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP))
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                pillWait.text = "녹음 중"
                pillWait.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.neon_blue))
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val userText = matches[0]
                    addMessageToChat(userText, true)
                    sendTextToServer(userText)
                }
                stopRecordingLogic()
            }

            override fun onError(error: Int) {
                isRecording = false
                pillWait.text = "오류"
                pillWait.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.neon_red))

                btnRecord.text = "녹음 시작"
                btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.neon_blue))
                speechRecognizer.cancel()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { pillWait.text = "처리 중" }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // 🔴 녹음 시작
    private fun startRecordingLogic() {
        isRecording = true
        btnRecord.text = "녹음 중지"
        btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.neon_red))

        layoutEmptyPopup.visibility = View.GONE
        rvChat.visibility = View.VISIBLE

        // 알약 상태: 유리 텍스트처럼 보이도록 투명도 조정
        pillReady.text = "대기"
        pillReady.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33FFFFFF"))

        speechRecognizer.cancel()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }
        speechRecognizer.startListening(intent)
    }

    // 🔵 녹음 중지
    private fun stopRecordingLogic() {
        isRecording = false
        btnRecord.text = "녹음 시작"
        btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.neon_blue))

        pillWait.text = "대기 중"
        pillWait.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33FFFFFF"))

        // 분석 중 상태 (노란색 네온)
        pillReady.text = "분석 중"
        pillReady.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.neon_yellow))

        speechRecognizer.stopListening()
    }

    private fun sendTextToServer(message: String) {
        val json = JSONObject().apply { put("message", message) }
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder().url("https://192.168.35.117:5000/chat").post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { pillReady.text = "서버 실패" }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    val aiAnswer = JSONObject(responseData).optString("answer", "결과 없음")
                    activity?.runOnUiThread {
                        // 분석 완료 상태 (초록색 네온)
                        pillReady.text = "분석완료"
                        pillReady.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.neon_green))
                        addMessageToChat(aiAnswer, false)
                    }
                }
            }
        })
    }

    private fun addMessageToChat(text: String, isUser: Boolean) {
        chatAdapter.addMessage(ChatMessage(text, isUser))
        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}