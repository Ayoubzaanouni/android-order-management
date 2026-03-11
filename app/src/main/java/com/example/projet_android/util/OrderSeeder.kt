package com.example.projet_android.util

import com.example.projet_android.model.Order
import com.example.projet_android.model.Product
import java.util.Calendar
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

object OrderSeeder {

    private val orderStatuses = listOf(
        "PENDING",
        "PROCESSING",
        "SHIPPED",
        "DELIVERED",
        "CANCELLED"
    )

    private val random = Random(System.currentTimeMillis())

    fun generateOrders(companyId: Int, orderCount: Int): List<Pair<Order, List<Product>>> {
        return (1..orderCount).map {
            val products = generateProducts()
            val totalWeight = products.sumOf { it.weight }.round(2)
            val totalVolume = products.sumOf { it.volume }.round(3)
            val totalPrice = products.sumOf { it.price }.round(2)

            generateOrder(companyId, totalWeight, totalVolume, totalPrice) to products
        }
    }

    private fun Double.round(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return (this * factor).roundToInt() / factor
    }

    private fun generateProducts(): List<Product> {
        val productCount = random.nextInt(1, 6)
        return (1..productCount).map { generateProduct() }
    }

    private fun generateProduct(): Product {
        val names = listOf("Laptop", "Smartphone", "Tablet", "Monitor", "Keyboard", "Mouse", "Chair", "Desk", "Lamp", "Headphones")
        return Product(
            name = "${names.random(random)} ${random.nextInt(100, 999)}",
            weight = randomDouble(0.1, 20.0, 2),
            volume = randomDouble(0.01, 1.0, 3),
            price = randomDouble(5.0, 2000.0, 2),
            fragility = random.nextBoolean()
        )
    }

    private fun generateOrder(companyId: Int, totalWeight: Double, totalVolume: Double, totalPrice: Double): Order {

        return Order(
            companyId   = companyId,
            orderNumber = generateOrderNumber(),
            totalWeight = totalWeight,
            totalVolume = totalVolume,
            totalPrice  = totalPrice,
            priority    = (1..5).random(random),
            status      = orderStatuses.random(random),
            createdAt   = randomPastTimestamp()
        )
    }

    private fun generateOrderNumber(): String {
        val prefix  = "ORD"
        val year    = Calendar.getInstance().get(Calendar.YEAR)
        val digits  = (10_000..99_999).random(random)
        return "$prefix-$year-$digits"
    }

    private fun randomDouble(min: Double, max: Double, decimals: Int): Double {
        val raw = min + (max - min) * random.nextDouble()
        val factor = 10.0.pow(decimals)
        return (raw * factor).roundToInt() / factor
    }

    private fun randomPastTimestamp(): Long {
        // Random date within the past year
        val oneYearInMillis = 365L * 24 * 60 * 60 * 1000
        return System.currentTimeMillis() - (random.nextLong() % oneYearInMillis).absoluteValue
    }
}

