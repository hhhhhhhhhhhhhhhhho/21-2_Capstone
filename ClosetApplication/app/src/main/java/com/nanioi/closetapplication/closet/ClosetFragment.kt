package com.nanioi.closetapplication.closet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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

    private lateinit var itemDB: DatabaseReference
    private lateinit var usersDB: DatabaseReference

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

    private val topItemList = mutableListOf<ItemModel>()
    private val pantsItemList = mutableListOf<ItemModel>()
    private val accessoryItemList = mutableListOf<ItemModel>()
    private val shoesItemList = mutableListOf<ItemModel>()

    private var binding: FragmentClosetBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding 변수가 nullable이기 때문 사용할때마다 null을 풀어주는 체크를 해줘야함
        //onViewCreated 안에서만 절대 null이 될 수 없는 변수로 사용하기 위해 지역변수로 선언
        val fragmentClosetBinding = FragmentClosetBinding.bind(view)
        binding = fragmentClosetBinding

        //initItemList()

        usersDB = Firebase.database.reference.child(DB_USERS)
        itemDB = Firebase.database.reference.child(DB_ITEM)

        initViews(view, fragmentClosetBinding)
        //itemDB.addChildEventListener(listener) // 리스너 등록
        viewModel.fetchData()
        observeState()
    }

//    //by.나연 아이템리스트 초기화 함수 (21.10.18)
//    private fun initItemList() {
//        topItemList.clear()
//        pantsItemList.clear()
//        accessoryItemList.clear()
//        shoesItemList.clear()
//    }

    //by.나연 뷰 초기화 함수 (21.10.18)
    private fun initViews(view: View, fragmentClosetBinding: FragmentClosetBinding) {

        fragmentClosetBinding.topItemRecyclerView.apply {
            adapter = topItemAdapter
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
        }
        fragmentClosetBinding.pantsItemRecyclerView.apply {
            adapter = pantsItemAdapter
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
        }
        fragmentClosetBinding.accessoryItemRecyclerView.apply {
            adapter = accessoryItemAdapter
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
        }
        fragmentClosetBinding.shoesItemRecyclerView.apply {
            adapter = shoesItemAdapter
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
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
            val checkList = viewModel.confirmCheckedPhotos()
            //viewModel.deleteItem(checkList)
        }
        //todo 선택이미지 가상스타일링 스타일링 탭으로 정보 전송
        fragmentClosetBinding.goDressUpButton.setOnClickListener {
            val checkList = viewModel.confirmCheckedPhotos()
            // todo 스타일링 탭으로 정보전송
        }
    }

    //관찰, UI 업데이트
    private fun observeState() = viewModel.itemStateLiveData.observe(viewLifecycleOwner) {
        for (item in it) {
            when (item.categoryNumber) {
                0-> topItemList.add(item)
                1-> pantsItemList.add(item)
                2-> accessoryItemList.add(item)
                3-> shoesItemList.add(item)
            }
        }
        topItemAdapter.setPhotoList(topItemList)
        pantsItemAdapter.setPhotoList(pantsItemList)
        accessoryItemAdapter.setPhotoList(accessoryItemList)
        shoesItemAdapter.setPhotoList(shoesItemList)
    }
}