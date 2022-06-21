package me.cjcrafter.armormechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;

import javax.annotation.Nonnull;
import java.util.Set;

public class ArmorSet implements Serializer<ArmorSet> {

    private String bonus;
    private String helmet;
    private String chestplate;
    private String leggings;
    private String boots;

    public ArmorSet() {
    }

    public ArmorSet(String bonus, String helmet, String chestplate, String leggings, String boots) {
        this.bonus = bonus;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public String getBonus() {
        return bonus;
    }

    public String getHelmet() {
        return helmet;
    }

    public String getChestplate() {
        return chestplate;
    }

    public String getLeggings() {
        return leggings;
    }

    public String getBoots() {
        return boots;
    }

    @Nonnull
    @Override
    public ArmorSet serialize(SerializeData data) throws SerializerException {

        // Store the config keys in an array, so we can use a loop
        String[] temp = new String[]{ "Helmet", "Chestplate", "Leggings", "Boots" };
        Set<String> options = ArmorMechanics.INSTANCE.armors.keySet();
        boolean allNull = true;

        for (int i = 0; i < temp.length; i++) {
            String key = temp[i];

            // The title is allowed to be null, since a user may want to have a
            // set of armor that doesn't include, for example, a helmet.
            String title = data.of(key).assertType(String.class).get(null);
            if (title == null) {
                temp[i] = null;
                continue;
            }

            // Ensure that the requested armor-title matches to an existing
            // piece of armor.
            allNull = false;
            if (!options.contains(title))
                throw new SerializerOptionsException(this, "Armor Title", options, title, data.of(key).getLocation());

            // Store the title for the serialized object
            temp[i] = title;
        }

        if (allNull) {
            throw data.exception(null, "Helmet, Chestplate, Leggings, and Boots were all missing!",
                    "A set of armor should include at least 2 pieces of armor");
        }

        BonusEffect bonus = data.of("Bonus_Effects").assertExists().serialize(BonusEffect.class);
        ArmorMechanics.INSTANCE.effects.put(data.key, bonus);

        ArmorSet set = new ArmorSet(data.key, temp[0], temp[1], temp[2], temp[3]);
        ArmorMechanics.INSTANCE.sets.put(data.key, set);
        return set;
    }
}
