package com.Polarice3.Goety.common.magic.spells.nether;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.HellBolt;
import com.Polarice3.Goety.common.entities.projectiles.ModFireball;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Learned you could use this method for better projectile accuracy from codes by @Yunus1903
 */
public class FireballSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return SpellConfig.FireballCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.FireballDuration.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.BLAZE_SHOOT;
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.FireballCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NETHER;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.BURNING.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff) {
        Vec3 vector3d = caster.getViewVector( 1.0F);
        AbstractHurtingProjectile smallFireballEntity = new ModFireball(worldIn,
                caster.getX() + vector3d.x / 2,
                caster.getEyeY() - 0.2,
                caster.getZ() + vector3d.z / 2,
                vector3d.x,
                vector3d.y,
                vector3d.z);
        if (CuriosFinder.hasUnholySet(caster)){
            smallFireballEntity = new HellBolt(caster.getX() + vector3d.x / 2,
                    caster.getEyeY() - 0.2,
                    caster.getZ() + vector3d.z / 2,
                    vector3d.x,
                    vector3d.y,
                    vector3d.z, worldIn);
        }
        smallFireballEntity.setOwner(caster);
        if (smallFireballEntity instanceof ModFireball fireball) {
            if (isShifting(caster)) {
                fireball.setDangerous(false);
            }
            fireball.setExtraDamage(WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster));
            fireball.setFiery(WandUtil.getLevels(ModEnchantments.BURNING.get(), caster));
        } else if (smallFireballEntity instanceof HellBolt hellBolt){
            hellBolt.setDamage(hellBolt.getDamage() + WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster));
            hellBolt.setFiery(WandUtil.getLevels(ModEnchantments.BURNING.get(), caster));
        }
        worldIn.addFreshEntity(smallFireballEntity);
        if (rightStaff(staff)) {
            for (int i = 0; i < 2; ++i) {
                AbstractHurtingProjectile smallFireballEntity2 = new ModFireball(worldIn,
                        caster.getX() + vector3d.x / 2 + worldIn.random.nextGaussian(),
                        caster.getEyeY() - 0.2,
                        caster.getZ() + vector3d.z / 2 + worldIn.random.nextGaussian(),
                        vector3d.x,
                        vector3d.y,
                        vector3d.z);
                if (CuriosFinder.hasUnholySet(caster)){
                    smallFireballEntity2 = new HellBolt(caster.getX() + vector3d.x / 2 + worldIn.random.nextGaussian(),
                            caster.getEyeY() - 0.2,
                            caster.getZ() + vector3d.z / 2 + worldIn.random.nextGaussian(),
                            vector3d.x,
                            vector3d.y,
                            vector3d.z, worldIn);
                }
                smallFireballEntity2.setOwner(caster);
                if (smallFireballEntity2 instanceof ModFireball fireball) {
                    if (isShifting(caster)) {
                        fireball.setDangerous(false);
                    }
                    fireball.setExtraDamage(WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster));
                    fireball.setFiery(WandUtil.getLevels(ModEnchantments.BURNING.get(), caster));
                } else if (smallFireballEntity2 instanceof HellBolt hellBolt){
                    hellBolt.setDamage(hellBolt.getDamage() + WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster));
                    hellBolt.setFiery(WandUtil.getLevels(ModEnchantments.BURNING.get(), caster));
                }
                worldIn.addFreshEntity(smallFireballEntity2);
            }
        }
        worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), CastingSound(), this.getSoundSource(), 1.0F, 1.0F);
    }
}
