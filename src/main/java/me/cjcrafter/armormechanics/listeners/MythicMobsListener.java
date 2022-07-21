package me.cjcrafter.armormechanics.listeners;

import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import me.cjcrafter.armormechanics.ArmorMechanics;
import me.cjcrafter.armormechanics.lib.MythicMobsArmorDrop;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobsListener implements Listener {

    public MythicMobsListener() {
        ArmorMechanics.INSTANCE.debug.info("Hooking into MythicMobs");
    }

    @EventHandler
    public void onMythicDropLoad(MythicDropLoadEvent event) {
        if (event.getDropName().equalsIgnoreCase("armorMechanicsArmor")) {
            event.register(new MythicMobsArmorDrop(event.getConfig(), event.getArgument()));
        }
    }
}
