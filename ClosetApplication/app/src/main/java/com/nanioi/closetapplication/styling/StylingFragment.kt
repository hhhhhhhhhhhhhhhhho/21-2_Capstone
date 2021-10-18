package com.nanioi.closetapplication.styling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nanioi.closetapplication.databinding.FragmentStylingBinding

class StylingFragment: Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {


        val binding = FragmentStylingBinding.inflate(inflater,container,false)
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
        binding.backButton.setOnClickListener {
            activity?.let {
                binding.itemRecycleView.visibility = View.INVISIBLE
            }
        }
        binding.selectItemButton.setOnClickListener {
            activity?.let {
                binding.itemRecycleView.visibility = View.INVISIBLE
                // 아바타에 옷입히기 코드 구현
            }
        }

        return binding.root
    }
}