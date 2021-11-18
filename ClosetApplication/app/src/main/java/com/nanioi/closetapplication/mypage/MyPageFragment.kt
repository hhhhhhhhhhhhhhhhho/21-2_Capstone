package com.nanioi.closetapplication.mypage

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.utils.LoginUserData
import com.nanioi.closetapplication.databinding.FragmentClosetBinding
import com.nanioi.closetapplication.databinding.FragmentMypageBinding
import com.nanioi.closetapplication.databinding.FragmentStylingBinding

class MyPageFragment : Fragment(R.layout.fragment_mypage){
    private lateinit var binding: FragmentMypageBinding

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentMypageBinding = FragmentMypageBinding.bind(view)
        binding = fragmentMypageBinding

        binding.nameTextView.text = "이름 : ${LoginUserData.name}"
        binding.emailTextView.text = LoginUserData.email
        binding.gender.text = "성별 : ${LoginUserData.gender}"
        binding.height.text = "키 : ${LoginUserData.cm} cm"
        binding.weight.text = "몸무게 : ${LoginUserData.kg} kg"

        //아바타 사진 넣기
        Glide.with(view)
            .load(LoginUserData.avatarImageUri)
            .into(binding.mypagaAvatarImageView)


        //by 나연. 프로필 수정 버튼 클릭 시 activity이동 (21.09.27)
        binding.editProfileButton.setOnClickListener {
            activity?.let {
                startActivity(Intent(requireContext(),EditProfileActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val userDB = FirebaseDatabase.getInstance().reference.child(DBkey.DB_USERS)
        val userUid = FirebaseAuth.getInstance().currentUser!!.uid
        userDB.child(userUid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                LoginUserData.uid =
                    dataSnapshot.child("uid").value.toString()
                LoginUserData.email =
                    dataSnapshot.child("email").value.toString()
                LoginUserData.name =
                    dataSnapshot.child("name").value.toString()
                LoginUserData.gender =
                    dataSnapshot.child("gender").value.toString()
                LoginUserData.cm =
                    dataSnapshot.child("cm").value.toString()
                LoginUserData.kg =
                    dataSnapshot.child("kg").value.toString()
                LoginUserData.faceImageUri = Uri.parse(dataSnapshot.child("faceImageUri").value.toString())
                LoginUserData.bodyImageUri = Uri.parse(dataSnapshot.child("bodyImageUri").value.toString())
                LoginUserData.avatarImageUri = dataSnapshot.child("avatarImageUri").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "SignIn",
                    error.toException().toString()
                )
            }
        })
    }

}