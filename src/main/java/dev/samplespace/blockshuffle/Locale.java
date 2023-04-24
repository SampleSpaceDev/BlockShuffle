package dev.samplespace.blockshuffle;

import org.bukkit.Material;

public class Locale {
    public static String getKey(Material material) {
        if (material.isBlock()) {
            return "block.minecraft." + material.getKey().getKey();
        } else if (material.isItem()) {
            return "item.minecraft." + material.getKey().getKey();
        }
        return "block.minecraft.dirt";
    }
}