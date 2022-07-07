package me.cjcrafter.armormechanics.listeners;

import me.cjcrafter.armormechanics.ArmorMechanics;
import me.cjcrafter.armormechanics.ArmorMechanicsAPI;
import me.cjcrafter.armormechanics.ArmorSet;
import me.cjcrafter.armormechanics.BonusEffect;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.events.EntityEquipmentEvent;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
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
        if (event.isEquipping() && event.isArmor())
            equip(event);
        if (event.isDequipping() && event.isArmor())
            dequip(event);
    }

    public void equip(EntityEquipmentEvent event) {

        LivingEntity entity = (LivingEntity) event.getEntity();
        EntityEquipment equipment = entity.getEquipment();
        ItemStack item = event.getEquipped();
        String title = CompatibilityAPI.getNBTCompatibility().getString(item, "ArmorMechanics", "armor-title");

        // When the equipped armor is not from ArmorMechanics, skip
        if (title == null || title.isEmpty())
            return;

        BonusEffect bonus = ArmorMechanics.INSTANCE.effects.get(title);

        // Determine which set they will be wearing after the armor is equipped
        ItemStack helmet = equipment.getHelmet();
        ItemStack chestplate = equipment.getChestplate();
        ItemStack leggings = equipment.getLeggings();
        ItemStack boots = equipment.getBoots();

        switch (event.getSlot()) {
            case HEAD:
                helmet = event.getEquipped();
                break;
            case CHEST:
                chestplate = event.getEquipped();
                break;
            case LEGS:
                leggings = event.getEquipped();
                break;
            case FEET:
                boots = event.getEquipped();
                break;
        }

        ArmorSet set = ArmorMechanicsAPI.getSet(helmet, chestplate, leggings, boots);

        if (bonus != null) {
            if (bonus.getEquipMechanics() != null)
                bonus.getEquipMechanics().use(new CastData(WeaponMechanics.getEntityWrapper(entity)));

            for (PotionEffect potion : bonus.getPotions())
                entity.addPotionEffect(potion);
        }

        if (set != null && set.getBonus() != null) {
            BonusEffect setBonus = ArmorMechanics.INSTANCE.effects.get(set.getBonus());

            if (setBonus.getEquipMechanics() != null)
                setBonus.getEquipMechanics().use(new CastData(WeaponMechanics.getEntityWrapper(entity)));

            for (PotionEffect potion : setBonus.getPotions())
                entity.addPotionEffect(potion);
        }
    }

    public void dequip(EntityEquipmentEvent event) {

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
            if (bonus.getDequipMechanics() != null)
                bonus.getDequipMechanics().use(new CastData(WeaponMechanics.getEntityWrapper(entity)));

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
            BonusEffect setBonus = ArmorMechanics.INSTANCE.effects.get(oldSet.getBonus());

            if (setBonus.getDequipMechanics() != null)
                setBonus.getDequipMechanics().use(new CastData(WeaponMechanics.getEntityWrapper(entity)));

            for (PotionEffect potion : setBonus.getPotions())
                entity.removePotionEffect(potion.getType());
        }


    }
}
