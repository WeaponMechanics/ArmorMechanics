package com.cjcrafter.armormechanics

import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.Serializer
import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SerializerOptionsException
import javax.annotation.Nonnull

class ArmorSet : Serializer<ArmorSet> {
    lateinit var bonus: BonusEffect
    var helmet: String? = null
    var chestplate: String? = null
    var leggings: String? = null
    var boots: String? = null

    constructor()
    constructor(bonus: BonusEffect, helmet: String?, chestplate: String?, leggings: String?, boots: String?) {
        this.bonus = bonus
        this.helmet = helmet
        this.chestplate = chestplate
        this.leggings = leggings
        this.boots = boots
    }

    @Nonnull
    @Throws(SerializerException::class)
    override fun serialize(data: SerializeData): ArmorSet {

        // Store the config keys in an array, so we can use a loop
        val temp = arrayOf<String?>("Helmet", "Chestplate", "Leggings", "Boots")
        val options: Set<String> = ArmorMechanics.INSTANCE.armors.keys
        var allNull = true
        for (i in temp.indices) {
            val key = temp[i]

            // The title is allowed to be null, since a user may want to have a
            // set of armor that doesn't include, for example, a helmet.
            val title = data.of(key).assertType(String::class.java).get<String?>(null)
            if (title == null) {
                temp[i] = null
                continue
            }

            // Ensure that the requested armor-title matches to an existing
            // piece of armor.
            allNull = false
            if (!options.contains(title)) throw SerializerOptionsException(
                this,
                "Armor Title",
                options,
                title,
                data.of(key).location
            )

            // Store the title for the serialized object
            temp[i] = title
        }

        if (allNull) {
            throw data.exception(
                null, "Helmet, Chestplate, Leggings, and Boots were all missing!",
                "A set of armor should include at least 2 pieces of armor"
            )
        }

        val bonus = data.of("Bonus_Effects").assertExists().serialize(BonusEffect::class.java)
        ArmorMechanics.INSTANCE.effects[data.key] = bonus
        val set = ArmorSet(bonus, temp[0], temp[1], temp[2], temp[3])
        ArmorMechanics.INSTANCE.sets[data.key] = set
        return set
    }
}
