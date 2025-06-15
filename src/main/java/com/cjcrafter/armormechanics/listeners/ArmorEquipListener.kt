package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanics
import com.cjcrafter.armormechanics.ArmorMechanicsAPI
import com.cjcrafter.armormechanics.BonusEffect
import com.cjcrafter.armormechanics.events.ArmorMechanicsDequipEvent
import com.cjcrafter.armormechanics.events.ArmorMechanicsEquipEvent
import me.deecaad.core.compatibility.CompatibilityAPI
import me.deecaad.core.events.EntityEquipmentEvent
import me.deecaad.core.mechanics.CastData
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import kotlin.text.get

class ArmorEquipListener : Listener {

    @EventHandler
    fun onEquip(event: EntityEquipmentEvent) {
        if (event.isDequipping && event.isArmor) dequip(event)
        if (event.isEquipping && event.isArmor) equip(event)
    }

    fun equip(event: EntityEquipmentEvent) {
        val entity = event.entity as LivingEntity
        val equipment = entity.equipment
        val item = event.equipped
        val title = CompatibilityAPI.getNBTCompatibility().getString(item, "ArmorMechanics", "armor-title")

        // When the equipped armor is not from ArmorMechanics, skip
        if (title == null || title.isEmpty()) return
        val bonus = ArmorMechanics.getInstance().armorConfigurations.get<BonusEffect>("$title.Bonus_Effects")

        // Determine which set they will be wearing after the armor is equipped
        var helmet = equipment!!.helmet
        var chestplate = equipment.chestplate
        var leggings = equipment.leggings
        var boots = equipment.boots
        when (event.slot) {
            EquipmentSlot.HEAD -> helmet = event.equipped
            EquipmentSlot.CHEST -> chestplate = event.equipped
            EquipmentSlot.LEGS -> leggings = event.equipped
            EquipmentSlot.FEET -> boots = event.equipped
            else -> throw IllegalArgumentException("impossible")
        }

        val equipEvent = ArmorMechanicsEquipEvent(entity, item, title, bonus?.equipMechanics, ArrayList(bonus?.potions ?: listOf()))
        Bukkit.getPluginManager().callEvent(equipEvent)
        equipEvent.equipMechanics?.use(CastData(entity, title, item))
        for (potion in equipEvent.potions)
            entity.addPotionEffect(potion)

        val set = ArmorMechanicsAPI.getSet(helmet, chestplate, leggings, boots)
        if (set?.bonus != null) {
            set.bonus.equipMechanics?.use(CastData(entity, title, item))
            for (potion in set.bonus.potions)
                entity.addPotionEffect(potion)
        }
    }

    fun dequip(event: EntityEquipmentEvent) {
        val entity = event.entity as LivingEntity
        val item = event.dequipped
        val title = ArmorMechanicsAPI.getArmorTitle(item)

        // When the equipped armor is not from ArmorMechanics, skip
        if (title == null || title.isEmpty()) return
        val bonus = ArmorMechanics.getInstance().armorConfigurations.get<BonusEffect>("$title.Bonus_Effects")

        val dequipEvent = ArmorMechanicsDequipEvent(entity, item, title, bonus?.dequipMechanics, bonus?.potions?.toMutableList() ?: mutableListOf())
        Bukkit.getPluginManager().callEvent(dequipEvent)

        dequipEvent.dequipMechanics?.use(CastData(entity, title, item))
        for (potion in dequipEvent.potions)
            entity.removePotionEffect(potion.type)

        // Set bonus is a little weird, as we need to check if the user
        // previously had a set bonus, and if it needs to be removed.
        // This code is done weirdly (by setting the changed armor both
        // for old set and new set) for support with <1.21.5 versions,
        // which bukkit's EntityEquipment did not update instantly
        val equipment = entity.equipment!!
        var helmet = ArmorMechanicsAPI.getArmorTitle(equipment.helmet)
        var chestplate = ArmorMechanicsAPI.getArmorTitle(equipment.chestplate)
        var leggings = ArmorMechanicsAPI.getArmorTitle(equipment.leggings)
        var boots = ArmorMechanicsAPI.getArmorTitle(equipment.boots)
        when (event.slot) {
            EquipmentSlot.HEAD -> helmet = title
            EquipmentSlot.CHEST -> chestplate = title
            EquipmentSlot.LEGS -> leggings = title
            EquipmentSlot.FEET -> boots = title
            else -> throw IllegalArgumentException("impossible")
        }
        val oldSet = ArmorMechanicsAPI.getSet(helmet, chestplate, leggings, boots) ?: return
        when (event.slot) {
            EquipmentSlot.HEAD -> helmet = null
            EquipmentSlot.CHEST -> chestplate = null
            EquipmentSlot.LEGS -> leggings = null
            EquipmentSlot.FEET -> boots = null
            else -> throw IllegalArgumentException("impossible")
        }

        val newSet = ArmorMechanicsAPI.getSet(helmet, chestplate, leggings, boots)
        if (oldSet !== newSet) {
            oldSet.bonus.dequipMechanics?.use(CastData(entity, title, item))
            for (potion in oldSet.bonus.potions)
                entity.removePotionEffect(potion.type)
        }
    }
}
