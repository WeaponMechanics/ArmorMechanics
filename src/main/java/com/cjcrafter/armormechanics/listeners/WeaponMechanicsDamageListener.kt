package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanicsAPI
import com.cjcrafter.armormechanics.events.ResistBulletDamageEvent
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class WeaponMechanicsDamageListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onDamage(event: WeaponDamageEntityEvent) {
        val damage = event.finalDamage

        val effects = ArmorMechanicsAPI.getBonusEffects(event.victim)
        var rate = 1.0
        for (effect in effects) {
            rate -= if (event.isExplosion)
                effect.getExplosionResistance(event.weaponTitle)
            else
                effect.getBulletResistance(event.weaponTitle)
        }

        val resistEvent = ResistBulletDamageEvent(event, rate)
        Bukkit.getPluginManager().callEvent(resistEvent)
        event.finalDamage = damage * resistEvent.rate
    }
}
