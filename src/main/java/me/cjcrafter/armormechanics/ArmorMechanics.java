package me.cjcrafter.armormechanics;

import me.cjcrafter.armormechanics.listeners.*;
import me.cjcrafter.auto.UpdateChecker;
import me.cjcrafter.auto.UpdateInfo;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.TaskChain;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ArmorMechanics extends JavaPlugin {

    public static ArmorMechanics INSTANCE;

    public Debugger debug;
    private Metrics metrics;
    private UpdateChecker update;

    public final Map<String, BonusEffect> effects = new HashMap<>();
    public final Map<String, ItemStack> armors = new HashMap<>();
    public final Map<String, ArmorSet> sets = new HashMap<>();


    @Override
    public void onLoad() {
        INSTANCE = this;

        int level = getConfig().getInt("Debug_Level", 2);
        boolean printTraces = getConfig().getBoolean("Print_Traces", false);
        debug = new Debugger(getLogger(), level, printTraces);

        if (ReflectionUtil.getMCVersion() < 13) {
            debug.error("  !!!!! ERROR !!!!!", "  !!!!! ERROR !!!!!", "  !!!!! ERROR !!!!!", "  Plugin only supports Minecraft 1.13 and higher");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onEnable() {

        reload();
        registerBStats();
        registerUpdateChecker();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ArmorEquipListener(), this);
        pm.registerEvents(new ArmorUpdateListener(), this);
        pm.registerEvents(new BlockPlaceListener(), this);
        pm.registerEvents(new DamageMechanicListener(), this);
        pm.registerEvents(new ImmunePotionCanceller(), this);
        pm.registerEvents(new PreventRemoveListener(), this);
        pm.registerEvents(new WeaponMechanicsDamageListener(), this);

        // Try to hook into MythicMobs, an error will be thrown if the user is
        // using any version below v5.0.0
        if (pm.getPlugin("MythicMobs") != null) {
            try {
                pm.registerEvents(new MythicMobsListener(), this);
            } catch (Throwable e) {
                debug.log(LogLevel.ERROR, "Could not hook into MythicMobs", e);
            }
        }

        Command.register();
    }

    public void reload() {

        // Write config from jar to datafolder
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0) {
            debug.info("Copying files from jar (This process may take up to 30 seconds during the first load!)");
            FileUtil.copyResourcesTo(getClassLoader().getResource("ArmorMechanics"), getDataFolder().toPath());
        }

        reloadConfig();

        // Clear old data
        effects.clear();
        armors.clear();
        sets.clear();

        // Serialize armor types
        File armorFile = new File(getDataFolder(), "Armor.yml");
        FileConfiguration armorConfig = YamlConfiguration.loadConfiguration(armorFile);

        for (String key : armorConfig.getKeys(false)) {
            ArmorSerializer serializer = new ArmorSerializer();
            SerializeData data = new SerializeData(serializer, armorFile, key, armorConfig);

            try {
                serializer.serialize(data);
            } catch (SerializerException e) {
                e.log(debug);
            }
        }

        File setFile = new File(getDataFolder(), "Set.yml");
        FileConfiguration setConfig = YamlConfiguration.loadConfiguration(setFile);

        for (String key : setConfig.getKeys(false)) {
            ArmorSet serializer = new ArmorSet();
            SerializeData data = new SerializeData(serializer, setFile, key, setConfig);

            try {
                serializer.serialize(data);
            } catch (SerializerException e) {
                e.log(debug);
            }
        }
    }

    private void registerBStats() {
        if (metrics != null) return;

        debug.debug("Registering bStats");

        // See https://bstats.org/plugin/bukkit/ArmorMechanics/15777. This is
        // the bStats plugin id used to track information.
        int id = 15777;

        this.metrics = new Metrics(this, id);

        metrics.addCustomChart(new SimplePie("registered_armors", () -> {
            int count = armors.size();

            if (count <= 10) {
                return "0-10";
            } else if (count <= 20) {
                return "11-20";
            } else if (count <= 30) {
                return "21-30";
            } else if (count <= 50) {
                return "31-50";
            } else if (count <= 100) {
                return "51-100";
            } else {
                return ">100";
            }
        }));

        metrics.addCustomChart(new SimplePie("registered_sets", () -> {
            int count = sets.size();

            if (count <= 2) {
                return "0-2";
            } else if (count <= 5) {
                return "3-5";
            } else if (count <= 10) {
                return "6-10";
            } else if (count <= 20) {
                return "11-20";
            } else if (count <= 50) {
                return "21-50";
            } else {
                return ">50";
            }
        }));
    }

    private void registerUpdateChecker() {
        update = new UpdateChecker(this, UpdateChecker.github("WeaponMechanics", "ArmorMechanics"));

        Listener listener = new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                if (event.getPlayer().isOp()) {
                    new TaskChain(ArmorMechanics.this)
                            .thenRunAsync((callback) -> update.hasUpdate())
                            .thenRunSync((callback) -> {
                                UpdateInfo update = (UpdateInfo) callback;
                                if (callback != null)
                                    event.getPlayer().sendMessage(ChatColor.RED + "ArmorMechanics is out of date! " + update.current + " -> " + update.newest);

                                return null;
                            });
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}
