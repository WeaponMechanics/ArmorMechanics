package me.cjcrafter.armormechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.*;
import me.deecaad.core.commands.arguments.EntityListArgumentType;
import me.deecaad.core.commands.arguments.MapArgumentType;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.StringUtil;
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
                .with("dontEquip", MapArgumentType.INT(0, 1))
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

                .withSubcommand(new CommandBuilder("giveset")
                        .withPermission("armormechanics.commands.giveset")
                        .withDescription("Gives the target(s) the requested set of armor")
                        .withArgument(new Argument<>("targets", new EntityListArgumentType()).withDesc("Which target(s) to give the set"))
                        .withArgument(new Argument<>("set", new StringArgumentType()).withDesc("Which set of armor to give").replace(SET_SUGGESTIONS))
                        .withArgument(new Argument<>("data", giveDataMap, new HashMap<>()).withDesc("How to equip the armor"))
                        .executes(CommandExecutor.any((sender, args) -> {
                            ArmorSet set = ArmorMechanics.INSTANCE.sets.get((String) args[1]);
                            if (set.getHelmet() != null) give(sender, (List<Entity>) args[0], set.getHelmet(), (Map<String, Object>) args[2]);
                            if (set.getChestplate() != null) give(sender, (List<Entity>) args[0], set.getChestplate(), (Map<String, Object>) args[2]);
                            if (set.getLeggings() != null) give(sender, (List<Entity>) args[0], set.getLeggings(), (Map<String, Object>) args[2]);
                            if (set.getBoots() != null) give(sender, (List<Entity>) args[0], set.getBoots(), (Map<String, Object>) args[2]);
                        })))

                .withSubcommand(new CommandBuilder("clear")
                        .withPermission("armormechanics.commands.clear")
                        .withDescription("Clears the target's armor")
                        .withArgument(new Argument<>("targets", new EntityListArgumentType()).withDesc("Which target(s) to clear"))
                        .withArgument(new Argument<>("slots", new StringArgumentType().withLiteral("*"), "*").withDesc("Which slot(s) to clear").replace(SuggestionsBuilder.from("head", "chest", "legs", "feet")))
                        .executes(CommandExecutor.any((sender, args) -> {
                            clear(sender, (List<Entity>) args[0], (String) args[1]);
                        })))

                .withSubcommand(new CommandBuilder("reload")
                        .withPermission("armormechanics.commands.reload")
                        .withDescription("Reloads the plugin's configurations")
                        .executes(CommandExecutor.any((sender, args) -> {
                            ArmorMechanics.INSTANCE.reload();
                            sender.sendMessage(GREEN + "Reloaded ArmorMechanics");
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

        boolean dontEquip = 1 == (int) data.getOrDefault("dontEquip", 0);
        boolean force = 1 == (int) data.getOrDefault("forceEquip", 0);
        boolean preventRemove = 1 == (int) data.getOrDefault("preventRemove", 0);

        if (!force && preventRemove) {
            sender.sendMessage(RED + "When using preventRemove, forceEquip must also be enabled!");
        }

        for (Entity entity : entities) {
            if (!entity.getType().isAlive())
                continue;

            LivingEntity living = (LivingEntity) entity;
            EntityEquipment equipment = living.getEquipment();

            if (!dontEquip && force) {
                if (preventRemove) {
                    ItemStack clone = armor.clone();
                    CompatibilityAPI.getNBTCompatibility().setInt(clone, "ArmorMechanics", "prevent-remove", 1);
                    ArmorMechanicsAPI.setItem(equipment, slot, clone);
                    return;
                }

                ArmorMechanicsAPI.setItem(equipment, slot, armor.clone());
                return;
            }

            if (!dontEquip && ArmorMechanicsAPI.getItem(equipment, slot) == null) {
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

    public static void clear(CommandSender sender, List<Entity> targets, String slots) {
        EquipmentSlot slot = "*".equals(slots) ? null : EquipmentSlot.valueOf(StringUtil.didYouMean(slots, EnumUtil.getOptions(EquipmentSlot.class)).toUpperCase(Locale.ROOT));

        sender.sendMessage(GREEN + "Removing armor from " + targets.size() + (targets.size() == 1 ? "entity" : "entities"));

        for (Entity target : targets) {
            if (!target.getType().isAlive())
                continue;

            LivingEntity entity = (LivingEntity) target;
            EntityEquipment equipment = entity.getEquipment();

            if (slot == null) {
                equipment.setBoots(null);
                equipment.setLeggings(null);
                equipment.setChestplate(null);
                equipment.setHelmet(null);
            } else {
                ArmorMechanicsAPI.setItem(equipment, slot, null);
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
        sender.sendMessage("  " + GRAY + SYM + GOLD + " WeaponMechanics: " + GRAY + WeaponMechanics.getPlugin().getDescription().getVersion());
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Java: " + GRAY + System.getProperty("java.version"));

        // Gets all supported plugins
        Set<String> softDepends = new LinkedHashSet<>(desc.getSoftDepend());
        softDepends.addAll(MechanicsCore.getPlugin().getDescription().getSoftDepend());
        softDepends.removeIf(name -> Bukkit.getPluginManager().getPlugin(name) == null);
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Supported plugins: " + GRAY + String.join(", ", softDepends));
    }
}
