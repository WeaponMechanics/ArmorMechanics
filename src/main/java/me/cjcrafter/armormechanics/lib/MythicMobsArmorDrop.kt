package me.cjcrafter.armormechanics.lib;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import me.cjcrafter.armormechanics.ArmorMechanics;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MythicMobsArmorDrop implements IItemDrop {

    private final String armorTitle;

    public MythicMobsArmorDrop(MythicLineConfig config, String argument) {
        String title = config.getString(new String[]{ "armorTitle", "armor" }, "", "");

        // Since we want to ignore spelling/capitalization errors, we should
        // make sure the given 'title' matches to an actual armor-title.
        List<String> startsWith = new ArrayList<>();
        Set<String> options = ArmorMechanics.INSTANCE.armors.keySet();
        for (String temp : options) {
            if (temp.toLowerCase(Locale.ROOT).startsWith(title.toLowerCase(Locale.ROOT)))
                startsWith.add(title);
        }

        armorTitle = StringUtil.didYouMean(title, startsWith.isEmpty() ? options : startsWith);
    }

    @Override
    public AbstractItemStack getDrop(DropMetadata dropMetadata, double v) {
        ItemStack item = ArmorMechanics.INSTANCE.armors.get(armorTitle);

        // Just in case MythicMobs edits this item, we want to use a clone
        // to avoid possible modification.
        return new BukkitItemStack(item.clone());
    }
}
