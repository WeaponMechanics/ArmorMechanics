package me.cjcrafter.armormechanics.listeners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PreventRemoveListener implements Listener {

    private final Permission permission;

    public PreventRemoveListener() {
        permission = new Permission("armormechanics.preventremovebypass");
        permission.setDefault(PermissionDefault.OP);
        permission.setDescription("Allow users to remove armor which normally can't be removed");
        Bukkit.getPluginManager().addPermission(permission);
    }

    @EventHandler (ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.ARMOR)
            return;
        if (event.getWhoClicked().hasPermission(permission))
            return;

        ItemStack item = event.getClickedInventory().getItem(event.getSlot());
        if (item == null || !item.hasItemMeta())
            return;

        boolean preventRemove = 1 == CompatibilityAPI.getNBTCompatibility().getInt(item, "ArmorMechanics", "prevent-remove");
        event.setCancelled(preventRemove);
    }
}
