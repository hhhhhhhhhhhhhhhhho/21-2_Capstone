package com.nanioi.closetapplication.closet

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.DBkey.Companion.DB_ITEM
import com.nanioi.closetapplication.DBkey.Companion.DB_USERS
import kotlinx.coroutines.tasks.await

class Repo {
    val mutableData = MutableLiveData<MutableList<ItemModel>>()
    var itemList: MutableList<ItemModel> = mutableListOf<ItemModel>()
    val userDB = Firebase.database.reference.child(DB_USERS)
    val user = Firebase.auth.currentUser?.uid

    suspend fun getData(): MutableList<ItemModel> {
//        user?.let {
//            val itemDB = userDB.child(user).child(DB_ITEM)
//            itemDB.addValueEventListener(object : ValueEventListener {
//                val listData: MutableList<ItemModel> = mutableListOf<ItemModel>()
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        for (userSnapshot in snapshot.children) {
//                            val getData = userSnapshot.getValue(ItemModel::class.java)
//                            getData?.let {
//                                listData.add(getData)
//                            }
//                            //mutableData.value = listData
//                        }
//                        Log.d("bb", "repo :" + listData.toString())
//                        mutableData.value = listData
//                        itemList = listData
//                        Log.d("bb", "repo, itemList :" + itemList.toString())
//                    }
//                }
//                override fun onCancelled(error: DatabaseError) {
//                    Log.d("bb", "repo error" + error.toString())
//                }
//            })
//        }
        val db = FirebaseFirestore.getInstance()
        val user = Firebase.auth.currentUser!!.uid

        val a = db.collection(DB_USERS).document(user)
            .collection(DB_ITEM).get().await()
            .toObjects(ItemModel::class.java)

        Log.d("bb", "repo, itemList2 :" + a.toString())
        return itemList
    }
}


//    suspend fun getData(): MutableList<ItemModel> {
//        var itemList = mutableListOf<ItemModel>()
//        val userDB = Firebase.database.reference.child(DB_USERS)
//        val user = Firebase.auth.currentUser?.uid
//        user?.let {
//            val itemDB = userDB.child(user).child(DB_ITEM)
//            itemDB.addValueEventListener(object : ValueEventListener {
//                val listData: MutableList<ItemModel> = mutableListOf<ItemModel>()
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        for (userSnapshot in snapshot.children) {
//                            val getData = userSnapshot.getValue(ItemModel::class.java)
//                            getData?.let {
//                                listData.add(getData)
//                            }
//                            itemList=listData
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//            })
//        }
//        return itemList
//    }