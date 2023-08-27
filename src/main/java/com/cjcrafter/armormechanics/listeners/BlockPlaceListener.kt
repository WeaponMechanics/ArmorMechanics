package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanics
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        if (!ArmorMechanics.INSTANCE.getConfig().getBoolean("Prevent_Armor_Place", true))
            return

        val isArmor = com.cjcrafter.armormechanics.ArmorMechanicsAPI.getArmorTitle(event.itemInHand) != null
        event.isCancelled = isArmor
    }
}
