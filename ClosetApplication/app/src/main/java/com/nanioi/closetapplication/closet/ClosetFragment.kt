package com.nanioi.closetapplication.closet

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
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
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.databinding.FragmentClosetBinding
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.Socket
import java.net.UnknownHostException


class ClosetFragment : Fragment(R.layout.fragment_closet) {

    // 소켓통신에 필요한것
    private val html = ""
    private var mHandler: Handler? = null
    private var socket: Socket? = null
    private var dos: DataOutputStream? = null
    private var dis: DataInputStream? = null
    private val ip = "127.0.0.1" // IP 번호
    private val port = 12345 // port 번호

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
    //private val viewModel by lazy { ViewModelProvider(this).get(itemViewModel::class.java) }

    private val topItemList = mutableListOf<ItemModel>()
    private val pantsItemList = mutableListOf<ItemModel>()
    private val accessoryItemList = mutableListOf<ItemModel>()
    private val shoesItemList = mutableListOf<ItemModel>()

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding 변수가 nullable이기 때문 사용할때마다 null을 풀어주는 체크를 해줘야함
        //onViewCreated 안에서만 절대 null이 될 수 없는 변수로 사용하기 위해 지역변수로 선언
        val fragmentClosetBinding = FragmentClosetBinding.bind(view)
        binding = fragmentClosetBinding

        Log.d("bb", "onViewCreated")
        initViews(view, fragmentClosetBinding)
//        viewModel.fetchData()
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
                    Log.d("bb", "add ")
                    startActivity(intent)
                } else {
                    Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
                }
            }
            Log.d("bb", "add 후")
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
        Log.d("bb", "observe success")
        for (item in state.photoList) {
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

        connect()
    }

    // 로그인 정보 db에 넣어주고 연결시켜야 함.
    fun connect() {
        mHandler = Handler()
        Log.w("connect", "연결 하는중")
        // 받아오는거
        val checkUpdate: Thread = object : Thread() {
            override fun run() {

                try {
                    socket = Socket(ip, port)
                    Log.w("서버 접속됨", "서버 접속됨")
                } catch (e1: IOException) {
                    Log.w("서버접속못함", "서버접속못함")
                    e1.printStackTrace()
                }
                Log.w("edit 넘어가야 할 값 : ", "안드로이드에서 서버로 연결요청")
                try {
                    //            //OutputStream에 전송할 데이터를 담아 보낸 뒤, InputStream을 통해 데이터를 읽
//            try {
//                val socket = Socket(host, port)
//                val outstream = ObjectOutputStream(socket.getOutputStream())
//                outstream.writeObject("hello")
//                outstream.flush()
//                Log.d("ClientStream", "Sent to server.")
//
//                val instream = ObjectInputStream(socket.getInputStream())
//                val input: closetObject = instream.readObject() as closetObject
//                Log.d("ClientThread", "Received data: $input")
//                //todo 받은거 스타일링 탭 전송
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
                    dos = DataOutputStream(socket!!.getOutputStream()) // output에 보낼꺼 넣음
                    dis = DataInputStream(socket!!.getInputStream()) // input에 받을꺼 넣어짐
                    dos!!.writeUTF("안드로이드에서 서버로 연결요청")
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.w("버퍼", "버퍼생성 잘못됨")
                }
                Log.w("버퍼", "버퍼생성 잘됨")

// 서버에서 계속 받아옴 - 한번은 문자, 한번은 숫자를 읽음. 순서 맞춰줘야 함.
                try {
                    var line = ""
                    var line2: Int
                    while (true) {
                        line = dis!!.readUTF() as String
                        line2 = dis!!.read()
                        Log.w("서버에서 받아온 값 ", "" + line)
                        Log.w("서버에서 받아온 값 ", "" + line2)
                    }
                } catch (e: Exception) {
                }
            }
        }
        // 소켓 접속 시도, 버퍼생성
        checkUpdate.start()
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