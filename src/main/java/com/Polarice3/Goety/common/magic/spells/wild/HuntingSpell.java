package com.Polarice3.Goety.common.magic.spells.wild;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.BlackWolf;
import com.Polarice3.Goety.common.entities.ally.Hellhound;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.ally.undead.skeleton.SkeletonWolf;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.BlockFinder;
import com.Polarice3.Goety.utils.EffectsUtil;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class HuntingSpell extends SummonSpell {
    @Override
    public int defaultSoulCost() {
        return SpellConfig.HuntingCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.HuntingDuration.get();
    }

    @Nullable
    @Override
    public SoundEvent CastingSound() {
        return ModSounds.WILD_PREPARE_SPELL.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.HuntingCoolDown.get();
    }

    @Override
    public int SummonDownDuration() {
        return SpellConfig.HuntingSummonDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.WILD;
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
        return livingEntity -> livingEntity instanceof BlackWolf || livingEntity instanceof SkeletonWolf;
    }

    @Override
    public int summonLimit() {
        return SpellConfig.HuntingLimit.get();
    }

    @Override
    public void commonResult(ServerLevel worldIn, LivingEntity caster) {
        if (isShifting(caster)) {
            for (Entity entity : worldIn.getAllEntities()) {
                if (entity instanceof BlackWolf) {
                    this.teleportServants(caster, entity);
                }
            }
            for (int i = 0; i < caster.level.random.nextInt(35) + 10; ++i) {
                worldIn.sendParticles(ParticleTypes.POOF, caster.getX(), caster.getEyeY(), caster.getZ(), 1, 0.0F, 0.0F, 0.0F, 0);
            }
            worldIn.playSound((Player) null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.SUMMON_SPELL.get(), this.getSoundSource(), 1.0F, 1.0F);
        }
    }

    public boolean specialStaffs(ItemStack stack){
        return typeStaff(stack, SpellType.NECROMANCY) || typeStaff(stack, SpellType.NETHER);
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        this.commonResult(worldIn, caster);
        int potency = spellStat.getPotency();
        int duration = spellStat.getDuration();
        if (WandUtil.enchantedFocus(caster)){
            potency += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
            duration += WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1;
        }
        if (!isShifting(caster)) {
            int i = 1;
            if (rightStaff(staff)){
                i = 3;
            } else if (specialStaffs(staff)){
                i = 2;
            }
            for (int i1 = 0; i1 < i; ++i1) {
                Summoned summonedentity = new BlackWolf(ModEntityType.BLACK_WOLF.get(), worldIn);
                if (this.typeStaff(staff, SpellType.NECROMANCY)){
                    summonedentity = new SkeletonWolf(ModEntityType.SKELETON_WOLF.get(), worldIn);
                }/* else if (worldIn.dimension() == Level.NETHER || this.typeStaff(staff, SpellType.NETHER)){
                    summonedentity = new Hellhound(ModEntityType.HELLHOUND.get(), worldIn);
                }*/
                BlockPos blockPos = BlockFinder.SummonRadius(caster.blockPosition(), summonedentity, worldIn);
                summonedentity.setTrueOwner(caster);
                summonedentity.moveTo(blockPos, 0.0F, 0.0F);
                MobUtil.moveDownToGround(summonedentity);
                summonedentity.setLimitedLife(MobUtil.getSummonLifespan(worldIn) * duration);
                summonedentity.setPersistenceRequired();
                summonedentity.finalizeSpawn(worldIn, caster.level.getCurrentDifficultyAt(caster.blockPosition()), MobSpawnType.MOB_SUMMONED,null,null);
                if (potency > 0){
                    int boost = Mth.clamp(potency - 1, 0, 10);
                    summonedentity.addEffect(new MobEffectInstance(GoetyEffects.BUFF.get(), EffectsUtil.infiniteEffect(), boost, false, false));
                }
                this.SummonSap(caster, summonedentity);
                this.setTarget(caster, summonedentity);
                worldIn.addFreshEntity(summonedentity);
                this.summonAdvancement(caster, summonedentity);
            }
            this.SummonDown(caster);
            worldIn.playSound((Player) null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.SUMMON_SPELL.get(), this.getSoundSource(), 1.0F, 1.0F);
        }
    }
}
