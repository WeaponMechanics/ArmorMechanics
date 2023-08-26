package me.cjcrafter.armormechanics

import me.cjcrafter.armormechanics.events.ArmorUpdateEvent
import me.deecaad.core.compatibility.CompatibilityAPI
import me.deecaad.core.utils.NumberUtil
import me.deecaad.core.utils.ReflectionUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import java.lang.IllegalArgumentException
import javax.annotation.Nonnull

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
            return CompatibilityAPI.getNBTCompatibility().getString(armor, "ArmorMechanics", "armor-title")
    }

    fun generateArmor(armorTitle: String?): ItemStack {
        require(armorTitle != null) { "Unknown armor-title '$armorTitle'" }
        require(ArmorMechanics.INSTANCE.armors.containsKey(armorTitle)) { "Unknown armor-title '$armorTitle'" }

        return ArmorMechanics.INSTANCE.armors[armorTitle]!!.clone()
    }

    /**
     * Returns the expected [EquipmentSlot] that the given material
     * would be worn on. However, using commands or plugins may allow players
     * to equip armor in other slots.
     *
     * @param mat The non-null material to check.
     * @return The associated equipment slot, or null.
     */
    fun getEquipmentSlot(mat: Material): EquipmentSlot? {
        val name = mat.name
        if (name.endsWith("BOOTS")) return EquipmentSlot.FEET
        if (name.endsWith("LEGGINGS")) return EquipmentSlot.LEGS
        if (name.endsWith("CHESTPLATE")) return EquipmentSlot.CHEST
        if (name.endsWith("HELMET") || name == "PLAYER_HEAD" || name == "CARVED_PUMPKIN") return EquipmentSlot.HEAD
        else return null
    }

    /**
     * Version safe method for [EntityEquipment.getItem].
     *
     * @param equipment The non-null equipment the entity is wearing.
     * @param slot      The non-null slot to check.
     * @return The nullable item in that slot.
     */
    fun getItem(equipment: EntityEquipment, slot: EquipmentSlot): ItemStack? {
        return when (slot) {
            EquipmentSlot.HEAD -> equipment.helmet
            EquipmentSlot.CHEST -> equipment.chestplate
            EquipmentSlot.LEGS -> equipment.leggings
            EquipmentSlot.FEET -> equipment.boots
            else -> null
        }
    }

    /**
     * Version safe method for [EntityEquipment.setItem].
     *
     * @param equipment The non-null equipment the entity is wearing.
     * @param slot      The non-null slot to set.
     * @param item      The item to use, or null.
     */
    fun setItem(equipment: EntityEquipment, slot: EquipmentSlot, item: ItemStack?) {
        when (slot) {
            EquipmentSlot.HEAD -> equipment.helmet = item
            EquipmentSlot.CHEST -> equipment.chestplate = item
            EquipmentSlot.LEGS -> equipment.leggings = item
            EquipmentSlot.FEET -> equipment.boots = item
            EquipmentSlot.HAND, EquipmentSlot.OFF_HAND -> throw IllegalArgumentException("Invalid slot $slot")
        }
    }

    /**
     * Shorthand for [.getSet].
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
        return getSet(getArmorTitle(helmet), getArmorTitle(chestplate), getArmorTitle(leggings), getArmorTitle(boots))
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
        for (set in ArmorMechanics.INSTANCE.sets.values) {
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

        val effects = ArmorMechanics.INSTANCE.effects
        effects[helmet]?.let { temp.add(it) }
        effects[chestplate]?.let { temp.add(it) }
        effects[leggings]?.let { temp.add(it) }
        effects[boots]?.let { temp.add(it) }
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
        val title = getArmorTitle(armor)

        // Check if we should delete this item.
        // If the armor no longer exists, we cannot update its properties.
        if (!ArmorMechanics.INSTANCE.armors.containsKey(title)) {
            val deleteOld = ArmorMechanics.INSTANCE.getConfig().getBoolean("Delete_Old_Armor", false)
            if (deleteOld) armor.amount = 0
            return
        }

        // We need to save the old durability and set it to the new item, since
        // setItemMeta will reset the item's durability.
        val durability = (armor.itemMeta as? org.bukkit.inventory.meta.Damageable)?.damage
        val template = ArmorMechanics.INSTANCE.armors[title]
        armor.setType(template!!.type)
        armor.setItemMeta(template.itemMeta)
        (armor.itemMeta as? org.bukkit.inventory.meta.Damageable)?.damage = durability!!
        Bukkit.getPluginManager().callEvent(ArmorUpdateEvent(entity!!, armor, title!!))
    }
}
