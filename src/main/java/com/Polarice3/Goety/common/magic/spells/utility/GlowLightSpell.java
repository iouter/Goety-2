package com.Polarice3.Goety.common.magic.spells.utility;

import com.Polarice3.Goety.common.entities.projectiles.GlowLight;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GlowLightSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return SpellConfig.GlowLightCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.GlowLightDuration.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.GlowLightCoolDown.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.EGG_THROW;
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff){
        GlowLight soulLightEntity = new GlowLight(worldIn, caster);
        soulLightEntity.setOwner(caster);
        soulLightEntity.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, 1.5F, 1.0F);
        worldIn.addFreshEntity(soulLightEntity);
        worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), CastingSound(), this.getSoundSource(), 1.0F, 1.0F);
    }
}
