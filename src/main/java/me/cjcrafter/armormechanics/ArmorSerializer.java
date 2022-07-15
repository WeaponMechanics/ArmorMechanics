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


        ///give @p minecraft:player_head{display:{Name:"{\"text\":\"Biohazard Suit\"}"},SkullOwner:{Id:"970e0a59-b95d-45a9-9039-b43ac4fbfc7c",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTA1NjQ4MTdmY2M4ZGQ1MWJjMTk1N2MwYjdlYTE0MmRiNjg3ZGQ2ZjFjYWFmZDM1YmI0ZGNmZWU1OTI0MjFjIn19fQ=="}]}}} 1
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

        return name.equals("PLAYER_HEAD")
                || name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
}
