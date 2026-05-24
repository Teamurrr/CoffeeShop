package com.example.coffeeshop.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Domain.OrderItemModel
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.OrderResult
import com.example.coffeeshop.Domain.OrderStatus
import com.example.coffeeshop.Domain.PaymentStatus
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

    private fun ItemModel.withFirebaseId(key: String?): ItemModel {
        if (id.isBlank()) {
            id = key.orEmpty()
        }
        return this
    }
}
