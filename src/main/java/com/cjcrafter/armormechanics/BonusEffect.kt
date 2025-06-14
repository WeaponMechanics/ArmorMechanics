package com.cjcrafter.armormechanics

import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.Serializer
import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.simple.DoubleSerializer
import me.deecaad.core.file.simple.RegistryValueSerializer
import me.deecaad.core.file.simple.StringSerializer
import me.deecaad.core.mechanics.CastData
import me.deecaad.core.mechanics.MechanicManager
import me.deecaad.core.mechanics.defaultmechanics.PotionMechanic
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.jvm.optionals.getOrNull

class BonusEffect : Serializer<BonusEffect> {
    var potions: List<PotionEffect> = ArrayList()
    var immunities: Set<PotionEffectType> = HashSet()
    var equipMechanics: MechanicManager? = null
    var dequipMechanics: MechanicManager? = null
    var damageMechanics: MechanicManager? = null

    // WeaponMechanics things
    var bulletResistance = 0.0
    var perWeaponBulletResistances: Map<String, Double> = HashMap()
    var explosionResistance = 0.0
    var perWeaponExplosionResistances: Map<String, Double> = HashMap()

    /**
     * Default constructor for serializer.
     */
    constructor()

    constructor(
        potions: List<PotionEffect>,
        immunities: Set<PotionEffectType>,
        equipMechanics: MechanicManager?,
        dequipMechanics: MechanicManager?,
        damageMechanics: MechanicManager?,
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

    @Throws(SerializerException::class)
    override fun serialize(data: SerializeData): BonusEffect {
        val mechanics = data.of("Potion_Effects").serialize(MechanicManager::class.java).getOrNull()
        val potions: MutableList<PotionEffect> = ArrayList()
        if (mechanics != null) {
            val list = mechanics.mechanics
            for (i in list.indices) {
                if (list[i] !is PotionMechanic) throw data.listException(
                    "Potion_Effects", i, "You can only use potion effects here",
                    "For value: ${list[i].javaClass.getSimpleName()}"
                )

                // We want an infinite potion effect
                // TODO 1.20 has actual infinite instead of MAX_VALUE, check it out
                val base: PotionEffect = (list[i] as PotionMechanic).potion
                val effect = PotionEffect(
                    base.type,
                    PotionEffect.INFINITE_DURATION,
                    base.amplifier,
                    base.isAmbient,
                    base.hasParticles(),
                    base.hasIcon()
                )
                potions.add(effect)
            }
        }
        val bulletResistance = data.of("Bullet_Resistance.Base").assertRange(0.0, 2.0).getDouble().orElse(0.0)
        val perWeaponData = data.ofList("Bullet_Resistance.Per_Weapon")
            .addArgument(StringSerializer())
            .addArgument(DoubleSerializer(0.0, 2.0))
            .requireAllPreviousArgs()
            .assertList()

        val perWeaponBulletResistances = HashMap<String, Double>()
        for (i in perWeaponData.indices) {
            val split = perWeaponData[i]
            val weapon = split[0].get() as String
            val resistance = split[1].get() as Double
            perWeaponBulletResistances[weapon] = resistance
        }

        val explosionResistance = data.of("Explosion_Resistance.Base").assertRange(0.0, 2.0).getDouble().orElse(0.0)
        val perWeaponExplosionData = data.ofList("Explosion_Resistance.Per_Weapon")
            .addArgument(StringSerializer())
            .addArgument(DoubleSerializer(0.0, 2.0))
            .requireAllPreviousArgs()
            .assertList()

        val perWeaponExplosionResistances = HashMap<String, Double>()
        for (i in perWeaponExplosionData.indices) {
            val split = perWeaponData[i]
            val weapon = split[0].get() as String
            val resistance = split[1].get() as Double
            perWeaponExplosionResistances[weapon] = resistance
        }

        val immunitiesStringList = data.ofList("Immune_Potions")
            .addArgument(RegistryValueSerializer(PotionEffectType::class.java, true))
            .requireAllPreviousArgs()
            .assertList()
        val immunities: MutableList<PotionEffectType> = ArrayList()
        for (split in immunitiesStringList) {
            immunities.addAll(split[0].get() as List<PotionEffectType>)
        }

        val equip = data.of("Equip_Mechanics").serialize(MechanicManager::class.java).getOrNull()
        val dequip = data.of("Dequip_Mechanics").serialize(MechanicManager::class.java).getOrNull()
        val damage = data.of("Damage_Mechanics").serialize(MechanicManager::class.java).getOrNull()

        return BonusEffect(
            potions, HashSet(immunities), equip, dequip, damage, bulletResistance,
            perWeaponBulletResistances, explosionResistance, perWeaponExplosionResistances
        )
    }
}
