package com.nanioi.closetapplication.styling

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.R.layout
import com.nanioi.closetapplication.User.LoginUserData
import com.nanioi.closetapplication.closet.*
import com.nanioi.closetapplication.databinding.FragmentStylingBinding
import com.nanioi.closetapplication.styling.recommend.RecommendItemListAdapter
import com.nanioi.closetapplication.styling.recommend.RecommendItemModel
import com.nanioi.closetapplication.styling.recommend.parsingData
import com.nanioi.closetapplication.styling.recommend.readFeed

class StylingFragment : Fragment(layout.fragment_styling) {

    private val topItemAdapter = stylingItemAdapter {
        viewModel.selectPhoto(it)
    }
    private val pantsItemAdapter = stylingItemAdapter {
        viewModel.selectPhoto(it)
    }
    private val accessoryItemAdapter = stylingItemAdapter {
        viewModel.selectPhoto(it)
    }
    private val shoesItemAdapter = stylingItemAdapter {
        viewModel.selectPhoto(it)
    }
    private val viewModel by viewModels<stylingItemViewModel>()

    private lateinit var itemList: List<ItemModel>
    private lateinit var topItemList: List<ItemModel>
    private lateinit var pantsItemList: List<ItemModel>
    private lateinit var accessoryItemList: List<ItemModel>
    private lateinit var shoesItemList: List<ItemModel>

