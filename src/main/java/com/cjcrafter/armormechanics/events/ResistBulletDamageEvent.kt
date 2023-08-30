package com.cjcrafter.armormechanics.events

import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ResistBulletDamageEvent(
    val weaponDamageEvent: WeaponDamageEntityEvent,
    var rate: Double
) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
