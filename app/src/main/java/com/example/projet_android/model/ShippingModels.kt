package com.example.projet_android.model

data class ProductItem(
    val orderProductId: Int,      
    val orderId: Int,
    val orderNumber: String,
    val productId: Int,
    val productName: String,
    val weight: Double,
    val volume: Double,
    val price: Double,
    val isPriority: Boolean,
    val isFragile: Boolean
)

data class ContainerSlot(
    val container: Container,
    val assignedProducts: MutableList<ProductItem> = mutableListOf(),
    var usedWeight: Double = 0.0,
    var usedVolume: Double = 0.0,
    var status: SlotStatus = SlotStatus.PENDING
) {
    val fillRateWeight get() = usedWeight / container.maxWeight
    val fillRateVolume get() = usedVolume / container.maxVolume
    val fillRate get() = maxOf(fillRateWeight, fillRateVolume)
    val remainingWeight get() = container.maxWeight - usedWeight
    val remainingVolume get() = container.maxVolume - usedVolume
    val totalPrice get() = assignedProducts.sumOf { it.price }
    val hasPriorityItem get() = assignedProducts.any { it.isPriority }
    val willShip get() = fillRate >= container.fillThreshold
}

enum class SlotStatus { PENDING, SHIPPED, POSTPONED }

data class ShippingPlanResult(
    val shippedSlots: List<ContainerSlot>,
    val postponedSlots: List<ContainerSlot>,
    val waitingProducts: List<ProductItem>,
    val totalRevenue: Double,
    val totalProductsShipped: Int,
    val totalProductsPostponed: Int
)
