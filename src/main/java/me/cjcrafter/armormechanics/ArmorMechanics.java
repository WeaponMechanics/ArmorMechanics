package me.cjcrafter.armormechanics;

import me.deecaad.core.utils.Debugger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class ArmorMechanics {

    public static ArmorMechanics INSTANCE;

    private JavaPlugin plugin;
    private Debugger debug;

    Map<String, BonusEffect> effects;
    Map<String, ItemStack> armors;
    Map<String, ArmorSet> sets;

}
