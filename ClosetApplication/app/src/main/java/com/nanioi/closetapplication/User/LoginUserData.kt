package com.nanioi.closetapplication.User

import android.graphics.Bitmap
import android.net.Uri
import org.json.JSONObject
import java.io.File

object LoginUserData {
    var uid: String? = null
    var email: String? = null
    var name: String? = null
    var gender: String? = null
    var cm: String? = null
    var kg: String? = null
    var body_front_ImageUrl: String? = null
    var body_back_ImageUrl: String? = null
    var avatar_front_ImageUrl : String? = null // 누끼 딴 이미지 앞면
    var avatar_back_ImageUrl : String? = null// 누끼 뒷면
}
//todo avartar 사진 받아서 저장