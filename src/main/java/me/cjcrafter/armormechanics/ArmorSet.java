package me.cjcrafter.armormechanics;

public class ArmorSet {

    private String bonus;
    private String helmet;
    private String chestplate;
    private String leggings;
    private String boots;

    public ArmorSet() {
    }

    public ArmorSet(String bonus, String helmet, String chestplate, String leggings, String boots) {
        this.bonus = bonus;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public String getBonus() {
        return bonus;
    }

    public String getHelmet() {
        return helmet;
    }

    public String getChestplate() {
        return chestplate;
    }

    public String getLeggings() {
        return leggings;
    }

    public String getBoots() {
        return boots;
    }
}
