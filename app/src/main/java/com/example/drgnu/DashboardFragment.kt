package com.example.drgnu

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val protectionView = view.findViewById<View>(R.id.view_status_protection)
        val contentRoot = view.findViewById<View>(R.id.layout_dashboard_root)
        val scrollView = view.findViewById<NestedScrollView>(R.id.scroll_dashboard)

        // 1. Edge-to-Edge 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            protectionView.layoutParams.height = statusBarHeight + 30.toPx()
            protectionView.requestLayout()
            contentRoot.updatePadding(top = statusBarHeight + 20.toPx())
            insets
        }

        // 2. 스크롤 애니메이션
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val alpha = (scrollY.toFloat() / 100.toPx()).coerceAtMost(1f)
            protectionView.alpha = 0.6f + (alpha * 0.4f)
        }

        // 3. 토글 로직
        val toggleHeader = view.findViewById<View>(R.id.layout_toggle_detail)
        val detailContent = view.findViewById<View>(R.id.layout_detail_content)
        val arrow = view.findViewById<ImageView>(R.id.iv_expand_arrow)

        toggleHeader.setOnClickListener {
            if (detailContent.visibility == View.GONE) {
                detailContent.visibility = View.VISIBLE
                arrow.animate().rotation(180f).start()
            } else {
                detailContent.visibility = View.GONE
                arrow.animate().rotation(0f).start()
            }
        }

        // 🌟 4. 차트 초기화 호출
        setupCharts(view)
    }

    private fun setupCharts(v: View) {
        val pieChart = v.findViewById<PieChart>(R.id.risk_gauge_chart)
        val lineChart = v.findViewById<LineChart>(R.id.time_line_chart)

        // --- [PieChart: 누적 위험도 게이지 초기화] ---
        pieChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT) // 가운데 구멍 투명
            holeRadius = 80f // 도넛 두께
            setCenterText("데이터 없음")
            setCenterTextColor(Color.parseColor("#8A8A8E"))
            setNoDataText("데이터를 불러오는 중...")

            // 빈 상태를 보여주기 위한 가짜 데이터 (회색 링)
            val entries = listOf(PieEntry(100f))
            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(Color.parseColor("#1E202B")) // 카드 배경색과 비슷한 어두운 회색
                setDrawValues(false)
            }
            data = PieData(dataSet)
            invalidate()
        }

        // --- [LineChart: 시간별 점수 추이 초기화] ---
        lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setScaleEnabled(false)
            setNoDataText("데이터가 부족합니다.")
            setNoDataTextColor(Color.parseColor("#8A8A8E"))

            // X축 설정
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#8A8A8E")
                setDrawGridLines(false)
                axisLineColor = Color.parseColor("#2A2C3A")
            }

            // Y축 설정 (왼쪽)
            axisLeft.apply {
                textColor = Color.parseColor("#8A8A8E")
                gridColor = Color.parseColor("#2A2C3A")
                axisMinimum = 0f
                axisMaximum = 100f
                setLabelCount(5, true)
            }
            axisRight.isEnabled = false // 오른쪽 축 숨김

            // 빈 상태를 보여주기 위한 가짜 데이터 (투명한 선)
            val entries = listOf(Entry(0f, 0f))
            val dataSet = LineDataSet(entries, "").apply {
                color = Color.TRANSPARENT // 안 보이게 처리
                setCircleColor(Color.TRANSPARENT)
                setDrawValues(false)
            }
            data = LineData(dataSet)
            invalidate()
        }
    }

    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()
}