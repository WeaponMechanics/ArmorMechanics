package me.cjcrafter.armormechanics.events;

import me.deecaad.core.mechanics.Mechanics;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArmorUpdateEvent extends EntityEvent {

    private static final HandlerList handlers = new HandlerList();

    private LivingEntity entity;
    private ItemStack armor;
    private String armorTitle;

    public ArmorUpdateEvent(@NotNull LivingEntity entity, ItemStack armor, String armorTitle) {
        super(entity);
        this.entity = entity;
        this.armor = armor;
        this.armorTitle = armorTitle;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
