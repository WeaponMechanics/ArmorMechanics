package me.cjcrafter.armormechanics.lib

import io.lumine.mythic.api.adapters.AbstractItemStack
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.drops.DropMetadata
import io.lumine.mythic.api.drops.IItemDrop
import io.lumine.mythic.bukkit.adapters.BukkitItemStack
import me.cjcrafter.armormechanics.ArmorMechanics
import me.deecaad.core.utils.StringUtil

class MythicMobsArmorDrop(config: MythicLineConfig, argument: String?) : IItemDrop {
    private val armorTitle: String

    init {
        val title = config.getString(arrayOf("armorTitle", "armor"), "", "")

        // Since we want to ignore spelling/capitalization errors, we should
        // make sure the given 'title' matches to an actual armor-title.
        val startsWith: MutableList<String> = ArrayList()
        val options: Set<String> = ArmorMechanics.INSTANCE.armors.keys
        for (temp in options) {
            if (temp.lowercase().startsWith(title.lowercase())) startsWith.add(title)
        }
        armorTitle = StringUtil.didYouMean(title, if (startsWith.isEmpty()) options else startsWith)
    }

    override fun getDrop(dropMetadata: DropMetadata, v: Double): AbstractItemStack {
        val item = ArmorMechanics.INSTANCE.armors[armorTitle]

        // Just in case MythicMobs edits this item, we want to use a clone
        // to avoid possible modification.
        return BukkitItemStack(item!!.clone())
    }
}
