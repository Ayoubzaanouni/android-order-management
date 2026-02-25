package com.example.projet_android.util

import com.example.projet_android.model.Order
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

    fun generateOrders(companyId: Int, orderCount: Int): List<Order> {
        return (1..orderCount).map {
            generateOrder(companyId)
        }
    }

    private fun generateOrder(companyId: Int): Order {
        val totalWeight = randomDouble(min = 0.5, max = 1000.0, decimals = 2)
        val totalVolume = randomDouble(min = 0.1, max = 500.0, decimals = 2)
        val totalPrice  = randomDouble(min = 10.0, max = 50_000.0, decimals = 2)

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

