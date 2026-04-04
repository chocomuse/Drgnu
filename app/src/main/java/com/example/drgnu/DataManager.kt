package com.example.drgnu

/**
 * 앱 전체에서 공유되는 데이터 창고 (싱글톤 객체)
 */
object DataManager {

    // 데이터 로드 여부 (이게 true가 되어야 각 탭에서 그래프를 그림)
    var isDataLoaded = false

    // --- [분석/대시보드 공통 데이터] ---
    var sessionScore: String = "—"          // 이번 세션 점수
    var riskStatus: String = "분석 전"       // 현재 위험도 상태 (정상/주의/위험)
    var aiReason: String = "분석 대기 중..."    // AI 분석 근거 문장
    var cumulativeRisk: String = "정상"      // 🌟 누적 위험 상태 (대시보드 메인)

    // --- [세부 분석 지표 (0~100점)] ---
    var scoreRepeat: Int = 0   // 질문 반복
    var scoreMemory: Int = 0   // 기억 혼란
    var scoreSpace: Int = 0    // 공간 지각
    var scoreLogic: Int = 0    // 논리 구성
    var scoreSpeed: Int = 0    // 반응 속도

    // --- [대시보드 통계] ---
    var totalAverage: Float = 0f    // 전체 평균 점수
    var recent5Average: Float = 0f  // 최근 5회 평균
    var totalChatCount: Int = 0     // 총 대화 횟수
}