package com.example.coffeeshop.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.ItemModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class MainRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadCategory(): LiveData<MutableList<CategoryModel>>{
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list=mutableListOf<CategoryModel>()
                for(child in snapshot.children){
                    val item = child.getValue(CategoryModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value=list
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return listData
    }

    fun loadPopular(): LiveData<MutableList<ItemModel>>{
        val listData = MutableLiveData<MutableList<ItemModel>>()
        val ref = firebaseDatabase.getReference("Popular")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemModel>()
                for (child in snapshot.children){
                    val item = child.getValue(ItemModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value=list
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return listData
    }

    fun loadSpecial(): LiveData<MutableList<ItemModel>>{
        val listData = MutableLiveData<MutableList<ItemModel>>()
        val ref = firebaseDatabase.getReference("Special")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemModel>()
                for (child in snapshot.children){
                    val item = child.getValue(ItemModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value=list
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return listData
    }


}