package com.cjcrafter.armormechanics.lib

import com.cjcrafter.armormechanics.ArmorMechanics
import io.lumine.mythic.api.adapters.AbstractItemStack
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.drops.DropMetadata
import io.lumine.mythic.api.drops.IItemDrop
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack
import me.deecaad.core.utils.StringUtil

class MythicMobsArmorDrop(config: MythicLineConfig, argument: String?) : IItemDrop {
    private val armorTitle: String

    init {
        val title = config.getString(arrayOf("armorTitle", "armor"), "", "")

        // Since we want to ignore spelling/capitalization errors, we should
        // make sure the given 'title' matches to an actual armor-title.
        val startsWith: MutableSet<String> = HashSet()
        var options: Set<String> = ArmorMechanics.INSTANCE.armors.keys
        for (temp in options) {
            if (temp.lowercase().startsWith(title.lowercase())) startsWith.add(title)
        }

        // Added redundancy to make sure we don't have any empty options.
        options = if (startsWith.isEmpty()) options else startsWith
        armorTitle = if (options.isEmpty()) title else StringUtil.didYouMean(title, options)
    }

    override fun getDrop(dropMetadata: DropMetadata, v: Double): AbstractItemStack {
        val item = ArmorMechanics.INSTANCE.armors[armorTitle]

        // Just in case MythicMobs edits this item, we want to use a clone
        // to avoid possible modification.
        return ItemComponentBukkitItemStack(item!!.clone())
    }
}
