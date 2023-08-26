package me.cjcrafter.armormechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.defaultmechanics.PotionMechanic;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.WeaponMechanics;
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
    private Set<PotionEffectType> immunities;
    private Mechanics equipMechanics;
    private Mechanics dequipMechanics;
    private Mechanics damageMechanics;

    // WeaponMechanics things
    private double bulletResistance;
    private DoubleMap<String> perWeaponBulletResistances;
    private double explosionResistance;
    private DoubleMap<String> perWeaponExplosionResistances;

    public BonusEffect() {
    }

    public BonusEffect(List<PotionEffect> potions, Set<PotionEffectType> immunities, Mechanics equipMechanics,
                       Mechanics dequipMechanics, Mechanics damageMechanics, double bulletResistance,
                       DoubleMap<String> perWeaponBulletResistances, double explosionResistance, DoubleMap<String> perWeaponExplosionResistances) {

        this.potions = potions;
        this.immunities = immunities;
        this.equipMechanics = equipMechanics;
        this.dequipMechanics = dequipMechanics;
        this.damageMechanics = damageMechanics;

        this.bulletResistance = bulletResistance;
        this.perWeaponBulletResistances = perWeaponBulletResistances;
        this.explosionResistance = explosionResistance;
        this.perWeaponExplosionResistances = perWeaponExplosionResistances;
    }

    public List<PotionEffect> getPotions() {
        return potions;
    }

    public Set<PotionEffectType> getImmunities() {
        return immunities;
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

        damageMechanics.use(new CastData((LivingEntity) event.getEntity(), null, null));
    }

    public double getBulletResistance() {
        return bulletResistance;
    }

    public double getBulletResistance(String weaponTitle) {
        if (perWeaponBulletResistances.containsKey(weaponTitle))
            return perWeaponBulletResistances.get(weaponTitle);

        return bulletResistance;
    }

    public DoubleMap<String> getPerWeaponBulletResistances() {
        return perWeaponBulletResistances;
    }

    public double getExplosionResistance() {
        return bulletResistance;
    }

    public double getExplosionResistance(String weaponTitle) {
        if (perWeaponExplosionResistances.containsKey(weaponTitle))
            return perWeaponExplosionResistances.get(weaponTitle);

        return explosionResistance;
    }

    public DoubleMap<String> getPerWeaponExplosionResistances() {
        return perWeaponExplosionResistances;
    }

    @Override
    public String getKeyword() {
        return "Bonus_Effects";
    }

    @Nonnull
    @Override
    public BonusEffect serialize(SerializeData data) throws SerializerException {

        Mechanics mechanics = data.of("Potion_Effects").serialize(Mechanics.class);
        List<PotionEffect> potions = new ArrayList<>();

        if (mechanics != null) {
            List<Mechanic> list = mechanics.getMechanics();
            for (int i = 0; i < list.size(); i++) {
                if (!(list.get(i) instanceof PotionMechanic potion))
                    throw data.listException("Potion_Effects", i, "You can only use potion effects here",
                            SerializerException.forValue(list.get(i).getClass().getSimpleName()));

                // We want an infinite potion effect
                // TODO 1.20 has actual infinite instead of MAX_VALUE, check it out
                PotionEffect base = potion.getPotion();
                PotionEffect effect = new PotionEffect(base.getType(), Integer.MAX_VALUE, base.getAmplifier(), base.isAmbient(), base.hasParticles());
                if (ReflectionUtil.getMCVersion() > 13) {
                    effect = new PotionEffect(base.getType(), Integer.MAX_VALUE, base.getAmplifier(), base.isAmbient(), base.hasParticles(), base.hasIcon());
                }

                potions.add(effect);
            }
        }

        double bulletResistance = data.of("Bullet_Resistance.Base").assertRange(0.0, 2.0).getDouble(0.0);
        List<String[]> perWeaponData = data.ofList("Bullet_Resistance.Per_Weapon")
                .addArgument(String.class, true, true)
                .addArgument(double.class, true).assertArgumentRange(0.0, 2.0)
                .assertList().get();
        DoubleMap<String> perWeaponBulletResistances = new DoubleMap<>();

        for (int i = 0; i < perWeaponData.size(); i++) {
            String[] split = perWeaponData.get(i);

            String weapon = split[0];
            double resistance = Double.parseDouble(split[1]);

            perWeaponBulletResistances.put(weapon, resistance);
        }

        double explosionResistance = data.of("Explosion_Resistance.Base").assertRange(0.0, 2.0).getDouble(0.0);
        List<String[]> perWeaponExplosionData = data.ofList("Explosion_Resistance.Per_Weapon")
                .addArgument(String.class, true, true)
                .addArgument(double.class, true).assertArgumentRange(0.0, 2.0)
                .assertList().get();
        DoubleMap<String> perWeaponExplosionResistances = new DoubleMap<>();

        for (int i = 0; i < perWeaponExplosionData.size(); i++) {
            String[] split = perWeaponData.get(i);

            String weapon = split[0];
            double resistance = Double.parseDouble(split[1]);

            perWeaponExplosionResistances.put(weapon, resistance);
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

        return new BonusEffect(potions, new HashSet<>(immunities), equip, dequip, damage, bulletResistance,
                perWeaponBulletResistances, explosionResistance, perWeaponExplosionResistances);
    }
}
