package com.nanioi.closetapplication.closet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.nanioi.closetapplication.DBkey
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

//viewModel은 repo에 있는 데이터를 관찰하고 있다가 변경이 되면 mutableData의 값을 변경시켜주는 역할
//viewModelScope : destroy 될 때 자식 코루틴들을 자동으로 취소하는 기능을 제공
class itemViewModel : ViewModel() {

    val db = FirebaseFirestore.getInstance()
    val user = Firebase.auth.currentUser!!.uid
    private val storage: FirebaseStorage by lazy { Firebase.storage }

    var itemList = mutableListOf<ItemModel>()
    private var _itemStateLiveData = MutableLiveData<ItemState>(ItemState.Uninitialized)
    val itemStateLiveData : LiveData<ItemState> = _itemStateLiveData

    fun fetchData() = viewModelScope.launch {
        Log.d("bb","fetch")
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
        Log.d("bb","select")
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
        Log.d("bb","delete")
        val findItem = itemList.filter { it.isSelected }
        for(item in findItem){
            db.collection(DBkey.DB_USERS).document(user)
                .collection(DBkey.DB_ITEM).document(item.itemId.toString()).delete()

            val fileName = item.itemId.toString() + ".jpg"
            storage.reference.child("item/photo").child(fileName).delete()
        }
        itemList.removeAll(findItem)
        setState(
            ItemState.Success(
                photoList = itemList
            )
        )
    }

    private fun setState(state: ItemState) {
        Log.d("bb","setState")
        _itemStateLiveData.postValue(state)
    }

    fun confirmCheckedPhotos() {
        Log.d("bb","confirm")
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