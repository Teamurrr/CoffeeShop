package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.CategoryAdapter
import com.example.coffeeshop.Adapter.MenuAdapter
import com.example.coffeeshop.Adapter.PopularAdapter
import com.example.coffeeshop.Adapter.SpecialAdapter
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Repository.CartManager
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()
    private lateinit var sessionManager: SessionManager

    private var allItems: List<ItemModel> = emptyList()
    private var selectedCategory: CategoryModel? = null
    private var searchQuery: String = ""

    private lateinit var menuAdapter: MenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val session = sessionManager.getSession()
        if (session == null || session.role != UserRole.CUSTOMER) {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        binding.roleBadgeTxt.text = "${session.name} - customer"

        setupStaticUi()
        initCategory()
        initPopular()
        initMenu()
        initSpecial()
        observeCart()
    }

    private fun setupStaticUi() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            searchQuery = text?.toString().orEmpty()
            renderFilteredMenu()
        }

        binding.cartSummaryBtn.setOnClickListener { openCart() }
        binding.cartTab.setOnClickListener { openCart() }
        binding.ordersTab.setOnClickListener {
            startActivity(Intent(this, CustomerOrdersActivity::class.java))
        }
        binding.reserveBtn.setOnClickListener {
            startActivity(Intent(this, ReservationActivity::class.java))
        }
        binding.roleBadgeTxt.setOnClickListener {
            sessionManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun initCategory() {
        binding.progressBarCategory.show()
        binding.recyclerViewCategory.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        viewModel.loadCategory().observe(this) { categories ->
            binding.recyclerViewCategory.adapter = CategoryAdapter(categories) { category ->
                selectedCategory = category
                renderFilteredMenu()
            }
            binding.progressBarCategory.hide()
        }
    }

    private fun initPopular() {
        binding.progressBarPopular.show()
        binding.recyclerViewPopular.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        viewModel.loadPopular().observe(this) { items ->
            binding.recyclerViewPopular.adapter = PopularAdapter(items) { item ->
                openDetails(item)
            }
            binding.progressBarPopular.hide()
        }
    }

    private fun initMenu() {
        binding.progressBarMenu.show()
        binding.recyclerViewMenu.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        menuAdapter = MenuAdapter(
            items = emptyList(),
            onItemClicked = { item -> openDetails(item) },
            onAddClicked = { item ->
                CartManager.addItem(item, 1)
                Toast.makeText(this, getString(com.example.coffeeshop.R.string.order_added), Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerViewMenu.adapter = menuAdapter

        viewModel.loadItems().observe(this) { items ->
            allItems = items
            binding.progressBarMenu.hide()
            renderFilteredMenu()
        }
    }

    private fun initSpecial() {
        binding.progressBarSpecial.show()
        binding.recyclerViewSpecial.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel.loadSpecial().observe(this) { items ->
            binding.recyclerViewSpecial.adapter = SpecialAdapter(items) { item ->
                openDetails(item)
            }
            binding.progressBarSpecial.hide()
        }
    }

    private fun observeCart() {
        CartManager.observeCart().observe(this) {
            binding.cartSummaryBtn.text = getString(
                com.example.coffeeshop.R.string.cart_summary,
                CartManager.totalCount()
            )
        }
    }

    private fun renderFilteredMenu() {
        val normalizedQuery = searchQuery.trim().lowercase()
        val filteredItems = allItems.filter { item ->
            val matchesCategory = selectedCategory == null || item.categoryId == selectedCategory?.id.toString()
            val matchesQuery = normalizedQuery.isBlank() ||
                item.title.lowercase().contains(normalizedQuery) ||
                item.description.lowercase().contains(normalizedQuery) ||
                item.extra.lowercase().contains(normalizedQuery)
            matchesCategory && matchesQuery
        }

        menuAdapter.updateItems(filteredItems)
        binding.menuCountTxt.text = "${filteredItems.size} items"
        binding.emptyMenuTxt.visibility = if (filteredItems.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun openDetails(item: ItemModel) {
        startActivity(Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_ITEM, item)
        })
    }

    private fun openCart() {
        startActivity(Intent(this, CartActivity::class.java))
    }

    private fun android.widget.ProgressBar.show() {
        visibility = android.view.View.VISIBLE
    }

    private fun android.widget.ProgressBar.hide() {
        visibility = android.view.View.GONE
    }
}
