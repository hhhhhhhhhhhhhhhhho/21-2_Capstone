package com.nanioi.closetapplication.retrofit

import com.nanioi.closetapplication.User.User
import com.nanioi.closetapplication.User.UserFromServer
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

    //아바타 이미지 받아오기
    @GET("")
    fun getAvatar(): Call<User>
    // 해당주소에 get요청을하는데 response가 뒤에 타입이다.


//    @POST("")
//    fun createStudent(
//        @Body params: HashMap<String, Any>
//    ): Call<UserFromServer>


    //라이브러리사용
    @POST("")
    fun createAvatar(
        @Body params: UserFromServer
    ): Call<User>

    //로그인
    @POST("/SignIn")
    @FormUrlEncoded
    fun login(
        @Field("userId") userId: String,
        @Field("faceImage") userFaceImage: String,
        @Field("bodyImage") userBodyImage: String
    ): Call<User>

    //closet
    @Multipart
    @POST("/closet")
    fun sendClosetItems(
        @Part image: MultipartBody.Part,
        @Part("content") requestBody: RequestBody
    ): Call<User>

    //styling
    @Multipart
    @POST("/styling")
    fun sendStylingItem(
        @Part image: MultipartBody.Part,
        @Part("content") requestBody: RequestBody
    ): Call<User>

}