package com.nanioi.closetapplication.styling

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
import com.nanioi.closetapplication.DBkey.Companion.DB_USERS
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.R.layout
import com.nanioi.closetapplication.User.LoginUserData
import com.nanioi.closetapplication.User.userDBkey
import com.nanioi.closetapplication.closet.*
import com.nanioi.closetapplication.databinding.FragmentStylingBinding
import com.nanioi.closetapplication.styling.recommend.RecommendItemListAdapter
import com.nanioi.closetapplication.styling.recommend.RecommendItemModel
import com.nanioi.closetapplication.styling.recommend.parsingData
import com.nanioi.closetapplication.styling.recommend.readFeed
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

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
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    val db = FirebaseFirestore.getInstance()
    private val userDB: FirebaseDatabase by lazy { Firebase.database }

    // bottomSheet 추천리스트
    private val recyclerAdapter = RecommendItemListAdapter(itemClicked = { item ->
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.DetailPageUrl))
        startActivity(intent)
    })
    val TAG = "StylingFragment"
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
        //XmlParsingTask().execute()
        WebParsingTask().execute()
    }

    inner class WebParsingTask() :
        AsyncTask<Any?, Any?, List<RecommendItemModel>>() { //input, progress update type, result type
        var itemList = mutableListOf<RecommendItemModel>()
        val weburl = "https://search.musinsa.com/ranking/best"
        override fun doInBackground(vararg params: Any?): List<RecommendItemModel> {
            val doc: Document = Jsoup.connect("$weburl").get()
            val elts: Elements = doc.select("ul li div.li_inner")


            elts.forEachIndexed { index, elem ->
                val pageUrl = elem.select("div.list_img a").attr("href")
                val itemImage = elem.select("div.list_img img").attr("data-original")
                val seller = elem.select("div.article_info p.item_title a").text()
                val itemTitle = elem.select("div.article_info p.list_info a").attr("title")
                var salePrice = elem.select("div.article_info p.price").text().replace(" ","*")
                var price = ""
                if("*" in salePrice) {
                    val arr = salePrice.split("*")
                    price = arr.get(0)
                    salePrice = arr.get(1)
                }

                var item = RecommendItemModel(itemTitle,price,itemImage,seller,pageUrl,salePrice)
                itemList.add(item)
            }
            return itemList
        }

        override fun onPostExecute(result: List<RecommendItemModel>) {
            super.onPostExecute(result)
            recyclerAdapter.submitList(result)
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
            //  XmlParsingTask().execute()
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
            //  XmlParsingTask().execute()
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
            //  XmlParsingTask().execute()
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
            //   XmlParsingTask().execute()
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

        showProgress()

        val selectItem = ItemFromServer()
        selectItem.userId = auth.currentUser!!.uid
        selectItem.userBodyImage = LoginUserData.body_front_ImageUrl!!

        when (state.photo.categoryNumber) {
            0 -> selectItem.topImageUrl = state.photo.imageUrl
            1 -> selectItem.bottomImageUrl = state.photo.imageUrl
            2 -> selectItem.accessoryImageUrl = state.photo.imageUrl
            3 -> selectItem.shoesImageUrl = state.photo.imageUrl
        }
        userDB.reference.child(DBkey.DB_SELECTED_ITEM)
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
                LoginUserData.body_front_ImageUrl =
                    dataSnapshot.child(userDBkey.DB_BODY_FRONT).value.toString()
                LoginUserData.avatar_front_ImageUrl =
                    dataSnapshot.child(userDBkey.DB_AVATAR_FRONT).value.toString()

                hideProgress()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "StylingFragment",
                    error.toException().toString()
                )
            }
        })
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

    //11번가 openApi xml Parsing
    //    inner class XmlParsingTask() : AsyncTask<Any?, Any?, List<RecommendItemModel>>() {
//        override fun onPostExecute(result: List<RecommendItemModel>) {
//            super.onPostExecute(result)
//            recyclerAdapter.submitList(result)
//        }
//
//        override fun doInBackground(vararg params: Any?): List<RecommendItemModel> { // xml 파싱할때 여기서 데이터 받아와 reedFeed 부분은 저 XmlParsingTask 파일보면 있으
//            return readFeed(parsingData(keyword))
//        }
//    }
}
