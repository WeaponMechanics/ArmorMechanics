package com.cjcrafter.armormechanics.placeholders

import com.cjcrafter.armormechanics.ArmorMechanics
import com.cjcrafter.armormechanics.BonusEffect
import me.deecaad.core.placeholder.NumericPlaceholderHandler
import me.deecaad.core.placeholder.PlaceholderData
import org.bukkit.NamespacedKey

class PExplosionResistance : NumericPlaceholderHandler() {

    override fun getKey() = NamespacedKey(ArmorMechanics.getInstance(), "explosion_resistance")

    override fun requestValue(data: PlaceholderData): Number? {
        val config = ArmorMechanics.getInstance().armorConfigurations
        val bonusEffect = config.get<BonusEffect>("${data.itemTitle()}.Bonus_Effects")
        return bonusEffect?.explosionResistance
    }
}