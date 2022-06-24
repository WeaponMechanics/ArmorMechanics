package me.cjcrafter.armormechanics.listeners;

import me.cjcrafter.armormechanics.ArmorMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponMechanicsDamageListener implements Listener {

    @EventHandler (ignoreCancelled = true)
    public void onDamage(WeaponDamageEntityEvent event) {
        double damage = event.getFinalDamage();
        double rate = 1.0 - ArmorMechanicsAPI.getBulletResistance(event.getVictim().getEquipment(), event.getWeaponTitle());
        event.setFinalDamage(damage * rate);
    }
}
