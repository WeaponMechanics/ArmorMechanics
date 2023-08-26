package me.cjcrafter.armormechanics.listeners;

import me.cjcrafter.armormechanics.ArmorMechanics;
import me.cjcrafter.armormechanics.ArmorMechanicsAPI;
import me.cjcrafter.armormechanics.ArmorSet;
import me.cjcrafter.armormechanics.BonusEffect;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class WeaponMechanicsDamageListener implements Listener {

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDamage(WeaponDamageEntityEvent event) {
        double damage = event.getFinalDamage();


        // When a damage point is present, we should only account for the
        // armor AT that damage point. For example, if you are not wearing
        // a helmet and get headshot, you should not be protected by your
        // chestplate/leggings/boots.
        double rate = 1.0;
        if (event.getPoint() != null) {

            // Account for the set bonus
            ArmorSet set = ArmorMechanicsAPI.getSet(event.getVictim().getEquipment());
            if (set != null && set.getBonus() != null)
                rate -= ArmorMechanics.INSTANCE.effects.get(set.getBonus()).getBulletResistance(event.getWeaponTitle());

            // Determine which armor/bonus effects to use
            EquipmentSlot slot = switch (event.getPoint()) {
                case ARMS, BODY -> EquipmentSlot.CHEST;
                case LEGS -> EquipmentSlot.LEGS;
                case HEAD -> EquipmentSlot.HEAD;
                case FEET -> EquipmentSlot.FEET;
            };

            ItemStack armor = ArmorMechanicsAPI.getItem(event.getVictim().getEquipment(), slot);
            String armorTitle = ArmorMechanicsAPI.getArmorTitle(armor);
            BonusEffect bonus = armorTitle == null ? null : ArmorMechanics.INSTANCE.effects.get(armorTitle);

            if (bonus != null)
                rate -= bonus.getBulletResistance();
        }

        // If no specific damage point is present, use all armor. This probably
        // only happens for explosions.
        else {
            rate -= ArmorMechanicsAPI.getBulletResistance(event.getVictim().getEquipment(), event.getWeaponTitle());
        }

        event.setFinalDamage(damage * rate);
    }
}
