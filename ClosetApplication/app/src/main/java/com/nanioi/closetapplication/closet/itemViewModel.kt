package com.nanioi.closetapplication.closet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.closetApplication.Companion.appContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

//viewModel은 repo에 있는 데이터를 관찰하고 있다가 변경이 되면 mutableData의 값을 변경시켜주는 역할
class itemViewModel : ViewModel() {

    val db = FirebaseFirestore.getInstance()
    val user = Firebase.auth.currentUser!!.uid

    var itemList = mutableListOf<ItemModel>()
    //var itemStateLiveData = MutableLiveData<MutableList<ItemModel>>()
    val itemStateLiveData = MutableLiveData<ItemState>(ItemState.Uninitialized)

    fun fetchData() = viewModelScope.launch {
        setState(
            ItemState.Loading
        )
        itemList = db.collection(DBkey.DB_USERS).document(user)
            .collection(DBkey.DB_ITEM).get().await()
            .toObjects(ItemModel::class.java)

        setState(
            ItemState.Success(
                photoList = itemList
            )
        )
    }

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

    fun deletePhoto() {
        val findItem = itemList.filter { it.isSelected }
        itemList.removeAll(findItem)
        setState(
            ItemState.Success(
                photoList = itemList
            )
        )
    }

    private fun setState(state: ItemState) {
        itemStateLiveData.postValue(state)
    }

    fun confirmCheckedPhotos() {
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