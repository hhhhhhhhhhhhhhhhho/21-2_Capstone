package com.nanioi.closetapplication.styling

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.closet.*
import com.nanioi.closetapplication.databinding.FragmentClosetBinding
import com.nanioi.closetapplication.databinding.FragmentStylingBinding
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.net.Socket

class StylingFragment: Fragment(R.layout.fragment_styling) {

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

    private lateinit var itemList : List<ItemModel>
    private lateinit var topItemList : List<ItemModel>
    private lateinit var pantsItemList : List<ItemModel>
    private lateinit var accessoryItemList : List<ItemModel>
    private lateinit var shoesItemList : List<ItemModel>

    private lateinit var binding: FragmentStylingBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentStylingBinding = FragmentStylingBinding.bind(view)
        binding = fragmentStylingBinding

        Log.d("aaa","onViewCreated")
        initViews()
        viewModel.fetchData()
        observeState()
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
            Log.d("aaa","pantButton")
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
        Log.d("aaa","initRecyclerView")
        when(categoryNum){
            0->{
                binding.itemRecycleView.adapter = topItemAdapter
            }
            1->{
                binding.itemRecycleView.adapter = pantsItemAdapter
            }
            2->{
                binding.itemRecycleView.adapter = accessoryItemAdapter
            }
            3->{
                binding.itemRecycleView.adapter = shoesItemAdapter
            }
        }
    }
    private fun observeState() = viewModel.itemStateLiveData.observe(viewLifecycleOwner) {
        Log.d("aaa","observestate")
        when (it) {
            is stylingState.Success -> handleSuccess(it)
            is stylingState.Confirm -> handleConfirm(it)
            else -> Unit
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccess(state: stylingState.Success) = with(binding) {
        //initList()
        Log.d("aaa","observe success")

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
        Log.d("aaa","observe confirm")
//        setResult(Activity.RESULT_OK, Intent().apply {
//            putExtra(URI_LIST_KEY, ArrayList(state.photoList.map { it.uri }))
//        })
//        finish()
        val auth = FirebaseAuth.getInstance()

        stylingObject.item =state.photo
        stylingObject.userId = auth.currentUser?.uid.toString()

        ClientThread().start()
    }

    class ClientThread : Thread() {
        override fun run() {
            super.run()

            val host = "localhost"
            val port = 5001

            //OutputStream에 전송할 데이터를 담아 보낸 뒤, InputStream을 통해 데이터를 읽
            try {
                val socket = Socket(host, port)
                val outstream = ObjectOutputStream(socket.getOutputStream())
                outstream.writeObject(stylingObject)
                outstream.flush()
                Log.d("ClientStream", "Sent to server.")

                val instream = ObjectInputStream(socket.getInputStream())
                val input: stylingObject = instream.readObject() as stylingObject
                Log.d("ClientThread", "Received data: $input")
                //todo 받은거 스타일링 탭 전송
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //handler.post(Runnable { textView.setText(input.toString()) })

        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData()
        Log.d("aaa","resume")
    }
    override fun onPause() {
        super.onPause()
        Log.d("aaa","onPause")
    }
    override fun onStop() {
        super.onStop()
        Log.d("aaa","onStop")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("aaa","destroy")
        //  viewModel.
    }
}
