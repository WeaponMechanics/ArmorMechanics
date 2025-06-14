package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanicsAPI
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPotionEffectEvent

class ImmunePotionCanceller : Listener {

    @EventHandler
    fun onPotion(event: EntityPotionEffectEvent) {
        val entity = event.entity
        if (entity !is LivingEntity)
            return

        for (effect in ArmorMechanicsAPI.getBonusEffects(entity)) {
            if (effect.immunities.contains(event.newEffect?.type)) {
                event.isCancelled = true
                return
            }
        }
    }
}
