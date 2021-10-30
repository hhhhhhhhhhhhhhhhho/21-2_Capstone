package com.nanioi.capstoneproject.gallery

import android.net.Uri

data class GalleryPhoto(
    val id: Long,
    val uri: Uri,
    val name: String,
    val date: String,
    val size: Int,
    var category : Int? = null,
    var isSelected: Boolean = false
)
