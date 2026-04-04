package com.example.drgnu

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class AnalysisFragment : Fragment() {

    private lateinit var radarChart: RadarChart
    private lateinit var tvSessionScore: TextView
    private lateinit var tvSessionRisk: TextView
    private lateinit var tvAiReasonText: TextView
    private lateinit var tvConfidenceScore: TextView
    private lateinit var tvScoreRepeat: TextView
    private lateinit var pbScoreRepeat: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val protectionView = view.findViewById<View>(R.id.view_status_protection)
        val contentRoot = view.findViewById<View>(R.id.layout_analysis_root)
        val scrollView = view.findViewById<NestedScrollView>(R.id.scroll_analysis)

        // 🌟 [Edge-to-Edge 가이드라인] 시스템 인셋 적용
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

            // 1. 그라데이션 보호막 높이를 상태바 높이에 맞춰 동적 조절
            protectionView.layoutParams.height = statusBarHeight + 30.toPx()
            protectionView.requestLayout()

            // 2. 콘텐츠가 상태바에 가려지지 않게 상단 여백 조절
            contentRoot.updatePadding(top = statusBarHeight + 20.toPx())
            insets
        }

        // 🌟 [추가 디테일] 스크롤 위치에 따라 보호막 투명도 조절
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val alpha = (scrollY.toFloat() / 100.toPx()).coerceAtMost(1f)
            protectionView.alpha = 0.6f + (alpha * 0.4f)
        }

        initViews(view)
        setupAllToggles(view)
        setupRadarChartDesign()
        simulateParsingAndUtilizingData()
    }

    private fun initViews(v: View) {
        radarChart = v.findViewById(R.id.radar_chart)
        tvSessionScore = v.findViewById(R.id.tv_session_score)
        tvSessionRisk = v.findViewById(R.id.tv_session_risk)
        tvAiReasonText = v.findViewById(R.id.tv_ai_reason_text)
        tvConfidenceScore = v.findViewById(R.id.tv_confidence_score)
        tvScoreRepeat = v.findViewById(R.id.tv_score_repeat)
        pbScoreRepeat = v.findViewById(R.id.pb_score_repeat)
    }

    private fun setupAllToggles(view: View) {
        setupToggle(view.findViewById(R.id.layout_toggle_lang), view.findViewById(R.id.layout_detail_lang), view.findViewById(R.id.iv_arrow_lang))
        setupToggle(view.findViewById(R.id.layout_toggle_history), view.findViewById(R.id.layout_detail_history), view.findViewById(R.id.iv_arrow_history))
        setupToggle(view.findViewById(R.id.layout_toggle_memory), view.findViewById(R.id.layout_detail_memory), view.findViewById(R.id.iv_arrow_memory))
    }

    private fun setupToggle(header: View, content: View, arrow: ImageView) {
        header.setOnClickListener {
            if (content.visibility == View.GONE) {
                content.visibility = View.VISIBLE
                arrow.animate().rotation(180f).start()
            } else {
                content.visibility = View.GONE
                arrow.animate().rotation(0f).start()
            }
        }
    }

    private fun setupRadarChartDesign() {
        radarChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            webColor = Color.parseColor("#2A2C3A")
            xAxis.valueFormatter = IndexAxisValueFormatter(listOf("반복", "기억", "공간", "논리", "속도"))
            xAxis.textColor = Color.WHITE
            yAxis.axisMinimum = 0f
            yAxis.axisMaximum = 100f
            yAxis.setDrawLabels(false)
        }
    }

    private fun simulateParsingAndUtilizingData() {
        tvSessionScore.text = "82.5점"
        tvSessionRisk.text = "주의"
        tvAiReasonText.text = "최근 대화에서 반복적인 질문 패턴이 관찰됩니다."
        tvConfidenceScore.text = "88%\n분석 신뢰도"
        tvScoreRepeat.text = "75"
        pbScoreRepeat.progress = 75

        val entries = listOf(RadarEntry(75f), RadarEntry(60f), RadarEntry(30f), RadarEntry(40f), RadarEntry(85f))
        val dataSet = RadarDataSet(entries, "언어 특징").apply {
            color = Color.parseColor("#2DB4FF")
            fillColor = Color.parseColor("#2DB4FF")
            setDrawFilled(true)
            fillAlpha = 60
            setDrawValues(false)
        }
        radarChart.data = RadarData(dataSet)
        radarChart.invalidate()
    }

    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()
}