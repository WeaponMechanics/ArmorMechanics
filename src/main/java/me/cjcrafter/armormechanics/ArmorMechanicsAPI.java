package me.cjcrafter.armormechanics;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.Debugger;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArmorMechanicsAPI {


    /**
     * Returns the armor-title associated with the given armor, or returns
     * <code>null</code> if the given item doesn't have a title.
     *
     * @param armor The nullable item to check.
     * @return The armor-title, or null.
     */
    public static String getArmorTitle(ItemStack armor) {
        if (armor == null || !armor.hasItemMeta())
            return null;

        return CompatibilityAPI.getNBTCompatibility().getString(armor, "ArmorMechanics", "armor-title");
    }

    @Nonnull
    public static ItemStack generateArmor(String armorTitle) {
        if (armorTitle == null || !ArmorMechanics.INSTANCE.armors.containsKey(armorTitle))
            throw new IllegalArgumentException("Unknown armor-title '" + armorTitle + "'");

        return ArmorMechanics.INSTANCE.armors.get(armorTitle).clone();
    }

    /**
     * Returns the expected {@link EquipmentSlot} that the given material
     * would be worn on. However, using commands or plugins may allow players
     * to equip armor in other slots.
     *
     * @param mat The non-null material to check.
     * @return The associated equipment slot, or null.
     */
    public static EquipmentSlot getEquipmentSlot(@Nonnull Material mat) {
        String name = mat.name();
        if (name.endsWith("BOOTS"))
            return EquipmentSlot.FEET;
        if (name.endsWith("LEGGINGS"))
            return EquipmentSlot.LEGS;
        if (name.endsWith("CHESTPLATE"))
            return EquipmentSlot.CHEST;
        if (name.endsWith("HELMET") || name.equals("PLAYER_HEAD"))
            return EquipmentSlot.HEAD;

        return null;
    }

    /**
     * Version safe method for {@link EntityEquipment#getItem(EquipmentSlot)}.
     *
     * @param equipment The non-null equipment the entity is wearing.
     * @param slot      The non-null slot to check.
     * @return The nullable item in that slot.
     */
    @Nullable
    public static ItemStack getItem(@Nonnull EntityEquipment equipment, @Nonnull EquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return equipment.getHelmet();
            case CHEST:
                return equipment.getChestplate();
            case LEGS:
                return equipment.getLeggings();
            case FEET:
                return equipment.getBoots();
            default:
                return null;
        }
    }

    /**
     * Version safe method for {@link EntityEquipment#setItem(EquipmentSlot, ItemStack)}.
     *
     * @param equipment The non-null equipment the entity is wearing.
     * @param slot      The non-null slot to set.
     * @param item      The item to use, or null.
     */
    public static void setItem(@Nonnull EntityEquipment equipment, @Nonnull EquipmentSlot slot, ItemStack item) {
        switch (slot) {
            case HEAD:
                equipment.setHelmet(item);
                break;
            case CHEST:
                equipment.setChestplate(item);
                break;
            case LEGS:
                equipment.setLeggings(item);
                break;
            case FEET:
                equipment.setBoots(item);
                break;
        }
    }

    /**
     * Shorthand for {@link #getSet(String, String, String, String)}.
     *
     * @param entity The non-null entity to check.
     * @return The set the entity is wearing, or null.
     */
    public static ArmorSet getSet(Entity entity) {
        if (entity.getType().isAlive())
            return null;

        return getSet(((LivingEntity) entity).getEquipment());
    }

    /**
     * Shorthand for {@link #getSet(String, String, String, String)}.
     *
     * @param equipment The equipment the entity is wearing.
     * @return The set the entity is wearing, or null.
     */
    public static ArmorSet getSet(EntityEquipment equipment) {
        if (equipment == null)
            return null;

        String helmet = getArmorTitle(equipment.getHelmet());
        String chestplate = getArmorTitle(equipment.getChestplate());
        String leggings = getArmorTitle(equipment.getLeggings());
        String boots = getArmorTitle(equipment.getBoots());

        return getSet(helmet, chestplate, leggings, boots);
    }

    /**
     * Shorthand for {@link #getSet(String, String, String, String)}.
     *
     * @param helmet The nullable helmet to check.
     * @param chestplate The nullable chestplate to check.
     * @param leggings The nullable leggings to check.
     * @param boots The nullable boots to check.
     * @return The set associated with the 4 items, or null.
     */
    public static ArmorSet getSet(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        return getSet(getArmorTitle(helmet), getArmorTitle(chestplate), getArmorTitle(leggings), getArmorTitle(boots));
    }

    /**
     * Returns the {@link ArmorSet} that matches the 4 pieces of armor. Some
     * important things to realize, players are able to use 3 pieces of armor
     * for a set, and use 1 as a wildcard, if the server admin configured this.
     *
     * <p>1 piece of armor may belong to multiple sets. Although a server admin
     * would be evil to do this, and probably should be locked up in an insane
     * asylum, it's something to look out for.
     *
     * <p>You should also carefully consider when you call this method for an
     * entity. It's equipment may have already updates, or it may not have
     * updated. If you run into issues running this method, make sure you print
     * out the armor so you check if it is what you expect it to be.
     *
     * @param helmet The nullable helmet to check.
     * @param chestplate The nullable chestplate to check.
     * @param leggings The nullable leggings to check.
     * @param boots The nullable boots to check.
     * @return The set associated with the 4 titles, or null.
     */
    public static ArmorSet getSet(String helmet, String chestplate, String leggings, String boots) {

        // Since an armor title may belong to multiple sets, we must check each
        // set to determine whether the player is wearing a set.
        ArmorMechanics plugin = ArmorMechanics.INSTANCE;
        for (ArmorSet set : plugin.sets.values()) {
            if (set.getHelmet() != null && !set.getHelmet().equals(helmet))
                continue;
            if (set.getChestplate() != null && !set.getChestplate().equals(chestplate))
                continue;
            if (set.getLeggings() != null && !set.getLeggings().equals(leggings))
                continue;
            if (set.getBoots() != null && !set.getBoots().equals(boots))
                continue;

            return set;
        }

        return null;
    }

    /**
     * Adds up the total bullet resistance of all armor pieces and the set
     * associated with the given equipment. The returned number is a percentage
     * damage reduction (0.0 = no protection, 1.0 = full protection). The value
     * will always be between 0.0 (inclusive) and 1.0 (inclusive).
     *
     * @param equipment The nullable entity equipment.
     * @param weaponTitle The non-null weapon used to damage the entity.
     * @return A percentage damage reduction, or 0.0.
     */
    public static double getBulletResistance(@Nullable EntityEquipment equipment, @Nonnull String weaponTitle) {
        if (equipment == null)
            return 0.0;

        ArmorMechanics plugin = ArmorMechanics.INSTANCE;
        BonusEffect helmet = plugin.effects.get(getArmorTitle(equipment.getHelmet()));
        BonusEffect chestplate = plugin.effects.get(getArmorTitle(equipment.getChestplate()));
        BonusEffect leggings = plugin.effects.get(getArmorTitle(equipment.getLeggings()));
        BonusEffect boots = plugin.effects.get(getArmorTitle(equipment.getBoots()));
        ArmorSet set = getSet(equipment);

        double rate = 0.0;
        if (set != null && set.getBonus() != null)
            rate += plugin.effects.get(set.getBonus()).getBulletResistance(weaponTitle);
        if (helmet != null)
            rate += helmet.getBulletResistance(weaponTitle);
        if (chestplate != null)
            rate += chestplate.getBulletResistance(weaponTitle);
        if (leggings != null)
            rate += leggings.getBulletResistance(weaponTitle);
        if (boots != null)
            rate += boots.getBulletResistance(weaponTitle);

        return NumberUtil.minMax(0.0, rate, 1.0);
    }

    /**
     * Returns <code>true</code> if the entity's armor or {@link ArmorSet} is
     * immune to the given potion effect.
     *
     * @param equipment The nullable equipment.
     * @param type The non-null potion effect type.
     * @return true if the entity is immune.
     */
    public static boolean isImmune(@Nullable EntityEquipment equipment, @Nonnull PotionEffectType type) {
        if (equipment == null)
            return false;

        ArmorMechanics plugin = ArmorMechanics.INSTANCE;
        BonusEffect helmet = plugin.effects.get(getArmorTitle(equipment.getHelmet()));
        BonusEffect chestplate = plugin.effects.get(getArmorTitle(equipment.getChestplate()));
        BonusEffect leggings = plugin.effects.get(getArmorTitle(equipment.getLeggings()));
        BonusEffect boots = plugin.effects.get(getArmorTitle(equipment.getBoots()));
        ArmorSet set = getSet(equipment);

        if (set != null && set.getBonus() != null && plugin.effects.get(set.getBonus()).getImmunities().contains(type))
            return true;
        if (helmet != null && helmet.getImmunities().contains(type))
            return true;
        if (chestplate != null && chestplate.getImmunities().contains(type))
            return true;
        if (leggings != null && leggings.getImmunities().contains(type))
            return true;
        if (boots != null && boots.getImmunities().contains(type))
            return true;

        return false;
    }

    /**
     * The server admin may have to make balance changes <i>after</i> they have
     * distributed their armor. Since attributes/enchantments are set PER ITEM,
     * we have to update items to make sure they have the proper stats.
     * ArmorMechanics calls this method whenever armor is equipped to an armor
     * slot.
     *
     * <p>The following options are updated: Attributes, Enchantments, Display
     * name, Lore, Unbreakable, Material Type, Player Skull.
     *
     * @param armor The non-null item to update.
     */
    public static void update(ItemStack armor) {
        Debugger debug = ArmorMechanics.INSTANCE.debug;
        String title = getArmorTitle(armor);

        // Check if we should delete this item.
        // If the armor no longer exists, we cannot update its properties.
        if (!ArmorMechanics.INSTANCE.armors.containsKey(title)) {
            boolean deleteOld = ArmorMechanics.INSTANCE.getConfig().getBoolean("Delete_Old_Armor", false);

            if (deleteOld)
                armor.setAmount(0);

            return;
        }

        // We need to save the old durability and set it to the new item, since
        // setItemMeta will reset the item's durability.
        short durability = armor.getDurability();

        ItemStack template = ArmorMechanics.INSTANCE.armors.get(title);
        armor.setType(template.getType());
        armor.setItemMeta(template.getItemMeta());
        armor.setDurability(durability);
    }
}
