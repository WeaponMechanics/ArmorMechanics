package com.cjcrafter.armormechanics.durability

import me.deecaad.core.utils.MinecraftVersions
import me.deecaad.weaponmechanics.utils.CustomTag
import me.deecaad.weaponmechanics.weapon.placeholders.PMaxDurability
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

fun ItemStack.setMaxDurability(durability: Int) {
    if (!MinecraftVersions.TRAILS_AND_TAILS[5].isAtLeast())
        return

    if (!hasItemMeta())
        return

    val meta = itemMeta as Damageable

    meta.setMaxDamage(durability)

    itemMeta = meta
}

fun ItemStack.getMaxDurability(): Int {
    if (MinecraftVersions.TRAILS_AND_TAILS[5].isAtLeast()) {
        if (!hasItemMeta())
            return 0

        return (itemMeta as Damageable).maxDamage
    }

    return type.maxDurability.toInt()
}

fun ItemStack.getItemDamage(): Int {
    if (MinecraftVersions.CAVES_AND_CLIFFS_1.isAtLeast()) {
        if (!hasItemMeta())
            return 0

        val meta = itemMeta
        if (meta !is Damageable)
            return 0

        return meta.damage
    }

    return durability.toInt()
}

fun ItemStack.setItemDamage(damage: Int) {
    if (MinecraftVersions.CAVES_AND_CLIFFS_1.isAtLeast()) {
        if (!hasItemMeta())
            return

        val meta = itemMeta
        if (meta !is Damageable)
            return

        meta.damage = damage
        itemMeta = meta
        return
    }

    durability = damage.toShort()
}

fun ItemStack.getCustomDurability(): Int? {
    return if (CustomTag.DURABILITY.hasInteger(this)) CustomTag.DURABILITY.getInteger(this)
    else getCustomMaxDurability()
}

fun ItemStack.getCustomMaxDurability(): Int? {
    return if (CustomTag.MAX_DURABILITY.hasInteger(this)) CustomTag.MAX_DURABILITY.getInteger(this) else null
}

fun ItemStack.setCustomDurability(durability: Int) {
    CustomTag.DURABILITY.setInteger(this, durability)
}

fun getProportion(durability: Int, maxDurability: Int): Float {
    return durability.toFloat() / maxDurability
}

fun getFromProportion(proportion: Float, maxDurability: Int): Int {
    return (proportion * maxDurability).toInt()
}

fun ItemStack.applyCustomDurabilitiesToItem(damage: Int, maxDurability: Int) {
    CustomTag.MAX_DURABILITY.setInteger(this, maxDurability)
    CustomTag.DURABILITY.setInteger(this, maxDurability - damage)

    setMaxDurability(maxDurability)
    val maxDura = getMaxDurability()
    this.setItemDamage(maxDura - getFromProportion(getProportion(maxDurability - damage, maxDurability), maxDura))
}