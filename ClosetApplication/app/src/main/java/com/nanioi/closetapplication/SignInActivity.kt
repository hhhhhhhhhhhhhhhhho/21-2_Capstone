package com.nanioi.closetapplication

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private var ivSignLogo: ImageView? = null //로고 이미지뷰 변수 선언
    private var etSignEmail: EditText? = null //이메일 입력 창 변수 선언
    private var etSignPassword: EditText? = null //비밀번호 입력 창 변수 선언
    private var btnSignSignIn: Button? = null //로그인 버튼 변수 선언
    private var btnSignSignUp: Spannable? = null //회원가입 버튼 변수 선언

    private var firebaseAuth: FirebaseAuth? = null //파이어 베이스 인스턴스 변수 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        firebaseAuth = FirebaseAuth.getInstance().also { firebaseAuth = it } //파이어 베이스 인스턴스 생성

        ivSignLogo = findViewById(R.id.iv_sign_logo) //로고 이미지뷰 변수에 레이아웃에서 만든 ID 값 할당
        etSignEmail = findViewById(R.id.et_sign_email) //이메일 입력 창 변수에 레이아웃에서 만든 ID 값 할당
        etSignPassword = findViewById(R.id.et_sign_password) //비밀번호 입력 창 변수에 레이아웃에서 만든 ID 값 할당
        btnSignSignIn = findViewById(R.id.btn_sign_sign_in) //로그인 버튼 변수에 레이아웃에서 만든 ID 값 할당

        // by 준승. 로그인 버튼 클릭 시 firebase 추가
        btnSignSignIn?.setOnClickListener {
            if (etSignEmail?.text!!.isNotEmpty())
                if (etSignPassword?.text!!.isNotEmpty())
                    firebaseAuth?.signInWithEmailAndPassword(etSignEmail?.text.toString(), etSignPassword?.text.toString()) //파이어 베이스 함수 중 로그인 함수 signInWithEmailAndPassword 사용
                        ?.addOnCompleteListener(this@SignInActivity) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@SignInActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                                finish() //로그인 성공 시 51번 줄을 통해 메인화면으로 넘어갔고, 로그인화면은 더이상 필요없으니 종료
                            } else { //실패했을때
                                Toast.makeText(this@SignInActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                else
                    Toast.makeText(this@SignInActivity, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this@SignInActivity, "이메일주소를 입력해 주세요.", Toast.LENGTH_SHORT).show()
        }

        //by 나연. 회원가입 클릭 시 회원가입 Activity로 이동 (2021.09.27)
        var signUpText = findViewById<TextView>(R.id.signUpTextView)
        var btnSignSignUp = signUpText.text as Spannable
        val clickSpan = object :ClickableSpan(){
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignInActivity,SignUpActivity::class.java))
            }
        }
        btnSignSignUp.setSpan(clickSpan, 13, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        signUpText.movementMethod = LinkMovementMethod.getInstance()

    }
}