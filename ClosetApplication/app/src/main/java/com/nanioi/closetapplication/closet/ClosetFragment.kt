package com.nanioi.closetapplication.closet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey.Companion.DB_ITEM
import com.nanioi.closetapplication.DBkey.Companion.DB_USERS
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.databinding.FragmentClosetBinding

class ClosetFragment : Fragment(R.layout.fragment_closet) {

    private var binding: FragmentClosetBinding? = null

    private val topItemAdapter = itemAdapter {
        viewModel.selectPhoto(it)
    }
    private val pantsItemAdapter = itemAdapter {
        viewModel.selectPhoto(it)
    }
    private val accessoryItemAdapter = itemAdapter {
        viewModel.selectPhoto(it)
    }
    private val shoesItemAdapter = itemAdapter {
        viewModel.selectPhoto(it)
    }

    private val viewModel by viewModels<itemViewModel>()
    //private val viewModel by lazy { ViewModelProvider(this).get(itemViewModel::class.java) }

    private val topItemList = mutableListOf<ItemModel>()
    private val pantsItemList = mutableListOf<ItemModel>()
    private val accessoryItemList = mutableListOf<ItemModel>()
    private val shoesItemList = mutableListOf<ItemModel>()

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding 변수가 nullable이기 때문 사용할때마다 null을 풀어주는 체크를 해줘야함
        //onViewCreated 안에서만 절대 null이 될 수 없는 변수로 사용하기 위해 지역변수로 선언
        val fragmentClosetBinding = FragmentClosetBinding.bind(view)
        binding = fragmentClosetBinding

        initViews(view, fragmentClosetBinding)
        viewModel.fetchData()
        observeState()
    }

    //by.나연 뷰 초기화 함수 (21.10.18)
    private fun initViews(view: View, fragmentClosetBinding: FragmentClosetBinding) {

        fragmentClosetBinding.topItemRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = topItemAdapter
        }
        fragmentClosetBinding.pantsItemRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = pantsItemAdapter
        }
        fragmentClosetBinding.accessoryItemRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = accessoryItemAdapter
        }
        fragmentClosetBinding.shoesItemRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = shoesItemAdapter
        }
        fragmentClosetBinding.addItemButton.setOnClickListener {
            context?.let {
                if (auth.currentUser != null) {
                    val intent = Intent(it, AddImageActivity::class.java)
                    startActivity(intent)
                } else {
                    Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        //todo 선택이미지 삭제기능
        fragmentClosetBinding.deleteItemButton.setOnClickListener {
            viewModel.deletePhoto()
        }
        //todo 선택이미지 가상스타일링 스타일링 탭으로 정보 전송
        fragmentClosetBinding.goDressUpButton.setOnClickListener {
            viewModel.confirmCheckedPhotos()
            // todo 스타일링 탭으로 정보전송
        }
    }

    //by나연. 아이템 중복 쌓임 방지 아이템 리스트 초기화
    private fun initList() {
        topItemList.clear()
        pantsItemList.clear()
        accessoryItemList.clear()
        shoesItemList.clear()
    }

    private fun observeState() = viewModel.itemStateLiveData.observe(this) {
        when (it) {
            is ItemState.Loading -> handleLoading()
            is ItemState.Success -> handleSuccess(it)
            is ItemState.Confirm -> handleConfirm(it)
            else -> Unit
        }
    }

    //by 나연. 데이터 변경 관찰, UI 업데이트 함수 (21.10.25)
    private fun handleLoading() = with(binding) {
        this!!.progressBar.isVisible = true
    }

    private fun handleSuccess(state: ItemState.Success) = with(binding) {
        this!!.progressBar.isGone = true
        initList()
        for (item in state.photoList) {
            when (item.categoryNumber) {
                0 -> topItemList.add(item)
                1 -> pantsItemList.add(item)
                2 -> accessoryItemList.add(item)
                3 -> shoesItemList.add(item)
            }
        }
        topItemAdapter.setPhotoList(topItemList)
        pantsItemAdapter.setPhotoList(pantsItemList)
        accessoryItemAdapter.setPhotoList(accessoryItemList)
        shoesItemAdapter.setPhotoList(shoesItemList)

    }

    private fun handleConfirm(state: ItemState.Confirm) {
//        setResult(Activity.RESULT_OK, Intent().apply {
//            putExtra(URI_LIST_KEY, ArrayList(state.photoList.map { it.uri }))
//        })
//        finish()
    }


    //    private fun observerData() {
//        viewModel.setState().observe(viewLifecycleOwner, Observer {
//            initList()
//            for (item in it) {
//                Log.d("aaaa",item.toString())
//                when (item.categoryNumber) {
//                    0 -> topItemList.add(item)
//                    1 -> pantsItemList.add(item)
//                    2 -> accessoryItemList.add(item)
//                    3 -> shoesItemList.add(item)
//                }
//            }
//            topItemAdapter.setPhotoList(topItemList)
//            pantsItemAdapter.setPhotoList(pantsItemList)
//            accessoryItemAdapter.setPhotoList(accessoryItemList)
//            shoesItemAdapter.setPhotoList(shoesItemList)
//        })
//    }
//    override fun onResume() {
//        super.onResume()
//
//        viewModel.fetchData()
////        topItemAdapter.notifyDataSetChanged()
////        pantsItemAdapter.notifyDataSetChanged()
////        accessoryItemAdapter.notifyDataSetChanged()
////        shoesItemAdapter.notifyDataSetChanged()
//    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//
//    }
}