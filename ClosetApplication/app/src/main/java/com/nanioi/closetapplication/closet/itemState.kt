package com.nanioi.closetapplication.closet

import androidx.annotation.IdRes

sealed class itemState {

    object Uninitialized: itemState()

    object Loading: itemState()

    data class Success(
        val itemList: List<ItemModel>,
        @IdRes val toastId: Int? = null
    ): itemState()

    data class Confirm(
        val itemList: List<ItemModel>
    ): itemState()

}
