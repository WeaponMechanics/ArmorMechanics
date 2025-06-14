@file:Suppress("UNCHECKED_CAST")

package com.cjcrafter.armormechanics.commands

import com.cjcrafter.armormechanics.ArmorMechanics
import com.cjcrafter.armormechanics.ArmorMechanicsAPI
import com.cjcrafter.armormechanics.events.ArmorGenerateEvent
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentManyEntities
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import me.deecaad.core.commands.CommandHelpBuilder
import me.deecaad.core.commands.CustomMapArgument
import me.deecaad.core.file.simple.BooleanSerializer
import me.deecaad.core.file.simple.CsvSerializer
import me.deecaad.core.file.simple.StringSerializer
import me.deecaad.core.utils.AdventureUtil
import me.deecaad.core.utils.EnumUtil
import me.deecaad.core.utils.StringUtil
import me.deecaad.weaponmechanics.utils.CustomTag
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

object Command {
    fun register() {
        val armorDataMapArgument =
            CustomMapArgument(
                "data",
                mapOf(
                    "dontEquip" to BooleanSerializer(),
                    "forceEquip" to BooleanSerializer(),
                    "preventRemove" to BooleanSerializer(),
                    "attachments" to CsvSerializer(StringSerializer()),
                )
            )

        commandAPICommand("am") {
            withAliases("armor", "armormechanics")
            withPermission("armormechanics.admin")
            withShortDescription("ArmorMechanics main command")

            subcommand("give") {
                withPermission("armormechanics.commands.give")
                withShortDescription("Gives the target(s) armor")

                entitySelectorArgumentManyEntities("targets")
                stringArgument("armor") {
                    replaceSuggestions(
                        ArgumentSuggestions.strings {
                            val options = ArmorMechanics.INSTANCE.armors.keys
                            options.toTypedArray()
                        }
                    )
                }
                withOptionalArguments(armorDataMapArgument)

                anyExecutor { sender, args ->
                    val targets = args["targets"] as List<Entity>
                    val armorTitle = args["armor"] as String
                    val data = args["data"] as? Map<String, Any> ?: mutableMapOf()

                    give(sender, targets, armorTitle, data)
                }
            }

            subcommand("get") {
                withPermission("armormechanics.commands.get")
                withShortDescription("Gives yourself armor")

                stringArgument("armor") {
                    replaceSuggestions(
                        ArgumentSuggestions.strings {
                            val options = ArmorMechanics.INSTANCE.armors.keys
                            options.toTypedArray()
                        }
                    )
                }
                withOptionalArguments(armorDataMapArgument)

                anyExecutor { sender, args ->
                    val armorTitle = args["armor"] as String
                    val data = args["data"] as? Map<String, Any> ?: mutableMapOf()

                    give(sender, listOf(sender as Player), armorTitle, data)
                }
            }

            subcommand("giveset") {
                withPermission("armormechanics.commands.giveset")
                withShortDescription("Gives the target(s) a set of armor")

                entitySelectorArgumentManyEntities("targets")
                stringArgument("set") {
                    replaceSuggestions(
                        ArgumentSuggestions.strings {
                            val options = ArmorMechanics.INSTANCE.sets.keys
                            options.toTypedArray()
                        }
                    )
                }
                withOptionalArguments(armorDataMapArgument)

                anyExecutor { sender, args ->
                    val targets = args["targets"] as List<Entity>
                    val setTitle = args["set"] as String
                    val data = args["data"] as? Map<String, Any> ?: mutableMapOf()

                    val set = ArmorMechanics.INSTANCE.sets[setTitle]
                    if (set == null) {
                        sender.sendMessage("${ChatColor.RED}Unknown set '$setTitle'")
                        return@anyExecutor
                    }

                    give(sender, targets, set.helmet ?: "", data)
                    give(sender, targets, set.chestplate ?: "", data)
                    give(sender, targets, set.leggings ?: "", data)
                    give(sender, targets, set.boots ?: "", data)
                }
            }

            subcommand("clear") {
                withPermission("armormechanics.commands.clear")
                withShortDescription("Clears the target(s) armor")

                entitySelectorArgumentManyEntities("targets")
                stringArgument("slots", optional = true) {
                    replaceSuggestions(
                        ArgumentSuggestions.strings {
                            val options = EnumUtil.getOptions(EquipmentSlot::class.java)
                            options.toTypedArray()
                        }
                    )
                }

                anyExecutor { sender, args ->
                    val targets = args["targets"] as List<Entity>
                    val slots = args["slots"] as? String

                    clear(sender, targets, slots)
                }
            }

            subcommand("reload") {
                withPermission("armormechanics.commands.reload")
                withShortDescription("Reloads ArmorMechanics configuration")

                anyExecutor { sender, args ->
                    ArmorMechanics.INSTANCE.reload().thenRun {
                        sender.sendMessage("${ChatColor.GREEN}Reloaded ArmorMechanics")
                    }
                }
            }

            val helpBuilder = CommandHelpBuilder(Style.style(NamedTextColor.GOLD), Style.style(NamedTextColor.GRAY))
            helpBuilder.register(this)
        }
    }

