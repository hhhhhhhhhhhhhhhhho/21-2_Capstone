package com.nanioi.closetapplication.closet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey

class itemViewModel : ViewModel() {

    var itemList =  mutableListOf<ItemModel>()
    val itemStateLiveData = MutableLiveData<List<ItemModel>>()
    val itemDB = Firebase.database.reference.child(DBkey.DB_ITEM)

    fun fetchData() {
        itemDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemModel = snapshot.getValue(ItemModel::class.java) // ItemModel 클래스로 데이터를 받아옴
                itemModel ?: return

                itemList.add(itemModel)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        setState(itemList)
    }

    fun selectPhoto(item: ItemModel) { // 선택된거 id 값으루 찾아서 그 인덱스에 있는 사진의 isSelected 값을 반대로 바꿔

        val findItem = itemList.find { it.itemId == item.itemId }
        findItem?.let { item ->
            itemList[itemList.indexOf(item)] =
                item.copy(
                    isSelected = item.isSelected.not()
                )
        }
        setState(itemList)
    }

    fun deleteItem(deleteItemList: List<ItemModel>) {
        this.itemList.removeAll(deleteItemList)
        setState(itemList)
    }

    private fun setState(itemList: List<ItemModel>) {
        itemStateLiveData.value = itemList
    }

    fun confirmCheckedPhotos(): List<ItemModel> {
        val photoList = itemList.filter { it.isSelected }
        return photoList
    }
}