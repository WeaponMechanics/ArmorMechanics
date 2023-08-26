package me.cjcrafter.armormechanics;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.AdventureUtil;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ArmorSerializer extends ItemSerializer {

    // Default constructor for serializer
    public ArmorSerializer() {
    }

    @Nonnull
    @Override
    public ItemStack serialize(SerializeData data) throws SerializerException {
        ItemStack item = super.serializeWithoutRecipe(data);

        if (!isArmor(item))
            throw data.exception("Type", "Material was not a valid armor type", SerializerException.forValue(item.getType()));

        String title = data.key;
        BonusEffect effect = data.of("Bonus_Effects").serialize(BonusEffect.class);

        // Make sure the item knows what kind of armor it is
        CompatibilityAPI.getNBTCompatibility().setString(item, "ArmorMechanics", "armor-title", title);

        // Register the effects
        ArmorMechanics.INSTANCE.armors.put(title, item);
        ArmorMechanics.INSTANCE.effects.put(title, effect);

        AdventureUtil.setLore(item, AdventureUtil.getLore(item));

        // Lore placeholder updating
        AdventureUtil.updatePlaceholders(null, item);

        return super.serializeRecipe(data, item);
    }

    public static boolean isArmor(ItemStack item) {
        String name = item.getType().name();

        // Let people turn off the isArmor() check since *technically*
        // any item can be equipped.
        if (!ArmorMechanics.INSTANCE.getConfig().getBoolean("Prevent_Illegal_Armor", true))
            return true;

        return name.equals("PLAYER_HEAD") || name.equals("CARVED_PUMPKIN")
                || name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
}
