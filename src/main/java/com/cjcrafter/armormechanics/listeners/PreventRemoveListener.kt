package com.cjcrafter.armormechanics.listeners

import com.cjcrafter.armormechanics.ArmorMechanics
import me.deecaad.weaponmechanics.utils.CustomTag
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.permissions.Permission

class PreventRemoveListener : Listener {
    private lateinit var permission: Permission

    @EventHandler(ignoreCancelled = true)
    fun onClick(event: InventoryClickEvent) {
        if (!::permission.isInitialized) {
            permission = Bukkit.getPluginManager().getPermission("armormechanics.preventremovebypass")
                ?: run {
                    ArmorMechanics.getInstance().debugger.warning("Permission 'armormechanics.preventremovebypass' not found!")
                    return
                }
        }

        if (event.slotType != InventoryType.SlotType.ARMOR) return
        if (event.whoClicked.hasPermission(permission)) return
        val item = event.clickedInventory!!.getItem(event.slot)
        if (item == null || !item.hasItemMeta()) return
        val preventRemove = 1 == CustomTag.PREVENT_REMOVE.getInteger(item)
        event.isCancelled = preventRemove
    }
}
