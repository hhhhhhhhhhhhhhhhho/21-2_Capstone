package com.nanioi.closetapplication.User

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey.Companion.DB_USERS
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import java.io.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.nanioi.closetapplication.User.userDBkey.*
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_AVATAR_FRONT
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_BODY_BACK
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_BODY_FRONT
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_EMAIL
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_GENDER
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_HEIGHT
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_NAME
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_UID
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_WEIGHT
import com.nanioi.closetapplication.databinding.ActivitySignInBinding
import com.nanioi.closetapplication.databinding.ActivitySignUpBinding

class SignInActivity : AppCompatActivity() {


    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val userDB : FirebaseDatabase by lazy { Firebase.database}
    private val binding by lazy { ActivitySignInBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // by 나연. 로그인 버튼 클릭 시 firestore 추가
        binding.btnSignSignIn.setOnClickListener { //로그인 버튼 클릭 리스너
            if (binding.etSignEmail.text.isNotEmpty())
                if (binding.etSignPassword.text.isNotEmpty()) {
                    Toast.makeText(this@SignInActivity, "로그인 중...", Toast.LENGTH_SHORT).show()
                    auth.signInWithEmailAndPassword(
                        binding.etSignEmail.text.toString(),
                        binding.etSignPassword.text.toString()
                    ).addOnCompleteListener(this@SignInActivity) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "로그인 성공!\n유저 데이터 가져오는 중...",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val user = auth.currentUser!!.uid
                                userDB.reference.child(DB_USERS).child(user).addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        LoginUserData.uid =
                                            dataSnapshot.child(DB_UID).value.toString()
                                        LoginUserData.email =
                                            dataSnapshot.child(DB_EMAIL).value.toString()
                                        LoginUserData.name =
                                            dataSnapshot.child(DB_NAME).value.toString()
                                        LoginUserData.gender =
                                            dataSnapshot.child(DB_GENDER).value.toString()
                                        LoginUserData.cm =
                                            dataSnapshot.child(DB_HEIGHT).value.toString()
                                        LoginUserData.kg =
                                            dataSnapshot.child(DB_WEIGHT).value.toString()
                                        LoginUserData.body_front_ImageUrl = dataSnapshot.child(DB_BODY_FRONT).value.toString()
                                        LoginUserData.body_back_ImageUrl = dataSnapshot.child(DB_BODY_BACK).value.toString()
                                        LoginUserData.avatar_back_ImageUrl = dataSnapshot.child(DB_AVATAR_FRONT).value.toString()

                                        if (LoginUserData.name != null) {
                                            Toast.makeText(
                                                this@SignInActivity,
                                                "${LoginUserData.name}님, 환영합니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            startActivity(
                                                Intent(
                                                    this@SignInActivity,
                                                    MainActivity::class.java
                                                )
                                            )
                                            finish() //로그인 성공 시 81번 줄을 통해 메인화면으로 넘어갔고, 로그인화면은 더이상 필요없으니 종료
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e(
                                            "SignIn",
                                            error.toException().toString()
                                        )
                                    }
                                })

                            } else { //실패했을때
                                Toast.makeText(this@SignInActivity, "로그인 실패", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else
                    Toast.makeText(this@SignInActivity, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this@SignInActivity, "이메일주소를 입력해 주세요.", Toast.LENGTH_SHORT).show()
        }
        //by 나연. 회원가입 클릭 시 회원가입 Activity로 이동 (2021.09.27)
        val btnSignSignUp = binding.signUpTextView.text as Spannable
        val clickSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }
        }
        btnSignSignUp.setSpan(clickSpan, 13, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.signUpTextView.movementMethod = LinkMovementMethod.getInstance()

    }
}