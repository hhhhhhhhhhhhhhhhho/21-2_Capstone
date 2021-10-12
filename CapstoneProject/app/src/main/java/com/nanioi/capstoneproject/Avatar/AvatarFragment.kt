package com.nanioi.capstoneproject.Avatar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.nanioi.capstoneproject.R
import com.nanioi.capstoneproject.databinding.FragmentAvatarBinding
import com.nanioi.capstoneproject.databinding.FragmentMypageBinding
import com.nanioi.capstoneproject.mypage.EditProfileActivity

class AvatarFragment: Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {


        val binding = FragmentAvatarBinding.inflate(inflater,container,false)
       binding.itemRecycleView.visibility = View.INVISIBLE

        //by 나연. 각 카테고리 버튼 클릭 시 item들 보여주는 RecyclerView 창 보여주기 (21.10.12)
        binding.topButton.setOnClickListener {
            activity?.let {
                binding.itemRecycleView.visibility = View.VISIBLE
            }
        }
        binding.accessoryButton.setOnClickListener {
            activity?.let {
                binding.itemRecycleView.visibility = View.VISIBLE
            }
        }
        binding.pantsButton.setOnClickListener {
            activity?.let {
                binding.itemRecycleView.visibility = View.VISIBLE
            }
        }
        binding.shoesButton.setOnClickListener {
            activity?.let {
                binding.itemRecycleView.visibility = View.VISIBLE
            }
        }
//        binding.backButton.setOnClickListener {
//
//        }

        return binding.root
    }

//    fun setItemRecycleView()
}