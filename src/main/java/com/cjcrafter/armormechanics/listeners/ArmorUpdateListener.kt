package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanics
import com.cjcrafter.armormechanics.ArmorMechanicsAPI
import me.deecaad.core.events.EntityEquipmentEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ArmorUpdateListener : Listener {

    @EventHandler
    fun onEquip(event: EntityEquipmentEvent) {
        if (!ArmorMechanics.INSTANCE.getConfig().getBoolean("Update_Armor")) return
        if (!event.isEquipping) return
        val entity = event.entity as LivingEntity

        // We want to update the armor, but unfortunately we cannot modify the
        // event since it contains a COPY of the armor. So, we need to check 1
        // tick after the event and check to update it. If no armor is in that
        // slot anymore, we can just assume the player has already removed it.
        ArmorMechanics.INSTANCE.scheduler.entity(entity).runDelayed(Runnable {
            val equipment = entity.equipment
            val item = ArmorMechanicsAPI.getItem(equipment!!, event.slot)

            // Either not ArmorMechanics armor, or just not any armor at
            // all. Either way, we don't need to update it.
            if (ArmorMechanicsAPI.getArmorTitle(item) == null) return@Runnable

            // Dupe protection, it is theoretically possible for a client
            // to swap out the item with a different armor. Not a very useful
            // dupe since it replaces the old armor, but still a potential bug
            if (event.equipped != item) return@Runnable

            ArmorMechanicsAPI.update(entity, item!!)
            ArmorMechanicsAPI.setItem(equipment, event.slot, item)
        }, 1L)
    }
}