package com.Polarice3.Goety.common.entities.projectiles;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MathHelper;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.ServantUtil;
import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Arrays;
import java.util.Map;

public class SoulBolt extends SpellHurtingProjectile {
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(SoulBolt.class, EntityDataSerializers.INT);
    public static final Map<Integer, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), (map) -> {
        map.put(0, Goety.location("textures/entity/projectiles/soul_bolt/soul_bolt_1.png"));
        map.put(1, Goety.location("textures/entity/projectiles/soul_bolt/soul_bolt_2.png"));
        map.put(2, Goety.location("textures/entity/projectiles/soul_bolt/soul_bolt_3.png"));
    });
    public boolean isNecro;
    private final Vec3[] trailPositions = new Vec3[64];
    private int trailPointer = -1;

    public SoulBolt(EntityType<? extends AbstractHurtingProjectile> p_36833_, Level p_36834_) {
        super(p_36833_, p_36834_);
    }

    public SoulBolt(double pX, double pY, double pZ, double pXPower, double pYPower, double pZPower, Level pLevel) {
        super(ModEntityType.SOUL_BOLT.get(), pX, pY, pZ, pXPower, pYPower, pZPower, pLevel);
    }

    public SoulBolt(LivingEntity pShooter, double pXPower, double pYPower, double pZPower, Level pLevel) {
        super(ModEntityType.SOUL_BOLT.get(), pShooter, pXPower, pYPower, pZPower, pLevel);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE_ID, 0);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setAnimation(compound.getInt("Animation"));
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Animation", this.getAnimation());
    }

    public ResourceLocation getResourceLocation() {
        return TEXTURE_BY_TYPE.getOrDefault(this.getAnimation(), TEXTURE_BY_TYPE.get(0));
    }

    public void rotateToMatchMovement() {
        this.updateRotation();
    }

    public int getAnimation() {
        return this.entityData.get(DATA_TYPE_ID);
    }

    public void setAnimation(int pType) {
        this.entityData.set(DATA_TYPE_ID, pType);
    }

    public boolean isOnFire() {
        return false;
    }

    public boolean isNecro() {
        return this.isNecro;
    }

    public void setNecro(boolean necro) {
        this.isNecro = necro;
    }

    protected void onHitEntity(EntityHitResult p_37626_) {
        super.onHitEntity(p_37626_);
        if (!this.level.isClientSide) {
            float baseDamage = SpellConfig.SoulBoltDamage.get().floatValue() * SpellConfig.SpellDamageMultiplier.get();
            Entity entity = p_37626_.getEntity();
            Entity entity1 = this.getOwner();
            boolean flag;
            if (entity1 instanceof LivingEntity livingentity) {
                if (livingentity instanceof Mob mob){
                    if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null && mob.getAttributeValue(Attributes.ATTACK_DAMAGE) > 0){
                        baseDamage = (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    }
                }
                baseDamage += this.getExtraDamage();
                flag = entity.hurt(entity.damageSources().indirectMagic(this, livingentity), baseDamage);
                if (flag) {
                    if (entity.isAlive()) {
                        this.doEnchantDamageEffects(livingentity, entity);
                    } else if (this.isNecro()) {
                        ServantUtil.convertZombies(entity, livingentity, false);
                        boolean wither = false;
                        if (livingentity instanceof Player player){
                            wither = SEHelper.hasResearch(player, ResearchList.BYGONE);
                        }
                        ServantUtil.convertSkeletons(entity, livingentity, wither, false);
                    }
                }
            } else {
                flag = entity.hurt(entity.damageSources().magic(), baseDamage);
            }

            if (flag && entity instanceof LivingEntity) {
                double x = this.getX();
                double z = this.getZ();
                if (entity1 != null){
                    x = entity1.getX();
                    z = entity1.getZ();
                }
                ((LivingEntity) entity).knockback(1.0F, x - entity.getX(), z - entity.getZ());
            }

        }
    }

    protected void onHit(HitResult p_37628_) {
        super.onHit(p_37628_);
        this.playSound(ModSounds.BOLT_IMPACT.get());
        if (!this.level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level;
            for (int p = 0; p < 32; ++p) {
                double d0 = (double)this.getX() + this.level.random.nextDouble();
                double d1 = (double)this.getY() + this.level.random.nextDouble();
                double d2 = (double)this.getZ() + this.level.random.nextDouble();
                serverLevel.sendParticles(ModParticleTypes.BULLET_EFFECT.get(), d0, d1, d2, 0, 0.45, 0.45, 0.45, 0.5F);
            }
            this.discard();
        }

    }

    public void tick() {
        super.tick();
        if (this.getAnimation() < 2) {
            this.setAnimation(this.getAnimation() + 1);
        } else {
            this.setAnimation(0);
        }
        Entity entity = this.getOwner();
        if (this.tickCount >= MathHelper.secondsToTicks(10)){
            this.discard();
        }
        if (this.level.isClientSide || (entity == null || !entity.isRemoved()) && this.level.hasChunkAt(this.blockPosition())) {
            Vec3 vec3 = this.getDeltaMovement();
            double d0 = this.getX() - vec3.x;
            double d1 = this.getY() - vec3.y;
            double d2 = this.getZ() - vec3.z;
            this.level.addParticle(ModParticleTypes.SUMMON_TRAIL.get(),
                    d0 + ((this.level.random.nextDouble() / 4) * (this.level.random.nextIntBetweenInclusive(-1, 1))),
                    d1 + 0.15D,
                    d2 + ((this.level.random.nextDouble() / 4) * (this.level.random.nextIntBetweenInclusive(-1, 1))),
                    0.0D, 0.0D, 0.0D);
        }
        Vec3 trailAt = this.position().add(0, this.getBbHeight() / 2F, 0);
        if (this.trailPointer == -1) {
            Arrays.fill(trailPositions, trailAt);
        }
        if (++this.trailPointer == this.trailPositions.length) {
            this.trailPointer = 0;
        }
        this.trailPositions[this.trailPointer] = trailAt;
    }

    /**
     * Ripped Trail effect from @AlexModGuy: <a href="https://github.com/AlexModGuy/AlexsCaves/blob/main/src/main/java/com/github/alexmodguy/alexscaves/server/entity/item/WaterBoltEntity.java">...</a>
     */
    public Vec3 getTrailPosition(int pointer, float partialTick) {
        if (this.isRemoved()) {
            partialTick = 1.0F;
        }
        int i = this.trailPointer - pointer & 63;
        int j = this.trailPointer - pointer - 1 & 63;
        Vec3 d0 = this.trailPositions[j];
        Vec3 d1 = this.trailPositions[i].subtract(d0);
        return d0.add(d1.scale(partialTick));
    }

    public boolean hasTrail() {
        return this.trailPointer != -1;
    }

    protected ParticleOptions getTrailParticle() {
        return ModParticleTypes.NONE.get();
    }

    public boolean isPickable() {
        return false;
    }

    public boolean hurt(DamageSource p_37616_, float p_37617_) {
        return false;
    }

    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
