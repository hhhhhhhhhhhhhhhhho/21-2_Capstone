package com.nanioi.closetapplication.User

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.text.htmlEncode
import com.bumptech.glide.load.engine.bitmap_recycle.ByteArrayAdapter
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
import com.nanioi.closetapplication.closet.closetObject
import com.nanioi.closetapplication.closetApplication
import com.nanioi.closetapplication.styling.StylingFragment
import com.nanioi.closetapplication.styling.stylingObject
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.lang.Exception
import java.net.Socket


class SignInActivity : AppCompatActivity() {

    private var etSignEmail: EditText? = null //이메일 입력 창 변수 선언
    private var etSignPassword: EditText? = null //비밀번호 입력 창 변수 선언
    private var btnSignSignIn: Button? = null //로그인 버튼 변수 선언
    private var firebaseAuth: FirebaseAuth? = null //파이어 베이스 인스턴스 변수 선언
    private val userDB : FirebaseDatabase by lazy { Firebase.database}

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
                                        Log.w("aaaaaaaaa","LoginUserData : " + LoginUserData.faceImageUri.toString())

                                        // //by나연. mysql통신 이미지 보내기 (21.11.14)
                                        val faceImageUri =  LoginUserData.faceImageUri
                                        val bodyImageUri = LoginUserData.bodyImageUri

                                        val faceImageFile = File(getImageFilePath(faceImageUri!!))
                                        val fileFaceRequestBody = RequestBody.create(MediaType.parse("image/*"),faceImageFile)
                                        val userFace = MultipartBody.Part.createFormData("image",faceImageFile.name,fileFaceRequestBody)

                                        val bodyImageFile = File(getImageFilePath(bodyImageUri!!))
                                        val fileBodyRequestBody = RequestBody.create(MediaType.parse("image/*"),bodyImageFile)
                                        val userBody = MultipartBody.Part.createFormData("image",faceImageFile.name,fileBodyRequestBody)

                                        (application as closetApplication).service.createAvatar(userFace,userBody).enqueue(
                                            object : Callback<User>{
                                                override fun onResponse(
                                                    call: Call<User>,
                                                    response: Response<User>
                                                ) {
                                                    if(response.isSuccessful) {
                                                        val avatar = response.body()
                                                        LoginUserData.avatarImageUri = Uri.parse(avatar!!.userAvatarImage)
                                                    }
                                                }

                                                override fun onFailure(
                                                    call: Call<User>,
                                                    t: Throwable
                                                ) {
                                                    Log.w("aaa", "실패  : "+ t.toString())
                                                }

                                            }
                                        )

                                        //mysql안되면시도
                                        //ClientThread(userFace,userBody).start()

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
    //by나연. 이미지 파일 절대경로 알아내기 (21.11.14)
    fun getImageFilePath(contentUri: Uri):String{
        var columnIndex = 0
        val projection = arrayOf(MediaStore.Images.Media.DATA) // 걸러내기
        val cursor = contentResolver.query(contentUri,projection,null,null,null)
        // list index 가르키기 , content 관리하는 resolver에 검색(query) 부탁
        if( cursor!!.moveToFirst()){
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        return cursor.getString(columnIndex)
    }

    //한번시도해보기
    class ClientThread(
        val userFaceImage : MultipartBody.Part,
        val userBodyImage : MultipartBody.Part
    ) : Thread() {
        override fun run() {
            super.run()
            Log.w("aaaaaaaaa", "clientThread")
            //소켓통신
//            private var mHandler: Handler? = null
//            private val ip = "192.168.144.226" // IP 번호
//            private val port = 12345 // port 번호
            val host = "192.168.144.226"
            val port = 9999

            //OutputStream에 전송할 데이터를 담아 보낸 뒤, InputStream을 통해 데이터를 읽
            try {
                val socket = Socket(host, port)
                val outstream = DataOutputStream(socket.getOutputStream())
                outstream.writeUTF(userFaceImage.toString())

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
}