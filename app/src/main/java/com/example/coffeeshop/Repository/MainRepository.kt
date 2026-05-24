package com.example.coffeeshop.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeshop.Domain.AuthResult
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.DataActionResult
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Domain.OrderItemModel
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.OrderResult
import com.example.coffeeshop.Domain.OrderStatus
import com.example.coffeeshop.Domain.PaymentStatus
import com.example.coffeeshop.Domain.ReportSummary
import com.example.coffeeshop.Domain.ReservationContext
import com.example.coffeeshop.Domain.ReservationModel
import com.example.coffeeshop.Domain.TableModel
import com.example.coffeeshop.Domain.UserProfileModel
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Domain.UserSession
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun signInCustomer(
        name: String,
        email: String,
        onComplete: (AuthResult) -> Unit
    ) {
        ensureDefaultUsers()
        val normalizedEmail = email.trim().lowercase()
        firebaseDatabase.getReference("Users")
            .orderByChild("email")
            .equalTo(normalizedEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                val existingUser = snapshot.children
                    .mapNotNull { it.getValue(UserProfileModel::class.java)?.copy(id = it.key.orEmpty()) }
                    .firstOrNull { it.role == UserRole.CUSTOMER }

                if (existingUser != null) {
                    val updatedProfile = existingUser.copy(name = name, email = normalizedEmail, active = true)
                    firebaseDatabase.getReference("Users").child(existingUser.id)
                        .setValue(updatedProfile)
                        .addOnSuccessListener {
                            onComplete(AuthResult(success = true, session = updatedProfile.toSession()))
                        }
                        .addOnFailureListener { error ->
                            onComplete(AuthResult(success = false, errorMessage = error.message ?: "Unable to save customer profile"))
                        }
                } else {
                    val userRef = firebaseDatabase.getReference("Users").push()
                    val userId = userRef.key.orEmpty()
                    if (userId.isBlank()) {
                        onComplete(AuthResult(success = false, errorMessage = "Unable to create customer profile"))
                        return@addOnSuccessListener
                    }
                    val newProfile = UserProfileModel(
                        id = userId,
                        name = name,
                        email = normalizedEmail,
                        role = UserRole.CUSTOMER,
                        active = true,
                        createdAt = System.currentTimeMillis()
                    )
                    userRef.setValue(newProfile)
                        .addOnSuccessListener {
                            onComplete(AuthResult(success = true, session = newProfile.toSession()))
                        }
                        .addOnFailureListener { error ->
                            onComplete(AuthResult(success = false, errorMessage = error.message ?: "Unable to create customer profile"))
                        }
                }
            }
            .addOnFailureListener { error ->
                onComplete(AuthResult(success = false, errorMessage = error.message ?: "Unable to access Users"))
            }
    }

    fun signInStaff(
        role: String,
        pin: String,
        displayName: String,
        onComplete: (AuthResult) -> Unit
    ) {
        ensureDefaultUsers()
        val userId = staffUserId(role)
        if (userId == null) {
            onComplete(AuthResult(success = false, errorMessage = "Unsupported role"))
            return
        }
        firebaseDatabase.getReference("Users").child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val storedProfile = snapshot.getValue(UserProfileModel::class.java)?.copy(id = snapshot.key.orEmpty())
                val profile = storedProfile ?: defaultStaffProfile(role)
                if (profile == null || !profile.active) {
                    onComplete(AuthResult(success = false, errorMessage = "Staff profile is unavailable"))
                    return@addOnSuccessListener
                }
                if (profile.pin != pin) {
                    onComplete(AuthResult(success = false, errorMessage = "Wrong PIN for selected role"))
                    return@addOnSuccessListener
                }

                val updatedProfile = if (displayName.isBlank()) profile else profile.copy(name = displayName)
                firebaseDatabase.getReference("Users").child(updatedProfile.id)
                    .setValue(updatedProfile)
                    .addOnSuccessListener {
                        onComplete(AuthResult(success = true, session = updatedProfile.toSession()))
                    }
                    .addOnFailureListener { error ->
                        onComplete(AuthResult(success = false, errorMessage = error.message ?: "Unable to update staff profile"))
                    }
            }
            .addOnFailureListener { error ->
                onComplete(AuthResult(success = false, errorMessage = error.message ?: "Unable to access staff profile"))
            }
    }

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
        reservationContext: ReservationContext?,
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
            reservationId = reservationContext?.reservationId.orEmpty(),
            reservedTableNumber = reservationContext?.tableNumber ?: 0,
            reservationTime = reservationContext?.reservationTime.orEmpty(),
            preorder = reservationContext != null,
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

    fun loadTables(): LiveData<MutableList<TableModel>> {
        val listData = MutableLiveData<MutableList<TableModel>>()
        ensureDefaultTables()
        firebaseDatabase.getReference("Tables")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<TableModel>()
                    for (child in snapshot.children) {
                        parseTable(child)?.let { list.add(it) }
                    }
                    listData.value = normalizeTables(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    listData.value = normalizeTables(emptyList())
                }
            })
        return listData
    }

    fun saveTable(table: TableModel, onComplete: (DataActionResult) -> Unit) {
        val tableRef = if (table.id.isBlank()) firebaseDatabase.getReference("Tables").push()
        else firebaseDatabase.getReference("Tables").child(table.id)
        val tableId = tableRef.key
        if (tableId.isNullOrBlank()) {
            onComplete(DataActionResult(false, errorMessage = "Unable to create table"))
            return
        }
        val payload = table.copy(id = tableId)
        tableRef.setValue(payload)
            .addOnSuccessListener { onComplete(DataActionResult(true, id = tableId)) }
            .addOnFailureListener { error ->
                onComplete(DataActionResult(false, errorMessage = error.message ?: "Unable to save table"))
            }
    }

    fun deleteTable(tableId: String, onComplete: (DataActionResult) -> Unit) {
        firebaseDatabase.getReference("Tables").child(tableId).removeValue()
            .addOnSuccessListener { onComplete(DataActionResult(true, id = tableId)) }
            .addOnFailureListener { error ->
                onComplete(DataActionResult(false, errorMessage = error.message ?: "Unable to delete table"))
            }
    }

    fun loadReservations(): LiveData<MutableList<ReservationModel>> {
        val listData = MutableLiveData<MutableList<ReservationModel>>()
        firebaseDatabase.getReference("Reservations")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ReservationModel>()
                    for (child in snapshot.children) {
                        val item = child.getValue(ReservationModel::class.java)
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

    fun createReservation(
        table: TableModel,
        reservationTime: String,
        session: UserSession,
        preorderRequested: Boolean,
        onComplete: (DataActionResult) -> Unit
    ) {
        val reservationRef = firebaseDatabase.getReference("Reservations").push()
        val reservationId = reservationRef.key
        if (reservationId.isNullOrBlank()) {
            onComplete(DataActionResult(false, errorMessage = "Unable to create reservation"))
            return
        }

        val reservation = ReservationModel(
            id = reservationId,
            customerId = session.userId,
            customerName = session.name,
            customerEmail = session.email,
            tableId = table.id,
            tableNumber = table.number,
            reservationTime = reservationTime,
            status = "Reserved",
            preorderRequested = preorderRequested,
            createdAt = System.currentTimeMillis()
        )

        reservationRef.setValue(reservation)
            .addOnSuccessListener {
                val tableUpdates = mapOf<String, Any>(
                    "status" to "Reserved",
                    "reservationId" to reservationId,
                    "reservedByName" to session.name,
                    "reservationTime" to reservationTime
                )
                firebaseDatabase.getReference("Tables").child(table.id).updateChildren(tableUpdates)
                onComplete(DataActionResult(true, id = reservationId))
            }
            .addOnFailureListener { error ->
                onComplete(DataActionResult(false, errorMessage = error.message ?: "Unable to reserve table"))
            }
    }

    fun cancelReservation(
        reservation: ReservationModel,
        onComplete: (DataActionResult) -> Unit
    ) {
        val reservationUpdates = mapOf<String, Any>(
            "status" to "Cancelled"
        )
        firebaseDatabase.getReference("Reservations").child(reservation.id)
            .updateChildren(reservationUpdates)
            .addOnSuccessListener {
                val tableUpdates = mapOf<String, Any>(
                    "status" to "Free",
                    "reservationId" to "",
                    "reservedByName" to "",
                    "reservationTime" to ""
                )
                firebaseDatabase.getReference("Tables").child(reservation.tableId)
                    .updateChildren(tableUpdates)
                onComplete(DataActionResult(true, id = reservation.id))
            }
            .addOnFailureListener { error ->
                onComplete(DataActionResult(false, errorMessage = error.message ?: "Unable to cancel reservation"))
            }
    }

    fun buildReportSummary(orders: List<OrderModel>): ReportSummary {
        val totalRevenue = orders.filter { it.status == OrderStatus.COMPLETED || it.status == OrderStatus.PAID }
            .sumOf { it.totalAmount }
        val totalOrders = orders.size
        val averageBill = if (totalOrders == 0) 0.0 else totalRevenue / totalOrders
        val completedOrders = orders.count { it.status == OrderStatus.COMPLETED }
        val pendingOrders = orders.count { it.status == OrderStatus.PENDING }
        val paidOrders = orders.count { it.status == OrderStatus.PAID }
        return ReportSummary(totalRevenue, totalOrders, averageBill, completedOrders, pendingOrders, paidOrders)
    }

    private fun ensureDefaultTables() {
        val ref = firebaseDatabase.getReference("Tables")
        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.childrenCount > 0L) return@addOnSuccessListener
            val defaults = (1..6).associate { index ->
                val id = "table_$index"
                id to TableModel(
                    id = id,
                    number = index,
                    qrCode = "table-$index",
                    status = "Free"
                )
            }
            ref.updateChildren(defaults)
        }
    }

    private fun ensureDefaultUsers() {
        val ref = firebaseDatabase.getReference("Users")
        ref.get().addOnSuccessListener { snapshot ->
            val updates = mutableMapOf<String, Any>()
            defaultStaffProfiles().forEach { profile ->
                if (!snapshot.hasChild(profile.id)) {
                    updates[profile.id] = profile
                }
            }
            if (updates.isNotEmpty()) {
                ref.updateChildren(updates)
            }
        }
    }

    private fun parseTable(child: DataSnapshot): TableModel? {
        val directModel = child.getValue(TableModel::class.java)
        if (directModel != null) {
            val resolvedId = directModel.id.ifBlank { child.key.orEmpty() }
            val resolvedNumber = if (directModel.number > 0) directModel.number else inferTableNumber(child.key)
            return directModel.copy(
                id = resolvedId,
                number = resolvedNumber,
                status = directModel.status.ifBlank { "Free" }
            )
        }

        val raw = child.value as? Map<*, *> ?: return null
        val number = (raw["number"] as? Number)?.toInt()
            ?: (raw["number"] as? String)?.toIntOrNull()
            ?: inferTableNumber(child.key)

        return TableModel(
            id = (raw["id"] as? String).orEmpty().ifBlank { child.key.orEmpty() },
            number = number,
            qrCode = (raw["qrCode"] as? String).orEmpty(),
            status = (raw["status"] as? String).orEmpty().ifBlank { "Free" },
            reservationId = (raw["reservationId"] as? String).orEmpty(),
            reservedByName = (raw["reservedByName"] as? String).orEmpty(),
            reservationTime = (raw["reservationTime"] as? String).orEmpty()
        )
    }

    private fun normalizeTables(existingTables: List<TableModel>): MutableList<TableModel> {
        val byNumber = existingTables
            .filter { it.number in 1..6 }
            .associateBy { it.number }

        return (1..6).map { index ->
            byNumber[index]?.copy(
                id = byNumber[index]?.id?.ifBlank { "table_$index" }.orEmpty(),
                number = index,
                status = byNumber[index]?.status?.ifBlank { "Free" }.orEmpty().ifBlank { "Free" }
            ) ?: TableModel(
                id = "table_$index",
                number = index,
                qrCode = "table-$index",
                status = "Free"
            )
        }.toMutableList()
    }

    private fun inferTableNumber(key: String?): Int {
        return key
            ?.substringAfterLast('_', "")
            ?.toIntOrNull()
            ?: 0
    }

    private fun defaultStaffProfiles(): List<UserProfileModel> {
        val now = System.currentTimeMillis()
        return listOf(
            UserProfileModel(
                id = "staff_cashier",
                name = "Cashier",
                email = "cashier@coffeeshop.local",
                role = UserRole.CASHIER,
                pin = "1111",
                active = true,
                createdAt = now
            ),
            UserProfileModel(
                id = "staff_barista",
                name = "Barista",
                email = "barista@coffeeshop.local",
                role = UserRole.BARISTA,
                pin = "2222",
                active = true,
                createdAt = now
            ),
            UserProfileModel(
                id = "staff_admin",
                name = "Administrator",
                email = "admin@coffeeshop.local",
                role = UserRole.ADMIN,
                pin = "3333",
                active = true,
                createdAt = now
            )
        )
    }

    private fun defaultStaffProfile(role: String): UserProfileModel? {
        return defaultStaffProfiles().firstOrNull { it.role == role }
    }

    private fun staffUserId(role: String): String? {
        return when (role) {
            UserRole.CASHIER -> "staff_cashier"
            UserRole.BARISTA -> "staff_barista"
            UserRole.ADMIN -> "staff_admin"
            else -> null
        }
    }

    private fun UserProfileModel.toSession(): UserSession {
        return UserSession(
            userId = id,
            role = role,
            name = name,
            email = email
        )
    }

    private fun ItemModel.withFirebaseId(key: String?): ItemModel {
        if (id.isBlank()) {
            id = key.orEmpty()
        }
        return this
    }
}
