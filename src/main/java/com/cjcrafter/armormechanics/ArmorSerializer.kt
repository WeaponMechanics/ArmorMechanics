package com.cjcrafter.armormechanics

import  com.cjcrafter.armormechanics.durability.DurabilityManager.setDurability
import com.cjcrafter.armormechanics.durability.DurabilityManager.setMaxDurability
import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.serializers.ItemSerializer
import me.deecaad.core.lib.adventure.text.serializer.legacy.LegacyComponentSerializer
import me.deecaad.core.placeholder.PlaceholderData
import me.deecaad.core.placeholder.PlaceholderMessage
import me.deecaad.weaponmechanics.utils.CustomTag
import org.bukkit.inventory.ItemStack
import javax.annotation.Nonnull

class ArmorSerializer : ItemSerializer() {

    @Nonnull
    @Throws(SerializerException::class)
    override fun serialize(data: SerializeData): ItemStack {
        val item = super.serializeWithoutRecipe(data)
        if (!isArmor(item)) throw data.exception(
            "Type",
            "Material was not a valid armor type",
            SerializerException.forValue(item.type)
        )


        if (data.has("Item_Durability")) {
            val durability = data.of("Item_Durability").getInt(0)
            if (durability > 0) {
                setDurability(item, durability)
                setMaxDurability(item, durability)
                val meta = item.itemMeta
                val lore = ArrayList<String>()
                if (meta!!.hasLore()) {
                    lore.addAll(meta.lore!!)
                }
                val placeholderMessage =  PlaceholderMessage(ArmorMechanics.DURABILITY_FORMAT)
                val component = placeholderMessage.replaceAndDeserialize(PlaceholderData.of(null, item, null, null))
                val legacy = LegacyComponentSerializer.legacySection().serialize(component)
                lore.add(ArmorMechanics.DURABILITY_PREFIX + legacy)
                meta.lore = lore
                item.itemMeta = meta
            }
        }



        val title = data.key
        val effect = data.of("Bonus_Effects").serialize(BonusEffect::class.java)

        // Make sure the item knows what kind of armor it is
        CustomTag.ARMOR_TITLE.setString(item, title)

        // Register the effects
        ArmorMechanics.INSTANCE.armors[title] = item
        if (effect != null) ArmorMechanics.INSTANCE.effects[title] = effect

        // Lore placeholder updating
        return super.serializeRecipe(data, item)
    }

    companion object {
        fun isArmor(item: ItemStack): Boolean {
            val name = item.type.name

            // Let people turn off the isArmor() check since *technically*
            // any item can be equipped.
            if (!ArmorMechanics.INSTANCE.getConfig().getBoolean("Prevent_Illegal_Armor", true))
                return true

            return name == "PLAYER_HEAD" || name == "CARVED_PUMPKIN" || name.endsWith("_HELMET")
                    || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
        }
    }
}
