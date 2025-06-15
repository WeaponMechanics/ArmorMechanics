package com.cjcrafter.armormechanics

import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.Serializer
import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.serializers.ItemSerializer
import me.deecaad.weaponmechanics.utils.CustomTag
import org.bukkit.inventory.ItemStack

class Armor : Serializer<Armor> {

    lateinit var armorTitle: String
        private set
    private lateinit var itemStack: ItemStack

    /**
     * Default constructor for serializer.
     */
    constructor()

    constructor(armorTitle: String, itemStack: ItemStack) {
        this.armorTitle = armorTitle
        this.itemStack = itemStack
    }

    fun generateItemStack(): ItemStack {
        return itemStack.clone()
    }

    @Throws(SerializerException::class)
    override fun serialize(data: SerializeData): Armor {
        val itemSerializer = ItemSerializer()
        var item = itemSerializer.serializeWithoutRecipe(data)

        if (ArmorMechanicsAPI.guessEquipmentSlot(item) == null) {
            throw data.exception(
                "Equippable",
                "Item was not an equippable item. For \"non standard armor,\" please include the 'Equippable' field",
                "For value: ${item.type}"
            )
        }

        // Make sure the item knows what kind of armor it is
        val title = data.key!!
        CustomTag.ARMOR_TITLE.setString(item, title)
        item = itemSerializer.serializeRecipe(data, item)

        return Armor(title, item)
    }
}
