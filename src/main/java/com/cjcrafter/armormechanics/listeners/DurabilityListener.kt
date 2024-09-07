package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanicsAPI
import com.cjcrafter.armormechanics.durability.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemDamageEvent

class DurabilityListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onDamageItem(event: PlayerItemDamageEvent) {
        ArmorMechanicsAPI.getArmorTitle(event.item) ?: return

        val customMaxDura = event.item.getCustomMaxDurability() ?: return
        val customDura = event.item.getCustomDurability()!!

        val newDura = customDura - event.damage
        val actualMaxDura = event.item.getMaxDurability()
        val actualDamage = event.item.getItemDamage()

        //we want to break the item
        if (newDura <= 0) {
            event.damage = actualMaxDura - actualDamage
            return
        }

        event.item.setCustomDurability(newDura)

        val newDamage = customMaxDura - newDura
        val actualTargetDamage = getFromProportion(getProportion(newDamage, customMaxDura), actualMaxDura)

        event.damage = actualTargetDamage - actualDamage
    }

}