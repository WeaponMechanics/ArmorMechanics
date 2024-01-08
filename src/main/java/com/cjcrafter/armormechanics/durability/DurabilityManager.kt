package com.cjcrafter.armormechanics.durability

import com.cjcrafter.armormechanics.ArmorMechanics
import me.deecaad.core.compatibility.CompatibilityAPI
import me.deecaad.core.lib.adventure.text.serializer.legacy.LegacyComponentSerializer
import me.deecaad.core.placeholder.PlaceholderData
import me.deecaad.core.placeholder.PlaceholderMessage
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

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
            if ((damageable as ItemMeta).hasLore()) {
                val lore: ArrayList<String> = ArrayList((damageable as ItemMeta).lore!!)
                val loreClone = lore.toMutableList()
                val index = loreClone.indexOfFirst { it.startsWith(ArmorMechanics.DURABILITY_PREFIX) }
                val placeholderMessage =  PlaceholderMessage(ArmorMechanics.DURABILITY_FORMAT)
                val component = placeholderMessage.replaceAndDeserialize(PlaceholderData.of(null, item, null, null))
                val legacy = LegacyComponentSerializer.legacySection().serialize(component)
                loreClone[index] = ArmorMechanics.DURABILITY_PREFIX + legacy
                (damageable as ItemMeta).lore = loreClone
            } else {
                (damageable as ItemMeta).lore = listOf(
                    ArmorMechanics.DURABILITY_PREFIX + LegacyComponentSerializer.legacySection().serialize(
                        PlaceholderMessage(
                            ArmorMechanics.DURABILITY_FORMAT
                        ).replaceAndDeserialize(PlaceholderData.of(null, item, null, null))
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