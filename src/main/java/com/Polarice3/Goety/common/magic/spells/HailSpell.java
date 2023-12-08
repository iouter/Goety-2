package com.Polarice3.Goety.common.magic.spells;

import com.Polarice3.Goety.SpellConfig;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.HailCloud;
import com.Polarice3.Goety.common.magic.Spells;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class HailSpell extends Spells {

    public int SoulCost() {
        return SpellConfig.HailCost.get();
    }

    public int CastDuration() {
        return SpellConfig.HailDuration.get();
    }

    public SoundEvent CastingSound() {
        return ModSounds.PREPARE_SPELL.get();
    }

    @Override
    public int SpellCooldown() {
        return SpellConfig.HailCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.FROST;
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity entityLiving, ItemStack staff){
        int range = 16;
        int duration = 100;
        double radius = 2.0D;
        float damage = 0.0F;
        if (WandUtil.enchantedFocus(entityLiving)) {
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), entityLiving);
            duration *= WandUtil.getLevels(ModEnchantments.DURATION.get(), entityLiving) + 1;
            damage += WandUtil.getLevels(ModEnchantments.POTENCY.get(), entityLiving);
            radius += WandUtil.getLevels(ModEnchantments.RADIUS.get(), entityLiving);
        }
        HitResult rayTraceResult = this.rayTrace(worldIn, entityLiving, range, radius);
        if (rayTraceResult instanceof EntityHitResult){
            Entity target = ((EntityHitResult) rayTraceResult).getEntity();
            if (target instanceof LivingEntity) {
                HailCloud hailCloud = new HailCloud(worldIn, entityLiving, (LivingEntity) target);
                hailCloud.setExtraDamage(damage);
                hailCloud.setRadius((float) radius);
                hailCloud.setLifeSpan(duration);
                worldIn.addFreshEntity(hailCloud);
            }
            worldIn.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), SoundEvents.PLAYER_HURT_FREEZE, this.getSoundSource(), 1.0F, 1.0F);
        } else if (rayTraceResult instanceof BlockHitResult){
            BlockPos blockPos = ((BlockHitResult) rayTraceResult).getBlockPos();
            HailCloud hailCloud = new HailCloud(worldIn, entityLiving, null);
            hailCloud.setExtraDamage(damage);
            hailCloud.setRadius((float) radius);
            hailCloud.setLifeSpan(duration);
            hailCloud.setPos(blockPos.getX() + 0.5F, blockPos.getY() + 4, blockPos.getZ() + 0.5F);
            worldIn.addFreshEntity(hailCloud);
            worldIn.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), SoundEvents.PLAYER_HURT_FREEZE, this.getSoundSource(), 1.0F, 1.0F);
        }
    }
}
