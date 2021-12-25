package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

import java.util.Optional;

public class HoglinTasksHelper {
    public static final Tags.IOptionalNamedTag<Item> HOGLIN_FOOD_ITEMS = ItemTags.createOptional(new ResourceLocation(Sapience.MODID, "hoglin_food_items"));
    public static final int BREEDING_ID = 18;

    public static boolean canPickUpItemStack(Animal animalEntity, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof BlockItem blockItem && BlockTags.HOGLIN_REPELLENTS.contains(blockItem.getBlock())) {
            return false;
        } else if (animalEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else {
            if (animalEntity.isFood(itemStack)) {
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

    public static void additionalSensorLogic(Hoglin entity) {
        Brain<?> brain = entity.getBrain();
        Optional<AbstractPiglin> optionalNearestPiglin = Optional.empty();
        int visiblePiglinCount = 0;
        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        for(LivingEntity livingEntity : nvle.findAll((le) -> !le.isBaby())) {
            if (livingEntity instanceof AbstractPiglin piglin) {
                ++visiblePiglinCount;
                if (optionalNearestPiglin.isEmpty()) {
                    optionalNearestPiglin = Optional.of(piglin);
                }
            }
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optionalNearestPiglin);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, visiblePiglinCount);

    }
}
