package com.cjcrafter.armormechanics

import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.serializers.ItemSerializer
import me.deecaad.weaponmechanics.utils.CustomTag
import org.bukkit.inventory.ItemStack
import javax.annotation.Nonnull
import kotlin.jvm.optionals.getOrNull

class ArmorSerializer : ItemSerializer() {

    @Nonnull
    @Throws(SerializerException::class)
    override fun serialize(data: SerializeData): ItemStack {
        val item = super.serializeWithoutRecipe(data)
        if (!isArmor(item)) throw data.exception(
            "Type",
            "Material was not a valid armor type",
            "For value: ${item.type}"
        )
        val title = data.key!!
        val effect = data.of("Bonus_Effects").serialize(BonusEffect::class.java).getOrNull()

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
            if (!ArmorMechanics.INSTANCE.config.getBoolean("Prevent_Illegal_Armor", true))
                return true

            return name == "PLAYER_HEAD" || name == "CARVED_PUMPKIN" || name.endsWith("_HELMET")
                    || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
        }
    }
}
