package com.example.drgnu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ChatFragment : Fragment() {

    private var audioFocusRequest: AudioFocusRequest? = null
    private var isRecording = false
    private lateinit var rvChat: RecyclerView
    private lateinit var emptyPopup: View
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뷰 바인딩
        val protectionView = view.findViewById<View>(R.id.view_status_protection)
        val headerBox = view.findViewById<View>(R.id.layout_header_box)
        val btnRecord = view.findViewById<LinearLayout>(R.id.btn_chat_record)
        val tvRecordText = view.findViewById<TextView>(R.id.tv_btn_record_text)
        rvChat = view.findViewById(R.id.rv_chat)
        emptyPopup = view.findViewById(R.id.layout_empty_popup)

        // 2. 상태바 높이 대응
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            protectionView.layoutParams.height = statusBarHeight + 30.toPx()
            protectionView.requestLayout()
            headerBox.updatePadding(top = statusBarHeight)
            insets
        }

        // 3. 리사이클러뷰 설정
        chatAdapter = ChatAdapter(chatMessages)
        rvChat.layoutManager = LinearLayoutManager(requireContext())
        rvChat.adapter = chatAdapter

        audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/drgnu_record.m4a"

        // 4. 녹음 버튼 클릭 이벤트
        btnRecord.setOnClickListener {
            // 모든 버튼에 햅틱 반응 추가
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isRecording) {
                startRecording()
                isRecording = true
                tvRecordText.text = "녹음 중지"
                btnRecord.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_red_gradient)
                showChatWindow()
            } else {
                stopRecording()
                isRecording = false
                tvRecordText.text = "녹음 시작"
                btnRecord.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_blue_gradient)

                chatAdapter.addMessage(ChatMessage(content = "음성 분석 중...", isUser = false))
                rvChat.scrollToPosition(chatAdapter.itemCount - 1)

                uploadAudioToServer()
            }
        }
    }

    private fun startRecording() {
        // 🌟 녹음 시작 시 미디어 중지 (오디오 포커스 획득)
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE).build()
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
        }

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireContext())
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }

        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            // 🌟 STT 인공지능 최적화 세팅 (16kHz, Mono)
            setAudioChannels(1)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(32000)

            setOutputFile(audioFilePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("ChatFragment", "녹음 시작 실패: ${e.message}")
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            // 🌟 녹음 중지 후 미디어 재개 (오디오 포커스 반환)
            val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.e("ChatFragment", "녹음 중지 실패: ${e.message}")
        }
    }

    private fun uploadAudioToServer() {
        val file = File(audioFilePath)
        if (!file.exists()) return

        // 파일 크기 로그 확인용
        Log.e("ChatFragment", "🎤 보낼 파일 크기: ${file.length()} bytes")

        val uploadUrl = "${Constants.BASE_URL}api/stt-analyze"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("audio", file.name, file.asRequestBody("audio/mp4".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder().url(uploadUrl).post(requestBody).build()

        // 🌟 서버 인내심 설정 (60초)
        val client = OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatFragment", "통신 실패: ${e.message}")
                updateUI("서버 연결 실패", "인터넷을 확인해 주세요.")
            }

            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string()
                Log.e("ChatFragment", "🎁 서버 응답 데이터: $resBody")

                if (response.isSuccessful && resBody != null) {
                    try {
                        val json = JSONObject(resBody)

                        // 🌟 도윤이가 보낸 실제 키값으로 매핑 (answer, stt_result)
                        val stt = json.optString("stt_result", "")
                        val ai = json.optString("answer", "")

                        // 결과가 비어있을 때 처리
                        val displayStt = if (stt.trim().isEmpty()) "(내용을 인식하지 못했습니다)" else stt
                        val displayAi = if (ai.trim().isEmpty()) "(AI 답변 생성 실패)" else ai

                        // 🌟 DataManager에 분석 결과 저장
                        val score = json.optDouble("session_score", 0.0).toInt()
                        DataManager.sessionScore = "${score}점"
                        DataManager.riskStatus = json.optString("risk_level", "정상")
                        DataManager.aiReason = json.optString("reason", "분석 완료")
                        DataManager.cumulativeRisk = json.optString("risk_level", "정상")

                        // 세부 점수 매핑 (도윤이 키값 기준)
                        DataManager.scoreRepeat = json.optInt("score_repeat", 0)
                        DataManager.scoreMemory = json.optInt("score_memory", 0)
                        DataManager.scoreSpace = json.optInt("score_incoherence", 0)
                        DataManager.scoreLogic = json.optInt("score_time_confusion", 0)
                        DataManager.scoreSpeed = json.optInt("score_total", 0)

                        DataManager.isDataLoaded = true
                        updateUI(displayStt, displayAi)

                    } catch (e: Exception) {
                        Log.e("ChatFragment", "JSON 에러: ${e.message}")
                        updateUI("데이터 오류", "응답 해석에 실패했습니다.")
                    }
                } else {
                    updateUI("서버 에러", "코드: ${response.code}")
                }
            }
        })
    }

    private fun updateUI(userText: String, aiText: String) {
        Handler(Looper.getMainLooper()).post {
            chatAdapter.removeLastMessage() // "분석 중..." 삭제
            chatAdapter.addMessage(ChatMessage(content = userText, isUser = true))
            chatAdapter.addMessage(ChatMessage(content = aiText, isUser = false))
            rvChat.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    private fun showChatWindow() {
        if (emptyPopup.visibility == View.VISIBLE) {
            emptyPopup.visibility = View.GONE
            rvChat.visibility = View.VISIBLE
        }
    }

    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()
}