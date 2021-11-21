package com.nanioi.closetapplication.retrofit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.nanioi.closetapplication.User.User
import com.nanioi.closetapplication.databinding.ActivityRetrofitBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//예시이고 retrofit 이용하는 activity에 service 등록해 사용
class RetrofitActivity : AppCompatActivity() {

    private val binding by lazy { ActivityRetrofitBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://~~~~~~~/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(RetrofitService::class.java)

        //GET 요청 - 아바타이미지 받아오기
        service.getAvatar()
            .enqueue(object : Callback<User> { // 이 통신을 대기줄에 올려놨다.
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    //통신 성공 시  호출
                    if (response.isSuccessful) {
                        val user = response.body()

                        val code = response.code() // 200
                        val error = response.errorBody() // 203/204등 완전한 성공 x
                        val header = response.headers() // 인증정보나 요약본

                    }
                }

                override fun onFailure(
                    call: Call<User>,
                    t: Throwable
                ) { //통신 실패 시 호출
                    Log.d("retrofitt", "Error") // 보통  에러메세지만 출력


                    call.isCanceled // 취소된건지
                    call.isExecuted // 실행은 된건지
                    call.cancel() // 실패 시 다시시도하지 말고 취소

                }
            })

        //POST 요청(1)
//        val params = HashMap<String, Any>()
//        params.put("name", "곽나연")
//        params.put("age", "24")
//        params.put("intro", "안녕하세요")
//        service.createStudent(params).enqueue(object : Callback<PersonFromServer> {
//            override fun onResponse(
//                call: Call<PersonFromServer>,
//                response: Response<PersonFromServer>
//            ) {
//                if (response.isSuccessful) {
//                    val person = response.body()
//                    Log.d("retrofitt", " name : " + person?.name)
//                }
//            }
//
//            override fun onFailure(call: Call<PersonFromServer>, t: Throwable) {
//            }
//        })

        //POST 요청(2)
//        val user = UserFromServer(userId ="asffasges", faceImage = "asfefhkaus ", bodyImage= "asfesgas")
//        service.createAvatar(user).enqueue(object : Callback<User> {
//            override fun onResponse(
//                call: Call<User>,
//                response: Response<User>
//            ) {
//                if (response.isSuccessful) {
//                    val person = response.body()
//                    Log.d("retrofitt", " name : " + person?.userAvatarImage)
//                }
//            }
//
//            override fun onFailure(call: Call<User>, t: Throwable) {
//            }
//        })
    }
}