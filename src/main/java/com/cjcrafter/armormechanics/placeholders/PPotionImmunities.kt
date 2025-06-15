package com.cjcrafter.armormechanics.placeholders

import com.cjcrafter.armormechanics.ArmorMechanics
import com.cjcrafter.armormechanics.BonusEffect
import me.deecaad.core.placeholder.ListPlaceholderHandler
import me.deecaad.core.placeholder.PlaceholderData
import org.bukkit.NamespacedKey

class PPotionImmunities : ListPlaceholderHandler() {

    override fun getKey() = NamespacedKey(ArmorMechanics.getInstance(), "potion_immunities")

    override fun requestValue(data: PlaceholderData): List<String>? {
        val config = ArmorMechanics.getInstance().armorConfigurations
        val bonusEffect = config.get<BonusEffect>("${data.itemTitle()}.Bonus_Effects")
        return bonusEffect?.immunities?.map { it.name }
    }
}