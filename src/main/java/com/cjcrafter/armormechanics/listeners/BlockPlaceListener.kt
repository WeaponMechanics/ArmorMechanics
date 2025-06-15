package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanics
import com.cjcrafter.armormechanics.ArmorMechanicsAPI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        val config = ArmorMechanics.getInstance().configuration
        if (!config.getBoolean("Prevent_Armor_Place", true))
            return

        val isArmor = ArmorMechanicsAPI.getArmorTitle(event.itemInHand) != null
        event.isCancelled = isArmor
    }
}
