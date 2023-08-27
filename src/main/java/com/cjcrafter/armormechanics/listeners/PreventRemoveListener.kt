package com.cjcrafter.armormechanics.listeners

import me.deecaad.core.compatibility.CompatibilityAPI
import me.deecaad.weaponmechanics.utils.CustomTag
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.permissions.Permission

class PreventRemoveListener : Listener {
    private val permission: Permission

    init {
        permission = Permission("armormechanics.preventremovebypass")
        permission.setDescription("Allow users to remove armor which normally can't be removed")
        Bukkit.getPluginManager().addPermission(permission)
    }

    @EventHandler(ignoreCancelled = true)
    fun onClick(event: InventoryClickEvent) {
        if (event.slotType != InventoryType.SlotType.ARMOR) return
        if (event.whoClicked.hasPermission(permission)) return
        val item = event.clickedInventory!!.getItem(event.slot)
        if (item == null || !item.hasItemMeta()) return
        val preventRemove = 1 == CustomTag.PREVENT_REMOVE.getInteger(item)
        event.isCancelled = preventRemove
    }
}
