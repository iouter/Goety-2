package com.Polarice3.Goety.common.blocks.entities;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.undead.zombie.ZombieServant;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayWorldSoundPacket;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.Tags;

public class GravestoneBlockEntity extends TrainingBlockEntity {

    public GravestoneBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(ModBlockEntities.SHADE_GRAVESTONE.get(), p_155229_, p_155230_);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, TrainingBlockEntity blockEntity) {
        super.tick(level, blockPos, blockState, blockEntity);
        if (blockEntity.isTraining()){
            if (blockEntity.trainTime != blockEntity.getMaxTrainTime()){
                if (blockEntity.trainTime % 20 == 0){
                    level.playSound(null, blockPos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            if (level instanceof ServerLevel serverLevel) {
                double d0 = (double)blockPos.getX() + level.random.nextDouble();
                double d1 = (double)blockPos.getY() + level.random.nextDouble();
                double d2 = (double)blockPos.getZ() + level.random.nextDouble();
                serverLevel.sendParticles(ModParticleTypes.NECRO_FIRE.get(), d0, d1, d2, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                if (level.random.nextFloat() < 0.3F) {
                    if (level.random.nextFloat() < 0.17F) {
                        ModNetwork.sendToALL(new SPlayWorldSoundPacket(blockPos, SoundEvents.FURNACE_FIRE_CRACKLE, 0.5F + level.random.nextFloat(), level.random.nextFloat() * 0.7F + 0.3F));
                    }
                }
            }
        }
    }

    @Override
    public void setVariant(ItemStack itemStack, Level level, BlockPos blockPos) {
        if (level instanceof ServerLevel serverLevel) {
            ZombieServant zombieServant = new ZombieServant(ModEntityType.ZOMBIE_SERVANT.get(), serverLevel);
            BlockState blockState = level.getBlockState(blockPos);
            if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)){
                this.setEntityType(ModEntityType.DROWNED_SERVANT.get());
                this.markUpdated();
            } else if (this.getTrainMob() != zombieServant.getVariant(serverLevel, blockPos.above())) {
                this.setEntityType(zombieServant.getVariant(serverLevel, blockPos.above()));
                this.markUpdated();
            }
        }
    }

    public void startTraining(int amount, ItemStack itemStack){
        super.startTraining(amount, itemStack);
        if (this.level != null) {
            this.level.playSound(null, this.getBlockPos(), ModSounds.GRAVESTONE_START.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public void playSpawnSound() {
        if (this.level != null) {
            this.level.playSound(null, this.getBlockPos(), ModSounds.NECROMANCER_SUMMON.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public int maxTrainAmount() {
        return 5;
    }

    @Override
    public boolean summonLimit() {
        int count = 0;
        if (this.level instanceof ServerLevel serverLevel) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof ZombieServant servant) {
                    if (this.getTrueOwner() != null && servant.getTrueOwner() == this.getTrueOwner() && servant.isAlive()) {
                        ++count;
                    }
                }
            }
        }
        return count >= SpellConfig.ZombieLimit.get();
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return itemStack.is(Tags.Items.NUGGETS_IRON);
    }
}
