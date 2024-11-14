package com.Polarice3.Goety.common.magic.spells;

import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.EverChargeSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TelekinesisSpell extends EverChargeSpell {

    @Override
    public SpellStat defaultStats() {
        return super.defaultStats().setPotency(1);
    }

    @Override
    public int defaultSoulCost() {
        return SpellConfig.TelekinesisCost.get();
    }

    @Override
    public int defaultCastUp() {
        return SpellConfig.TelekinesisChargeUp.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return ModSounds.PREPARE_SPELL.get();
    }

    @Override
    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster) {
        if (caster.hurtTime > 0){
            if (caster instanceof Player player){
                player.stopUsingItem();
                SEHelper.addCooldown(player, ModItems.TELEKINESIS_FOCUS.get(), MathHelper.secondsToTicks(5));
                SEHelper.sendSEUpdatePacket(player);
            }
            return false;
        }
        return true;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.RANGE.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        double potency = spellStat.getPotency();
        int range = spellStat.getRange();
        if (WandUtil.enchantedFocus(caster)){
            potency += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster) / 2.0D;
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
        }
        Entity target = MobUtil.getSingleTarget(worldIn, caster, range, 3, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        if (target != null){
            if ((caster.getBoundingBox().inflate(0.5D).getSize() * potency) >= target.getBoundingBox().getSize()) {
                boolean flag = true;
                if (target instanceof LivingEntity livingTarget){
                    if (livingTarget.getMaxHealth() >= SpellConfig.TelekinesisMaxHealth.get()
                            || MobUtil.hasEntityTypesConfig(SpellConfig.TelekinesisBlackList.get(), livingTarget.getType())){
                        flag = false;
                    }
                }
                if (flag) {
                    Vec3 lookVec = caster.getLookAngle();
                    double x = caster.getX() + lookVec.x * 1.6D - target.getX();
                    double y = caster.getEyeY() + lookVec.y * 1.6D - target.getY();
                    double z = caster.getZ() + lookVec.z * 1.6D - target.getZ();
                    double offset = 0.1D;
                    target.setDeltaMovement(x * offset, y * offset, z * offset);
                    target.move(MoverType.SELF, target.getDeltaMovement());
                    if (CuriosFinder.hasCurio(caster, ModItems.RING_OF_FORCE.get())){
                        target.setAirSupply(Math.max(-20, target.getAirSupply() - 10));
                        if (target.getAirSupply() <= -20){
                            target.setAirSupply(0);
                            target.hurt(ModDamageSource.choke(caster, caster), 2.0F);
                        }
                    }
                    ServerParticleUtil.addParticlesAroundSelf(worldIn, ParticleTypes.PORTAL, target);
                }
            }
        }
    }
}
