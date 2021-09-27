package com.nanioi.capstoneproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nanioi.capstoneproject.databinding.ActivitySignInBinding
import com.nanioi.capstoneproject.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // by 나연. 회원가입 완료 버튼 클릭 시 액티비티 이동 (21.09.27)
        binding.signUpButton.setOnClickListener {

            //TODO 입력 데이터들 저장하는거 추가해주세요

            startActivity(Intent(this, MainActivity::class.java))
        }


    }
}