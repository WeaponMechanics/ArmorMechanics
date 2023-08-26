package me.cjcrafter.armormechanics.listeners;

import me.cjcrafter.armormechanics.ArmorMechanicsAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public class ImmunePotionCanceller implements Listener {

    @EventHandler
    public void onPotion(EntityPotionEffectEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType().isAlive())
            event.setCancelled(ArmorMechanicsAPI.isImmune(((LivingEntity) entity).getEquipment(), event.getModifiedType()));
    }
}
