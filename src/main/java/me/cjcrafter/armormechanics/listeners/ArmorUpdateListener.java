package me.cjcrafter.armormechanics.listeners;

import me.cjcrafter.armormechanics.ArmorMechanics;
import me.cjcrafter.armormechanics.ArmorMechanicsAPI;
import me.deecaad.core.events.EntityEquipmentEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ArmorUpdateListener implements Listener {

    @EventHandler
    public void onEquip(EntityEquipmentEvent event) {
        if (!ArmorMechanics.INSTANCE.getConfig().getBoolean("Update_Armor"))
            return;
        if (!event.isEquipping())
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();

        // We want to update the armor, but unfortunately we cannot modify the
        // event since it contains a COPY of the armor. So, we need to check 1
        // tick after the event and check to update it. If no armor is in that
        // slot anymore, we can just assume the player has already removed it.
        new BukkitRunnable() {
            @Override
            public void run() {
                EntityEquipment equipment = entity.getEquipment();
                ItemStack item = ArmorMechanicsAPI.getItem(equipment, event.getSlot());

                // Either not ArmorMechanics armor, or just not any armor at
                // all. Either way, we don't need to update it.
                if (ArmorMechanicsAPI.getArmorTitle(item) == null)
                    return;

                ArmorMechanicsAPI.update(item);
                ArmorMechanicsAPI.setItem(equipment, event.getSlot(), item);
            }
        }.runTask(ArmorMechanics.INSTANCE);
    }
}