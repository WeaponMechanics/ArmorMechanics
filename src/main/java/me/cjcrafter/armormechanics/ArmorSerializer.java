package me.cjcrafter.armormechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ArmorSerializer extends ItemSerializer {

    @Nonnull
    @Override
    public ItemStack serialize(SerializeData data) throws SerializerException {
        ItemStack item = super.serialize(data);

        if (!isArmor(item))
            throw data.exception("Type", "Material was not a valid armor type", SerializerException.forValue(item.getType()));


    }

    public static boolean isArmor(ItemStack item) {
        String name = item.getType().name();

        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
}
