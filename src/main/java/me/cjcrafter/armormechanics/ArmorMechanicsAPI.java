package me.cjcrafter.armormechanics;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.NumberUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class ArmorMechanicsAPI {

    private static ArmorMechanics plugin;



    public static String getArmorTitle(ItemStack armor) {
        if (armor == null || !armor.hasItemMeta())
            return null;

        return CompatibilityAPI.getNBTCompatibility().getString(armor, "ArmorMechanics", "armor-title");
    }

    public static ArmorSet getSet(Entity entity) {
        if (entity.getType().isAlive())
            return null;

        return getSet(((LivingEntity) entity).getEquipment());
    }

    public static ArmorSet getSet(EntityEquipment equipment) {
        if (equipment == null)
            return null;

        String helmet = getArmorTitle(equipment.getHelmet());
        String chestplate = getArmorTitle(equipment.getChestplate());
        String leggings = getArmorTitle(equipment.getLeggings());
        String boots = getArmorTitle(equipment.getBoots());

        return getSet(helmet, chestplate, leggings, boots);
    }

    public static ArmorSet getSet(String helmet, String chestplate, String leggings, String boots) {

        // Since an armor title may belong to multiple sets, we must check each
        // set to determine whether the player is wearing a set.
        for (ArmorSet set : plugin.sets.values()) {
            if (!Objects.equals(helmet, set.getHelmet()))
                continue;
            if (!Objects.equals(chestplate, set.getChestplate()))
                continue;
            if (!Objects.equals(leggings, set.getLeggings()))
                continue;
            if (!Objects.equals(boots, set.getBoots()))
                continue;

            return set;
        }

        return null;
    }

    public static double getBulletResistance(EntityEquipment equipment) {
        if (equipment == null)
            return 0.0;

        BonusEffect helmet = plugin.effects.get(getArmorTitle(equipment.getHelmet()));
        BonusEffect chestplate = plugin.effects.get(getArmorTitle(equipment.getChestplate()));
        BonusEffect leggings = plugin.effects.get(getArmorTitle(equipment.getLeggings()));
        BonusEffect boots = plugin.effects.get(getArmorTitle(equipment.getBoots()));
        ArmorSet set = getSet(equipment);

        double rate = 0.0;
        if (set != null && set.getBonus() != null)
            rate += plugin.effects.get(set.getBonus()).getBulletResistance();
        if (helmet != null)
            rate += helmet.getBulletResistance();
        if (chestplate != null)
            rate += chestplate.getBulletResistance();
        if (leggings != null)
            rate += leggings.getBulletResistance();
        if (boots != null)
            rate += boots.getBulletResistance();

        return NumberUtil.minMax(0.0, rate, 1.0);
    }

    public static boolean isImmune(EntityEquipment equipment, PotionEffectType type) {
        if (equipment == null)
            return false;

        BonusEffect helmet = plugin.effects.get(getArmorTitle(equipment.getHelmet()));
        BonusEffect chestplate = plugin.effects.get(getArmorTitle(equipment.getChestplate()));
        BonusEffect leggings = plugin.effects.get(getArmorTitle(equipment.getLeggings()));
        BonusEffect boots = plugin.effects.get(getArmorTitle(equipment.getBoots()));
        ArmorSet set = getSet(equipment);

        if (set != null && set.getBonus() != null && plugin.effects.get(set.getBonus()).getImmunities().contains(type))
            return true;
        if (helmet != null && helmet.getImmunities().contains(type))
            return true;
        if (chestplate != null && chestplate.getImmunities().contains(type))
            return true;
        if (leggings != null && leggings.getImmunities().contains(type))
            return true;
        if (boots != null && boots.getImmunities().contains(type))
            return true;

        return false;
    }
}
