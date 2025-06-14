package com.cjcrafter.armormechanics

import com.cjcrafter.armormechanics.commands.Command
import com.cjcrafter.armormechanics.listeners.ArmorEquipListener
import com.cjcrafter.armormechanics.listeners.ArmorUpdateListener
import com.cjcrafter.armormechanics.listeners.BlockPlaceListener
import com.cjcrafter.armormechanics.listeners.DamageMechanicListener
import com.cjcrafter.armormechanics.listeners.ImmunePotionCanceller
import com.cjcrafter.armormechanics.listeners.MythicMobsListener
import com.cjcrafter.armormechanics.listeners.PreventRemoveListener
import com.cjcrafter.armormechanics.listeners.WeaponMechanicsDamageListener
import me.deecaad.core.MechanicsPlugin
import me.deecaad.core.events.QueueSerializerEvent
import me.deecaad.core.file.Configuration
import me.deecaad.core.file.FastConfiguration
import me.deecaad.core.file.IValidator
import me.deecaad.core.file.JarInstancer
import me.deecaad.core.file.RootFileReader
import me.deecaad.core.file.SearchMode
import me.deecaad.core.file.SerializerInstancer
import org.bstats.charts.SimplePie
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.jar.JarFile

class ArmorMechanics : MechanicsPlugin(bStatsId = 15777) {

    var armorConfigurations: Configuration = FastConfiguration()
        private set
    var setConfigurations: Configuration = FastConfiguration()
        private set

    override fun onLoad() {
        INSTANCE = this
        super.onLoad()
    }

    override fun handleCommands(): CompletableFuture<Void> {
        Command.register()
        return super.handleCommands()
    }

    override fun handleConfigs(): CompletableFuture<Void> {
        // Look for all serializers/validators in the jar
        val jar = JarFile(file)
        val serializers = SerializerInstancer(jar).createAllInstances(classLoader, SearchMode.ON_DEMAND)
        val validators = JarInstancer(jar).createAllInstances(IValidator::class.java, classLoader, SearchMode.ON_DEMAND)

        val event = QueueSerializerEvent(this, dataFolder)
        event.addSerializers(serializers)
        event.addValidators(validators)
        server.pluginManager.callEvent(event)

        armorConfigurations = RootFileReader(this, Armor::class.java, "armors")
            .withSerializers(event.serializers)
            .withValidators(event.validators)
            .assertFiles()
            .read()
        debugger.info("Loaded ${armorConfigurations.keys(deep = false).size} armors")

        setConfigurations = RootFileReader(this, ArmorSet::class.java, "sets")
            .withSerializers(event.serializers)
            .withValidators(event.validators)
            .assertFiles()
            .read()
        debugger.info("Loaded ${setConfigurations.keys(deep = false).size} sets")

        return super.handleConfigs()
    }

    override fun handleListeners(): CompletableFuture<Void> {
        server.pluginManager.run {
            val plugin = this@ArmorMechanics
            registerEvents(ArmorEquipListener(), plugin)
            registerEvents(ArmorUpdateListener(), plugin)
            registerEvents(BlockPlaceListener(), plugin)
            registerEvents(DamageMechanicListener(), plugin)
            registerEvents(ImmunePotionCanceller(), plugin)
            registerEvents(PreventRemoveListener(), plugin)

            if (getPlugin("WeaponMechanics") != null) {
                registerEvents(WeaponMechanicsDamageListener(), plugin)
            }

            // Try to hook into MythicMobs, an error will be thrown if the user is
            // using any version below v5.0.0
            if (getPlugin("MythicMobs") != null) {
                try {
                    registerEvents(MythicMobsListener(), plugin)
                } catch (e: Throwable) {
                    debugger.severe("Could not hook into MythicMobs", e)
                }
            }

            // Automatically reload ArmorMechanics if WeaponMechanics reloads (only after 20 ticks to avoid
            // the first load from WM)
            foliaScheduler.global().runDelayed(Runnable {
                registerEvents(object : Listener {
                    @EventHandler
                    fun onQueue(event: QueueSerializerEvent) {
                        if ("WeaponMechanics" == event.sourceName) reload()
                    }
                }, plugin)
            }, 20L)
        }

        return super.handleListeners()
    }

    override fun handleMetrics(): CompletableFuture<Void> {
        metrics!!.addCustomChart(SimplePie("registered_armors", Callable {
            val count = armorConfigurations.keys(deep = false).size
            if (count <= 10) {
                return@Callable "0-10"
            } else if (count <= 20) {
                return@Callable "11-20"
            } else if (count <= 30) {
                return@Callable "21-30"
            } else if (count <= 50) {
                return@Callable "31-50"
            } else if (count <= 100) {
                return@Callable "51-100"
            } else {
                return@Callable ">100"
            }
        }))
        metrics!!.addCustomChart(SimplePie("registered_sets", Callable {
            val count = setConfigurations.keys(deep = false).size
            if (count <= 2) {
                return@Callable "0-2"
            } else if (count <= 5) {
                return@Callable "3-5"
            } else if (count <= 10) {
                return@Callable "6-10"
            } else if (count <= 20) {
                return@Callable "11-20"
            } else if (count <= 50) {
                return@Callable "21-50"
            } else {
                return@Callable ">50"
            }
        }))
        return super.handleMetrics()
    }

    companion object {
        private lateinit var INSTANCE: ArmorMechanics

        @JvmStatic
        fun getInstance(): ArmorMechanics {
            return INSTANCE
        }
    }
}
