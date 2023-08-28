package com.cjcrafter.armormechanics.listeners

import me.deecaad.core.mechanics.CastData
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class DamageMechanicListener : Listener {

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity !is LivingEntity) return

        for (effect in com.cjcrafter.armormechanics.ArmorMechanicsAPI.getBonusEffects(entity)) {
            effect.damageMechanics?.use(CastData(entity, null, null))
        }
    }
}
