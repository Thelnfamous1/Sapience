package com.infamous.sapience.capability.ageable;

import com.infamous.sapience.SapienceConfig;

public interface Ageable {

    boolean wasBorn();

    void setBorn(boolean wasBorn);

    int getGrowingAge();

    int getForcedAge();

    int getForcedAgeTimer();

    void ageUp(int growthSeconds, boolean updateForcedAge);

    void addGrowth(int growth);

    void setGrowingAge(int age);

    void setForcedAge(int forcedAge);

    void setForcedAgeTimer(int forcedAgeTimer);

    void onGrowingAdult();

    byte getFoodLevel();

    void setFoodLevel(byte foodLevel);

    void depleteFoodValue();

    boolean canBreed();

    boolean canSelfAge();

    default void decreaseFoodLevel(int qty) {
        this.setFoodLevel((byte)(this.getFoodLevel() - qty));
    }

    default void increaseFoodLevel(int qty){
        this.setFoodLevel((byte)(this.getFoodLevel() + qty));
    }

    class Impl implements Ageable {

        public static final int FORCED_AGE_TIME = 40;
        private boolean wasBorn;
        private int growingAge;
        private int forcedAge;
        private int forcedAgeTimer;
        private byte foodLevel;

        public Impl(){
        }

        @Override
        public boolean wasBorn() {
            return this.wasBorn;
        }

        @Override
        public void setBorn(boolean wasBorn) {
            this.wasBorn = wasBorn;
        }

        /**
         * The age value may be negative or positive or zero. If it's negative, it get's incremented on each tick, if it's
         * positive, it get's decremented each tick. Don't confuse this with EntityLiving.getAge. With a negative value the
         * Entity is considered a child.
         */
        @Override
        public int getGrowingAge() {
            /*
            if (this.world.isRemote) {
                return this.dataManager.get(BABY) ? -1 : 1;
            } else {
                return this.growingAge;
            }
             */
            return this.growingAge;
        }

        @Override
        public int getForcedAge() {
            return forcedAge;
        }

        @Override
        public int getForcedAgeTimer() {
            return forcedAgeTimer;
        }

        /**
         * Increases this entity's age, optionally updating {@link #forcedAge}. If the entity is an adult (if the entity's
         * age is greater than or equal to 0) then the entity's age will be set to {@link #forcedAge}.
         */
        public void ageUp(int growthSeconds, boolean updateForcedAge) {
            int growingAge = this.getGrowingAge();
            int originalAge = growingAge;
            growingAge += growthSeconds * 20;
            if (growingAge > 0) {
                growingAge = 0;
            }

            int ageAmount = growingAge - originalAge;
            this.setGrowingAge(growingAge);
            if (updateForcedAge) {
                this.forcedAge += ageAmount;
                if (this.forcedAgeTimer == 0) {
                    this.forcedAgeTimer = FORCED_AGE_TIME;
                }
            }

            if (this.getGrowingAge() == 0) {
                this.setGrowingAge(this.forcedAge);
            }

        }

        /**
         * Increases this entity's age. If the entity is an adult (if the entity's age is greater than or equal to 0) then
         * the entity's age will be set to {@link #forcedAge}. This method does not update {@link #forcedAge}.
         */
        @Override
        public void addGrowth(int growth) {
            this.ageUp(growth, false);
        }

        /**
         * The age value may be negative or positive or zero. If it's negative, it get's incremented on each tick, if it's
         * positive, it get's decremented each tick. With a negative value the Entity is considered a child.
         */
        @Override
        public void setGrowingAge(int age) {
            int i = this.growingAge;
            this.growingAge = age;
            if (i < 0 && age >= 0 || i >= 0 && age < 0) {
                this.onGrowingAdult();
            }

        }

        @Override
        public void setForcedAge(int forcedAge) {
            this.forcedAge = forcedAge;
        }

        @Override
        public void setForcedAgeTimer(int forcedAgeTimer) {
            this.forcedAgeTimer = forcedAgeTimer;
        }

        /**
         * This is called when Entity's growing age timer reaches 0 (negative values are considered as a child, positive as
         * an adult)
         */
        @Override
        public void onGrowingAdult() {
        }

        // BREEDING METHODS //

        @Override
        public byte getFoodLevel() {
            return this.foodLevel;
        }

        @Override
        public void setFoodLevel(byte foodLevel) {
            this.foodLevel = foodLevel;
        }

        @Override
        public void depleteFoodValue() {
            //this.eat();
            this.decreaseFoodLevel(SapienceConfig.COMMON.MIN_FOOD_VALUE_FOR_BREEDING.get());
        }

        @Override
        public boolean canBreed() {
            return this.getFoodLevel()
                    //+ this.getFoodValueFromInventory()
                    >= SapienceConfig.COMMON.MIN_FOOD_VALUE_FOR_BREEDING.get()
                    && this.getGrowingAge() == 0;
        }

        @Override
        public boolean canSelfAge() {
            return this.getFoodLevel()
                    //+ this.getFoodValueFromInventory()
                    >= SapienceConfig.COMMON.MIN_FOOD_VALUE_FOR_BREEDING.get()
                    && this.getGrowingAge() < 0;
        }
    }
}