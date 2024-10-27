package com.Polarice3.Goety.common.magic.spells.necromancy;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.client.particles.GatherTrailParticle;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.EverChargeSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LeechingSpell extends EverChargeSpell {

    public int defaultSoulCost() {
        return SpellConfig.LeechingCost.get();
    }

    @Override
    public int defaultCastUp() {
        return SpellConfig.LeechingChargeUp.get();
    }

    @Nullable
    @Override
    public SoundEvent CastingSound() {
        return null;
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NECROMANCY;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.RANGE.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity entityLiving, ItemStack staff) {
        float potency = SpellConfig.LeechingDamage.get().floatValue() * SpellConfig.SpellDamageMultiplier.get();
        int range = 8;
        if (WandUtil.enchantedFocus(entityLiving)){
            potency += WandUtil.getLevels(ModEnchantments.POTENCY.get(), entityLiving) / 2.0F;
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), entityLiving);
        }
        LivingEntity livingEntity = this.getTarget(entityLiving, range);
        if (livingEntity != null){
            ColorUtil colorUtil = new ColorUtil(ChatFormatting.DARK_RED);
            Vec3 vector3d = new Vec3(livingEntity.getRandomX(1.0F), livingEntity.getRandomY(), livingEntity.getRandomZ(1.0F));
            Vec3 vector3d1 = new Vec3(entityLiving.getRandomX(1.0F), entityLiving.getEyeY(), entityLiving.getRandomZ(1.0F));
            worldIn.sendParticles(new GatherTrailParticle.Option(colorUtil, vector3d1), vector3d.x, vector3d.y, vector3d.z, 0, 0.0F, 0.0F, 0.0F, 0.5F);
            if (livingEntity.hurt(ModDamageSource.lifeLeech(entityLiving, entityLiving), potency)) {
                if (this.rightStaff(staff)){
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                }
                worldIn.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), ModSounds.SOUL_EAT.get(), this.getSoundSource(), 1.0F, 1.0F);
            }
        }
    }
}
