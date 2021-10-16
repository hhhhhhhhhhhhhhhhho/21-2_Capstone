package com.nanioi.capstoneproject.closet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.nanioi.capstoneproject.databinding.FragmentMypageBinding
import com.nanioi.capstoneproject.mypage.EditProfileActivity

class ClosetFragment: Fragment(R.layout.fragment_closet) {

    private val ItemListAdapter = ItemAdapter{
        viewModel.selectPhoto(it)
    }
    private val viewModel by viewModels<ItemViewModel>()
    private val itemList = mutableListOf<ItemModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentClosetBinding.inflate(inflater,container,false)

       initViews(binding)

        return binding.root
    }


    private fun initViews(binding: FragmentClosetBinding) {
        binding.topItemRecyclerView.adapter = ItemListAdapter

        binding.addItemButton.setOnClickListener {
            activity?.let{
                startActivity(Intent(context,AddImageActivity::class.java))
            }
        }
        binding.deleteItemButton.setOnClickListener {
            val list = itemList.filter { it.isSelected }
            removePhoto(list)
        }
        binding.goDressUpButton.setOnClickListener {
            val list = itemList.filter { it.isSelected }
            //todo 스타일링 탭으로 정보 전송
        }
    }

    private fun removePhoto(removeItems: List<ItemModel>) {
        itemList.remove(removeItems)
        ItemListAdapter.setPhotoList(itemList)
    }
    override fun onResume() {
        super.onResume()

        ItemListAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}