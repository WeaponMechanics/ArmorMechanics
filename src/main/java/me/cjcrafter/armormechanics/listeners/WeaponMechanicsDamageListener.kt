package me.cjcrafter.armormechanics.listeners

import me.cjcrafter.armormechanics.ArmorMechanicsAPI
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent
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
        event.finalDamage = damage * rate
    }
}
