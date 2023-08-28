@file:Suppress("UNCHECKED_CAST")

package com.cjcrafter.armormechanics.commands

import com.cjcrafter.armormechanics.ArmorMechanicsAPI.getEquipmentSlot
import com.cjcrafter.armormechanics.ArmorMechanicsAPI.getItem
import com.cjcrafter.armormechanics.ArmorMechanicsAPI.setItem
import me.deecaad.core.commands.CommandData
import me.deecaad.core.commands.HelpCommandBuilder
import me.deecaad.core.commands.SuggestionsBuilder
import me.deecaad.core.commands.Tooltip
import me.deecaad.core.commands.arguments.EntityListArgumentType
import me.deecaad.core.commands.arguments.MapArgumentType
import me.deecaad.core.commands.arguments.StringArgumentType
import me.deecaad.core.compatibility.CompatibilityAPI
import me.deecaad.core.utils.EnumUtil
import me.deecaad.core.utils.StringUtil
import me.deecaad.weaponmechanics.utils.CustomTag
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import java.util.function.Function

object Command {
    const val SYM = '\u27A2'
    var ARMOR_SUGGESTIONS = Function<CommandData, Array<Tooltip>> {
        return@Function com.cjcrafter.armormechanics.ArmorMechanics.INSTANCE.armors.keys.map { armorTitle ->
            Tooltip.of(
                armorTitle
            )
        }.toTypedArray()
    }

    var SET_SUGGESTIONS = Function<CommandData, Array<Tooltip>> {
        return@Function com.cjcrafter.armormechanics.ArmorMechanics.INSTANCE.sets.keys.map { setName ->
            Tooltip.of(
                setName
            )
        }.toTypedArray()
    }

    fun register() {
        val giveDataMap = MapArgumentType()
            .with("dontEquip", MapArgumentType.INT(0, 1))
            .with("forceEquip", MapArgumentType.INT(0, 1))
            .with("preventRemove", MapArgumentType.INT(0, 1))

        val command = command("am") {
            aliases("armor", "armormechanics")
            permission("armormechanics.admin")
            description("ArmorMechanics main command")

            subcommand("give") {
                permission("armormechanics.commands.give")
                description("Gives the target(s) armor")

                argument("targets", EntityListArgumentType()) {
                    description = "Who to give armor to"
                }
                argument("armor", StringArgumentType()) {
                    description = "Which armor to give"
                    replace(ARMOR_SUGGESTIONS)
                }
                argument("data", giveDataMap) {
                    description = "How to equip the armor, json input {}"
                    default = HashMap()
                }
                executeAny { sender, args ->
                    give(sender, args[0] as List<Entity>, args[1] as String, args[2] as Map<String?, Any>)
                }
            }

            subcommand("get") {
                permission("armormechanics.commands.get")
                description("Give yourself armor")

                argument("armor", StringArgumentType()) {
                    description = "Which armor to give"
                    replace(ARMOR_SUGGESTIONS)
                }
                argument("data", giveDataMap) {
                    description = "How to equip the armor, json input {}"
                    default = HashMap()
                }
                executeEntity { entity, args ->
                    give(entity, listOf(entity as LivingEntity), args[0] as String, args[1] as Map<String?, Any>)
                }
            }

            subcommand("giveset") {
                permission("armormechanics.commands.giveset")
                description("Give the target[s] a set of armor")

                argument("targets", EntityListArgumentType()) {
                    description = "Who to give armor to"
                }
                argument("set", StringArgumentType()) {
                    description = "Which set to give"
                    replace(SET_SUGGESTIONS)
                }
                argument("data", giveDataMap) {
                    description = "How to equip the armor, json input {}"
                    default = HashMap()
                }
                executeAny { sender, args ->
                    val set = com.cjcrafter.armormechanics.ArmorMechanics.INSTANCE.sets[args[1] as String]
                    if (set == null) {
                        sender.sendMessage("${ChatColor.RED}Unknown set '" + args[1] + "'")
                        return@executeAny
                    }

                    val targets = args[0] as List<Entity>
                    val data = args[2] as Map<String?, Any>
                    set.helmet?.let { give(sender, targets, it, data) }
                    set.chestplate?.let { give(sender, targets, it, data) }
                    set.leggings?.let { give(sender, targets, it, data) }
                    set.boots?.let { give(sender, targets, it, data) }
                }
            }

            subcommand("clear") {
                permission("armormechanics.commands.clear")
                description("Clears the target entity's armor")

                argument("targets", EntityListArgumentType()) {
                    description = "Which targets to clear"
                }
                argument("slots", StringArgumentType().withLiteral("*")) {
                    description = "Which slot to clear"
                    default = "*"
                    replace(SuggestionsBuilder.from("head", "chest", "legs", "feet"))
                }
                executeAny { sender, args ->
                    clear(sender, args[0] as List<Entity>, args[1] as String)
                }
            }

            subcommand("reload") {
                permission("armormechanics.commands.reload")
                description("Reloads ArmorMechanics configuration")
                executeAny { sender, args ->
                    com.cjcrafter.armormechanics.ArmorMechanics.INSTANCE.reload()
                        .thenRunSync(Runnable { sender.sendMessage("${ChatColor.GREEN}Reloaded ArmorMechanics") })
                }
            }
        }

        command.registerHelp(HelpCommandBuilder.HelpColor.from(ChatColor.GOLD, ChatColor.GRAY, SYM))
        command.register()
    }

