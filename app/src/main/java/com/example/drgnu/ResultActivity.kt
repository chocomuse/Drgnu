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
        // 1. 여기서 XML 이름을 정확히 연결했는지 확인!
        setContentView(R.layout.activity_result)

        val resultData = intent.getStringExtra("JSON_DATA")
        if (resultData != null) {
            updateUI(resultData)
        }

        // 2. R.id.btn_retry가 XML의 id와 일치해야 합니다.
        findViewById<Button>(R.id.btn_retry).setOnClickListener {
            finish()
        }
    }

    private fun updateUI(data: String) {
        try {
            val json = JSONObject(data)

            // 서버 JSON 응답 형식에 맞춰 키값 수정 (score, judgment, reason)
            val score = json.optInt("score", 0)
            val status = json.optString("judgment", "분석 완료")
            val summary = json.optString("reason", "분석 내용이 없습니다.")

            findViewById<TextView>(R.id.tv_result_score).text = "$score%"
            findViewById<TextView>(R.id.tv_result_status).text = status
            findViewById<TextView>(R.id.tv_result_summary).text = summary

            // 상태별 색상 변경
            val colorRes = when(status) {
                "위험", "매우위험" -> R.color.neon_red
                "주의" -> R.color.neon_yellow
                else -> R.color.neon_green
            }
            findViewById<TextView>(R.id.tv_result_score).setTextColor(ContextCompat.getColor(this, colorRes))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}