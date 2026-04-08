package com.cjcrafter.armormechanics

import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.Serializer
import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.serializers.ItemSerializer
import me.deecaad.weaponmechanics.utils.CustomTag
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class Armor : Serializer<Armor> {

    lateinit var armorTitle: String
        private set
    private lateinit var itemStack: ItemStack
    private var itemModel: NamespacedKey? = null

    /**
     * Default constructor for serializer.
     */
    constructor()

    constructor(armorTitle: String, itemStack: ItemStack, itemModel: NamespacedKey? = null) {
        this.armorTitle = armorTitle
        this.itemStack = itemStack
        this.itemModel = itemModel
    }

    fun generateItemStack(): ItemStack {
        val item = itemStack.clone()
        val model = itemModel ?: return item
        if (!SUPPORTS_ITEM_MODEL) return item

        val meta = item.itemMeta ?: return item
        meta.setItemModel(model)
        item.setItemMeta(meta)
        return item
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

        // Parse optional Item_Model (requires Paper 1.21.4+)
        val itemModel: NamespacedKey? = data.of("Item_Model").getNamespacedKey().orElse(null)

        return Armor(title, item, itemModel)
    }

    companion object {
        /**
         * True when the server is running Paper 1.21.4 or newer, which added
         * the `item_model` data component and the corresponding
         * [ItemMeta.setItemModel] API method.
         */
        val SUPPORTS_ITEM_MODEL: Boolean = try {
            ItemMeta::class.java.getDeclaredMethod("setItemModel", NamespacedKey::class.java)
            true
        } catch (_: NoSuchMethodException) {
            false
        }
    }
}
