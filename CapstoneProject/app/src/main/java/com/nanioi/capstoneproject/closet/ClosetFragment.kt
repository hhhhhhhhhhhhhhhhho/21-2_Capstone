package com.nanioi.capstoneproject.closet

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.nanioi.capstoneproject.DBkey.Companion.DB_ITEM
import com.nanioi.capstoneproject.DBkey.Companion.DB_USERS
import com.nanioi.capstoneproject.R
import com.nanioi.capstoneproject.databinding.FragmentClosetBinding

class ClosetFragment : Fragment(R.layout.fragment_closet) {

    private lateinit var itemDB: DatabaseReference
    private lateinit var usersDB: DatabaseReference
    private lateinit var topItemAdapter: iiiAdapter
    private lateinit var pantsItemAdapter: iiiAdapter
    private lateinit var accessoryItemAdapter: iiiAdapter
    private lateinit var shoesItemAdapter: iiiAdapter

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


        initViews(view,fragmentClosetBinding)

        itemDB.addChildEventListener(listener) // 리스너 등록
    }

    //by.나연 뷰 초기화 함수 (21.10.18)
    private fun initViews(view: View, fragmentClosetBinding: FragmentClosetBinding) {
        topItemAdapter = iiiAdapter()
        pantsItemAdapter = iiiAdapter()
        accessoryItemAdapter = iiiAdapter()
        shoesItemAdapter = iiiAdapter()

        fragmentClosetBinding.topItemRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentClosetBinding.topItemRecyclerView.adapter = topItemAdapter
        fragmentClosetBinding.pantsItemRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentClosetBinding.pantsItemRecyclerView.adapter = pantsItemAdapter
        fragmentClosetBinding.accessoryItemRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentClosetBinding.accessoryItemRecyclerView.adapter = accessoryItemAdapter
        fragmentClosetBinding.shoesItemRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentClosetBinding.shoesItemRecyclerView.adapter = shoesItemAdapter

        fragmentClosetBinding.addItemButton.setOnClickListener {
            val intent = Intent(requireContext(), AddImageActivity::class.java) //context가 널일 수 있어 requireContext 사용
            startActivity(intent) // 아이템 등록 창으로 이동
//            if (auth.currentUser != null) {
//                val intent = Intent(requireContext(), AddImageActivity::class.java) //context가 널일 수 있어 requireContext 사용
//                startActivity(intent) // 아이템 등록 창으로 이동
//            } else {
//                Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
//            }
        }

        fragmentClosetBinding.deleteItemButton.setOnClickListener {
//            val list = itemList.filter { it.isSelected }
//            removePhoto(list)
        }
        fragmentClosetBinding.goDressUpButton.setOnClickListener {
//            val list = itemList.filter { it.isSelected }
//            //todo 스타일링 탭으로 정보 전송
        }
    }


    //by.나연 아이템리스트 초기화 함수 (21.10.18)
    private fun initItemList() {
        topItemList.clear()
        pantsItemList.clear()
        accessoryItemList.clear()
        shoesItemList.clear()
    }

//    private fun removePhoto(removeItems: List<ItemModel>) {
//        itemList.remove(removeItems)
//        ItemListAdapter.setPhotoList(itemList)
//    }
    //by.나연 뷰가 다시 보일때 데이터 다시 불러와 뷰 다시 그리기 (21.10.18)
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
}