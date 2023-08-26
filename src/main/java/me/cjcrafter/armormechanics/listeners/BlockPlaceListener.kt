package me.cjcrafter.armormechanics.listeners

import me.cjcrafter.armormechanics.ArmorMechanics
import me.cjcrafter.armormechanics.ArmorMechanicsAPI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        if (!ArmorMechanics.INSTANCE.getConfig().getBoolean("Prevent_Armor_Place", true))
            return

        val isArmor = ArmorMechanicsAPI.getArmorTitle(event.itemInHand) != null
        event.isCancelled = isArmor
    }
}
