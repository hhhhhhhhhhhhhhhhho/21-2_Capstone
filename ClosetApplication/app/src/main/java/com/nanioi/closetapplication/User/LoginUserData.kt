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
    var faceImageUri: Uri? = null
    var bodyImageUri: Uri? = null
    var avatarImageUri : String? = null
}
//todo avartar 사진 받아서 저장