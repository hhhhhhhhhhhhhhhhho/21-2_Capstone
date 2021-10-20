package com.nanioi.closetapplication.closet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class itemGalleryViewModel : ViewModel() {

  //  private val galleryPhotoRepository by lazy { GalleryPhotoRepository(appContext!!) }

    private lateinit var itemList: MutableList<ItemModel>
    val galleryStateLiveData = MutableLiveData<itemState>(itemState.Uninitialized)

//    fun fetchData() = viewModelScope.launch {
//        setState(
//            itemState.Loading
//        )
//        itemList = galleryPhotoRepository.getAllPhotos()
//        setState(
//            itemState.Success(
//                itemList = itemList
//            )
//        )
//    }

    fun selectPhoto(item: ItemModel) {
        val findGalleryPhoto = itemList.find { it.itemId == item.itemId }
        findGalleryPhoto?.let { photo ->
            itemList[itemList.indexOf(photo)] =
                photo.copy(
                    isSelected = photo.isSelected.not()
                )
            setState(
                itemState.Success(
                    itemList = itemList
                )
            )
        }
    }
    private fun setState(state: itemState) {
        galleryStateLiveData.postValue(state)
    }
    fun confirmCheckedPhotos() {
        setState(
            itemState.Loading
        )
        setState(
            itemState.Confirm(
                itemList = itemList.filter { it.isSelected }
            )
        )
    }
}