package com.infamous.sapience.capability.greed;

import com.infamous.sapience.SapienceConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IGreed {

    Inventory getGreedInventory();

    boolean isSharingGold();

    void setSharingGold(boolean shareGold);


}
