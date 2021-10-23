package com.nanioi.closetapplication.styling

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.closet.ItemModel
import com.nanioi.closetapplication.closet.itemAdapter
import com.nanioi.closetapplication.databinding.FragmentStylingBinding

class StylingFragment: Fragment(R.layout.fragment_styling) {

    private lateinit var ItemAdapter: itemAdapter
    private val topItemList = mutableListOf<ItemModel>()
    private val pantsItemList = mutableListOf<ItemModel>()
    private val accessoryItemList = mutableListOf<ItemModel>()
    private val shoesItemList = mutableListOf<ItemModel>()

    private var binding: FragmentStylingBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private lateinit var itemDB: DatabaseReference
    private lateinit var usersDB: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentStylingBinding = FragmentStylingBinding.bind(view)
        binding = fragmentStylingBinding

        initViews()
    }

    private fun initViews() {
        binding!!.selectItemTap.visibility = View.INVISIBLE

        //by 나연. 각 카테고리 버튼 클릭 시 item들 보여주는 RecyclerView 창 보여주기 (21.10.12)
        binding!!.topButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.VISIBLE
              //  initRecyclerView(0)
            }
        }
        binding!!.accessoryButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.VISIBLE
                //initRecyclerView(2)
            }
        }
        binding!!.pantsButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.VISIBLE
                //initRecyclerView(1)
            }
        }
        binding!!.shoesButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.VISIBLE
                //initRecyclerView(3)
            }
        }
        binding!!.backButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.INVISIBLE
            }
        }
        binding!!.selectItemButton.setOnClickListener {
            activity?.let {
                binding!!.selectItemTap.visibility = View.INVISIBLE
                // 아바타에 옷입히기 코드 구현
            }
        }
    }

    private fun initRecyclerView(categoryNum: Int) {
        when(categoryNum){
            0->{

            }
            1->{

            }
            2->{

            }
            3->{

            }
        }
    }
}
