package com.infamous.sapience.util;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.Sapience;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.LookTask;
import net.minecraft.entity.ai.brain.task.PickupWantedItemTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class HoglinTasksHelper {
    private static final Tags.IOptionalNamedTag<Item> HOGLIN_FOOD_ITEMS = ItemTags.createOptional(new ResourceLocation(Sapience.MODID, "hoglin_food_items"));
    public static final int GROWING_ID = 12;
    public static final int BREEDING_ID = 18;

    private static boolean hasNearestRepllentMemory(HoglinEntity hoglinEntity) {
        return hoglinEntity.getBrain().hasMemory(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean hasBreedTargetMemory(HoglinEntity hoglinEntity) {
        return hoglinEntity.getBrain().hasMemory(MemoryModuleType.BREED_TARGET);
    }

    protected static boolean hasPacifiedMemory(HoglinEntity hoglinEntity) {
        return hoglinEntity.getBrain().hasMemory(MemoryModuleType.PACIFIED);
    }

    public static void registerCoreTasks(Brain<HoglinEntity> hoglinEntityBrain) {
        hoglinEntityBrain.registerActivity(Activity.CORE, 0, getCoreTasks());
    }

    private static ImmutableList<Task<? super HoglinEntity>> getCoreTasks(){
        return ImmutableList.of(
                new LookTask(45, 90),
                new WalkToTargetTask(),
                new PickupWantedItemTask<>(1.0F, true, 9));
    }

    public static boolean canPickUpItemStack(AnimalEntity animalEntity, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof BlockItem && ((BlockItem) item).getBlock().isIn(BlockTags.HOGLIN_REPELLENTS)) {
            return false;
        } else if (animalEntity.getBrain().hasMemory(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else {
            if (item.isIn(HoglinTasksHelper.HOGLIN_FOOD_ITEMS)) {
                return !hasAteRecently(animalEntity);
            }
            else{
                return false;
            }
        }
    }

    public static void pickUpBreedingItem(AnimalEntity animalEntity, ItemEntity itemEntity) {
        animalEntity.onItemPickup(itemEntity, 1);
        ItemStack temptationStack = itemEntity.getItem();
        ItemStack pickupStack = temptationStack.split(1);
        if(temptationStack.isEmpty()){
            itemEntity.remove();
        } else {
            itemEntity.setItem(temptationStack);
        }
        if(!hasAteRecently(animalEntity)){
            setAteRecently(animalEntity);
        }
        int growingAge = animalEntity.getGrowingAge();
        World animalWorld = animalEntity.world;
        if(!animalWorld.isRemote && growingAge == 0 && !animalEntity.isInLove()){
            animalEntity.setInLove(null);
        }
        else if(!animalWorld.isRemote && animalEntity.isChild()){
            animalEntity.ageUp((int)((float)(-growingAge / 20) * 0.1F), true);
            //animalWorld.setEntityState(animalEntity, (byte) HoglinTasksHelper.GROWING_ID);
        }
    }

    private static boolean hasAteRecently(AnimalEntity animalEntity) {
        return animalEntity.getBrain().hasMemory(MemoryModuleType.ATE_RECENTLY);
    }

    public static boolean isHoglinFoodItem(Item item){
        return item.isIn(HoglinTasksHelper.HOGLIN_FOOD_ITEMS) || item == Items.CRIMSON_FUNGUS;
    }

    public static void setAteRecently(AnimalEntity animalEntity) {
        animalEntity.getBrain().replaceMemory(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }
}
