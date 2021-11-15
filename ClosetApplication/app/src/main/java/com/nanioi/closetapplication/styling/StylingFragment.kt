package com.nanioi.closetapplication.styling

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.R.layout
import com.nanioi.closetapplication.User.userObject
import com.nanioi.closetapplication.User.utils.LoginUserData
import com.nanioi.closetapplication.closet.*
import com.nanioi.closetapplication.databinding.BottomSheetBinding
import com.nanioi.closetapplication.databinding.FragmentStylingBinding
import com.nanioi.closetapplication.styling.recommend.RecommendItemListAdapter
import com.nanioi.closetapplication.styling.recommend.RecommendItemModel
import com.nanioi.closetapplication.styling.recommend.parsingData
import com.nanioi.closetapplication.styling.recommend.readFeed
import java.io.DataOutputStream
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.net.Socket

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

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

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
        XmlParsingTask().execute()
    }

    inner class XmlParsingTask() : AsyncTask<Any?, Any?, List<RecommendItemModel>>() {
        override fun onPostExecute(result: List<RecommendItemModel>) {
            super.onPostExecute(result)
            recyclerAdapter.submitList(result)
        }

        override fun doInBackground(vararg params: Any?): List<RecommendItemModel> { // xml 파싱할때 여기서 데이터 받아와 reedFeed 부분은 저 XmlParsingTask 파일보면 있으
            var keyword: String = "의류"
            when (LoginUserData.gender) {
                "남자" -> keyword = "20대 남성의류"
                "여자" -> keyword = "20대 여성의류"
            }

            return readFeed(parsingData(keyword))
        }
    }

    private fun initViews() {
        binding.selectItemTap.visibility = View.INVISIBLE

        //by 나연. 각 카테고리 버튼 클릭 시 item들 보여주는 RecyclerView 창 보여주기 (21.10.12)
        binding.topButton.setOnClickListener {
            activity?.let {
                binding.selectItemTap.visibility = View.VISIBLE
                initRecyclerView(0)
            }
        }
        binding.pantsButton.setOnClickListener {
            Log.d("aaa", "pantButton")
            activity?.let {
                initRecyclerView(1)
                binding.selectItemTap.visibility = View.VISIBLE
            }
        }
        binding.accessoryButton.setOnClickListener {
            activity?.let {
                initRecyclerView(2)
                binding.selectItemTap.visibility = View.VISIBLE
            }
        }
        binding.shoesButton.setOnClickListener {
            activity?.let {
                binding.selectItemTap.visibility = View.VISIBLE
                initRecyclerView(3)
            }
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
//        setResult(Activity.RESULT_OK, Intent().apply {
//            putExtra(URI_LIST_KEY, ArrayList(state.photoList.map { it.uri }))
//        })
//        finish()
        selectedItem = state.photo
        ClientThread().start()
    }

    //소켓통신
    inner class ClientThread() : Thread() {
        override fun run() {
            super.run()
            Log.w("aaaaaaaaa", "clientThread")

            val host = "172.30.1.55"
            val port = 9999

            try {
                val socket = Socket(host, port)
                val outstream = DataOutputStream(socket.getOutputStream())
                val file: File = File(getImageFilePath(Uri.parse(selectedItem.imageUrl)))
                outstream.writeUTF(file.toString())

                outstream.flush()
                Log.w("aaaaaaaaa", "Sent to server.")

                val instream = ObjectInputStream(socket.getInputStream())
                val input: userObject = instream.readObject() as userObject
                Log.w("aaaaaaaaa", "Received data: $input")
                //todo 받은거 스타일링 탭 전송
            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("aaaaaaaaa", "error" + e.toString())
            }
        }
    }

    //by나연. 이미지 파일 절대경로 알아내기 (21.11.14)
    fun getImageFilePath(contentUri: Uri): String {
        var columnIndex = 0
        val projection = arrayOf(MediaStore.Images.Media.DATA) // 걸러내기
        val cursor = context?.contentResolver?.query(contentUri, projection, null, null, null)
        // list index 가르키기 , content 관리하는 resolver에 검색(query) 부탁
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        return cursor.getString(columnIndex)
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
