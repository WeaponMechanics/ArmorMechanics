package me.cjcrafter.armormechanics

import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.Serializer
import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SerializerOptionsException
import me.deecaad.core.mechanics.CastData
import me.deecaad.core.mechanics.Mechanics
import me.deecaad.core.mechanics.defaultmechanics.PotionMechanic
import me.deecaad.core.utils.ReflectionUtil
import me.deecaad.core.utils.primitive.DoubleMap
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.stream.Collectors
import javax.annotation.Nonnull
import kotlin.collections.ArrayList

class BonusEffect : Serializer<BonusEffect> {
    var potions: List<PotionEffect> = ArrayList()
    var immunities: Set<PotionEffectType> = HashSet()
    var equipMechanics: Mechanics? = null
    var dequipMechanics: Mechanics? = null
    var damageMechanics: Mechanics? = null

    // WeaponMechanics things
    var bulletResistance = 0.0
    var perWeaponBulletResistances: Map<String, Double> = HashMap()
    var explosionResistance = 0.0
    var perWeaponExplosionResistances: Map<String, Double> = HashMap()

    constructor()
    constructor(
        potions: List<PotionEffect>,
        immunities: Set<PotionEffectType>,
        equipMechanics: Mechanics?,
        dequipMechanics: Mechanics?,
        damageMechanics: Mechanics?,
        bulletResistance: Double,
        perWeaponBulletResistances: Map<String, Double>,
        explosionResistance: Double,
        perWeaponExplosionResistances: Map<String, Double>
    ) {
        this.potions = potions
        this.immunities = immunities
        this.equipMechanics = equipMechanics
        this.dequipMechanics = dequipMechanics
        this.damageMechanics = damageMechanics
        this.bulletResistance = bulletResistance
        this.perWeaponBulletResistances = perWeaponBulletResistances
        this.explosionResistance = explosionResistance
        this.perWeaponExplosionResistances = perWeaponExplosionResistances
    }

    fun onDamage(event: EntityDamageEvent) {
        if (damageMechanics == null) return
        damageMechanics!!.use(CastData(event.entity as LivingEntity, null, null))
    }

    fun getBulletResistance(weaponTitle: String?): Double {
        return perWeaponBulletResistances[weaponTitle] ?: bulletResistance
    }

    fun getExplosionResistance(weaponTitle: String?): Double {
        return perWeaponExplosionResistances[weaponTitle] ?: explosionResistance
    }

    override fun getKeyword(): String {
        return "Bonus_Effects"
    }

    @Nonnull
    @Throws(SerializerException::class)
    override fun serialize(data: SerializeData): BonusEffect {
        val mechanics = data.of("Potion_Effects").serialize(Mechanics::class.java)
        val potions: MutableList<PotionEffect> = ArrayList()
        if (mechanics != null) {
            val list = mechanics.mechanics
            for (i in list.indices) {
                if (list[i] !is PotionMechanic) throw data.listException(
                    "Potion_Effects", i, "You can only use potion effects here",
                    SerializerException.forValue(list[i].javaClass.getSimpleName())
                )

                // We want an infinite potion effect
                // TODO 1.20 has actual infinite instead of MAX_VALUE, check it out
                val base: PotionEffect = (list[i] as PotionMechanic).potion
                var effect = PotionEffect(base.type, Int.MAX_VALUE, base.amplifier, base.isAmbient, base.hasParticles())
                if (ReflectionUtil.getMCVersion() > 13) {
                    effect = PotionEffect(
                        base.type,
                        Int.MAX_VALUE,
                        base.amplifier,
                        base.isAmbient,
                        base.hasParticles(),
                        base.hasIcon()
                    )
                }
                potions.add(effect)
            }
        }
        val bulletResistance = data.of("Bullet_Resistance.Base").assertRange(0.0, 2.0).getDouble(0.0)
        val perWeaponData = data.ofList("Bullet_Resistance.Per_Weapon")
            .addArgument(String::class.java, true, true)
            .addArgument(Double::class.javaPrimitiveType, true).assertArgumentRange(0.0, 2.0)
            .assertList().get()

        val perWeaponBulletResistances = HashMap<String, Double>()
        for (i in perWeaponData.indices) {
            val split = perWeaponData[i]
            val weapon = split[0]
            val resistance = split[1].toDouble()
            perWeaponBulletResistances[weapon] = resistance
        }

        val explosionResistance = data.of("Explosion_Resistance.Base").assertRange(0.0, 2.0).getDouble(0.0)
        val perWeaponExplosionData = data.ofList("Explosion_Resistance.Per_Weapon")
            .addArgument(String::class.java, true, true)
            .addArgument(Double::class.javaPrimitiveType, true).assertArgumentRange(0.0, 2.0)
            .assertList().get()

        val perWeaponExplosionResistances = HashMap<String, Double>()
        for (i in perWeaponExplosionData.indices) {
            val split = perWeaponData[i]
            val weapon = split[0]
            val resistance = split[1].toDouble()
            perWeaponExplosionResistances[weapon] = resistance
        }

        val immunitiesStringList = data.ofList("Immune_Potions")
            .addArgument(PotionEffectType::class.java, true, true)
            .assertList().get()
        val immunities: MutableList<PotionEffectType> = ArrayList()
        for (split in immunitiesStringList) {
            val potionEffectType = PotionEffectType.getByName(split[0].trim { it <= ' ' })
                ?: throw SerializerOptionsException(
                    this,
                    "Potion Effect",
                    Arrays.stream(PotionEffectType.values()).map { obj: PotionEffectType -> obj.toString() }
                        .collect(Collectors.toList()),
                    split[0],
                    data.of().location)
            immunities.add(potionEffectType)
        }

        val equip = data.of("Equip_Mechanics").serialize(Mechanics::class.java)
        val dequip = data.of("Dequip_Mechanics").serialize(Mechanics::class.java)
        val damage = data.of("Damage_Mechanics").serialize(Mechanics::class.java)

        return BonusEffect(
            potions, HashSet(immunities), equip, dequip, damage, bulletResistance,
            perWeaponBulletResistances, explosionResistance, perWeaponExplosionResistances
        )
    }
}
