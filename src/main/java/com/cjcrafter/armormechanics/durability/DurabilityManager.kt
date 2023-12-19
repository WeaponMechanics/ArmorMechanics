package com.cjcrafter.armormechanics.durability

import com.cjcrafter.armormechanics.ArmorMechanics
import me.deecaad.core.compatibility.CompatibilityAPI
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
object DurabilityManager {
    fun changeDurability(item: ItemStack, change: Int) {
        if (getDurability(item) == -1) {
            return
        }
        var durability = getDurability(item)
        val maxDurability = getMaxDurability(item)
        durability = Math.min(durability + change, maxDurability)
        setDurability(item, durability)
        val damageable = item.itemMeta
        if (damageable !is Damageable) {
            return
        }

        if (durability > 0) {
            damageable.damage = item.type.maxDurability - (durability * item.type.maxDurability) / maxDurability
            if (damageable.hasLore()) {
                val lore: ArrayList<String> = ArrayList(damageable.getLore()!!)
                for (i in lore.indices) {
                    if (lore[i].startsWith(ArmorMechanics.DURABILITY_PREFIX)) {
                        lore[i] = java.lang.String.format(ArmorMechanics.DURABILITY_FORMAT, durability, maxDurability)
                    }
                }
                damageable.setLore(lore)
            } else {
                damageable.setLore(
                    listOf(
                        java.lang.String.format(
                            ArmorMechanics.DURABILITY_FORMAT,
                            durability,
                            maxDurability
                        )
                    )
                )
            }
        } else {
            damageable.damage = item.type.maxDurability.toInt()
            item.amount = 0
        }
        item.itemMeta = damageable
    }

    fun getMaxDurability(item: ItemStack): Int {
        return if (CompatibilityAPI.getNBTCompatibility().hasInt(item, "ArmorMechanics", "armor-durability-max")) {
            CompatibilityAPI.getNBTCompatibility().getInt(item, "ArmorMechanics", "armor-durability-max")
        } else -1
    }

    fun getDurability(item: ItemStack): Int {
        return if (CompatibilityAPI.getNBTCompatibility().hasInt(item, "ArmorMechanics", "armor-durability")) {
            CompatibilityAPI.getNBTCompatibility().getInt(item, "ArmorMechanics", "armor-durability")
        } else -1
    }

    fun setMaxDurability(item: ItemStack, value: Int) {
        CompatibilityAPI.getNBTCompatibility().setInt(item, "ArmorMechanics", "armor-durability-max", value)
    }

    fun setDurability(item: ItemStack, value: Int) {
        CompatibilityAPI.getNBTCompatibility().setInt(item, "ArmorMechanics", "armor-durability", value)
    }
}