    fun give(sender: CommandSender, entities: List<Entity>, title: String?, data: Map<String?, Any>) {

        // Since we want to ignore spelling/capitalization errors, we should
        // make sure the given 'title' matches to an actual armor-title.
        var title = title
        val startsWith: MutableList<String?> = ArrayList()
        val options: Set<String> = com.cjcrafter.armormechanics.ArmorMechanics.INSTANCE.armors.keys
        for (temp in options) {
            if (temp.lowercase().startsWith(title!!.lowercase())) startsWith.add(title)
        }
        title = StringUtil.didYouMean(title, if (startsWith.isEmpty()) options else startsWith)
        val armor = com.cjcrafter.armormechanics.ArmorMechanics.INSTANCE.armors[title]
        if (armor == null) {
            sender.sendMessage(ChatColor.RED.toString() + "Couldn't find armor '" + title + "'... Choose from " + options)
            return
        }
        val slot = getEquipmentSlot(armor.type)!!
        val dontEquip = 1 == (data["dontEquip"] ?: 0) as Int
        val force = 1 == (data["forceEquip"] ?: 0) as Int
        val preventRemove = 1 == (data["preventRemove"] ?: 0) as Int
        if (!force && preventRemove) {
            sender.sendMessage(ChatColor.RED.toString() + "When using preventRemove, forceEquip must also be enabled!")
        }
        for (entity in entities) {
            if (entity !is LivingEntity) continue
            val equipment = entity.equipment ?: continue

            if (!dontEquip && force) {
                if (preventRemove) {
                    val clone = armor.clone()
                    CustomTag.PREVENT_REMOVE.setInteger(clone, 1)
                    setItem(equipment, slot, clone)
                    return
                }
                setItem(equipment, slot, armor.clone())
                return
            }
            if (!dontEquip && getItem(equipment, slot) == null) {
                setItem(equipment, slot, armor.clone())
                return
            }
            if (entity is Player) {
                val overflow = entity.inventory.addItem(armor.clone())
                if (overflow.isNotEmpty()) {
                    sender.sendMessage(ChatColor.RED.toString() + entity.name + "'s inventory was full!")
                }
            }
        }
    }

    fun clear(sender: CommandSender, targets: List<Entity>, slots: String) {
        val slot = if ("*" == slots) null else EquipmentSlot.valueOf(
            StringUtil.didYouMean(
                slots, EnumUtil.getOptions(
                    EquipmentSlot::class.java
                )
            ).uppercase()
        )
        sender.sendMessage(ChatColor.GREEN.toString() + "Removing armor from " + targets.size + if (targets.size == 1) "entity" else "entities")
        for (target in targets) {
            if (!target.type.isAlive) continue
            val entity = target as LivingEntity
            val equipment = entity.equipment
            if (slot == null) {
                equipment!!.boots = null
                equipment.leggings = null
                equipment.chestplate = null
                equipment.helmet = null
            } else {
                setItem(equipment!!, slot, null)
            }
        }
    }
}
