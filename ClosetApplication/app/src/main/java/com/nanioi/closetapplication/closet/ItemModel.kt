package com.nanioi.closetapplication.closet

import android.net.Uri

data class ItemModel(
    val userId: String,
    val itemId: Long,
    val categoryNumber: Int,
    val imageUrl: String,
    var isSelected: Boolean = false
){
    constructor(): this("", 0,0,"",false)
}