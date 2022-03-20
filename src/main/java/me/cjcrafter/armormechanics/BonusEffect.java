package me.cjcrafter.armormechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.utils.ReflectionUtil;
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
    private Set<PotionEffectType> immunities;


    public BonusEffect() {
    }

    public BonusEffect(List<PotionEffect> potions, double bulletResistance, Set<PotionEffectType> immunities) {
        this.potions = potions;
        this.bulletResistance = bulletResistance;
        this.immunities = immunities;
    }

    public List<PotionEffect> getPotions() {
        return potions;
    }

    public double getBulletResistance() {
        return bulletResistance;
    }

    public Set<PotionEffectType> getImmunities() {
        return immunities;
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

        double bulletResistance = data.of("Bullet_Resistance").assertRange(0.0, 1.0).getDouble();

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

        return new BonusEffect(potionEffectList, bulletResistance, new HashSet<>(immunities));
    }
}
