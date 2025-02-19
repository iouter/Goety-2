package com.Polarice3.Goety.client.audio;

import com.Polarice3.Goety.common.entities.projectiles.AbstractBeam;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class LoopSound extends AbstractTickableSoundInstance {
    protected final Entity entity;

    public LoopSound(SoundEvent soundEvent, float volume, Entity entity) {
        super(soundEvent, entity.getSoundSource(), SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.x = (double)((float)entity.getX());
        this.y = (double)((float)entity.getY());
        this.z = (double)((float)entity.getZ());
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
    }

    public LoopSound(SoundEvent soundEvent, Entity entity) {
        this(soundEvent, 1.0F, entity);
    }

    public void tick() {
        if (this.entity.isRemoved() || !this.entity.isAlive()){
            this.stop();
        }

        if (this.entity instanceof AbstractBeam corruptedBeam){
            if (corruptedBeam.getOwner() != null){
                LivingEntity living = corruptedBeam.getOwner();
                this.x = living.getX();
                this.y = living.getY();
                this.z = living.getZ();
            }
        } else {
            this.x = this.entity.getX();
            this.y = this.entity.getY();
            this.z = this.entity.getZ();
        }
    }
}
