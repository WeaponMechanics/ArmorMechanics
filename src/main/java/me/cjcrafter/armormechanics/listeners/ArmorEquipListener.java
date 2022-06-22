package me.cjcrafter.armormechanics.listeners;

import me.cjcrafter.armormechanics.ArmorMechanics;
import me.cjcrafter.armormechanics.ArmorMechanicsAPI;
import me.cjcrafter.armormechanics.ArmorSet;
import me.cjcrafter.armormechanics.BonusEffect;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.events.EntityEquipmentEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import static me.cjcrafter.armormechanics.ArmorMechanicsAPI.getArmorTitle;

public class ArmorEquipListener implements Listener {

    @EventHandler
    public void onEquip(EntityEquipmentEvent event) {
        if (!event.isEquipping() || !event.isArmor())
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        ItemStack item = event.getEquipped();
        String title = CompatibilityAPI.getNBTCompatibility().getString(item, "ArmorMechanics", "armor-title");

        // When the equipped armor is not from ArmorMechanics, skip
        if (title == null || title.isEmpty())
            return;

        BonusEffect bonus = ArmorMechanics.INSTANCE.effects.get(title);
        ArmorSet set = ArmorMechanicsAPI.getSet(entity);

        if (bonus != null) {
            for (PotionEffect potion : bonus.getPotions())
                entity.addPotionEffect(potion);
        }

        if (set != null && set.getBonus() != null) {
            for (PotionEffect potion : ArmorMechanics.INSTANCE.effects.get(set.getBonus()).getPotions())
                entity.addPotionEffect(potion);
        }
    }

    @EventHandler
    public void onDequip(EntityEquipmentEvent event) {
        if (!event.isDequipping() || !event.isArmor())
            return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        ItemStack item = event.getDequipped();
        String title = CompatibilityAPI.getNBTCompatibility().getString(item, "ArmorMechanics", "armor-title");

        // When the equipped armor is not from ArmorMechanics, skip
        if (title == null || title.isEmpty())
            return;

        BonusEffect bonus = ArmorMechanics.INSTANCE.effects.get(title);

        // Set bonus is a little weird, as we need to check if the user
        // previously had a set bonus, and if it needs to be removed.
        EntityEquipment equipment = entity.getEquipment();
        String helmet = getArmorTitle(equipment.getHelmet());
        String chestplate = getArmorTitle(equipment.getChestplate());
        String leggings = getArmorTitle(equipment.getLeggings());
        String boots = getArmorTitle(equipment.getBoots());

        if (bonus != null) {
            for (PotionEffect potion : bonus.getPotions())
                entity.removePotionEffect(potion.getType());
        }

        ArmorSet oldSet = ArmorMechanicsAPI.getSet(helmet, chestplate, leggings, boots);
        if (oldSet == null)
            return;

        switch (event.getSlot()) {
            case HEAD:
                helmet = null;
                break;
            case CHEST:
                chestplate = null;
                break;
            case LEGS:
                leggings = null;
                break;
            case FEET:
                boots =  null;
                break;
            default:
                throw new IllegalStateException("NOOOOOOOOOOO, that's not true, that's impossible!");
        }

        ArmorSet newSet = ArmorMechanicsAPI.getSet(helmet, chestplate, leggings, boots);

        if (oldSet != newSet) {
            for (PotionEffect potion : ArmorMechanics.INSTANCE.effects.get(oldSet.getBonus()).getPotions())
                entity.removePotionEffect(potion.getType());
        }


    }
}
