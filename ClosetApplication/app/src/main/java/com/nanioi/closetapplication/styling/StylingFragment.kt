package com.nanioi.closetapplication.styling

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.closet.ItemModel
import com.nanioi.closetapplication.closet.ItemState
import com.nanioi.closetapplication.closet.itemAdapter
import com.nanioi.closetapplication.closet.itemViewModel
import com.nanioi.closetapplication.databinding.FragmentStylingBinding

class StylingFragment: Fragment(R.layout.fragment_styling) {

    private val topItemList = mutableListOf<ItemModel>()
    private val pantsItemList = mutableListOf<ItemModel>()
    private val accessoryItemList = mutableListOf<ItemModel>()
    private val shoesItemList = mutableListOf<ItemModel>()

    private var binding: FragmentStylingBinding? = null

    private val ItemAdapter = stylingItemAdapter {
        viewModel.selectPhoto(it)
    }

    private val viewModel by viewModels<stylingItemViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentStylingBinding = FragmentStylingBinding.bind(view)
        binding = fragmentStylingBinding

        initViews()
        observeState()
    }

    private fun initViews() {
        binding!!.selectItemTap.visibility = View.INVISIBLE

        binding!!.itemRecycleView.apply {
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = ItemAdapter
        }

        //by 나연. 각 카테고리 버튼 클릭 시 item들 보여주는 RecyclerView 창 보여주기 (21.10.12)
        binding!!.topButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.VISIBLE
                initRecyclerView(0)
            }
        }
        binding!!.accessoryButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.VISIBLE
                initRecyclerView(2)
            }
        }
        binding!!.pantsButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.VISIBLE
                initRecyclerView(1)
            }
        }
        binding!!.shoesButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.VISIBLE
                initRecyclerView(3)
            }
        }
        binding!!.backButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.INVISIBLE
            }
        }
        binding!!.selectItemButton.setOnClickListener {
            activity?.let {
                viewModel.confirmCheckedPhotos()
                binding!!.selectItemTap.visibility = View.INVISIBLE
                // 아바타에 옷입히기 코드 구현
            }
        }
    }
    //by나연. 아이템 중복 쌓임 방지 아이템 리스트 초기화
    private fun initList() {
        topItemList.clear()
        pantsItemList.clear()
        accessoryItemList.clear()
        shoesItemList.clear()
    }

    private fun initRecyclerView(categoryNum: Int) {
        when(categoryNum){
            0->{
                ItemAdapter.submitList(topItemList)
                Log.d("aa", "topItem : " + topItemList.toString())
            }
            1->{
                ItemAdapter.submitList(pantsItemList)
                Log.d("aa", "topItem : " + topItemList.toString())
            }
            2->{
                ItemAdapter.submitList(accessoryItemList)
                Log.d("aa", "topItem : " + topItemList.toString())
            }
            3->{
                ItemAdapter.submitList(shoesItemList)
                Log.d("aa", "topItem : " + topItemList.toString())
            }
        }
    }
    private fun observeState() = viewModel.itemStateLiveData.observe(viewLifecycleOwner) {
        when (it) {
            is stylingState.Success -> handleSuccess(it)
            is stylingState.Confirm -> handleConfirm(it)
            else -> Unit
        }
    }
    private fun handleSuccess(state: stylingState.Success) = with(binding) {
        initList()
        Log.d("bb","observe success")
        for (item in state.photoList) {
            when (item.categoryNumber) {
                0 -> topItemList.add(item)
                1 -> pantsItemList.add(item)
                2 -> accessoryItemList.add(item)
                3 -> shoesItemList.add(item)
            }
        }
    }

    private fun handleConfirm(state: stylingState.Confirm) {
        Log.d("bb","observe confirm")
//        setResult(Activity.RESULT_OK, Intent().apply {
//            putExtra(URI_LIST_KEY, ArrayList(state.photoList.map { it.uri }))
//        })
//        finish()
    }

    override fun onResume() {
        super.onResume()

        Log.d("bb","resume")
        viewModel.fetchData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("bb","destroy")
        //  viewModel.
    }
}
