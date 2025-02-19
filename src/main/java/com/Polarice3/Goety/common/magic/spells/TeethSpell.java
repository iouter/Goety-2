package com.Polarice3.Goety.common.magic.spells;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.projectiles.ViciousTooth;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.*;

import java.util.ArrayList;
import java.util.List;

public class TeethSpell extends Spell {

    public int defaultSoulCost() {
        return SpellConfig.TeethCost.get();
    }

    public int defaultCastDuration() {
        return SpellConfig.TeethDuration.get();
    }

    public SoundEvent CastingSound() {
        return SoundEvents.EVOKER_PREPARE_ATTACK;
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.TeethCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.ILL;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.RANGE.get());
        return list;
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat){
        int range = spellStat.getRange();
        double radius = spellStat.getRadius();
        float potency = spellStat.getPotency();
        if (WandUtil.enchantedFocus(caster)){
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
            potency += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
        }
        HitResult rayTraceResult = this.rayTrace(worldIn, caster, range, radius);
        BlockPos blockPos = caster.blockPosition();
        if (rightStaff(staff)) {
            if (this.isShifting(caster)) {
                this.surroundTeeth(caster, blockPos, potency, true);
            } else {
                LivingEntity target = this.getTarget(caster, range);
                if (target != null) {
                    blockPos = target.blockPosition();
                } else if (rayTraceResult instanceof BlockHitResult) {
                    blockPos = ((BlockHitResult) rayTraceResult).getBlockPos().above();
                }
                for (int length = 0; length < 16; length++) {
                    blockPos = blockPos.offset(-2 + caster.getRandom().nextInt(4), 0, -2 + caster.getRandom().nextInt(4));
                    BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                    while (blockpos$mutable.getY() < blockPos.getY() + 8.0D && !worldIn.getBlockState(blockpos$mutable).blocksMotion()) {
                        blockpos$mutable.move(Direction.UP);
                    }

                    if (worldIn.noCollision(new AABB(blockpos$mutable))) {
                        ViciousTooth viciousTooth = new ViciousTooth(ModEntityType.VICIOUS_TOOTH.get(), worldIn);
                        viciousTooth.setPos(Vec3.atCenterOf(blockpos$mutable));
                        viciousTooth.setOwner(caster);
                        viciousTooth.setExtraDamage(potency);
                        if (worldIn.addFreshEntity(viciousTooth)) {
                            viciousTooth.playSound(ModSounds.TOOTH_SPAWN.get());
                        }
                    }
                }
            }
        } else {
            if (this.isShifting(caster)){
                blockPos = caster.blockPosition();
            } else if (rayTraceResult instanceof EntityHitResult){
                Entity target = ((EntityHitResult) rayTraceResult).getEntity();
                blockPos = target.blockPosition();
            } else if (rayTraceResult instanceof BlockHitResult) {
                blockPos = ((BlockHitResult) rayTraceResult).getBlockPos().above();
            }
            BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());

            while(blockpos$mutable.getY() < blockPos.getY() + 8.0D && !worldIn.getBlockState(blockpos$mutable).blocksMotion()) {
                blockpos$mutable.move(Direction.UP);
            }
            if (worldIn.noCollision(new AABB(blockpos$mutable))){
                ViciousTooth viciousTooth = new ViciousTooth(ModEntityType.VICIOUS_TOOTH.get(), worldIn);
                viciousTooth.setPos(Vec3.atCenterOf(blockpos$mutable));
                viciousTooth.setOwner(caster);
                viciousTooth.setExtraDamage(potency);
                if (worldIn.addFreshEntity(viciousTooth)) {
                    viciousTooth.playSound(ModSounds.TOOTH_SPAWN.get());
                }
            }
        }
        worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.EVOKER_CAST_SPELL, this.getSoundSource(), 1.0F, 1.0F);
    }

    public void surroundTeeth(LivingEntity livingEntity, BlockPos blockPos, float damage, boolean isStaff){
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        while (blockpos$mutable.getY() < blockPos.getY() + 8.0D && !livingEntity.level.getBlockState(blockpos$mutable).blocksMotion()) {
            blockpos$mutable.move(Direction.UP);
        }

        if (isStaff) {
            for (int i = 0; i < 5; ++i) {
                float f1 = (float) i * (float) Math.PI * 0.4F;
                ViciousTooth viciousTooth = new ViciousTooth(ModEntityType.VICIOUS_TOOTH.get(), livingEntity.level);
                viciousTooth.setPos(blockPos.getX() + (double) Mth.cos(f1) * 1.5D, blockpos$mutable.getY(), blockPos.getZ() + (double) Mth.cos(f1) * 1.5D);
                viciousTooth.setOwner(livingEntity);
                if (livingEntity.level.addFreshEntity(viciousTooth)) {
                    viciousTooth.playSound(ModSounds.TOOTH_SPAWN.get());
                }
            }
            for (int k = 0; k < 8; ++k) {
                float f2 = (float) k * (float) Math.PI * 2.0F / 8.0F + 1.2566371F;
                ViciousTooth viciousTooth = new ViciousTooth(ModEntityType.VICIOUS_TOOTH.get(), livingEntity.level);
                viciousTooth.setPos(blockPos.getX() + (double) Mth.cos(f2) * 2.5D, blockpos$mutable.getY(), blockPos.getZ() + (double) Mth.sin(f2) * 2.5D);
                viciousTooth.setOwner(livingEntity);
                viciousTooth.setExtraDamage(damage);
                if (livingEntity.level.addFreshEntity(viciousTooth)) {
                    viciousTooth.playSound(ModSounds.TOOTH_SPAWN.get());
                }
            }
        }
    }

}
