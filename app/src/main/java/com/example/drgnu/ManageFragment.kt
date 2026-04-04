package com.example.drgnu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// 🔴 1번 중요 포인트: 반드시 androidx.fragment.app.Fragment 를 불러와야 해!
import androidx.fragment.app.Fragment

// 🔴 2번 중요 포인트: 클래스 이름 옆에 ': Fragment()' 를 꼭 붙여줘!
class ManageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_dashboard.xml 레이아웃을 화면에 띄우는 코드야
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
}