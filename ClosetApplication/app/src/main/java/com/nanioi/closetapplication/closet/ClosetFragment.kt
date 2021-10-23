package com.nanioi.closetapplication.closet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

    private lateinit var topItemAdapter: itemAdapter
    private lateinit var pantsItemAdapter: itemAdapter
    private lateinit var accessoryItemAdapter: itemAdapter
    private lateinit var shoesItemAdapter: itemAdapter

    private val viewModel by viewModels<itemGalleryViewModel>()

    private val topItemList = mutableListOf<ItemModel>()
    private val pantsItemList = mutableListOf<ItemModel>()
    private val accessoryItemList = mutableListOf<ItemModel>()
    private val shoesItemList = mutableListOf<ItemModel>()

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val itemModel = snapshot.getValue(ItemModel::class.java) // ItemModel 클래스로 데이터를 받아옴
            itemModel ?: return
            when (itemModel.categoryNumber) {
                0 -> {
                    topItemList.add(itemModel)
                    topItemAdapter.submitList(topItemList)
                }
                1 -> {
                    pantsItemList.add(itemModel)
                    pantsItemAdapter.submitList(pantsItemList)
                }
                2 -> {
                    accessoryItemList.add(itemModel)
                    accessoryItemAdapter.submitList(accessoryItemList)
                }
                3 -> {
                    shoesItemList.add(itemModel)
                    shoesItemAdapter.submitList(shoesItemList)
                }
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }
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

        initItemList()

        usersDB = Firebase.database.reference.child(DB_USERS)
        itemDB = Firebase.database.reference.child(DB_ITEM)

        initViews(view, fragmentClosetBinding)

        itemDB.addChildEventListener(listener) // 리스너 등록
    }

    //by.나연 아이템리스트 초기화 함수 (21.10.18)
    private fun initItemList() {
        topItemList.clear()
        pantsItemList.clear()
        accessoryItemList.clear()
        shoesItemList.clear()
    }

    //by.나연 뷰 초기화 함수 (21.10.18)
    private fun initViews(view: View, fragmentClosetBinding: FragmentClosetBinding) {
        topItemAdapter = itemAdapter(onItemClicked = { ItemModel ->
            ItemModel.isSelected = true
            Snackbar.make(view, "해당 아이템을 선택하였습니다.", Snackbar.LENGTH_LONG).show()

        })
        pantsItemAdapter = itemAdapter(onItemClicked = { ItemModel ->
            ItemModel.isSelected = true
            Snackbar.make(view, "해당 아이템을 선택하였습니다.", Snackbar.LENGTH_LONG).show()
        })
        accessoryItemAdapter = itemAdapter(onItemClicked = { ItemModel ->
            ItemModel.isSelected = true
            Snackbar.make(view, "해당 아이템을 선택하였습니다.", Snackbar.LENGTH_LONG).show()
        })
        shoesItemAdapter = itemAdapter(onItemClicked = { ItemModel ->
            ItemModel.isSelected = true
            Snackbar.make(view, "해당 아이템을 선택하였습니다.", Snackbar.LENGTH_LONG).show()
        })

        fragmentClosetBinding.topItemRecyclerView.layoutManager = LinearLayoutManager(context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
        fragmentClosetBinding.topItemRecyclerView.adapter = topItemAdapter
        fragmentClosetBinding.pantsItemRecyclerView.layoutManager = LinearLayoutManager(context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
        fragmentClosetBinding.pantsItemRecyclerView.adapter = pantsItemAdapter
        fragmentClosetBinding.accessoryItemRecyclerView.layoutManager = LinearLayoutManager(context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
        fragmentClosetBinding.accessoryItemRecyclerView.adapter = accessoryItemAdapter
        fragmentClosetBinding.shoesItemRecyclerView.layoutManager = LinearLayoutManager(context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
        fragmentClosetBinding.shoesItemRecyclerView.adapter = shoesItemAdapter

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
            viewModel.confirmCheckedPhotos()
//            val list = itemList.filter { it.isSelected }
//            removePhoto(list)
        }
        //todo 선택이미지 가상스타일링 스타일링 탭으로 정보 전송
        fragmentClosetBinding.goDressUpButton.setOnClickListener {
            viewModel.confirmCheckedPhotos()
        }
    }

    //by.나연 뷰가 다시 보일때 데이터 다시 불러와 뷰 다시 그리기 (21.10.18)
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        topItemAdapter.notifyDataSetChanged()
        pantsItemAdapter.notifyDataSetChanged()
        accessoryItemAdapter.notifyDataSetChanged()
        shoesItemAdapter.notifyDataSetChanged()
    }

    //by.나연 뷰에서 나갈때 리스너 제거 (21.10.18)
    override fun onDestroyView() {
        super.onDestroyView()
        itemDB.removeEventListener(listener)
    }

    //todo item 삭제함수 구현
    private fun removePhoto(removeItems: List<ItemModel>) {
//        itemList.remove(removeItems)
//        ItemListAdapter.setPhotoList(itemList)
    }
}