    private lateinit var binding: FragmentStylingBinding
    var keyword: String = "의류"
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    val db = FirebaseFirestore.getInstance()
    // bottomSheet 추천리스트
    private val recyclerAdapter = RecommendItemListAdapter(itemClicked = { item ->
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.DetailPageUrl))
        startActivity(intent)
    })

    lateinit var selectedItem: ItemModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentStylingBinding = FragmentStylingBinding.bind(view)
        binding = fragmentStylingBinding

        Log.d("aaa", "onViewCreated")
        initViews()
        viewModel.fetchData()
        observeState()

        val recommendItemRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recommendItemRecyclerView?.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        when (LoginUserData.gender) {
            "남자" -> keyword = "20대 남성의류"
            "여자" -> keyword = "20대 여성의류"
        }
        XmlParsingTask().execute()
    }

    inner class XmlParsingTask() : AsyncTask<Any?, Any?, List<RecommendItemModel>>() {
        override fun onPostExecute(result: List<RecommendItemModel>) {
            super.onPostExecute(result)
            recyclerAdapter.submitList(result)
        }

        override fun doInBackground(vararg params: Any?): List<RecommendItemModel> { // xml 파싱할때 여기서 데이터 받아와 reedFeed 부분은 저 XmlParsingTask 파일보면 있으
            return readFeed(parsingData(keyword))
        }
    }

    private fun initViews() {
        Glide.with(this)
            .load(LoginUserData.avatar_front_ImageUrl)
            .into(binding.personImage)

        binding.selectItemTap.visibility = View.INVISIBLE

        //by 나연. 각 카테고리 버튼 클릭 시 item들 보여주는 RecyclerView 창 보여주기 (21.10.12)
        binding.topButton.setOnClickListener {
            activity?.let {
                binding.selectItemTap.visibility = View.VISIBLE
                initRecyclerView(0)
            }
            when (LoginUserData.gender) {
                "남자" -> keyword = "20대 남성 상의"
                "여자" -> keyword = "20대 여성 상의"
            }
            XmlParsingTask().execute()
        }
        binding.pantsButton.setOnClickListener {
            Log.d("aaa", "pantButton")
            activity?.let {
                initRecyclerView(1)
                binding.selectItemTap.visibility = View.VISIBLE
            }
            when (LoginUserData.gender) {
                "남자" -> keyword = "20대 남성 하의"
                "여자" -> keyword = "20대 여성 하의"
            }
            XmlParsingTask().execute()
        }
        binding.accessoryButton.setOnClickListener {
            activity?.let {
                initRecyclerView(2)
                binding.selectItemTap.visibility = View.VISIBLE
            }
            when (LoginUserData.gender) {
                "남자" -> keyword = "20대 남성 액세서리"
                "여자" -> keyword = "20대 여성 액세서리"
            }
            XmlParsingTask().execute()
        }
        binding.shoesButton.setOnClickListener {
            activity?.let {
                binding.selectItemTap.visibility = View.VISIBLE
                initRecyclerView(3)
            }
            when (LoginUserData.gender) {
                "남자" -> keyword = "20대 남성 신발"
                "여자" -> keyword = "20대 여성 신발"
            }
            XmlParsingTask().execute()
        }
        binding.backButton.setOnClickListener {
            activity?.let {
                binding.selectItemTap.visibility = View.INVISIBLE
            }
        }
        binding.selectItemButton.setOnClickListener {
            activity?.let {
                viewModel.confirmCheckedPhotos()
                binding.selectItemTap.visibility = View.INVISIBLE
                // 아바타에 옷입히기 코드 구현
            }
        }
    }

    private fun initRecyclerView(categoryNum: Int) {
        viewModel.fetchData()
        binding.itemRecycleView.layoutManager = LinearLayoutManager(context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
        Log.d("aaa", "initRecyclerView")
        when (categoryNum) {
            0 -> {
                binding.itemRecycleView.adapter = topItemAdapter
            }
            1 -> {
                binding.itemRecycleView.adapter = pantsItemAdapter
            }
            2 -> {
                binding.itemRecycleView.adapter = accessoryItemAdapter
            }
            3 -> {
                binding.itemRecycleView.adapter = shoesItemAdapter
            }
        }
    }

    private fun observeState() = viewModel.itemStateLiveData.observe(viewLifecycleOwner) {
        Log.d("aaa", "observestate")
        when (it) {
            is stylingState.Success -> handleSuccess(it)
            is stylingState.Confirm -> handleConfirm(it)
            else -> Unit
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccess(state: stylingState.Success) = with(binding) {
        //initList()
        Log.d("aaa", "observe success")

        itemList = state.photoList
        topItemList = itemList.filter { it.categoryNumber == 0 }
        pantsItemList = itemList.filter { it.categoryNumber == 1 }
        accessoryItemList = itemList.filter { it.categoryNumber == 2 }
        shoesItemList = itemList.filter { it.categoryNumber == 3 }

        topItemAdapter.setPhotoList(topItemList)
        pantsItemAdapter.setPhotoList(pantsItemList)
        accessoryItemAdapter.setPhotoList(accessoryItemList)
        shoesItemAdapter.setPhotoList(shoesItemList)
    }

    private fun handleConfirm(state: stylingState.Confirm) {
        Log.d("aaa", "observe confirm")

        selectedItem = state.photo
        val userId = auth.currentUser!!.uid

        db.collection(DBkey.DB_SELECTED_ITEM).document(selectedItem.itemId.toString()).set(selectedItem)
            .addOnSuccessListener { Log.d("aaaa", "모델 업로드 성공")
                showProgress()}
            .addOnFailureListener { e ->
                Log.d("aaaa", "Error writing document", e)
            }

        db.collection("Styling").addSnapshotListener { snapshots, e ->

            // 오류 발생 시
            if (e != null) {
                Log.w("StylingFragment", "Listen failed: $e")
                return@addSnapshotListener
            }
            snapshots?.documentChanges?.forEach { doc ->
                //LoginUserData.avatarImageUri = doc.~~~
                hideProgress()
            }

        }
    }
    private fun showProgress() {
        binding.progressBar.isVisible = true
    }
    private fun hideProgress() {
        binding.progressBar.isVisible = false
    }
    override fun onResume() {
        super.onResume()
        viewModel.fetchData()
        Log.d("aaa", "resume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("aaa", "destroy")
        //  viewModel.
    }
}
