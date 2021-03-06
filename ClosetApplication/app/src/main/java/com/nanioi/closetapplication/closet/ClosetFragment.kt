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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.DBkey.Companion.DB_ITEM
import com.nanioi.closetapplication.DBkey.Companion.DB_USERS
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.LoginUserData
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
    val db = FirebaseFirestore.getInstance()
    private val userDB: FirebaseDatabase by lazy { Firebase.database }
    lateinit var itemList : List<ItemModel>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding ????????? nullable?????? ?????? ?????????????????? null??? ???????????? ????????? ????????????
        //onViewCreated ???????????? ?????? null??? ??? ??? ?????? ????????? ???????????? ?????? ??????????????? ??????
        val fragmentClosetBinding = FragmentClosetBinding.bind(view)
        binding = fragmentClosetBinding

        Log.d("bb", "onViewCreated")
        initViews(view, fragmentClosetBinding)
        viewModel.fetchData()
        observeState()
    }

    //by.?????? ??? ????????? ?????? (21.10.18)
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
                    Snackbar.make(view, "????????? ??? ??????????????????", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        //todo ??????????????? ????????????
        fragmentClosetBinding.deleteItemButton.setOnClickListener {
            viewModel.deletePhoto()
        }
        //todo ??????????????? ?????????????????? ???????????? ????????? ?????? ??????
        fragmentClosetBinding.goDressUpButton.setOnClickListener {
            viewModel.confirmCheckedPhotos()
            // todo ???????????? ????????? ????????????
        }
    }

    //by??????. ????????? ?????? ?????? ?????? ????????? ????????? ?????????
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

    //by ??????. ????????? ?????? ??????, UI ???????????? ?????? (21.10.25)
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
                    "select item ????????? "
                )
                (activity as MainActivity).replaceFragment(StylingFragment())
            }.addOnFailureListener {
                Log.w(
                    "ClosetFragment",
                    "select item ????????? ?????? : " + it.toString()
                )
            }

        //todo ???????????? ????????? ??????
        (activity as MainActivity).replaceFragment(StylingFragment())
    }

    fun deleteAllItems(userId : String){
        for( item in itemList ){
            Log.d("bbbbb","item : "+ item.itemId)
            db.collection(DB_USERS).document(userId).collection(DB_ITEM).document(item.itemId.toString()).delete()
        }
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