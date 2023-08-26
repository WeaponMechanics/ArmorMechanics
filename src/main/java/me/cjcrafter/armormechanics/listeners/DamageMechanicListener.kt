package me.cjcrafter.armormechanics.listeners;

import me.cjcrafter.armormechanics.ArmorMechanics;
import me.cjcrafter.armormechanics.ArmorMechanicsAPI;
import me.cjcrafter.armormechanics.ArmorSet;
import me.cjcrafter.armormechanics.BonusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;


import static me.cjcrafter.armormechanics.ArmorMechanicsAPI.getArmorTitle;

public class DamageMechanicListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!event.getEntityType().isAlive())
            return;

        ArmorSet set = ArmorMechanicsAPI.getSet(event.getEntity());

        ArmorMechanics plugin = ArmorMechanics.INSTANCE;
        EntityEquipment equipment = ((LivingEntity) event.getEntity()).getEquipment();
        BonusEffect helmet = plugin.effects.get(getArmorTitle(equipment.getHelmet()));
        BonusEffect chestplate = plugin.effects.get(getArmorTitle(equipment.getChestplate()));
        BonusEffect leggings = plugin.effects.get(getArmorTitle(equipment.getLeggings()));
        BonusEffect boots = plugin.effects.get(getArmorTitle(equipment.getBoots()));

        if (set != null && set.getBonus() != null) plugin.effects.get(set.getBonus()).onDamage(event);
        if (helmet != null) helmet.onDamage(event);
        if (chestplate != null) chestplate.onDamage(event);
        if (leggings != null) leggings.onDamage(event);
        if (boots != null) boots.onDamage(event);
    }
}
