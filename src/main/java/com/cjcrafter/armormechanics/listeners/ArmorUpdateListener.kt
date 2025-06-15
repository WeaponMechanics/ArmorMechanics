package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanics
import com.cjcrafter.armormechanics.ArmorMechanicsAPI
import me.deecaad.core.events.EntityEquipmentEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot

class ArmorUpdateListener : Listener {

    /**
     * To avoid infinite loops, we have to save a hashable [EntityEquipmentEvent]
     */
    private data class EntityEquipTimestamp(
        val slot: EquipmentSlot,
        val timestamp: Int
    )

    private val entityEquipTimestamps = mutableMapOf<LivingEntity, EntityEquipTimestamp>()

    @EventHandler
    fun onEquip(event: EntityEquipmentEvent) {
        if (!ArmorMechanics.getInstance().configuration.getBoolean("Update_Armor")) return
        if (!event.isEquipping) return
        val entity = event.entity as LivingEntity

        // We need to do this timestamp check to avoid infinite loops.
        val currentTick = event.entity.ticksLived
        val previousTimestamp = entityEquipTimestamps[entity]
        if (previousTimestamp != null && previousTimestamp.slot == event.slot && currentTick == previousTimestamp.timestamp) {
            // If the last time we updated this armor was less than 2 ticks ago,
            // then we don't need to update it again.
            return
        }

        // We want to update the armor, but unfortunately we cannot modify the
        // event since it contains a COPY of the armor. So, we need to check 1
        // tick after the event and check to update it. If no armor is in that
        // slot anymore, we can just assume the player has already removed it.
        ArmorMechanics.getInstance().foliaScheduler.entity(entity).runDelayed(Runnable {
            val equipment = entity.equipment!!
            val item = equipment.getItem(event.slot)

            // Either not ArmorMechanics armor, or just not any armor at
            // all. Either way, we don't need to update it.
            if (ArmorMechanicsAPI.getArmorTitle(item) == null) return@Runnable

            // Dupe protection, it is theoretically possible for a client
            // to swap out the item with a different armor. Not a very useful
            // dupe since it replaces the old armor, but still a potential bug
            if (event.equipped != item) return@Runnable

            // Save the timestamp so we can check it next time
            entityEquipTimestamps[entity] = EntityEquipTimestamp(event.slot, entity.ticksLived)

            ArmorMechanicsAPI.update(entity, item)
            equipment.setItem(event.slot, item)
        }, 1L)
    }
}