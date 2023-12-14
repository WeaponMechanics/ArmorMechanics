package com.cjcrafter.armormechanics.durability

import com.cjcrafter.armormechanics.ArmorMechanics
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.util.List

object DurabilityManager {
    private val DURABILITY: NamespacedKey = NamespacedKey(ArmorMechanics.INSTANCE, "armor-durability")
    private val DURABILITY_MAX: NamespacedKey = NamespacedKey(ArmorMechanics.INSTANCE, "armor-durability-max")
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



            damageable.damage = item.type.maxDurability - (durability*item.type.maxDurability)/maxDurability
            if (damageable.hasLore()) {
                val lore: ArrayList<String> = ArrayList<String>(damageable.getLore())
                for (i in lore.indices) {
                    if (lore[i].startsWith(ArmorMechanics.DURABILITY_PRE)) {
                        lore[i] = java.lang.String.format(ArmorMechanics.DURABILITY, durability, maxDurability)
                    }
                }
                damageable.setLore(lore)
            } else {
                damageable.setLore(
                    List.of(
                        java.lang.String.format(
                            ArmorMechanics.DURABILITY,
                            durability,
                            maxDurability
                        )
                    )
                )
            }
        } else {
            damageable.setDamage(item.getType().getMaxDurability().toInt())
            item.setAmount(0)
        }
        item.setItemMeta(damageable)
    }

    fun getMaxDurability(item: ItemStack): Int {
        val meta: ItemMeta = item.getItemMeta()!!
        return meta.getPersistentDataContainer().getOrDefault<Int, Int>(DURABILITY_MAX, PersistentDataType.INTEGER, -1)
    }

    fun getDurability(item: ItemStack): Int {
        val meta: ItemMeta = item.getItemMeta()!!
        return meta.getPersistentDataContainer().getOrDefault<Int, Int>(DURABILITY, PersistentDataType.INTEGER, -1)
    }

    fun setMaxDurability(item: ItemStack, value: Int) {
        val meta: ItemMeta = item.getItemMeta()!!
        meta.getPersistentDataContainer().set<Int, Int>(DURABILITY_MAX, PersistentDataType.INTEGER, value)
        item.setItemMeta(meta)
    }

    fun setDurability(item: ItemStack, value: Int) {
        val meta: ItemMeta = item.getItemMeta()!!
        meta.getPersistentDataContainer().set<Int, Int>(DURABILITY, PersistentDataType.INTEGER, value)
        item.setItemMeta(meta)
    }
}