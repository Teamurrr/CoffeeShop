package com.example.coffeeshop.Activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.CategoryAdapter
import com.example.coffeeshop.Adapter.PopularAdapter
import com.example.coffeeshop.Adapter.SpecialAdapter
import com.example.coffeeshop.R
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel= MainViewModel(

    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCategory()
        initPopular()
        initSpecial()

    }

    private fun initSpecial() {
        binding.progressBarSpecial.visibility = View.VISIBLE
        viewModel.loadSpecial().observeForever{
            binding.recyclerViewSpecial.layoutManager=
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.progressBarSpecial.visibility = View.GONE
            binding.recyclerViewSpecial.adapter = SpecialAdapter(it)

        }
        viewModel.loadSpecial()


    }

    private fun initPopular() {
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.loadPopular().observeForever{
            binding.recyclerViewPopular.layoutManager=
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.progressBarPopular.visibility = View.GONE
            binding.recyclerViewPopular.adapter = PopularAdapter(it)

        }

    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.loadCategory().observeForever{
            binding.recyclerViewCategory.layoutManager=
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            binding.recyclerViewCategory.adapter = CategoryAdapter(it)
            binding.progressBarCategory.visibility = View.GONE
        }
        viewModel.loadCategory()

    }
}

