package me.cjcrafter.armormechanics.events;

import me.deecaad.core.mechanics.Mechanics;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArmorMechanicsEquipEvent extends EntityEvent {

    private static final HandlerList handlers = new HandlerList();

    private Mechanics equipMechanics;
    private List<PotionEffect> potions;

    public ArmorMechanicsEquipEvent(@NotNull Entity what, Mechanics equipMechanics, List<PotionEffect> potions) {
        super(what);
        this.equipMechanics = equipMechanics;
        this.potions = potions;
    }

    public Mechanics getEquipMechanics() {
        return equipMechanics;
    }

    public void setEquipMechanics(Mechanics equipMechanics) {
        this.equipMechanics = equipMechanics;
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
