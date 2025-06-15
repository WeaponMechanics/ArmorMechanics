package com.cjcrafter.armormechanics

import com.cjcrafter.armormechanics.events.ArmorUpdateEvent
import me.deecaad.core.compatibility.CompatibilityAPI
import me.deecaad.weaponmechanics.utils.CustomTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object ArmorMechanicsAPI {

    /**
     * Returns the armor-title associated with the given armor, or returns
     * `null` if the given item doesn't have a title.
     *
     * @param armor The nullable item to check.
     * @return The armor-title, or null.
     */
    fun getArmorTitle(armor: ItemStack?): String? {
        if (armor == null || !armor.hasItemMeta())
            return null
        else
            return CustomTag.ARMOR_TITLE.getString(armor)
    }

    fun generateArmor(armorTitle: String): ItemStack {
        val armor = ArmorMechanics.getInstance().armorConfigurations.get<Armor>(armorTitle)
        require(armor != null) {
            "Armor with title '$armorTitle' does not exist in the configuration."
        }

        return armor.generateItemStack()
    }

    fun guessEquipmentSlot(item: ItemStack): EquipmentSlot? {
        // Support for new 1.21.4 equippable slot
        if (item.hasItemMeta()) {
            val meta = item.itemMeta!!
            if (meta.hasEquippable())
                return meta.equippable.slot
        }

        val name = item.type.name
        if (name.endsWith("HELMET")) return EquipmentSlot.HEAD
        if (name.endsWith("CHESTPLATE")) return EquipmentSlot.CHEST
        if (name.endsWith("LEGGINGS")) return EquipmentSlot.LEGS
        if (name.endsWith("BOOTS")) return EquipmentSlot.FEET

        if (name == Material.PLAYER_HEAD.name) return EquipmentSlot.HEAD
        return null
    }

    /**
     * Shorthand for [getSet].
     *
     * @param entity The non-null entity to check.
     * @return The set the entity is wearing, or null.
     */
    fun getSet(entity: Entity): ArmorSet? {
        return getSet((entity as? LivingEntity)?.equipment)
    }

    /**
     * Shorthand for [.getSet].
     *
     * @param equipment The equipment the entity is wearing.
     * @return The set the entity is wearing, or null.
     */
    fun getSet(equipment: EntityEquipment?): ArmorSet? {
        if (equipment == null)
            return null

        val helmet = getArmorTitle(equipment.helmet)
        val chestplate = getArmorTitle(equipment.chestplate)
        val leggings = getArmorTitle(equipment.leggings)
        val boots = getArmorTitle(equipment.boots)
        return getSet(helmet, chestplate, leggings, boots)
    }

    /**
     * Shorthand for [.getSet].
     *
     * @param helmet The nullable helmet to check.
     * @param chestplate The nullable chestplate to check.
     * @param leggings The nullable leggings to check.
     * @param boots The nullable boots to check.
     * @return The set associated with the 4 items, or null.
     */
    fun getSet(helmet: ItemStack?, chestplate: ItemStack?, leggings: ItemStack?, boots: ItemStack?): ArmorSet? {
        return getSet(
            getArmorTitle(helmet),
            getArmorTitle(chestplate),
            getArmorTitle(leggings),
            getArmorTitle(boots)
        )
    }

    /**
     * Returns the [ArmorSet] that matches the 4 pieces of armor. Some
     * important things to realize, players are able to use 3 pieces of armor
     * for a set, and use 1 as a wildcard, if the server admin configured this.
     *
     *
     * 1 piece of armor may belong to multiple sets. Although a server admin
     * would be evil to do this, and probably should be locked up in an insane
     * asylum, it's something to look out for.
     *
     *
     * You should also carefully consider when you call this method for an
     * entity. It's equipment may have already updates, or it may not have
     * updated. If you run into issues running this method, make sure you print
     * out the armor so you check if it is what you expect it to be.
     *
     * @param helmet The nullable helmet to check.
     * @param chestplate The nullable chestplate to check.
     * @param leggings The nullable leggings to check.
     * @param boots The nullable boots to check.
     * @return The set associated with the 4 titles, or null.
     */
    fun getSet(helmet: String?, chestplate: String?, leggings: String?, boots: String?): ArmorSet? {

        // Since an armor title may belong to multiple sets, we must check each
        // set to determine whether the player is wearing a set.
        val config = ArmorMechanics.getInstance().setConfigurations
        val allSets = config.values().filterIsInstance<ArmorSet>()
        for (set in allSets) {
            if (set.helmet != null && set.helmet != helmet) continue
            if (set.chestplate != null && set.chestplate != chestplate) continue
            if (set.leggings != null && set.leggings != leggings) continue
            if (set.boots != null && set.boots != boots) continue
            return set
        }
        return null
    }

    fun getBonusEffects(entity: LivingEntity): List<BonusEffect> {
        val armor = entity.equipment ?: return emptyList()
        val helmet = getArmorTitle(armor.helmet)
        val chestplate = getArmorTitle(armor.chestplate)
        val leggings = getArmorTitle(armor.leggings)
        val boots = getArmorTitle(armor.boots)

        val set = getSet(helmet, chestplate, leggings, boots)
        val temp = ArrayList<BonusEffect>()

        val config = ArmorMechanics.getInstance().armorConfigurations
        config.get<BonusEffect>("$helmet.Bonus_Effects")?.let { temp.add(it) }
        config.get<BonusEffect>("$chestplate.Bonus_Effects")?.let { temp.add(it) }
        config.get<BonusEffect>("$leggings.Bonus_Effects")?.let { temp.add(it) }
        config.get<BonusEffect>("$boots.Bonus_Effects")?.let { temp.add(it) }
        set?.bonus?.let { temp.add(it) }

        return temp
    }

    /**
     * The server admin may have to make balance changes *after* they have
     * distributed their armor. Since attributes/enchantments are set PER ITEM,
     * we have to update items to make sure they have the proper stats.
     * ArmorMechanics calls this method whenever armor is equipped to an armor
     * slot.
     *
     * The following options are updated: Attributes, Enchantments, Display
     * name, Lore, Unbreakable, Material Type, Player Skull.
     *
     * @param entity The entity involved.
     * @param armor  The non-null item to update.
     */
    fun update(entity: LivingEntity?, armor: ItemStack) {
        val title = getArmorTitle(armor) ?: return

        // Check if we should delete this item.
        // If the armor no longer exists, we cannot update its properties.
        if (!ArmorMechanics.getInstance().armorConfigurations.hasObject(title)) {
            val deleteOld = ArmorMechanics.getInstance().configuration.getBoolean("Delete_Old_Armor", false)
            if (deleteOld) armor.amount = 0
            return
        }

        // We need to save the old durability and set it to the new item, since
        // setItemMeta will reset the item's durability.
        val durability = (armor.itemMeta as? org.bukkit.inventory.meta.Damageable)?.damage
        val old = armor.clone()

        val template = ArmorMechanics.getInstance().armorConfigurations.get<Armor>(title)!!
        val templateItem = template.generateItemStack()
        armor.setType(templateItem.type)
        armor.setItemMeta(templateItem.itemMeta)

        val meta = armor.itemMeta!!
        old.itemMeta!!.persistentDataContainer.copyTo(meta.persistentDataContainer, true)
        (meta as? org.bukkit.inventory.meta.Damageable)?.damage = durability!!
        armor.setItemMeta(meta)

        Bukkit.getPluginManager().callEvent(ArmorUpdateEvent(entity!!, armor, title!!))
    }
}
