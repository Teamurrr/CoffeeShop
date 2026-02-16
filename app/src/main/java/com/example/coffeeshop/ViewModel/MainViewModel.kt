package com.example.coffeeshop.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Repository.MainRepository

class MainViewModel: ViewModel() {
    private val repository = MainRepository()

    fun loadCategory(): LiveData<MutableList<CategoryModel>>{
        return repository.loadCategory()
    }

    fun loadPopular(): LiveData<MutableList<ItemModel>>{
        return repository.loadPopular()
    }

    fun loadSpecial(): LiveData<MutableList<ItemModel>> {
        return repository.loadSpecial()
    }

}