package me.cjcrafter.armormechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.*;
import me.deecaad.core.commands.arguments.EntityListArgumentType;
import me.deecaad.core.commands.arguments.MapArgumentType;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.UpdateChecker;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.*;
import java.util.function.Function;

import static org.bukkit.ChatColor.*;

@SuppressWarnings("unchecked")
public class Command {

    public static final char SYM = '\u27A2';

    public static Function<CommandData, Tooltip[]> ARMOR_SUGGESTIONS = (data) -> {
        return ArmorMechanics.INSTANCE.armors.keySet().stream().map(Tooltip::of).toArray(Tooltip[]::new);
    };

    public static Function<CommandData, Tooltip[]> SET_SUGGESTIONS = (data) -> {
        return ArmorMechanics.INSTANCE.sets.keySet().stream().map(Tooltip::of).toArray(Tooltip[]::new);
    };

    public static void register() {

        MapArgumentType giveDataMap = new MapArgumentType()
                .with("forceEquip", MapArgumentType.INT(0, 1))
                .with("preventRemove", MapArgumentType.INT(0, 1));

        CommandBuilder command = new CommandBuilder("am")
                .withAliases("armor", "armormechanics")
                .withPermission("armormechanics.admin")
                .withDescription("ArmorMechanics' main command")

                .withSubcommand(new CommandBuilder("give")
                        .withPermission("armormechanics.commands.give")
                        .withDescription("Gives the target(s) the requested armor")
                        .withArgument(new Argument<>("targets", new EntityListArgumentType()).withDesc("Which target(s) to give the armor"))
                        .withArgument(new Argument<>("armor", new StringArgumentType()).withDesc("Which armor to give").replace(ARMOR_SUGGESTIONS))
                        .withArgument(new Argument<>("data", giveDataMap, new HashMap<>()).withDesc("How to equip the armor"))
                        .executes(CommandExecutor.any((sender, args) -> {
                            give(sender, (List<Entity>) args[0], (String) args[1], (HashMap<String, Object>) args[2]);
                        })))

                .withSubcommand(new CommandBuilder("get")
                        .withPermission("armormechanics.commands.get")
                        .withDescription("Gives you the requested armor")
                        .withArgument(new Argument<>("armor", new StringArgumentType()).withDesc("Which armor to give").replace(ARMOR_SUGGESTIONS))
                        .executes(CommandExecutor.entity((sender, args) -> {
                            give(sender, Collections.singletonList(sender), (String) args[0], new HashMap<>());
                        })))

                .withSubcommand(new CommandBuilder("info")
                        .withPermission("armormechanics.commands.info")
                        .withDescription("Displays information about ArmorMechanics")
                        .executes(CommandExecutor.any((sender, args) -> {
                            info(sender);
                        })));

        command.registerHelp(HelpCommandBuilder.HelpColor.from(GOLD, GRAY, SYM));
        command.register();
    }

    public static void give(CommandSender sender, List<Entity> entities, String title, Map<String, Object> data) {

        // Since we want to ignore spelling/capitalization errors, we should
        // make sure the given 'title' matches to an actual armor-title.
        List<String> startsWith = new ArrayList<>();
        Set<String> options = ArmorMechanics.INSTANCE.armors.keySet();
        for (String temp : options) {
            if (temp.toLowerCase(Locale.ROOT).startsWith(title.toLowerCase(Locale.ROOT)))
                startsWith.add(title);
        }

        title = StringUtil.didYouMean(title, startsWith.isEmpty() ? options : startsWith);
        ItemStack armor = ArmorMechanics.INSTANCE.armors.get(title);
        EquipmentSlot slot = ArmorMechanicsAPI.getEquipmentSlot(armor.getType());

        boolean force = 1 == (int) data.get("forceEquip");
        boolean preventRemove = 1 == (int) data.get("preventRemove");

        for (Entity entity : entities) {
            if (!entity.getType().isAlive())
                continue;

            LivingEntity living = (LivingEntity) entity;
            EntityEquipment equipment = living.getEquipment();

            if (force) {
                if (preventRemove) {
                    ItemStack clone = armor.clone();
                    CompatibilityAPI.getNBTCompatibility().setInt(clone, "ArmorMechanics", "prevent-remove", 1);
                    ArmorMechanicsAPI.setItem(equipment, slot, clone);
                    return;
                }

                ArmorMechanicsAPI.setItem(equipment, slot, armor.clone());
                return;
            }

            if (ArmorMechanicsAPI.getItem(equipment, slot) == null) {
                ArmorMechanicsAPI.setItem(equipment, slot, armor.clone());
                return;
            }

            if (living.getType() == EntityType.PLAYER) {
                Player player = (Player) living;
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(armor.clone());

                if (!overflow.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + player.getName() + "'s inventory was full!");
                }
            }
        }
    }

    public static void info(CommandSender sender) {
        PluginDescriptionFile desc =  ArmorMechanics.INSTANCE.getDescription();
        sender.sendMessage("" + GRAY + GOLD + BOLD + "Armor" + GRAY + BOLD + "Mechanics"
                + GRAY + ", v" + ITALIC + desc.getVersion());

        sender.sendMessage("  " + GRAY + SYM + GOLD + " Authors: " + GRAY + String.join(", ", desc.getAuthors()));
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Command:" + GRAY + " /armormechanics");

        sender.sendMessage("  " + GRAY + SYM + GOLD + " Server: " + GRAY + Bukkit.getName() + " " + Bukkit.getVersion());
        sender.sendMessage("  " + GRAY + SYM + GOLD + " MechanicsCore: " + GRAY + MechanicsCore.getPlugin().getDescription().getVersion());
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Java: " + GRAY + System.getProperty("java.version"));

        // Gets all supported plugins
        Set<String> softDepends = new LinkedHashSet<>(desc.getSoftDepend());
        softDepends.addAll(MechanicsCore.getPlugin().getDescription().getSoftDepend());
        softDepends.remove("MechanicsCore");
        softDepends.removeIf(name -> Bukkit.getPluginManager().getPlugin(name) == null);
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Supported plugins: " + GRAY + String.join(", ", softDepends));
    }
}
