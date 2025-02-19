package com.Polarice3.Goety.client.audio;

import com.Polarice3.Goety.client.events.ClientEvents;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.config.MainConfig;
import com.Polarice3.Goety.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;

public class BossLoopMusic extends AbstractTickableSoundInstance {
    protected final Mob mobEntity;

    public BossLoopMusic(SoundEvent soundEvent, Mob mobEntity) {
        this(soundEvent, mobEntity, 1.0F);
    }

    public BossLoopMusic(SoundEvent soundEvent, Mob mobEntity, float volume) {
        super(soundEvent, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.mobEntity = mobEntity;
        this.x = (double)((float)mobEntity.getX());
        this.y = (double)((float)mobEntity.getY());
        this.z = (double)((float)mobEntity.getZ());
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
    }

    public boolean canPlaySound() {
        return ClientEvents.BOSS_MUSIC == this;
    }

    public void tick() {
        if (!MainConfig.BossMusic.get()){
            ClientEvents.BOSS_MUSIC = null;
            this.stop();
        }
        if (this.mobEntity.isRemoved() || this.mobEntity.isDeadOrDying() || !this.mobEntity.isAlive()){
            if (this.mobEntity.isDeadOrDying()){
                if (this.mobEntity.level.isClientSide){
                    Minecraft minecraft = Minecraft.getInstance();
                    SoundManager soundHandler = minecraft.getSoundManager();
                    if (!this.isStopped()){
                        if (this.mobEntity instanceof Apostle) {
                            soundHandler.queueTickingSound(new PostBossMusic(ModSounds.APOSTLE_THEME_POST.get(), mobEntity));
                        } else {
                            soundHandler.queueTickingSound(new PostBossMusic(ModSounds.BOSS_POST.get(), mobEntity));
                        }
                    }
                }
            }
            ClientEvents.BOSS_MUSIC = null;
            this.stop();
        }
    }
}
