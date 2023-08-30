package com.cjcrafter.armormechanics.events

import org.bukkit.entity.LivingEntity
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent
import org.bukkit.inventory.ItemStack

class ArmorUpdateEvent(
    val entity: LivingEntity,
    val armor: ItemStack,
    val armorTitle: String
) : EntityEvent(entity) {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
