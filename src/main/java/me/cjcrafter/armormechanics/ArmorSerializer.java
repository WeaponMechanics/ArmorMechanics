package me.cjcrafter.armormechanics;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ArmorSerializer extends ItemSerializer {

    // Default constructor for serializer
    public ArmorSerializer() {
    }

    @Nonnull
    @Override
    public ItemStack serialize(SerializeData data) throws SerializerException {
        ItemStack item = super.serialize(data);

        if (!isArmor(item))
            throw data.exception("Type", "Material was not a valid armor type", SerializerException.forValue(item.getType()));

        String title = data.key;
        BonusEffect effect = data.of("Bonus_Effects").serialize(BonusEffect.class);

        // Make sure the item knows what kind of armor it is
        CompatibilityAPI.getNBTCompatibility().setString(item, "ArmorMechanics", "armor-title", title);

        // Register the effects
        ArmorMechanics.INSTANCE.armors.put(title, item);
        ArmorMechanics.INSTANCE.effects.put(title, effect);

        return item;
    }

    public static boolean isArmor(ItemStack item) {
        String name = item.getType().name();

        return name.equals("PLAYER_HEAD") || name.equals("CARVED_PUMPKIN")
                || name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
}
