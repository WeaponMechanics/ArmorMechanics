package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.durability.DurabilityManager.changeDurability
import com.cjcrafter.armormechanics.durability.DurabilityManager.getDurability
import me.deecaad.core.compatibility.CompatibilityAPI
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerItemMendEvent
import org.bukkit.inventory.ItemStack

class DurabilityListener : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onItemDamaged(event: PlayerItemDamageEvent) {
        changeDurability(event.item, -event.damage)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onItemMend(event: PlayerItemMendEvent) {
        changeDurability(event.item, event.repairAmount)
    }

    @EventHandler
    fun onRepairAnvil(event: PrepareAnvilEvent) {
        val (names, totalDurability, result) = namesTotalDurabilityAndResult(event.inventory.contents) ?: return // Returns if ArmorMechanics is not involved
        if (names.size != 1) {
            event.result = null // We can set result to null because we are sure about ArmorMechanics is involved
            return
        }
        changeDurability(result, totalDurability - getDurability(result))
        event.result = result
    }

    @EventHandler
    fun onRepairCraft(event: PrepareItemCraftEvent) {
        if (!event.isRepair) return
        val (names, totalDurability, result) = namesTotalDurabilityAndResult(event.inventory.matrix) ?: return // Returns if ArmorMechanics is not involved
        if (names.size != 1) {
            event.inventory.result = null // We can set result to null because we are sure about ArmorMechanics is involved
            return
        }
        changeDurability(result, totalDurability - getDurability(result))
        event.inventory.result = result
    }

    private fun namesTotalDurabilityAndResult(items: Array<ItemStack?>): Triple<Set<String>, Int, ItemStack>? {
        var totalDurability = 0
        val names = mutableSetOf<String>()
        var resultItem: ItemStack? = null
        for (item in items) {
            if (item == null || item.type == Material.AIR) continue
            val durability = getDurability(item)
            if (durability == -1) return null // An unknown item is involved
            if (resultItem == null) resultItem = item.clone()
            totalDurability += durability
            val name = CompatibilityAPI.getNBTCompatibility().getString(item, "ArmorMechanics", "armor-title") ?: return null // Returns if ArmorMechanics is not involved
            names += name
        }
        if (resultItem == null) return null // None of the items have custom durability
        return Triple(names, totalDurability, resultItem)
    }
}