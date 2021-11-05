package com.nanioi.closetapplication.User

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.DBkey.Companion.DB_USERS
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.utils.LoginUserData


class SignInActivity : AppCompatActivity() {

    private var etSignEmail: EditText? = null //이메일 입력 창 변수 선언
    private var etSignPassword: EditText? = null //비밀번호 입력 창 변수 선언
    private var btnSignSignIn: Button? = null //로그인 버튼 변수 선언
    private var firebaseAuth: FirebaseAuth? = null //파이어 베이스 인스턴스 변수 선언
    private val userDB : FirebaseDatabase by lazy { Firebase.database}
   // val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        etSignEmail = findViewById(R.id.et_sign_email) //이메일 입력 창 변수에 레이아웃에서 만든 ID 값 할당
        etSignPassword = findViewById(R.id.et_sign_password) //비밀번호 입력 창 변수에 레이아웃에서 만든 ID 값 할당
        btnSignSignIn = findViewById(R.id.btn_sign_sign_in) //로그인 버튼 변수에 레이아웃에서 만든 ID 값 할당
        firebaseAuth = FirebaseAuth.getInstance().also { firebaseAuth = it } //파이어 베이스 인스턴스 생성


        // by 나연. 로그인 버튼 클릭 시 firestore 추가
        btnSignSignIn?.setOnClickListener { //로그인 버튼 클릭 리스너
            if (etSignEmail?.text!!.isNotEmpty())
                if (etSignPassword?.text!!.isNotEmpty()) {
                    Toast.makeText(this@SignInActivity, "로그인 중...", Toast.LENGTH_SHORT).show()
                    firebaseAuth?.signInWithEmailAndPassword(
                        etSignEmail?.text.toString(),
                        etSignPassword?.text.toString()
                    ) //파이어 베이스 함수 중 로그인 함수 signInWithEmailAndPassword 사용
                        ?.addOnCompleteListener(this@SignInActivity) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "로그인 성공!\n유저 데이터 가져오는 중...",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val user = Firebase.auth.currentUser!!.uid
                                userDB.reference.child(DB_USERS).child(user).addValueEventListener(object : ValueEventListener {
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
        var signUpText = findViewById<TextView>(R.id.signUpTextView)
        val btnSignSignUp = signUpText.text as Spannable
        val clickSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }
        }
        btnSignSignUp.setSpan(clickSpan, 13, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        signUpText.movementMethod = LinkMovementMethod.getInstance()

    }

//    private fun getPermission() {
//        if ((ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED) ||
//            (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED) ||
//            (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED)
//        ) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                    this,
//                    Manifest.permission.CAMERA
//                ) ||
//                ActivityCompat.shouldShowRequestPermissionRationale(
//                    this,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                ) ||
//                ActivityCompat.shouldShowRequestPermissionRationale(
//                    this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                )
//            ) {
//                Toast.makeText(this, "권한이 설정되지 않았습니다.", Toast.LENGTH_LONG).show()
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ),
//                    101
//                )
//            } else {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ),
//                    101
//                )
//                //권한 확인 완료
//            }
//        } else {
//            //권한 확인 완료
//        }
//    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 101) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "앱 실행을 위한 권한이 설정 되었습니다.", Toast.LENGTH_LONG).show()
//            } else {
//                Toast.makeText(
//                    this,
//                    "권한이 허용되지 않을 경우 앱 사용이 불가합니다.\n시스템 설정에서 권한을 허용 해 주세요.",
//                    Toast.LENGTH_LONG
//                ).show()
//                finish()
//            }
//        }
//    }
}