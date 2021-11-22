package com.nanioi.closetapplication.User

data class userModel(
    var uid: String?,
    var email: String?,
    var name: String?,
    var gender: String?,
    var cm: String?,
    var kg: String?,
    var body_front_imageUri: String?,
    var body_back_imageUri: String?
) {
    constructor() : this(null,null,null,null,null,null,null,null)
}
