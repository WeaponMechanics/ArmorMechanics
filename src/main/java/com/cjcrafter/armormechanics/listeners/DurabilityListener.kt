package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.durability.DurabilityManager.changeDurability
import com.cjcrafter.durability.DurabilityManager.getDurability
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
        var totalDurability = 0
        val contents = event.inventory.contents.filter{ it != null && it.type != Material.AIR }
        if (contents.size != 2) {
            event.result = null
            return
        }
        val resultItem: ItemStack = event.result ?: return
        val titles = ArrayList<String>()
        contents.forEach { item ->
            if (item != null && item.type.maxDurability > 0) {
                val title =
                    CompatibilityAPI.getNBTCompatibility().getString(item, "ArmorMechanics", "armor-title")
                titles.add(title)
                totalDurability += getDurability(item)
            }
        }
        if (titles.distinct().size != 1) {
            event.result = null
            return
        }
        changeDurability(resultItem, totalDurability-getDurability(resultItem))
        event.result = resultItem
    }

    @EventHandler
    fun onRepairCraft(event: PrepareItemCraftEvent) {
        if (!event.isRepair) return
        var totalDurability = 0
        val contents = event.inventory.contents.drop(1).filter{ it != null && it.type != Material.AIR }
        val resultItem: ItemStack = contents[0].clone()
        val titles = ArrayList<String>()
        contents.forEach { item ->
            if (item != null && item.type.maxDurability > 0) {
                val title =
                    CompatibilityAPI.getNBTCompatibility().getString(item, "ArmorMechanics", "armor-title")
                titles.add(title)
                totalDurability += getDurability(item)
            }
        }
        if (titles.distinct().size != 1) {
            event.inventory.result = null
            return
        }
        changeDurability(resultItem, totalDurability - getDurability(resultItem))
        event.inventory.result = resultItem
    }
}