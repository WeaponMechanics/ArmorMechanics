package me.cjcrafter.armormechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BonusEffect implements Serializer<BonusEffect> {

    private List<PotionEffect> potions;
    private double bulletResistance;
    private DoubleMap<String> perWeaponResistances;
    private Set<PotionEffectType> immunities;
    private Mechanics equipMechanics;
    private Mechanics dequipMechanics;
    private Mechanics damageMechanics;

    public BonusEffect() {
    }

    public BonusEffect(List<PotionEffect> potions, double bulletResistance, DoubleMap<String> perWeaponResistances,
                       Set<PotionEffectType> immunities, Mechanics equipMechanics, Mechanics dequipMechanics,
                       Mechanics damageMechanics) {

        this.potions = potions;
        this.bulletResistance = bulletResistance;
        this.perWeaponResistances = perWeaponResistances;
        this.immunities = immunities;
        this.equipMechanics = equipMechanics;
        this.dequipMechanics = dequipMechanics;
        this.damageMechanics = damageMechanics;
    }

    public List<PotionEffect> getPotions() {
        return potions;
    }

    public double getBulletResistance() {
        return bulletResistance;
    }

    public double getBulletResistance(String weaponTitle) {
        if (perWeaponResistances.containsKey(weaponTitle))
            return perWeaponResistances.get(weaponTitle);

        return bulletResistance;
    }

    public Set<PotionEffectType> getImmunities() {
        return immunities;
    }

    public DoubleMap<String> getPerWeaponResistances() {
        return perWeaponResistances;
    }

    public Mechanics getEquipMechanics() {
        return equipMechanics;
    }

    public Mechanics getDequipMechanics() {
        return dequipMechanics;
    }

    public Mechanics getDamageMechanics() {
        return damageMechanics;
    }

    public void onDamage(EntityDamageEvent event) {
        if (damageMechanics == null)
            return;

        damageMechanics.use(new CastData(WeaponMechanics.getEntityWrapper((LivingEntity) event.getEntity())));
    }

    @Override
    public String getKeyword() {
        return "Bonus_Effects";
    }

    @Nonnull
    @Override
    public BonusEffect serialize(SerializeData data) throws SerializerException {

        // Uses the format: <PotionEffectType>-<Amplifier>-<Ambient>-<Hide>-<Icon>
        List<String[]> stringPotionList = data.ofList("Potion_Effects")
                .addArgument(PotionEffectType.class, true, true)
                .addArgument(int.class, true).assertArgumentPositive()
                .addArgument(boolean.class, false)
                .addArgument(boolean.class, false)
                .addArgument(boolean.class, false)
                .assertList().get();

        List<PotionEffect> potionEffectList = new ArrayList<>();
        for (String[] split : stringPotionList) {

            PotionEffectType potionEffectType = PotionEffectType.getByName(split[0].trim()); // auto applies lowercase
            if (potionEffectType == null)
                throw new SerializerOptionsException(this, "Potion Effect", Arrays.stream(PotionEffectType.values()).map(Object::toString).collect(Collectors.toList()), split[0], data.of().getLocation());

            int amplifier = Integer.parseInt(split[1]);
            boolean ambient = split.length > 2 ? Boolean.parseBoolean(split[2]) : false;
            boolean particles = split.length > 3 ? Boolean.parseBoolean(split[3]) : false;
            boolean icon = split.length > 4 ? Boolean.parseBoolean(split[4]) : false;

            if (ReflectionUtil.getMCVersion() < 14) {
                potionEffectList.add(new PotionEffect(potionEffectType, Integer.MAX_VALUE, amplifier, ambient, particles));// 84
            } else {
                potionEffectList.add(new PotionEffect(potionEffectType, Integer.MAX_VALUE, amplifier, ambient, particles, icon));// 86
            }
        }

        double bulletResistance = data.of("Bullet_Resistance.Base").assertRange(0.0, 1.0).getDouble(0.0);
        List<String[]> perWeaponData = data.ofList("Bullet_Resistance.Per_Weapon")
                .addArgument(String.class, true, true)
                .addArgument(double.class, true).assertArgumentRange(0.0, 1.0)
                .assertList().get();
        DoubleMap<String> perWeapon = new DoubleMap<>();

        for (int i = 0; i < perWeaponData.size(); i++) {
            String[] split = perWeaponData.get(i);

            String weapon = split[0];
            double resistance = Double.parseDouble(split[1]);

            // Check if the weapon exists
            List<String> allWeapons = WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList();
            if (!allWeapons.contains(weapon)) {
                throw new SerializerOptionsException(this, "Weapon", allWeapons, weapon, data.ofList("Bullet_Resistance.Per_Weapon").getLocation(i));
            }

            perWeapon.put(weapon, resistance);
        }

        List<String[]> immunitiesStringList = data.ofList("Immune_Potions")
                .addArgument(PotionEffectType.class, true, true)
                .assertList().get();

        List<PotionEffectType> immunities = new ArrayList<>();
        for (String[] split : immunitiesStringList) {

            PotionEffectType potionEffectType = PotionEffectType.getByName(split[0].trim());
            if (potionEffectType == null)
                throw new SerializerOptionsException(this, "Potion Effect", Arrays.stream(PotionEffectType.values()).map(Object::toString).collect(Collectors.toList()), split[0], data.of().getLocation());

            immunities.add(potionEffectType);
        }

        Mechanics equip = data.of("Equip_Mechanics").serialize(Mechanics.class);
        Mechanics dequip = data.of("Dequip_Mechanics").serialize(Mechanics.class);
        Mechanics damage = data.of("Damage_Mechanics").serialize(Mechanics.class);

        return new BonusEffect(potionEffectList, bulletResistance, perWeapon, new HashSet<>(immunities), equip, dequip, damage);
    }
}
