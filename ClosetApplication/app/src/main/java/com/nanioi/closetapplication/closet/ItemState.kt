package com.nanioi.closetapplication.closet

import androidx.annotation.IdRes

sealed class ItemState {

    object Uninitialized: ItemState()

    object Loading: ItemState()

    data class Success(
        val photoList: List<ItemModel>,
      //  @IdRes val toastId: Int? = null
    ): ItemState()

    data class Confirm(
        val photoList: List<ItemModel>
    ): ItemState()

}
