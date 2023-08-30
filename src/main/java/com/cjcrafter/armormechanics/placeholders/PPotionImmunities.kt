package com.cjcrafter.armormechanics.placeholders

import com.cjcrafter.armormechanics.ArmorMechanics
import me.deecaad.core.placeholder.ListPlaceholderHandler
import me.deecaad.core.placeholder.PlaceholderData

class PPotionImmunities : ListPlaceholderHandler("potion_immunities") {
    override fun requestValue(data: PlaceholderData): List<String>? {
        return ArmorMechanics.INSTANCE.effects[data.itemTitle()]?.immunities?.map { it.name }
    }
}