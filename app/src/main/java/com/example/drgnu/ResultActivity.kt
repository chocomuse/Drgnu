package com.example.drgnu

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.json.JSONObject

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // ChatFragment에서 보낸 JSON 데이터 받기
        val resultData = intent.getStringExtra("JSON_DATA")
        if (resultData != null) {
            updateUI(resultData)
        }

        // 다시 대화하기 버튼 클릭 시 현재 액티비티 종료 (채팅 화면으로 돌아감)
        findViewById<Button>(R.id.btn_retry).setOnClickListener {
            finish()
        }
    }

    private fun updateUI(data: String) {
        try {
            val json = JSONObject(data)

            // ⭐ 서버 JSON 응답 형식에 맞춰 키값 추출
            // 전체 세션 점수를 소수점일 수 있으므로 Double로 받아서 Int로 변환
            val sessionScore = json.optDouble("session_score", 0.0).toInt()

            // 상태와 상세 분석 내용 (키값이 judgment, reason)
            val status = json.optString("judgment", "상태 불명")
            val summary = json.optString("reason", "분석 내용이 없습니다.")

            // UI에 값 적용
            findViewById<TextView>(R.id.tv_result_score).text = "${sessionScore}%"
            findViewById<TextView>(R.id.tv_result_status).text = status
            findViewById<TextView>(R.id.tv_result_summary).text = summary

            // ⭐ 상태별 색상 변경 (서버가 보내는 judgment 문자열에 따라 분기)
            val colorRes = when (status) {
                "위험", "매우위험", "High", "Critical" -> R.color.neon_red
                "주의", "Warning" -> R.color.neon_yellow
                else -> R.color.neon_green // "정상", "Normal" 등
            }

            val resolvedColor = ContextCompat.getColor(this, colorRes)
            findViewById<TextView>(R.id.tv_result_score).setTextColor(resolvedColor)
            findViewById<TextView>(R.id.tv_result_status).setTextColor(resolvedColor)

        } catch (e: Exception) {
            e.printStackTrace()
            // 파싱 에러 발생 시 기본 에러 메시지 노출
            findViewById<TextView>(R.id.tv_result_status).text = "데이터 오류"
            findViewById<TextView>(R.id.tv_result_summary).text = "데이터를 불러오는 중 문제가 발생했습니다."
        }
    }
}