    fun give(sender: CommandSender, entities: List<Entity>, title: String, data: Map<String, Any>) {

        // Since we want to ignore spelling/capitalization errors, we should
        // make sure the given 'title' matches to an actual armor-title.
        var title = title
        val startsWith: MutableList<String> = ArrayList()
        val options: Set<String> = ArmorMechanics.INSTANCE.armors.keys
        for (temp in options) {
            if (temp.lowercase().startsWith(title.lowercase())) startsWith.add(title)
        }
        title = StringUtil.didYouMean(title, if (startsWith.isEmpty()) options else startsWith)
        val armor = ArmorMechanics.INSTANCE.armors[title]
        if (armor == null) {
            sender.sendMessage(ChatColor.RED.toString() + "Couldn't find armor '" + title + "'... Choose from " + options)
            return
        }

        val slot = ArmorMechanicsAPI.guessEquipmentSlot(armor)!!
        val dontEquip = data["dontEquip"] as? Boolean ?: false
        val force = data["forceEquip"] as? Boolean ?: false
        val preventRemove = data["preventRemove"] as? Boolean ?: false
        if (!force && preventRemove) {
            sender.sendMessage(ChatColor.RED.toString() + "When using preventRemove, forceEquip must also be enabled!")
        }
        for (entity in entities) {
            if (entity !is LivingEntity) continue
            val equipment = entity.equipment ?: continue

            // Let other plugins modify generated armor
            val clone = armor.clone()
            val event = ArmorGenerateEvent(sender, entity, clone, title, data)
            Bukkit.getPluginManager().callEvent(event)

            AdventureUtil.updatePlaceholders(entity as? Player, clone)

            if (!dontEquip && force) {
                if (preventRemove) {
                    CustomTag.PREVENT_REMOVE.setInteger(clone, 1)
                    equipment.setItem(slot, clone)
                    return
                }
                equipment.setItem(slot, clone)
                return
            }
            if (entity is Player) {
                val overflow = entity.inventory.addItem(clone)
                if (overflow.isNotEmpty()) {
                    sender.sendMessage(ChatColor.RED.toString() + entity.name + "'s inventory was full!")
                }
            }
        }
    }

    fun clear(sender: CommandSender, targets: List<Entity>, slot: EquipmentSlot?) {
        sender.sendMessage(ChatColor.GREEN.toString() + "Removing armor from " + targets.size + if (targets.size == 1) "entity" else "entities")
        for (target in targets) {
            if (!target.type.isAlive) continue
            val entity = target as LivingEntity
            val equipment = entity.equipment!!
            if (slot == null) {
                equipment.boots = null
                equipment.leggings = null
                equipment.chestplate = null
                equipment.helmet = null
            } else {
                equipment.setItem(slot, null)
            }
        }
    }
}
