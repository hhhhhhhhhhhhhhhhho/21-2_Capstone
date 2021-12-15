package com.nanioi.closetapplication.closet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.DBkey.Companion.DB_ITEM
import com.nanioi.closetapplication.DBkey.Companion.DB_USERS
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.LoginUserData
import com.nanioi.closetapplication.User.userDBkey
import com.nanioi.closetapplication.databinding.FragmentClosetBinding
import com.nanioi.closetapplication.styling.StylingFragment
import java.io.*


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

    private val topItemList = mutableListOf<ItemModel>()
    private val pantsItemList = mutableListOf<ItemModel>()
    private val accessoryItemList = mutableListOf<ItemModel>()
    private val shoesItemList = mutableListOf<ItemModel>()

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val userDB: FirebaseDatabase by lazy { Firebase.database }
    lateinit var itemList : List<ItemModel>
    val TAG = "ClosetFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding 변수가 nullable이기 때문 사용할때마다 null을 풀어주는 체크를 해줘야함
        //onViewCreated 안에서만 절대 null이 될 수 없는 변수로 사용하기 위해 지역변수로 선언
        val fragmentClosetBinding = FragmentClosetBinding.bind(view)
        binding = fragmentClosetBinding

        Log.d("bb", "onViewCreated")
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
        Log.d("bb", "init :" + topItemList.toString())
        topItemList.clear()
        pantsItemList.clear()
        accessoryItemList.clear()
        shoesItemList.clear()
    }

    private fun observeState() = viewModel.itemStateLiveData.observe(viewLifecycleOwner) {
        when (it) {
            is ItemState.Loading -> handleLoading()
            is ItemState.Success -> handleSuccess(it)
            is ItemState.Confirm -> handleConfirm(it)
            else -> Unit
        }
    }

    //by 나연. 데이터 변경 관찰, UI 업데이트 함수 (21.10.25)
    private fun handleLoading() = with(binding) {
        Log.d("bb", "observe loading")
        this!!.progressBar.isVisible = true
    }

    private fun handleSuccess(state: ItemState.Success) = with(binding) {
        this!!.progressBar.isGone = true
        initList()
        itemList = state.photoList
        Log.d("bb", "observe success")
        for (item in itemList) {
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
        Log.d("bb", "observe confirm")

        val selectItem = ItemFromServer()
        selectItem.userId = auth.currentUser!!.uid
        selectItem.userBodyImage = LoginUserData.body_front_ImageUrl!!

        for ( item in state.photoList ) {
            when(item.categoryNumber){
                0-> selectItem.topImageUrl = item.imageUrl
                1-> selectItem.bottomImageUrl = item.imageUrl
                2-> selectItem.accessoryImageUrl = item.imageUrl
                3-> selectItem.shoesImageUrl = item.imageUrl
            }
        }
        userDB.reference.child(DBkey.DB_SELECTED_ITEM).child(DB_USERS)
            .setValue(selectItem)
            .addOnCompleteListener {
                Log.w(
                    "ClosetFragment",
                    "select item 업로드 "
                )
            }.addOnFailureListener {
                Log.w(
                    "ClosetFragment",
                    "select item 업로드 실패 : " + it.toString()
                )
            }

        userDB.reference.child(DB_USERS).child(selectItem.userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                LoginUserData.avatar_front_ImageUrl =
                    dataSnapshot.child(userDBkey.DB_AVATAR_FRONT).value.toString()
                LoginUserData.avatar_back_ImageUrl =
                    dataSnapshot.child(userDBkey.DB_AVATAR_BACK).value.toString()

                (activity as MainActivity).replaceFragment(StylingFragment())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toException().toString())
            }
        })
    }

    override fun onResume() {
        super.onResume()

        Log.d("bb", "resume")
        viewModel.fetchData()
        observeState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("bb", "destroy")
    }
}