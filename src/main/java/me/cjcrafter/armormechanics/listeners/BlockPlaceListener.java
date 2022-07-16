package me.cjcrafter.armormechanics.listeners;

import me.cjcrafter.armormechanics.ArmorMechanics;
import me.cjcrafter.armormechanics.ArmorMechanicsAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    @EventHandler (ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!ArmorMechanics.INSTANCE.getConfig().getBoolean("Prevent_Armor_Place", true))
            return;

        boolean isArmor = ArmorMechanicsAPI.getArmorTitle(event.getItemInHand()) != null;
        event.setCancelled(isArmor);
    }
}
