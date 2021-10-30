package com.nanioi.capstoneproject.closet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ItemViewModel : ViewModel() {

    private lateinit var itemList: MutableList<ItemModel>

    val itemStateLiveData = MutableLiveData<ItemState>(ItemState.Uninitialized)

    fun selectPhoto(item: ItemModel) {
        val findItem = itemList.find { it.itemId == item.itemId }
        findItem?.let { photo ->
            itemList[itemList.indexOf(photo)] =
                photo.copy(
                    isSelected = photo.isSelected.not()
                )
            setState(
                ItemState.Success(
                    photoList = itemList
                )
            )
        }
    }
    private fun setState(state: ItemState) {
        itemStateLiveData.postValue(state)
    }

    fun confirmCheckedPhotos(category: Int) {
        setState(
            ItemState.Loading
        )
        setState(
            ItemState.Confirm(
                photoList = itemList.filter { it.isSelected }
            )
        )
    }
}