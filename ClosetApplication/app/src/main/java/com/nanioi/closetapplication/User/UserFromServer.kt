package com.nanioi.closetapplication.User

import java.io.Serializable

class UserFromServer(
    var userId : String? = null,
    var faceImage: String? = null,
    var bodyImage: String? = null
) : Serializable