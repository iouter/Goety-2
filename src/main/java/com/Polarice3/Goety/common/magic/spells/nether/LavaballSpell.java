package com.Polarice3.Goety.common.magic.spells.nether;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.HellBlast;
import com.Polarice3.Goety.common.entities.projectiles.Lavaball;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.ColorUtil;
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

public class LavaballSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return SpellConfig.LavaballCost.get();
    }

    public int defaultCastDuration() {
        return SpellConfig.LavaballDuration.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.EVOKER_PREPARE_ATTACK;
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.LavaballCoolDown.get();
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
        list.add(ModEnchantments.RADIUS.get());
        return list;
    }

    @Override
    public ColorUtil particleColors(LivingEntity entityLiving) {
        return new ColorUtil(1.0F, 0.0F, 0.0F);
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity entityLiving, ItemStack staff) {
        Vec3 vector3d = entityLiving.getViewVector( 1.0F);
        float extraBlast = WandUtil.getLevels(ModEnchantments.RADIUS.get(), entityLiving) / 2.0F;
        AbstractHurtingProjectile fireballEntity = new Lavaball(worldIn,
                entityLiving.getX() + vector3d.x / 2,
                entityLiving.getEyeY() - 0.2,
                entityLiving.getZ() + vector3d.z / 2,
                vector3d.x,
                vector3d.y,
                vector3d.z);
        if (CuriosFinder.hasUnholySet(entityLiving)){
            fireballEntity = new HellBlast(entityLiving.getX() + vector3d.x / 2,
                    entityLiving.getEyeY() - 0.2,
                    entityLiving.getZ() + vector3d.z / 2,
                    vector3d.x,
                    vector3d.y,
                    vector3d.z,
                    worldIn);
        }
        fireballEntity.setOwner(entityLiving);
        if (fireballEntity instanceof Lavaball lavaball) {
            if (rightStaff(staff)) {
                lavaball.setUpgraded(true);
            }
            if (isShifting(entityLiving)) {
                lavaball.setDangerous(false);
            }
            lavaball.setExtraDamage(WandUtil.getLevels(ModEnchantments.POTENCY.get(), entityLiving));
            lavaball.setFiery(WandUtil.getLevels(ModEnchantments.BURNING.get(), entityLiving));
            lavaball.setExplosionPower(lavaball.getExplosionPower() + extraBlast);
        } else if (fireballEntity instanceof HellBlast hellBlast){
            hellBlast.setDamage(hellBlast.getDamage() + WandUtil.getLevels(ModEnchantments.POTENCY.get(), entityLiving));
            hellBlast.setRadius(hellBlast.getRadius() + extraBlast);
            hellBlast.setFiery(WandUtil.getLevels(ModEnchantments.BURNING.get(), entityLiving));
        }
        worldIn.addFreshEntity(fireballEntity);
        if (rightStaff(staff)) {
            for (int i = 0; i < 2; ++i) {
                AbstractHurtingProjectile fireballEntity1 = new Lavaball(worldIn,
                        entityLiving.getX() + vector3d.x / 2 + worldIn.random.nextGaussian(),
                        entityLiving.getEyeY() - 0.2,
                        entityLiving.getZ() + vector3d.z / 2 + worldIn.random.nextGaussian(),
                        vector3d.x,
                        vector3d.y,
                        vector3d.z);
                if (CuriosFinder.hasUnholySet(entityLiving)){
                    fireballEntity1 = new HellBlast(entityLiving.getX() + vector3d.x / 2,
                            entityLiving.getEyeY() - 0.2,
                            entityLiving.getZ() + vector3d.z / 2,
                            vector3d.x,
                            vector3d.y,
                            vector3d.z,
                            worldIn);
                }
                fireballEntity1.setOwner(entityLiving);
                if (fireballEntity1 instanceof Lavaball lavaball) {
                    lavaball.setUpgraded(true);
                    if (isShifting(entityLiving)) {
                        lavaball.setDangerous(false);
                    }
                    lavaball.setExtraDamage(WandUtil.getLevels(ModEnchantments.POTENCY.get(), entityLiving));
                    lavaball.setFiery(WandUtil.getLevels(ModEnchantments.BURNING.get(), entityLiving));
                    lavaball.setExplosionPower(lavaball.getExplosionPower() + extraBlast);
                } else if (fireballEntity1 instanceof HellBlast hellBlast){
                    hellBlast.setDamage(hellBlast.getDamage() + WandUtil.getLevels(ModEnchantments.POTENCY.get(), entityLiving));
                    hellBlast.setRadius(hellBlast.getRadius() + extraBlast);
                    hellBlast.setFiery(WandUtil.getLevels(ModEnchantments.BURNING.get(), entityLiving));
                }
                worldIn.addFreshEntity(fireballEntity1);
            }
        }
        worldIn.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), SoundEvents.GHAST_SHOOT, this.getSoundSource(), 1.0F, 1.0F);
    }
}
