package com.Polarice3.Goety.common.magic.spells.nether;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.client.particles.FoggyCloudParticleOption;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.HellBolt;
import com.Polarice3.Goety.common.entities.projectiles.ModFireball;
import com.Polarice3.Goety.common.magic.EverChargeSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FireRainSpell extends EverChargeSpell {

    @Override
    public int defaultSoulCost() {
        return SpellConfig.ArrowRainCost.get();
    }

    @Override
    public int defaultCastUp() {
        return SpellConfig.ArrowRainChargeUp.get();
    }

    @Override
    public int shotsNumber() {
        return SpellConfig.ArrowRainDuration.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.ArrowRainCoolDown.get();
    }

    @Nullable
    @Override
    public SoundEvent CastingSound() {
        return ModSounds.APOSTLE_PREPARE_SPELL.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NETHER;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.RANGE.get());
        list.add(ModEnchantments.BURNING.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        int potency = spellStat.getPotency();
        int burning = spellStat.getBurning();
        int range = spellStat.getRange();
        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
            burning += WandUtil.getLevels(ModEnchantments.BURNING.get(), caster);
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
        }
        HitResult rayTrace = this.rayTrace(worldIn, caster, range, 3.0D);
        Vec3 location = rayTrace.getLocation();
        LivingEntity target = this.getTarget(caster, range);
        if (target != null){
            location = target.position();
        }
        BlockPos mutableBlockPos = new BlockPos.MutableBlockPos(location.x, location.y + 1.5D, location.z);
        int maxHeight = 15;
        int i = 0;
        while (mutableBlockPos.getY() < worldIn.getMaxBuildHeight() && worldIn.isEmptyBlock(mutableBlockPos) && i < maxHeight){
            mutableBlockPos = mutableBlockPos.above();
            i++;
        }
        for (int j = 0; j < potency + 1; ++j){
            Vec3 vec3 = mutableBlockPos.getCenter()
                    .add(worldIn.random.nextFloat() * 16.0F - 8.0F,
                            worldIn.random.nextFloat() * 4.0F - 2.0F,
                            worldIn.random.nextFloat() * 16.0F - 8.0F);
            int clearTries = 0;
            while (clearTries < 6 && !worldIn.isEmptyBlock(BlockPos.containing(vec3)) && worldIn.getFluidState(BlockPos.containing(vec3)).isEmpty()){
                clearTries++;
                vec3 = mutableBlockPos.getCenter()
                        .add(worldIn.random.nextFloat() * 16.0F - 8.0F,
                                worldIn.random.nextFloat() * 4.0F - 2.0F,
                                worldIn.random.nextFloat() * 16.0F - 8.0F);
            }
            if (!worldIn.isEmptyBlock(BlockPos.containing(vec3)) && worldIn.getFluidState(BlockPos.containing(vec3)).isEmpty()){
                vec3 = mutableBlockPos.getCenter();
            }
            worldIn.sendParticles(new FoggyCloudParticleOption(ColorUtil.BLACK, 1.5F, 6), vec3.x(), vec3.y(), vec3.z(), 1, 0, 0, 0, 0);
            Vec3 vec31 = location.subtract(vec3);
            AbstractHurtingProjectile fireball = new ModFireball(worldIn,
                    vec3.x,
                    vec3.y,
                    vec3.z,
                    vec31.x,
                    vec31.y,
                    vec31.z);
            if (CuriosFinder.hasUnholySet(caster)){
                fireball = new HellBolt(vec3.x,
                        vec3.y,
                        vec3.z,
                        vec31.x,
                        vec31.y,
                        vec31.z, worldIn);
            }
            fireball.setOwner(caster);
            fireball.setPos(vec3);
            if (fireball instanceof ModFireball fireball1) {
                if (isShifting(caster)) {
                    fireball1.setDangerous(false);
                }
                fireball1.setExtraDamage(potency);
                fireball1.setFiery(burning);
            } else if (fireball instanceof HellBolt hellBolt){
                hellBolt.setDamage(hellBolt.getDamage() + potency);
                hellBolt.setFiery(burning);
            }
            if (worldIn.addFreshEntity(fireball)){
                worldIn.playSound(null, fireball.getX(), fireball.getY(), fireball.getZ(), SoundEvents.BLAZE_SHOOT, this.getSoundSource(), 2.0F, 1.0F);
            }
        }
    }
}
