package com.infamous.sapience.capability.emotive;

public interface Emotive {
    int DEFAULT_SHAKE_TICKS = 40;

    int getShakeHeadTicks();

    void setShakeHeadTicks(int ticks);

    class Impl implements Emotive{

        private int shakeHeadTicks;

        public Impl(){
        }

        @Override
        public int getShakeHeadTicks() {
            return this.shakeHeadTicks;
        }

        @Override
        public void setShakeHeadTicks(int ticks) {
            this.shakeHeadTicks = ticks;
        }
    }
}
