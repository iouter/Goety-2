package com.Polarice3.Goety.api.blocks.entities;

import net.minecraft.world.entity.EntityType;

public interface ITrainingBlock extends IOwnedBlock{
    int getTrainingTime();

    int getMaxTrainTime();

    int amountTrainLeft();

    int maxTrainAmount();

    EntityType<?> getTrainMob();

    default boolean isSensorSensitive(){
        return false;
    }

    default boolean isPatrolling(){
        return true;
    }

    default boolean isGrounding(){
        return false;
    }

    default boolean reachedLimit(){
        return false;
    }
}
