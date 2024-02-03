package com.cjcrafter.armormechanics.events

import org.bukkit.command.CommandSender
import org.bukkit.entity.LivingEntity
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent
import org.bukkit.inventory.ItemStack

class ArmorGenerateEvent(
    val sender: CommandSender,
    val entity: LivingEntity,
    val armor: ItemStack,
    val armorTitle: String,
    val data: Map<String, Any>,
) : EntityEvent(entity) {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
