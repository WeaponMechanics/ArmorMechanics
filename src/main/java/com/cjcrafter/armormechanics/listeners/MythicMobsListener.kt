package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanics
import com.cjcrafter.armormechanics.lib.MythicMobsArmorDrop
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MythicMobsListener : Listener {
    init {
        ArmorMechanics.getInstance().debugger.info("Hooking into MythicMobs")
    }

    @EventHandler
    fun onMythicDropLoad(event: MythicDropLoadEvent) {
        if (event.dropName.equals("armorMechanicsArmor", ignoreCase = true)) {
            event.register(MythicMobsArmorDrop(event.config, event.argument))
        }
    }
}
