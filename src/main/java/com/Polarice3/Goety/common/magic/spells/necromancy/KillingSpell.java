package com.Polarice3.Goety.common.magic.spells.necromancy;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.client.particles.GatherTrailParticle;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SThunderBoltPacket;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KillingSpell extends Spell {
    @Override
    public int defaultSoulCost() {
        return SpellConfig.KillingCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.KillingDuration.get();
    }

    @Nullable
    @Override
    public SoundEvent CastingSound() {
        return ModSounds.PREPARE_SPELL.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.KillingCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NECROMANCY;
    }

    @Override
    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster) {
        return this.getTarget(caster) != null;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.RANGE.get());
        return list;
    }

    @Override
    public void useSpell(ServerLevel worldIn, LivingEntity caster, ItemStack staff, int castTime) {
        LivingEntity target = this.getTarget(caster);
        if (target != null){
            ColorUtil colorUtil = new ColorUtil(0xd91516);
            ServerParticleUtil.windParticle(worldIn, colorUtil, 1.0F, 0.0F, target.getId(), target.position());
        }
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff) {
        Vec3 vec3 = caster.getEyePosition();
        LivingEntity target = this.getTarget(caster);
        if (target != null){
            ColorUtil colorUtil = new ColorUtil(0xd91516);
            Vec3 vec31 = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ());
            DamageSource damageSource = ModDamageSource.deathCurse(caster);
            float damage = target.getHealth();
            float casterDamage = damage * 1.25F;
            if (caster.getHealth() - this.hurtCalculation(caster, damageSource, casterDamage) <= 0.0F){
                damage = caster.getHealth() - 1;
                casterDamage = caster.getHealth() - 1;
            }
            if (caster.hurt(damageSource, casterDamage) || !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(caster)) {
                if (target.hurt(damageSource, damage)) {
                    for (int i = 0; i < 8; ++i) {
                        Vec3 vector3d = new Vec3(target.getX(), target.getEyeY(), target.getZ());
                        Vec3 vector3d1 = vector3d.offsetRandom(target.getRandom(), 8.0F);
                        worldIn.sendParticles(new GatherTrailParticle.Option(colorUtil, vector3d1), vector3d.x, vector3d.y, vector3d.z, 0, 0.0F, 0.0F, 0.0F, 0.5F);
                        ServerParticleUtil.windParticle(worldIn, colorUtil, 1.0F, 0.0F, target.getId(), target.position());
                    }
                    ModNetwork.sendToALL(new SThunderBoltPacket(vec3, vec31, colorUtil, 10));
                    worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.THUNDERBOLT.get(), this.getSoundSource(), 3.0F, 0.75F);
                    worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, this.getSoundSource(), 3.0F, 0.75F);
                    /*if (!target.isAlive() && this.rightStaff(staff)){
                        if (target instanceof Zombie zombieEntity && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(zombieEntity, ModEntityType.ZOMBIE_SERVANT.get(), (timer) -> {})) {
                            EntityType<? extends Mob> entityType = ModEntityType.ZOMBIE_SERVANT.get();
                            if (zombieEntity instanceof Husk){
                                entityType = ModEntityType.HUSK_SERVANT.get();
                            } else if (zombieEntity instanceof Drowned){
                                entityType = ModEntityType.DROWNED_SERVANT.get();
                            }
                            ZombieServant zombieMinionEntity = (ZombieServant) zombieEntity.convertTo(entityType, false);
                            if (zombieMinionEntity != null) {
                                zombieMinionEntity.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(zombieMinionEntity.blockPosition()), MobSpawnType.CONVERSION, null, null);
                                zombieMinionEntity.setLimitedLife(10 * (15 + worldIn.random.nextInt(45)));
                                zombieMinionEntity.setTrueOwner(entityLiving);
                                net.minecraftforge.event.ForgeEventFactory.onLivingConvert(zombieEntity, zombieMinionEntity);
                                if (!zombieMinionEntity.isSilent()) {
                                    worldIn.levelEvent((Player) null, 1026, zombieMinionEntity.blockPosition(), 0);
                                }
                            }
                        } else if (target instanceof AbstractSkeleton skeleton && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(skeleton, ModEntityType.SKELETON_SERVANT.get(), (timer) -> {})) {
                            EntityType<? extends Mob> entityType = ModEntityType.SKELETON_SERVANT.get();
                            if (skeleton instanceof Stray){
                                entityType = ModEntityType.STRAY_SERVANT.get();
                            }
                            AbstractSkeletonServant skeletonServant = (AbstractSkeletonServant) skeleton.convertTo(entityType, false);
                            if (skeletonServant != null) {
                                skeletonServant.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(skeletonServant.blockPosition()), MobSpawnType.CONVERSION, null, null);
                                skeletonServant.setLimitedLife(10 * (15 + worldIn.random.nextInt(45)));
                                skeletonServant.setTrueOwner(entityLiving);
                                net.minecraftforge.event.ForgeEventFactory.onLivingConvert(skeleton, skeletonServant);
                                if (!skeletonServant.isSilent()) {
                                    worldIn.levelEvent((Player) null, 1026, skeletonServant.blockPosition(), 0);
                                }
                            }
                        }
                    }*/
                }
            }
        }
    }

    protected float hurtCalculation(LivingEntity livingEntity, DamageSource p_21240_, float p_21241_) {
        p_21241_ = net.minecraftforge.common.ForgeHooks.onLivingHurt(livingEntity, p_21240_, p_21241_);
        p_21241_ = this.getDamageAfterArmorAbsorb(livingEntity, p_21240_, p_21241_);
        p_21241_ = this.getDamageAfterMagicAbsorb(livingEntity, p_21240_, p_21241_);
        float f1 = Math.max(p_21241_ - livingEntity.getAbsorptionAmount(), 0.0F);
        f1 = net.minecraftforge.common.ForgeHooks.onLivingDamage(livingEntity, p_21240_, f1);
        return f1;
    }

    protected float getDamageAfterArmorAbsorb(LivingEntity livingEntity, DamageSource p_21162_, float p_21163_) {
        if (!p_21162_.is(DamageTypeTags.BYPASSES_ARMOR)) {
            p_21163_ = CombatRules.getDamageAfterAbsorb(p_21163_, (float)livingEntity.getArmorValue(), (float)livingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }

        return p_21163_;
    }

    protected float getDamageAfterMagicAbsorb(LivingEntity livingEntity, DamageSource p_21193_, float p_21194_) {
        if (p_21193_.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return p_21194_;
        } else {
            if (livingEntity.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !p_21193_.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
                int i = (livingEntity.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = p_21194_ * (float)j;
                p_21194_ = Math.max(f / 25.0F, 0.0F);
            }

            if (p_21194_ <= 0.0F) {
                return 0.0F;
            } else if (p_21193_.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
                return p_21194_;
            } else {
                int k = EnchantmentHelper.getDamageProtection(livingEntity.getArmorSlots(), p_21193_);
                if (k > 0) {
                    p_21194_ = CombatRules.getDamageAfterMagicAbsorb(p_21194_, (float)k);
                }

                return p_21194_;
            }
        }
    }
}
