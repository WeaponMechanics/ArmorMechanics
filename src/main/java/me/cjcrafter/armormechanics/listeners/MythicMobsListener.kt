package me.cjcrafter.armormechanics.listeners

import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import me.cjcrafter.armormechanics.ArmorMechanics
import me.cjcrafter.armormechanics.lib.MythicMobsArmorDrop
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MythicMobsListener : Listener {
    init {
        ArmorMechanics.INSTANCE.debug.info("Hooking into MythicMobs")
    }

    @EventHandler
    fun onMythicDropLoad(event: MythicDropLoadEvent) {
        if (event.dropName.equals("armorMechanicsArmor", ignoreCase = true)) {
            event.register(MythicMobsArmorDrop(event.config, event.argument))
        }
    }
}
