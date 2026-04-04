package com.example.drgnu

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// --- 채팅용 데이터 모델 ---
data class ChatRequest(val message: String, val session_id: String = "test_user_001")
data class ChatResponse(
    @SerializedName("answer") val answer: String?,
    @SerializedName("response") val response: String?
)

// --- 대시보드 파싱용 데이터 모델 (친구가 말한 파싱의 핵심!) ---
data class ScoreRecord(
    @SerializedName("date") val date: String,   // 예: "04-03"
    @SerializedName("score") val score: Float   // 예: 85.5
)

data class DashboardHistoryResponse(
    @SerializedName("history") val history: List<ScoreRecord> // 점수 리스트 파싱
)

// --- 통합 API 인터페이스 ---
interface ApiService {
    @POST("chat")
    fun getChatResponse(@Body request: ChatRequest): Call<ChatResponse>

    // 대시보드 데이터를 가져오는 API (도윤이 서버 주소에 맞춰 '/api/history' 등으로 수정 가능)
    @GET("api/get_history")
    fun getDashboardHistory(@Query("session_id") sessionId: String): Call<DashboardHistoryResponse>
}