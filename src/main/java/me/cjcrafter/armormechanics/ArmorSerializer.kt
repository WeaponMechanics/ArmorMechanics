package me.cjcrafter.armormechanics

import me.deecaad.core.compatibility.CompatibilityAPI
import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.serializers.ItemSerializer
import me.deecaad.core.utils.AdventureUtil
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
        val title = data.key
        val effect = data.of("Bonus_Effects").serialize(BonusEffect::class.java)

        // Make sure the item knows what kind of armor it is
        CompatibilityAPI.getNBTCompatibility().setString(item, "ArmorMechanics", "armor-title", title)

        // Register the effects
        ArmorMechanics.INSTANCE.armors[title] = item
        ArmorMechanics.INSTANCE.effects[title] = effect
        AdventureUtil.setLore(item, AdventureUtil.getLore(item)!!)

        // Lore placeholder updating
        AdventureUtil.updatePlaceholders(null, item)
        return super.serializeRecipe(data, item)
    }

    companion object {
        fun isArmor(item: ItemStack): Boolean {
            val name = item.type.name

            // Let people turn off the isArmor() check since *technically*
            // any item can be equipped.
            return if (!ArmorMechanics.INSTANCE.getConfig()
                    .getBoolean("Prevent_Illegal_Armor", true)
            ) true else (name == "PLAYER_HEAD" || name == "CARVED_PUMPKIN" || name.endsWith(
                "_HELMET"
            ) || name.endsWith("_CHESTPLATE")
                    || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS"))
        }
    }
}
