package com.nanioi.capstoneproject.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nanioi.capstoneproject.R
import com.nanioi.capstoneproject.databinding.ActivityEditProfileBinding
import com.nanioi.capstoneproject.databinding.ActivitySignInBinding

class EditProfileActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //TODO 준승님이 해야할 것 각각 EditText 부분에 회원정보 불러서 setText 시키기
        // 즉 클릭 전에 이미 저장된 자신의 정보들이 칸마다 정보가 불러와 져있고 화면에 그게 떠야 합니다.
        // 클릭해서 정보 입력하면 그 정보들은 그 프로필 수정 버튼이 눌려야 저장된 정보가 바뀌게 구현해주세요

    }
}