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
        if (ArmorMechanicsAPI.guessEquipmentSlot(item) == null) {
            throw data.exception(
                "Equippable",
                "Item was not an equippable item. For \"non standard armor,\" please include the 'Equippable' field",
                "For value: ${item.type}"
            )
        }
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
}
