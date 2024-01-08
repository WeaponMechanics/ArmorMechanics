package com.cjcrafter.armormechanics.durability

import me.deecaad.core.placeholder.NumericPlaceholderHandler
import me.deecaad.core.placeholder.PlaceholderData
import me.deecaad.core.placeholder.PlaceholderHandler

fun registerDurabilityPlaceholders() {
    PlaceholderHandler.REGISTRY.add(object : DurabilityPlaceholderHandler() {})
    PlaceholderHandler.REGISTRY.add(object : MaxDurabilityPlaceholderHandler() {})
}
abstract class DurabilityPlaceholderHandler : NumericPlaceholderHandler("durability_current") {
    override fun requestValue(p0: PlaceholderData): Number? {
        return DurabilityManager.getDurability(p0.item()!!)
    }
}

abstract class MaxDurabilityPlaceholderHandler : NumericPlaceholderHandler("durability_max") {
    override fun requestValue(p0: PlaceholderData): Number? {
        return DurabilityManager.getMaxDurability(p0.item()!!)
    }
}