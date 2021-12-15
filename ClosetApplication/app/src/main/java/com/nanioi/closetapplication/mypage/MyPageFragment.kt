package com.nanioi.closetapplication.mypage

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.LoginUserData
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_AVATAR_FRONT
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_BODY_BACK
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_BODY_FRONT
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_EMAIL
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_GENDER
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_HEIGHT
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_NAME
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_UID
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_WEIGHT
import com.nanioi.closetapplication.databinding.FragmentClosetBinding
import com.nanioi.closetapplication.databinding.FragmentMypageBinding
import com.nanioi.closetapplication.databinding.FragmentStylingBinding

class MyPageFragment : Fragment(R.layout.fragment_mypage) {
    private lateinit var binding: FragmentMypageBinding

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentMypageBinding = FragmentMypageBinding.bind(view)
        binding = fragmentMypageBinding

        binding.nameTextView.text = "이름 : ${LoginUserData.name}"
        binding.emailTextView.text = LoginUserData.email
        binding.gender.text = "성별 : ${LoginUserData.gender}"
        binding.height.text = "키 : ${LoginUserData.cm} cm"
        binding.weight.text = "몸무게 : ${LoginUserData.kg} kg"

        //아바타 사진 넣기
        Glide.with(view)
            .load(LoginUserData.avatar_front_ImageUrl)
            .into(binding.mypagaAvatarImageView)


        //아바타에 애니메이션 효과 추가
        val avataranim :Animation = AnimationUtils.loadAnimation(requireContext(),R.anim.avataranim)
        binding.mypagaAvatarImageView.startAnimation(avataranim)


        //by 나연. 프로필 수정 버튼 클릭 시 activity이동 (21.09.27)
        binding.editProfileButton.setOnClickListener {
            activity?.let {
                startActivity(Intent(requireContext(), EditProfileActivity::class.java))
            }
        }
    }
}