package com.nanioi.capstoneproject.closet

import android.net.Uri

data class ItemModel(
    val userId: String,
    val itemId: Long,
    val categoryNumber: Int,
    val imageUrl: Uri,
    var isSelected: Boolean = false
)