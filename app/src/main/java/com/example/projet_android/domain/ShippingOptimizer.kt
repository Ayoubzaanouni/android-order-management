package com.example.projet_android.domain

import com.example.projet_android.model.Container
import com.example.projet_android.model.ContainerSlot
import com.example.projet_android.model.ProductItem
import com.example.projet_android.model.ShippingPlanResult
import com.example.projet_android.model.SlotStatus

class ShippingOptimizer(
    private val containers: List<Container>,
    private val weightFillScore: Double = 0.4,
    private val priceDensityScore: Double = 0.4,
    private val wasteScore: Double = 0.2
) {
    private var maxPriceDensity: Double = 1.0

    fun optimize(products: List<ProductItem>): ShippingPlanResult {
        if (containers.isEmpty() || products.isEmpty()) {
            return ShippingPlanResult(emptyList(), emptyList(), products, 0.0, 0, products.size)
        }

        val maxContainerWeight = containers.maxOfOrNull { it.maxWeight } ?: 1.0
        val maxContainerVolume = containers.maxOfOrNull { it.maxVolume } ?: 1.0
        
        val priceDensities = products.map { p ->
            val wRatio = p.weight / maxContainerWeight
            val vRatio = p.volume / maxContainerVolume
            p.price / (wRatio + vRatio + 0.0001)
        }.sorted()

        maxPriceDensity = if (priceDensities.isNotEmpty()) {
            priceDensities[(priceDensities.size * 0.9).toInt().coerceAtMost(priceDensities.lastIndex)]
        } else {
            1.0
        }

        if (maxPriceDensity <= 0.0) maxPriceDensity = 1.0

        // --- STEP 0: Separate & Sort ---
        val priority = products.filter { it.isPriority }
        
        val normal = if (weightFillScore > priceDensityScore) {
            products.filter { !it.isPriority }
                .groupBy { it.orderId }
                .values
                .sortedByDescending { orderProducts ->
                    val oWeight = orderProducts.sumOf { it.weight } / maxContainerWeight
                    val oVolume = orderProducts.sumOf { it.volume } / maxContainerVolume
                    val combinedSize = oWeight + oVolume + 0.0001
                    orderProducts.size / combinedSize
                }
                .flatten()
        } else {
            products.filter { !it.isPriority }
                .sortedByDescending { p ->
                    val wRatio = p.weight / maxContainerWeight
                    val vRatio = p.volume / maxContainerVolume
                    p.price / (wRatio + vRatio + 0.0001)
                }
        }

        val slots = containers.map { ContainerSlot(it) }.toMutableList()
        val waiting = mutableListOf<ProductItem>()

        // --- STEP 1: Place Priority products first ---
        for (p in priority) {
            val slot = slots.firstOrNull { fits(p, it) }
            if (slot != null) {
                assign(p, slot)
            } else {
                waiting.add(p)
            }
        }

        // --- STEP 2 & 3: Greedy best-fit for normal products ---
        for (p in normal) {
            val best = slots
                .filter { fits(p, it) }
                .maxByOrNull { score(p, it) }

            if (best != null) {
                assign(p, best)
            } else {
                waiting.add(p)
            }
        }

        // --- STEP 4: Evaluate fill rates ---
        for (slot in slots) {
            slot.status = if (slot.willShip) SlotStatus.SHIPPED else SlotStatus.POSTPONED
        }
        localSearch(slots.filter { it.status == SlotStatus.SHIPPED }, waiting)

        val shippedSlots = slots.filter { it.status == SlotStatus.SHIPPED && it.assignedProducts.isNotEmpty() }
        val postponedSlots = slots.filter { it.status == SlotStatus.POSTPONED && it.assignedProducts.isNotEmpty() }

        return ShippingPlanResult(
            shippedSlots = shippedSlots,
            postponedSlots = postponedSlots,
            waitingProducts = waiting,
            totalRevenue = shippedSlots.sumOf { it.totalPrice },
            totalProductsShipped = shippedSlots.sumOf { it.assignedProducts.size },
            totalProductsPostponed = postponedSlots.sumOf { it.assignedProducts.size } + waiting.size
        )
    }

    private fun fits(product: ProductItem, slot: ContainerSlot): Boolean {
        return (slot.usedWeight + product.weight <= slot.container.maxWeight) &&
               (slot.usedVolume + product.volume <= slot.container.maxVolume)
    }

    private fun score(product: ProductItem, slot: ContainerSlot): Double {
        val remW = slot.remainingWeight
        val remV = slot.remainingVolume

        // How much of the remaining space does this product use?
        val wRatio = product.weight / slot.container.maxWeight
        val vRatio = product.volume / slot.container.maxVolume

        val fillContrib = (wRatio + vRatio) / 2.0

        val wasteW = (remW - product.weight) / slot.container.maxWeight
        val wasteV = (remV - product.volume) / slot.container.maxVolume
        val waste = (wasteW + wasteV) / 2.0

        val pricePerSpace = product.price / (wRatio + vRatio + 0.0001)

        return (weightFillScore * fillContrib) + 
               (priceDensityScore * (pricePerSpace / maxPriceDensity)) - 
               (wasteScore * waste)
    }

    private fun valueDensity(p: ProductItem): Double {
        val wNorm = p.weight / (containers.firstOrNull()?.maxWeight ?: 1.0)
        val vNorm = p.volume / (containers.firstOrNull()?.maxVolume ?: 1.0)
        return p.price / (wNorm + vNorm + 0.0001)
    }

    private fun assign(p: ProductItem, slot: ContainerSlot) {
        slot.assignedProducts.add(p)
        slot.usedWeight += p.weight
        slot.usedVolume += p.volume
    }

    private fun remove(p: ProductItem, slot: ContainerSlot) {
        slot.assignedProducts.remove(p)
        slot.usedWeight -= p.weight
        slot.usedVolume -= p.volume
    }

    private fun localSearch(shippedSlots: List<ContainerSlot>, waiting: MutableList<ProductItem>) {
        var improved = true
        var iterations = 0
        val maxIterations = 50

        while (improved && iterations < maxIterations) {
            improved = false
            iterations++

            // Try to pull items from waiting list if they fit into existing shipped slots
            val iterator = waiting.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                val bestSlot = shippedSlots.filter { fits(p, it) }.maxByOrNull { score(p, it) }
                if (bestSlot != null) {
                    assign(p, bestSlot)
                    iterator.remove()
                    improved = true
                }
            }

            // 2swap: exchange 1 product from slot A with 1 product from slot B to improve total value
            for (i in shippedSlots.indices) {
                for (j in i + 1 until shippedSlots.size) {
                    val slotA = shippedSlots[i]
                    val slotB = shippedSlots[j]

                    for (pA in slotA.assignedProducts.toList()) {
                        for (pB in slotB.assignedProducts.toList()) {
                            // Can't swap if either is priority
                            if (pA.isPriority || pB.isPriority) continue
                            val fitsA = slotA.usedWeight - pA.weight + pB.weight <= slotA.container.maxWeight &&
                                        slotA.usedVolume - pA.volume + pB.volume <= slotA.container.maxVolume
                                        
                            val fitsB = slotB.usedWeight - pB.weight + pA.weight <= slotB.container.maxWeight &&
                                        slotB.usedVolume - pB.volume + pA.volume <= slotB.container.maxVolume

                            if (fitsA && fitsB) {
                                // check if swap improves something
                                val currentFill = slotA.fillRate + slotB.fillRate
                                remove(pA, slotA)
                                remove(pB, slotB)
                                assign(pB, slotA)
                                assign(pA, slotB)
                                
                                val newFill = slotA.fillRate + slotB.fillRate
                                
                                if (newFill > currentFill) {
                                    improved = true
                                    // keep swap
                                } else {
                                    // undo swap
                                    remove(pB, slotA)
                                    remove(pA, slotB)
                                    assign(pA, slotA)
                                    assign(pB, slotB)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
