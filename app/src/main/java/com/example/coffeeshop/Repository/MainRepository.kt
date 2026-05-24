package com.example.coffeeshop.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.DataActionResult
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Domain.OrderItemModel
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.OrderResult
import com.example.coffeeshop.Domain.OrderStatus
import com.example.coffeeshop.Domain.PaymentStatus
import com.example.coffeeshop.Domain.UserSession
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()
                for (child in snapshot.children) {
                    val item = child.getValue(CategoryModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                listData.value = mutableListOf()
            }
        })
        return listData
    }

    fun loadPopular(): LiveData<MutableList<ItemModel>> {
        val listData = MutableLiveData<MutableList<ItemModel>>()
        val ref = firebaseDatabase.getReference("Popular")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemModel>()
                for (child in snapshot.children) {
                    val item = child.getValue(ItemModel::class.java)?.withFirebaseId(child.key)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                listData.value = mutableListOf()
            }
        })
        return listData
    }

    fun loadSpecial(): LiveData<MutableList<ItemModel>> {
        val listData = MutableLiveData<MutableList<ItemModel>>()
        val ref = firebaseDatabase.getReference("Special")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemModel>()
                for (child in snapshot.children) {
                    val item = child.getValue(ItemModel::class.java)?.withFirebaseId(child.key)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                listData.value = mutableListOf()
            }
        })
        return listData
    }

    fun loadItems(): LiveData<MutableList<ItemModel>> {
        val listData = MutableLiveData<MutableList<ItemModel>>()
        val ref = firebaseDatabase.getReference("Items")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemModel>()
                for (child in snapshot.children) {
                    val item = child.getValue(ItemModel::class.java)?.withFirebaseId(child.key)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                listData.value = mutableListOf()
            }
        })
        return listData
    }

    fun createOrder(
        items: List<ItemModel>,
        customerNote: String,
        paymentMethod: String,
        session: UserSession,
        onComplete: (OrderResult) -> Unit
    ) {
        val ordersRef = firebaseDatabase.getReference("Orders")
        val newOrderRef = ordersRef.push()
        val orderId = newOrderRef.key

        if (orderId.isNullOrBlank()) {
            onComplete(OrderResult(success = false, errorMessage = "Unable to create order"))
            return
        }

        val orderItems = items.map {
            OrderItemModel(
                itemId = it.id,
                title = it.title,
                quantity = it.numberInCart,
                price = it.price,
                imageUrl = it.firstImageUrl()
            )
        }

        val order = OrderModel(
            id = orderId,
            customerId = session.userId,
            customerName = session.name,
            customerEmail = session.email,
            status = OrderStatus.PENDING,
            paymentMethod = paymentMethod,
            paymentStatus = PaymentStatus.PENDING,
            totalAmount = items.sumOf { it.price * it.numberInCart },
            createdAt = System.currentTimeMillis(),
            customerNote = customerNote,
            items = orderItems
        )

        newOrderRef.setValue(order)
            .addOnSuccessListener {
                onComplete(OrderResult(success = true, orderId = orderId))
            }
            .addOnFailureListener { error ->
                val message = if (error.message?.contains("permission", ignoreCase = true) == true) {
                    "Firebase rules block write access to Orders"
                } else {
                    error.message ?: "Unknown Firebase error"
                }
                onComplete(OrderResult(success = false, errorMessage = message))
            }
    }

    fun loadOrders(): LiveData<MutableList<OrderModel>> {
        val listData = MutableLiveData<MutableList<OrderModel>>()
        val ref = firebaseDatabase.getReference("Orders")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<OrderModel>()
                for (child in snapshot.children) {
                    val item = child.getValue(OrderModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list.sortedByDescending { it.createdAt }.toMutableList()
            }

            override fun onCancelled(error: DatabaseError) {
                listData.value = mutableListOf()
            }
        })
        return listData
    }

    fun loadOrdersForCustomer(customerId: String): LiveData<MutableList<OrderModel>> {
        val listData = MutableLiveData<MutableList<OrderModel>>()
        val ref = firebaseDatabase.getReference("Orders")
        ref.orderByChild("customerId").equalTo(customerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<OrderModel>()
                    for (child in snapshot.children) {
                        val item = child.getValue(OrderModel::class.java)
                        item?.let { list.add(it) }
                    }
                    listData.value = list.sortedByDescending { it.createdAt }.toMutableList()
                }

                override fun onCancelled(error: DatabaseError) {
                    listData.value = mutableListOf()
                }
            })
        return listData
    }

    fun confirmPayment(
        orderId: String,
        onComplete: (OrderResult) -> Unit
    ) {
        val updates = mapOf<String, Any>(
            "status" to OrderStatus.PAID,
            "paymentStatus" to PaymentStatus.SUCCESS,
            "paidAt" to System.currentTimeMillis()
        )
        updateOrder(orderId, updates, onComplete)
    }

    fun completeOrder(
        orderId: String,
        onComplete: (OrderResult) -> Unit
    ) {
        val updates = mapOf<String, Any>(
            "status" to OrderStatus.COMPLETED,
            "completedAt" to System.currentTimeMillis()
        )
        updateOrder(orderId, updates, onComplete)
    }

    private fun updateOrder(
        orderId: String,
        updates: Map<String, Any>,
        onComplete: (OrderResult) -> Unit
    ) {
        firebaseDatabase.getReference("Orders")
            .child(orderId)
            .updateChildren(updates)
            .addOnSuccessListener {
                onComplete(OrderResult(success = true, orderId = orderId))
            }
            .addOnFailureListener { error ->
                val message = if (error.message?.contains("permission", ignoreCase = true) == true) {
                    "Firebase rules block write access to Orders"
                } else {
                    error.message ?: "Unknown Firebase error"
                }
                onComplete(OrderResult(success = false, errorMessage = message))
            }
    }

    fun saveItem(
        item: ItemModel,
        onComplete: (DataActionResult) -> Unit
    ) {
        val itemsRef = firebaseDatabase.getReference("Items")
        val itemRef = if (item.id.isBlank()) itemsRef.push() else itemsRef.child(item.id)
        val itemId = itemRef.key

        if (itemId.isNullOrBlank()) {
            onComplete(DataActionResult(success = false, errorMessage = "Unable to create item"))
            return
        }

        val payload = item.copy(
            id = itemId,
            picUrl = ArrayList(item.picUrl.filter { it.isNotBlank() })
        )

        itemRef.setValue(payload)
            .addOnSuccessListener {
                onComplete(DataActionResult(success = true, id = itemId))
            }
            .addOnFailureListener { error ->
                onComplete(DataActionResult(success = false, errorMessage = error.message ?: "Unable to save item"))
            }
    }

    fun deleteItem(
        itemId: String,
        onComplete: (DataActionResult) -> Unit
    ) {
        firebaseDatabase.getReference("Items")
            .child(itemId)
            .removeValue()
            .addOnSuccessListener {
                onComplete(DataActionResult(success = true, id = itemId))
            }
            .addOnFailureListener { error ->
                onComplete(DataActionResult(success = false, errorMessage = error.message ?: "Unable to delete item"))
            }
    }

    fun saveCategory(
        category: CategoryModel,
        onComplete: (DataActionResult) -> Unit
    ) {
        val categoryId = category.id.toString()
        firebaseDatabase.getReference("Category")
            .child(categoryId)
            .setValue(category)
            .addOnSuccessListener {
                onComplete(DataActionResult(success = true, id = categoryId))
            }
            .addOnFailureListener { error ->
                onComplete(DataActionResult(success = false, errorMessage = error.message ?: "Unable to save category"))
            }
    }

    fun deleteCategory(
        categoryId: Int,
        onComplete: (DataActionResult) -> Unit
    ) {
        firebaseDatabase.getReference("Category")
            .child(categoryId.toString())
            .removeValue()
            .addOnSuccessListener {
                onComplete(DataActionResult(success = true, id = categoryId.toString()))
            }
            .addOnFailureListener { error ->
                onComplete(DataActionResult(success = false, errorMessage = error.message ?: "Unable to delete category"))
            }
    }

    private fun ItemModel.withFirebaseId(key: String?): ItemModel {
        if (id.isBlank()) {
            id = key.orEmpty()
        }
        return this
    }
}
