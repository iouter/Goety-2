package com.Polarice3.Goety.common.magic.spells.necromancy;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.undead.zombie.BlackguardServant;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BlackguardSpell extends SummonSpell {

    public int defaultSoulCost() {
        return SpellConfig.BlackguardCost.get();
    }

    public int defaultCastDuration() {
        return SpellConfig.BlackguardDuration.get();
    }

    public int SummonDownDuration() {
        return SpellConfig.BlackguardSummonDown.get();
    }

    public SoundEvent CastingSound() {
        return null;
    }

    @Override
    public SoundEvent loopSound(LivingEntity caster) {
        return ModSounds.VANGUARD_SPELL.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.BlackguardCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NECROMANCY;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.DURATION.get());
        return list;
    }

    @Override
    public Predicate<LivingEntity> summonPredicate() {
        return livingEntity -> livingEntity instanceof BlackguardServant;
    }

    @Override
    public int summonLimit() {
        return SpellConfig.BlackguardLimit.get();
    }

    public void commonResult(ServerLevel worldIn, LivingEntity caster){
        if (isShifting(caster)) {
            for (Entity entity : worldIn.getAllEntities()) {
                if (entity instanceof BlackguardServant) {
                    this.teleportServants(caster, entity);
                }
            }
            for (int i = 0; i < caster.level.random.nextInt(35) + 10; ++i) {
                worldIn.sendParticles(ModParticleTypes.LICH.get(), caster.getX(), caster.getEyeY(), caster.getZ(), 1, 0.0F, 0.0F, 0.0F, 0);
            }
            worldIn.playSound((Player) null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.VANGUARD_SUMMON.get(), this.getSoundSource(), 1.0F, 1.0F);
        }
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        this.commonResult(worldIn, caster);
        int potency = spellStat.getPotency();
        int duration = spellStat.getDuration();
        if (WandUtil.enchantedFocus(caster)){
            potency += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
            duration += WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1;
        }
        if (!isShifting(caster)) {
            if (staff.is(ModItems.NAMELESS_STAFF.get())){
                Vec3 vec3 = caster.position();
                Direction direction = caster.getDirection();
                double stepX = direction.getStepX();
                double stepZ = direction.getStepZ();
                for (int i1 = -3; i1 <= 3; ++i1) {
                    BlackguardServant summonedentity = new BlackguardServant(ModEntityType.BLACKGUARD_SERVANT.get(), worldIn);
                    summonedentity.setTrueOwner(caster);
                    Vec3 vec32 = new Vec3(((2 * stepX) + (i1 * stepZ)) + vec3.x(), vec3.y(), ((2 * stepZ) + (i1 * stepX)) + vec3.z());
                    if (!worldIn.noCollision(summonedentity, summonedentity.getBoundingBox().move(vec32))){
                        vec32 = Vec3.atCenterOf(BlockFinder.SummonRadius(caster.blockPosition(), summonedentity, caster.level));
                    }
                    summonedentity.setPos(vec32);
                    MobUtil.moveDownToGround(summonedentity);
                    summonedentity.setPersistenceRequired();
                    summonedentity.setLimitedLife(MobUtil.getSummonLifespan(worldIn) * duration);
                    if (potency > 0){
                        int boost = Mth.clamp(potency - 1, 0, 10);
                        summonedentity.addEffect(new MobEffectInstance(GoetyEffects.BUFF.get(), EffectsUtil.infiniteEffect(), boost, false, false));
                    }
                    summonedentity.finalizeSpawn(worldIn, caster.level.getCurrentDifficultyAt(caster.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                    summonedentity.setYHeadRot(caster.getYHeadRot());
                    summonedentity.setYRot(caster.getYRot());
                    this.SummonSap(caster, summonedentity);
                    this.setTarget(caster, summonedentity);
                    if (worldIn.addFreshEntity(summonedentity)) {
                        ColorUtil colorUtil = new ColorUtil(0xa7fc3e);
                        ServerParticleUtil.windShockwaveParticle(worldIn, colorUtil, 0.1F, 0.1F, 0.05F, -1, summonedentity.position());
                        worldIn.sendParticles(ModParticleTypes.LICH.get(), summonedentity.getX(), summonedentity.getY(), summonedentity.getZ(), 1, 0, 0, 0, 0.0F);
                        worldIn.playSound((Player) null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.SOUL_EXPLODE.get(), this.getSoundSource(), 0.25F + (worldIn.random.nextFloat() / 2.0F), 1.0F);
                        worldIn.playSound((Player) null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 0.25F + (worldIn.random.nextFloat() / 2.0F), 1.0F);
                    }
                    this.summonAdvancement(caster, summonedentity);
                }
            } else {
                int i = 1;
                if (rightStaff(staff)){
                    i = 2 + caster.level.random.nextInt(6);
                }
                for (int i1 = 0; i1 < i; ++i1) {
                    BlackguardServant summonedentity = new BlackguardServant(ModEntityType.BLACKGUARD_SERVANT.get(), worldIn);
                    summonedentity.setTrueOwner(caster);
                    summonedentity.moveTo(BlockFinder.SummonRadius(caster.blockPosition(), summonedentity, worldIn), 0.0F, 0.0F);
                    MobUtil.moveDownToGround(summonedentity);
                    summonedentity.setPersistenceRequired();
                    summonedentity.setLimitedLife(MobUtil.getSummonLifespan(worldIn) * duration);
                    if (potency > 0){
                        int boost = Mth.clamp(potency - 1, 0, 10);
                        summonedentity.addEffect(new MobEffectInstance(GoetyEffects.BUFF.get(), EffectsUtil.infiniteEffect(), boost, false, false));
                    }
                    summonedentity.finalizeSpawn(worldIn, caster.level.getCurrentDifficultyAt(caster.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                    this.SummonSap(caster, summonedentity);
                    this.setTarget(caster, summonedentity);
                    worldIn.addFreshEntity(summonedentity);
                    this.summonAdvancement(caster, summonedentity);
                }
            }
            this.SummonDown(caster);
            worldIn.playSound((Player) null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.VANGUARD_SUMMON.get(), this.getSoundSource(), 1.0F, 1.0F);
        }
    }
}
