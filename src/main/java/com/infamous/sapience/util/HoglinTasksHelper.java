package com.infamous.sapience.util;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.Sapience;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

public class HoglinTasksHelper {
    private static final Tags.IOptionalNamedTag<Item> HOGLIN_FOOD_ITEMS = ItemTags.createOptional(new ResourceLocation(Sapience.MODID, "hoglin_food_items"));
    public static final int GROWING_ID = 12;
    public static final int BREEDING_ID = 18;

    private static boolean hasNearestRepllentMemory(Hoglin hoglinEntity) {
        return hoglinEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean hasBreedTargetMemory(Hoglin hoglinEntity) {
        return hoglinEntity.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    protected static boolean hasPacifiedMemory(Hoglin hoglinEntity) {
        return hoglinEntity.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
    }

    public static void registerCoreTasks(Brain<Hoglin> hoglinEntityBrain) {
        hoglinEntityBrain.addActivity(Activity.CORE, 0, getCoreTasks());
    }

    private static ImmutableList<Behavior<? super Hoglin>> getCoreTasks(){
        return ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new GoToWantedItem<>(1.0F, true, 9));
    }

    public static boolean canPickUpItemStack(Animal animalEntity, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof BlockItem blockItem && BlockTags.HOGLIN_REPELLENTS.contains(blockItem.getBlock())) {
            return false;
        } else if (animalEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else {
            if (itemStack.is(HoglinTasksHelper.HOGLIN_FOOD_ITEMS)) {
                return !hasAteRecently(animalEntity);
            }
            else{
                return false;
            }
        }
    }

    public static void pickUpBreedingItem(Animal animalEntity, ItemEntity itemEntity) {
        animalEntity.take(itemEntity, 1);
        ItemStack temptationStack = itemEntity.getItem();
        ItemStack pickupStack = temptationStack.split(1);
        if(temptationStack.isEmpty()){
            itemEntity.remove(Entity.RemovalReason.DISCARDED);
        } else {
            itemEntity.setItem(temptationStack);
        }
        if(!hasAteRecently(animalEntity)){
            setAteRecently(animalEntity);
        }
        int growingAge = animalEntity.getAge();
        Level animalWorld = animalEntity.level;
        if(!animalWorld.isClientSide && growingAge == 0 && !animalEntity.isInLove()){
            animalEntity.setInLove(null);
        }
        else if(!animalWorld.isClientSide && animalEntity.isBaby()){
            animalEntity.ageUp((int)((float)(-growingAge / 20) * 0.1F), true);
            //animalWorld.setEntityState(animalEntity, (byte) HoglinTasksHelper.GROWING_ID);
        }
    }

    private static boolean hasAteRecently(Animal animalEntity) {
        return animalEntity.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    public static boolean isHoglinFoodItem(ItemStack item){
        return item.is(HoglinTasksHelper.HOGLIN_FOOD_ITEMS);
    }

    public static void setAteRecently(Animal animalEntity) {
        animalEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }
}
