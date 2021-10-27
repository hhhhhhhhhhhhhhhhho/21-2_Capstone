package com.nanioi.closetapplication.styling

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.closet.ItemModel
import com.nanioi.closetapplication.closet.ItemState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class stylingItemViewModel :ViewModel() {

    val db = FirebaseFirestore.getInstance()
    val user = Firebase.auth.currentUser!!.uid

    var itemList = mutableListOf<ItemModel>()
    //private var _stylingItem = MutableLiveData<ItemModel>()
    //val stylingItem : LiveData<ItemModel> = _stylingItem
    private var _itemStateLiveData = MutableLiveData<stylingState>(stylingState.Uninitialized)
    val itemStateLiveData : LiveData<stylingState> = _itemStateLiveData

    fun fetchData() = viewModelScope.launch {
        itemList = db.collection(DBkey.DB_USERS).document(user)
            .collection(DBkey.DB_ITEM).get().await()
            .toObjects(ItemModel::class.java)
        setState(
            stylingState.Success(
                photoList = itemList
            )
        )
    }
    fun selectPhoto(item: ItemModel) {
        Log.d("bb","select")
        val findItem = itemList.find { it.itemId == item.itemId }
        findItem?.let { photo ->
            itemList[itemList.indexOf(photo)] =
                photo.copy(
                    isSelected = photo.isSelected.not()
                )
            setState(
                stylingState.Success(
                    photoList = itemList
                )
            )
        }
    }
    private fun setState(state: stylingState) {
        Log.d("bb","setState")
        _itemStateLiveData.postValue(state)
    }
    fun confirmCheckedPhotos() {
        Log.d("bb","confirm")
        val findItem = itemList.find { it.isSelected == true }
        findItem?.let { item->
            setState(
                stylingState.Confirm(
                    photo = item
                )
            )
        }
    }
}