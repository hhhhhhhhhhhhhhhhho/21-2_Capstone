package com.nanioi.closetapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private var etSignUpName: EditText? = null //이름 입력창 변수 선언
    private var etSignUpEmail: EditText? = null //이메일 입력창 변수 선언
    private var etSignUpPassword: EditText? = null //비밀번호 입력창 변수 선언
    private var etSignUpPasswordCheck: EditText? = null //비밀번호 확인 입력창 변수 선언
    private var etSignUpCm: EditText? = null //키 입력창 변수 선언
    private var etSignUpKg: EditText? = null //몸무게 입력창 변수 선언
    private var rgSignUpGender: RadioGroup? = null //성별 선택 그룹 변수 선언
    private var rbSignUpMan: RadioButton? = null //남자 선택 변수 선언
    private var rbSignUpWoman: RadioButton? = null //여자 선택 변수 선언
    private var ivSignUpFace: ImageView? = null //얼굴 사진 뷰 변수 선언
    private var ivSignUpBody: ImageView? = null //전신 사진 뷰 변수 선언
    private var btnSignUpFace: Button? = null //얼굴 사진 버튼 변수 선언
    private var btnSignUpBody: Button? = null //전신 사진 버튼 변수 선언
    private var btnSignUpPass: Button? = null //회원가입 완료 버튼 변수 선언

    private var firebaseAuth: FirebaseAuth? = null //파이어 베이스 인스턴스 변수 선언


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etSignUpName = findViewById(R.id.et_sign_up_name)
        etSignUpEmail = findViewById(R.id.et_sign_up_email)
        etSignUpPassword = findViewById(R.id.et_sign_up_password)
        etSignUpPasswordCheck = findViewById(R.id.et_sign_up_password_check)
        etSignUpCm = findViewById(R.id.et_sign_up_cm)
        etSignUpKg = findViewById(R.id.et_sign_up_kg)
        rgSignUpGender = findViewById(R.id.rg_sign_up_gender)
        rbSignUpMan = findViewById(R.id.rb_sign_up_man)
        rbSignUpWoman = findViewById(R.id.rb_sign_up_woman)
        ivSignUpFace = findViewById(R.id.iv_sign_up_face)
        ivSignUpBody = findViewById(R.id.iv_sign_up_body)
        btnSignUpFace = findViewById(R.id.btn_sign_up_face)
        btnSignUpBody = findViewById(R.id.btn_sign_up_body)
        btnSignUpPass = findViewById(R.id.btn_sign_up_pass)

        firebaseAuth = FirebaseAuth.getInstance().also { firebaseAuth = it } //파이어 베이스 인스턴스 생성


        rgSignUpGender?.check(R.id.rb_sign_up_man) //아무것도 체크가 되어있지 않으면 안되니 기본적으로 남자로 체크되어 있음

        btnSignUpPass?.setOnClickListener {
            if (etSignUpName?.text!!.isNotEmpty())
                if (etSignUpEmail?.text!!.isNotEmpty())
                    if (etSignUpPassword?.text!!.isNotEmpty())
                        if (etSignUpPasswordCheck?.text!!.isNotEmpty())
                            if(etSignUpPassword!!.text.toString() == etSignUpPasswordCheck?.text.toString())
                                firebaseAuth!!.createUserWithEmailAndPassword(etSignUpEmail?.text.toString(), etSignUpPassword?.text.toString()) //파이어 베이스 함수 중 회원가 함수 createUserWithEmailAndPassword 사용
                                    .addOnCompleteListener(this) { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(this@SignUpActivity, "회원가입 완료", Toast.LENGTH_SHORT).show()
                                            finish() //회원가입을 완료했다면 해당 액티비티는 필요없으니 종료
                                        } else {
                                            Toast.makeText(this@SignUpActivity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            else
                                Toast.makeText(this@SignUpActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this@SignUpActivity, "비밀번호 확인을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this@SignUpActivity, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@SignUpActivity, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this@SignUpActivity, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}

