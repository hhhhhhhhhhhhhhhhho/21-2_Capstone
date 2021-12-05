package com.nanioi.closetapplication.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.closet.ClosetFragment
import com.nanioi.closetapplication.databinding.FragmentHomeBinding
import com.nanioi.closetapplication.styling.StylingFragment

class HomeFragment : Fragment(R.layout.fragment_home) {
    // 이미지 받는대로 구현

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.mainViewPager.apply {
            adapter = ImageSliderAdapter(requireContext())
        }
        binding.indicator.apply {
            setViewPager(binding.mainViewPager)
            createIndicators(4, 0)
        }

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
