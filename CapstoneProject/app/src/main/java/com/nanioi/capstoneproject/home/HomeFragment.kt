package com.nanioi.capstoneproject.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nanioi.capstoneproject.MainActivity
import com.nanioi.capstoneproject.R
import com.nanioi.capstoneproject.Styling.StylingFragment
import com.nanioi.capstoneproject.closet.ClosetFragment
import com.nanioi.capstoneproject.databinding.FragmentHomeBinding

class HomeFragment: Fragment(R.layout.fragment_home) {
    // 이미지 받는대로 구현

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater,container,false)

        //by 나연. 버튼 클릭 시 해당 Fragment이동 (21.10.15)
        binding.goClosetButton.setOnClickListener(View.OnClickListener {
            (activity as MainActivity).replaceFragment(ClosetFragment())
        })
        binding.goStylingButton.setOnClickListener {
            (activity as MainActivity).replaceFragment(StylingFragment())
        }

        return binding.root
    }

}
