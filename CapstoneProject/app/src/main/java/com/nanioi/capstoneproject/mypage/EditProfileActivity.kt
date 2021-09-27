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

    }
}