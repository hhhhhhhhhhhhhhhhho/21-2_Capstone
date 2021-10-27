package com.nanioi.closetapplication.styling

import androidx.annotation.IdRes
import com.nanioi.closetapplication.closet.ItemModel

sealed class stylingState {

    object Uninitialized: stylingState()

    data class Success(
        val photoList: List<ItemModel>,
      //  @IdRes val toastId: Int? = null
    ): stylingState()

    data class Confirm(
        val photo: ItemModel
    ): stylingState()

}
