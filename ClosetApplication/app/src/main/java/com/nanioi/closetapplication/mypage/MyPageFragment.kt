package com.nanioi.closetapplication.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nanioi.closetapplication.databinding.FragmentMypageBinding

class MyPageFragment : Fragment(){

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentMypageBinding.inflate(inflater,container,false)

        //by 나연. 프로필 수정 버튼 클릭 시 activity이동 (21.09.27)
        binding.editProfileButton.setOnClickListener {
            activity?.let {
                startActivity(Intent(requireContext(),EditProfileActivity::class.java))
            }
        }

        return binding.root
    }


}