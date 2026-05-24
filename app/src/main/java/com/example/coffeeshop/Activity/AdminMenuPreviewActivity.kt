package com.example.coffeeshop.Activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.AdminCategoryAdapter
import com.example.coffeeshop.Adapter.AdminItemAdapter
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityAdminMenuPreviewBinding

class AdminMenuPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMenuPreviewBinding
    private val viewModel = MainViewModel()
    private lateinit var itemAdapter: AdminItemAdapter
    private lateinit var categoryAdapter: AdminCategoryAdapter

    private var categories: List<CategoryModel> = emptyList()
    private var items: List<ItemModel> = emptyList()
    private var editingItemId: String? = null
    private var editingCategoryId: Int? = null
    private var itemMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminMenuPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this).getSession()
        if (session == null || session.role != UserRole.ADMIN) {
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        setupTabs()
        setupLists()
        setupActions()
        observeData()
        renderMode()
    }

    private fun setupTabs() {
        binding.itemsTab.setOnClickListener {
            itemMode = true
            renderMode()
        }
        binding.categoriesTab.setOnClickListener {
            itemMode = false
            renderMode()
        }
    }

    private fun setupLists() {
        itemAdapter = AdminItemAdapter(
            items = emptyList(),
            categoryNameProvider = { categoryId ->
                categories.firstOrNull { it.id.toString() == categoryId }?.title ?: "Category $categoryId"
            },
            onEditClicked = { item -> populateItemForm(item) },
            onDeleteClicked = { item -> deleteItem(item) }
        )
        binding.itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.itemsRecyclerView.adapter = itemAdapter

        categoryAdapter = AdminCategoryAdapter(
            items = emptyList(),
            onEditClicked = { category -> populateCategoryForm(category) },
            onDeleteClicked = { category -> deleteCategory(category) }
        )
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun setupActions() {
        binding.backBtn.setOnClickListener { finish() }
        binding.clearItemBtn.setOnClickListener { clearItemForm() }
        binding.saveItemBtn.setOnClickListener { saveItem() }
        binding.clearCategoryBtn.setOnClickListener { clearCategoryForm() }
        binding.saveCategoryBtn.setOnClickListener { saveCategory() }
    }

    private fun observeData() {
        viewModel.loadCategory().observe(this) { categoryList ->
            categories = categoryList.sortedBy { it.id }
            categoryAdapter.updateItems(categories)
            updateCategorySpinner()
            binding.categoriesEmptyTxt.visibility =
                if (categories.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            binding.nextCategoryIdTxt.text = "Next category id: ${nextCategoryId()}"
        }

        viewModel.loadItems().observe(this) { itemList ->
            items = itemList.sortedBy { it.title.lowercase() }
            itemAdapter.updateItems(items)
            binding.itemsEmptyTxt.visibility =
                if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun updateCategorySpinner() {
        val entries = if (categories.isEmpty()) listOf("No categories yet") else categories.map { "${it.id} • ${it.title}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, entries)
        binding.categorySpinner.adapter = adapter
        if (categories.isNotEmpty() && binding.categorySpinner.selectedItemPosition !in categories.indices) {
            binding.categorySpinner.setSelection(0)
        }
    }

    private fun renderMode() {
        binding.itemsTab.setBackgroundResource(
            if (itemMode) com.example.coffeeshop.R.drawable.orange_bg else com.example.coffeeshop.R.drawable.brown_bg
        )
        binding.categoriesTab.setBackgroundResource(
            if (itemMode) com.example.coffeeshop.R.drawable.brown_bg else com.example.coffeeshop.R.drawable.orange_bg
        )
        binding.itemsSection.visibility = if (itemMode) android.view.View.VISIBLE else android.view.View.GONE
        binding.categoriesSection.visibility = if (itemMode) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun saveItem() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "Create at least one category first", Toast.LENGTH_LONG).show()
            return
        }

        val title = binding.itemTitleEditText.text.toString().trim()
        val description = binding.itemDescriptionEditText.text.toString().trim()
        val extra = binding.itemExtraEditText.text.toString().trim()
        val imageUrl = binding.itemImageUrlEditText.text.toString().trim()
        val price = binding.itemPriceEditText.text.toString().toDoubleOrNull()
        val rating = binding.itemRatingEditText.text.toString().toDoubleOrNull() ?: 4.5
        val category = categories.getOrNull(binding.categorySpinner.selectedItemPosition)

        if (title.isBlank() || description.isBlank() || imageUrl.isBlank() || price == null || price <= 0.0 || category == null) {
            Toast.makeText(this, "Fill title, description, image URL, category and valid price", Toast.LENGTH_LONG).show()
            return
        }

        val item = ItemModel(
            id = editingItemId.orEmpty(),
            categoryId = category.id.toString(),
            title = title,
            description = description,
            picUrl = arrayListOf(imageUrl),
            price = price,
            rating = rating,
            extra = extra
        )

        viewModel.saveItem(item) { result ->
            runOnUiThread {
                if (result.success) {
                    Toast.makeText(this, "Item saved", Toast.LENGTH_SHORT).show()
                    clearItemForm()
                } else {
                    Toast.makeText(this, result.errorMessage ?: "Unable to save item", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deleteItem(item: ItemModel) {
        if (item.id.isBlank()) {
            Toast.makeText(this, "This item has no firebase key", Toast.LENGTH_LONG).show()
            return
        }
        viewModel.deleteItem(item.id) { result ->
            runOnUiThread {
                if (result.success) {
                    if (editingItemId == item.id) clearItemForm()
                    Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, result.errorMessage ?: "Unable to delete item", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateItemForm(item: ItemModel) {
        editingItemId = item.id
        binding.itemFormTitleTxt.text = "Edit item"
        binding.itemTitleEditText.setText(item.title)
        binding.itemDescriptionEditText.setText(item.description)
        binding.itemExtraEditText.setText(item.extra)
        binding.itemImageUrlEditText.setText(item.firstImageUrl())
        binding.itemPriceEditText.setText(item.price.toString())
        binding.itemRatingEditText.setText(item.rating.toString())
        val index = categories.indexOfFirst { it.id.toString() == item.categoryId }
        if (index >= 0) binding.categorySpinner.setSelection(index)
    }

    private fun clearItemForm() {
        editingItemId = null
        binding.itemFormTitleTxt.text = "Create item"
        binding.itemTitleEditText.text?.clear()
        binding.itemDescriptionEditText.text?.clear()
        binding.itemExtraEditText.text?.clear()
        binding.itemImageUrlEditText.text?.clear()
        binding.itemPriceEditText.text?.clear()
        binding.itemRatingEditText.setText("4.5")
        if (categories.isNotEmpty()) binding.categorySpinner.setSelection(0)
    }

    private fun saveCategory() {
        val title = binding.categoryTitleEditText.text.toString().trim()
        val categoryId = editingCategoryId ?: nextCategoryId()
        if (title.isBlank()) {
            Toast.makeText(this, "Category title is required", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.saveCategory(CategoryModel(title = title, id = categoryId)) { result ->
            runOnUiThread {
                if (result.success) {
                    Toast.makeText(this, "Category saved", Toast.LENGTH_SHORT).show()
                    clearCategoryForm()
                } else {
                    Toast.makeText(this, result.errorMessage ?: "Unable to save category", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deleteCategory(category: CategoryModel) {
        val hasLinkedItems = items.any { it.categoryId == category.id.toString() }
        if (hasLinkedItems) {
            Toast.makeText(this, "Reassign or delete items in this category first", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.deleteCategory(category.id) { result ->
            runOnUiThread {
                if (result.success) {
                    if (editingCategoryId == category.id) clearCategoryForm()
                    Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, result.errorMessage ?: "Unable to delete category", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateCategoryForm(category: CategoryModel) {
        editingCategoryId = category.id
        binding.categoryFormTitleTxt.text = "Edit category"
        binding.categoryTitleEditText.setText(category.title)
        binding.nextCategoryIdTxt.text = "Editing category id: ${category.id}"
    }

    private fun clearCategoryForm() {
        editingCategoryId = null
        binding.categoryFormTitleTxt.text = "Create category"
        binding.categoryTitleEditText.text?.clear()
        binding.nextCategoryIdTxt.text = "Next category id: ${nextCategoryId()}"
    }

    private fun nextCategoryId(): Int = (categories.maxOfOrNull { it.id } ?: -1) + 1
}
