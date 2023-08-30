package com.cjcrafter.armormechanics.placeholders

import com.cjcrafter.armormechanics.ArmorMechanics
import me.deecaad.core.placeholder.NumericPlaceholderHandler
import me.deecaad.core.placeholder.PlaceholderData

class PExplosionResistance : NumericPlaceholderHandler("explosion_resistance") {
    override fun requestValue(data: PlaceholderData): Number? {
        return ArmorMechanics.INSTANCE.effects[data.itemTitle()]?.explosionResistance
    }
}