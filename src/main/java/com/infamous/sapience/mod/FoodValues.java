package com.infamous.sapience.mod;

import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class FoodValues {

    public static final Map<Item, Integer> FOOD_VALUES = new HashMap<>();

    public static void init(){
        for(Item item : ForgeRegistries.ITEMS){
            if(item.getFood() != null){
                //OhGrowUp.LOGGER.info("Adding food to FOOD_VALUES: " + item.getRegistryName().toString() + " with food value " + item.getFood().getHealing());
                FOOD_VALUES.put(item, item.getFood().getHealing());
            }
        }
    }
}
