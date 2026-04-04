package com.example.drgnu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavContainer: LinearLayout
    private lateinit var pillDashboard: View
    private lateinit var pillChat: View
    private lateinit var pillAnalysis: View
    private lateinit var pillManage: View

    // 🌟 마이크 권한 요청을 처리할 런처 (반드시 onCreate 이전에 선언해야 안전함)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 사용자가 권한을 허용했을 때 (정상 진행)
        } else {
            // 사용자가 권한을 거부했을 때 (필요시 안내 메시지 추가 가능)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🌟 Edge-to-Edge 활성화
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🌟 앱 시작 시 마이크 권한이 있는지 확인하고 없으면 팝업 띄우기
        checkMicrophonePermission()

        // 최상단 레이아웃(main)에 하단 인셋 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 하단 탭바가 가려지지 않게 하단만 패딩 부여
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        // 뷰 연결
        bottomNavContainer = findViewById(R.id.bottom_nav_container)
        pillDashboard = findViewById(R.id.nav_dashboard_pill)
        pillChat = findViewById(R.id.nav_chat_pill)
        pillAnalysis = findViewById(R.id.nav_analysis_pill)
        pillManage = findViewById(R.id.nav_manage_pill)

        // 클릭 리스너
        findViewById<FrameLayout>(R.id.btn_nav_dashboard).setOnClickListener { selectTab(0) }
        findViewById<FrameLayout>(R.id.btn_nav_chat).setOnClickListener { selectTab(1) }
        findViewById<FrameLayout>(R.id.btn_nav_analysis).setOnClickListener { selectTab(2) }
        findViewById<FrameLayout>(R.id.btn_nav_manage).setOnClickListener { selectTab(3) }

        selectTab(1) // 기본: 대화 탭
    }

    // 🌟 마이크 권한 체크 함수
    private fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 사용자에게 요청 팝업 띄우기
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun selectTab(index: Int) {
        pillDashboard.visibility = View.INVISIBLE
        pillChat.visibility = View.INVISIBLE
        pillAnalysis.visibility = View.INVISIBLE
        pillManage.visibility = View.INVISIBLE

        when (index) {
            0 -> { pillDashboard.visibility = View.VISIBLE; replaceFragment(DashboardFragment()) }
            1 -> { pillChat.visibility = View.VISIBLE; replaceFragment(ChatFragment()) }
            2 -> { pillAnalysis.visibility = View.VISIBLE; replaceFragment(AnalysisFragment()) }
            3 -> { pillManage.visibility = View.VISIBLE; replaceFragment(ManageFragment()) }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}