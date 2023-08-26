package me.cjcrafter.armormechanics.events;

import me.deecaad.core.mechanics.Mechanics;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArmorMechanicsDequipEvent extends EntityEvent {

    private static final HandlerList handlers = new HandlerList();

    private Mechanics dequipMechanics;
    private List<PotionEffect> potions;

    public ArmorMechanicsDequipEvent(@NotNull Entity what, ItemStack armor, String armorTitle, Mechanics dequipMechanics, List<PotionEffect> potions) {
        super(what);
        this.dequipMechanics = dequipMechanics;
        this.potions = potions;
    }

    public Mechanics getDequipMechanics() {
        return dequipMechanics;
    }

    public void setDequipMechanics(Mechanics dequipMechanics) {
        this.dequipMechanics = dequipMechanics;
    }

    public List<PotionEffect> getPotions() {
        return potions;
    }

    public void setPotions(List<PotionEffect> potions) {
        this.potions = potions;
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
