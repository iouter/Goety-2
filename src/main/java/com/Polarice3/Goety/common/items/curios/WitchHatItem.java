package com.Polarice3.Goety.common.items.curios;

import com.Polarice3.Goety.utils.CuriosFinder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WitchHatItem extends SingleStackItem{

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof LivingEntity livingEntity) {
            if (CuriosFinder.hasWitchSet(livingEntity)){
                if (livingEntity.getRandom().nextFloat() < 7.5E-4F){
                    for(int i = 0; i < livingEntity.getRandom().nextInt(35) + 10; ++i) {
                        livingEntity.level.addParticle(ParticleTypes.WITCH, livingEntity.getX() + livingEntity.getRandom().nextGaussian() * (double)0.13F, livingEntity.getBoundingBox().maxY + 0.5D + livingEntity.getRandom().nextGaussian() * (double)0.13F, livingEntity.getZ() + livingEntity.getRandom().nextGaussian() * (double)0.13F, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }

        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

}
