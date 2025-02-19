package com.Polarice3.Goety.common.entities.ally.golem;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.common.entities.projectiles.ScatterMine;
import com.Polarice3.Goety.common.entities.util.CameraShake;
import com.Polarice3.Goety.common.items.block.RedstoneGolemSkullItem;
import com.Polarice3.Goety.config.AttributesConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MathHelper;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class RedstoneGolem extends AbstractGolemServant {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(RedstoneGolem.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> ANIM_STATE = SynchedEntityData.defineId(RedstoneGolem.class, EntityDataSerializers.INT);
    public static String ACTIVATE = "activate";
    public static String IDLE = "idle";
    public static String ATTACK = "attack";
    public static String SUMMON = "summon";
    public static String TO_SIT = "to_sit";
    public static String TO_STAND = "to_stand";
    public static String SIT = "sit";
    public static String NOVELTY = "novelty";
    public static String DEATH = "death";
    public static float SUMMON_SECONDS_TIME = 5.15F; //Actually 4 in MCD
    private int activateTick;
    private int idleTime;
    public int summonTick;
    private int summonCool;
    private int mineCount;
    public int attackTick;
    public int postAttackTick;
    public int isSittingDown;
    public int isStandingUp;
    public float getGlow;
    public float glowAmount = 0.03F;
    public int noveltyTick;
    public int deathTime = 0;
    public float deathRotation = 0.0F;
    public boolean markChanged = true;
    public boolean isNovelty = false;
    public boolean isPostAttack = false;
    public boolean isFlash = false;
    public AnimationState activateAnimationState = new AnimationState();
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState noveltyAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState summonAnimationState = new AnimationState();
    public AnimationState toSitAnimationState = new AnimationState();
    public AnimationState toStandAnimationState = new AnimationState();
    public AnimationState sitAnimationState = new AnimationState();
    public AnimationState deathAnimationState = new AnimationState();

    public RedstoneGolem(EntityType<? extends Owned> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new SummonMinesGoal());
        this.goalSelector.addGoal(2, new MeleeGoal());
        this.goalSelector.addGoal(5, new AttackGoal(1.2D));
        this.goalSelector.addGoal(8, new WanderGoal<>(this, 1.0D, 10));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, AttributesConfig.RedstoneGolemHealth.get())
                .add(Attributes.ARMOR, AttributesConfig.RedstoneGolemArmor.get())
                .add(Attributes.MOVEMENT_SPEED, (double)0.3F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 3.0D)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1.0D)
                .add(Attributes.ATTACK_DAMAGE, AttributesConfig.RedstoneGolemDamage.get())
                .add(Attributes.FOLLOW_RANGE, AttributesConfig.RedstoneGolemFollowRange.get());
    }

    public void setConfigurableAttributes(){
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), AttributesConfig.RedstoneGolemHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), AttributesConfig.RedstoneGolemArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), AttributesConfig.RedstoneGolemDamage.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.FOLLOW_RANGE), AttributesConfig.RedstoneGolemFollowRange.get());
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(ANIM_STATE, 0);
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket((LivingEntity)this, this.hasPose(Pose.EMERGING) ? 1 : 0);
    }

    public void recreateFromPacket(ClientboundAddEntityPacket p_219420_) {
        super.recreateFromPacket(p_219420_);
        if (p_219420_.getData() == 1) {
            this.setPose(Pose.EMERGING);
        }

    }

    public void setAnimationState(String input) {
        this.setAnimationState(this.getAnimationState(input));
    }

    public void setAnimationState(int id) {
        this.entityData.set(ANIM_STATE, id);
    }

    public int getAnimationState(String animation) {
        if (Objects.equals(animation, "activate")){
            return 1;
        } else if (Objects.equals(animation, "idle")){
            return 2;
        } else if (Objects.equals(animation, "attack")){
            return 3;
        } else if (Objects.equals(animation, "summon")){
            return 4;
        } else if (Objects.equals(animation, "to_sit")){
            return 5;
        } else if (Objects.equals(animation, "to_stand")){
            return 6;
        } else if (Objects.equals(animation, "sit")){
            return 7;
        } else if (Objects.equals(animation, "novelty")){
            return 8;
        } else if (Objects.equals(animation, "death")){
            return 9;
        } else {
            return 0;
        }
    }

    public void stopMostAnimation(AnimationState exception){
        for (AnimationState state : this.getAnimations()){
            if (state != exception){
                state.stop();
            }
        }
    }

    public int getCurrentAnimation(){
        return this.entityData.get(ANIM_STATE);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        if (ANIM_STATE.equals(p_219422_)) {
            if (this.level.isClientSide){
                switch (this.entityData.get(ANIM_STATE)){
                    case 0:
                        break;
                    case 1:
                        this.activateAnimationState.start(this.tickCount);
                        this.stopMostAnimation(this.activateAnimationState);
                        break;
                    case 2:
                        this.idleAnimationState.startIfStopped(this.tickCount);
                        this.stopMostAnimation(this.idleAnimationState);
                        break;
                    case 3:
                        this.attackAnimationState.startIfStopped(this.tickCount);
                        this.stopMostAnimation(this.attackAnimationState);
                        break;
                    case 4:
                        this.summonAnimationState.startIfStopped(this.tickCount);
                        this.stopMostAnimation(this.summonAnimationState);
                        break;
                    case 5:
                        this.stopMostAnimation(this.toSitAnimationState);
                        this.toSitAnimationState.startIfStopped(this.tickCount);
                        break;
                    case 6:
                        this.stopMostAnimation(this.toStandAnimationState);
                        this.toStandAnimationState.startIfStopped(this.tickCount);
                        break;
                    case 7:
                        this.sitAnimationState.startIfStopped(this.tickCount);
                        this.stopMostAnimation(this.sitAnimationState);
                        break;
                    case 8:
                        this.noveltyAnimationState.startIfStopped(this.tickCount);
                        this.stopMostAnimation(this.noveltyAnimationState);
                        break;
                    case 9:
                        this.deathAnimationState.start(this.tickCount);
                        this.stopMostAnimation(this.deathAnimationState);
                        break;
                }
            }
        }

        super.onSyncedDataUpdated(p_219422_);
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("ActivateTick", this.activateTick);
        pCompound.putInt("SummonTick", this.summonTick);
        pCompound.putInt("CoolDown", this.summonCool);
        pCompound.putInt("MineCount", this.mineCount);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("ActivateTick")) {
            this.activateTick = pCompound.getInt("ActivateTick");
        }
        if (pCompound.contains("SummonTick")) {
            this.summonTick = pCompound.getInt("SummonTick");
        }
        if (pCompound.contains("CoolDown")){
            this.summonCool = pCompound.getInt("CoolDown");
        }
        if (pCompound.contains("MineCount")){
            this.mineCount = pCompound.getInt("MineCount");
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        if (pReason == MobSpawnType.MOB_SUMMONED){
            this.setPose(Pose.EMERGING);
        }
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    public boolean canAnimateMove(){
        return super.canAnimateMove() && this.getCurrentAnimation() == this.getAnimationState(IDLE);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.REDSTONE_GOLEM_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_21239_) {
        return ModSounds.REDSTONE_GOLEM_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
        this.playSound(ModSounds.REDSTONE_GOLEM_STEP.get());
        CameraShake.cameraShake(this.level, this.position(), 10.0F, 0.01F, 5, 0);
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.REDSTONE_GOLEM_DEATH.get();
    }

    private boolean getGolemFlag(int mask) {
        int i = this.entityData.get(DATA_FLAGS_ID);
        return (i & mask) != 0;
    }

    private void setGolemFlags(int mask, boolean value) {
        int i = this.entityData.get(DATA_FLAGS_ID);
        if (value) {
            i = i | mask;
        } else {
            i = i & ~mask;
        }

        this.entityData.set(DATA_FLAGS_ID, (byte)(i & 255));
    }

    public boolean isMeleeAttacking() {
        return this.getGolemFlag(1);
    }

    public void setMeleeAttacking(boolean attacking) {
        this.setGolemFlags(1, attacking);
        this.attackTick = 0;
        this.level.broadcastEntityEvent(this, (byte) 5);
    }

    protected boolean isImmobile() {
        return super.isImmobile() || this.isSummoning() || this.isActivating();
    }

    public boolean hasLineOfSight(Entity p_149755_) {
        return this.summonTick <= 0 && !this.isActivating() && super.hasLineOfSight(p_149755_);
    }

    public boolean isSummoning(){
        return this.summonTick > 0;
    }

    public boolean isSitting(){
        return this.isStaying() && !this.isMeleeAttacking() && !this.isSummoning() && !this.isMoving();
    }

    public List<AnimationState> getAnimations(){
        List<AnimationState> animationStates = new ArrayList<>();
        animationStates.add(this.activateAnimationState);
        animationStates.add(this.attackAnimationState);
        animationStates.add(this.idleAnimationState);
        animationStates.add(this.noveltyAnimationState);
        animationStates.add(this.summonAnimationState);
        animationStates.add(this.toSitAnimationState);
        animationStates.add(this.toStandAnimationState);
        animationStates.add(this.sitAnimationState);
        animationStates.add(this.deathAnimationState);
        return animationStates;
    }

    public void setStaying(boolean staying){
        super.setStaying(staying);
        if (staying){
            this.level.broadcastEntityEvent(this, (byte) 12);
        } else if (this.isFollowing()) {
            this.level.broadcastEntityEvent(this, (byte) 13);
        }
    }

    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 30) {
            this.spawnAnim();
            ItemStack itemStack = new ItemStack(ModBlocks.REDSTONE_GOLEM_SKULL_ITEM.get());
            if (this.getTrueOwner() != null){
                RedstoneGolemSkullItem.setOwner(this.getTrueOwner(), itemStack);
                if (this.getCustomName() != null){
                    RedstoneGolemSkullItem.setCustomName(this.getCustomName().getString(), itemStack);
                }
                ItemEntity itemEntity = this.spawnAtLocation(itemStack);
                if (itemEntity != null){
                    itemEntity.setExtendedLifetime();
                }
            } else if (this.level.random.nextFloat() <= 0.11F){
                this.spawnAtLocation(itemStack);
            }
            this.remove(RemovalReason.KILLED);
        }
        this.hurtTime = 1;
        this.setYRot(this.deathRotation);
        this.setYBodyRot(this.deathRotation);
    }

    public void die(DamageSource p_21014_) {
        this.setAnimationState(DEATH);
        this.deathRotation = this.getYRot();
        super.die(p_21014_);
    }

    public EntityDimensions getDimensions(Pose p_29531_) {
        if (this.isSitting()) {
            return super.getDimensions(p_29531_).scale(1.0F, 0.85F);
        } else {
            return super.getDimensions(p_29531_);
        }
    }

    private boolean isActivating() {
        return this.hasPose(Pose.EMERGING);
    }

    public void tick() {
        super.tick();
        if (this.isDeadOrDying()){
            this.setYRot(this.deathRotation);
            this.setYBodyRot(this.deathRotation);
        }
        //Nobody constantly calls refreshDimensions(). Only calls them once if there's certain changes. No idea why, probably for performance
        if (this.isSitting() && this.markChanged){
            this.refreshDimensions();
            this.markChanged = false;
        } else if (!this.isSitting() && !this.markChanged){
            this.refreshDimensions();
            this.markChanged = true;
        }
        if (this.hasPose(Pose.EMERGING)){
            ++this.activateTick;
            this.setAnimationState(ACTIVATE);
            if (this.activateTick > 20){
                this.setPose(Pose.STANDING);
            }
        }
        if (this.isHostile()) {
            if (this.tickCount % 10 == 0) {
                Mob mob = this.convertTo(ModEntityType.HOSTILE_REDSTONE_GOLEM.get(), false);
                if (mob != null) {
                    mob.setXRot(this.getXRot());
                    mob.setYRot(this.getYRot());
                    mob.setYBodyRot(this.getYRot());
                    mob.setYHeadRot(this.getYHeadRot());
                }
            }
        }
        if (this.level.isClientSide()) {
            if (this.isAlive() && !this.isActivating()) {
                if (!this.isSummoning()){
                    this.glow();
                }
                if (this.summonTick > 0) {
                    this.isFlash = this.summonTick < 60 && this.summonTick > 55 || this.summonTick < 52 && this.summonTick > 47 || this.summonTick < 45 && this.summonTick > 40 || this.summonTick < 38 && this.summonTick > 20;
                    --this.summonTick;
                } else {
                    this.isFlash = false;
                }
            }
        }
        if (!this.level.isClientSide){
            if (!this.isDeadOrDying()) {
                if (!this.isActivating() && !this.isMeleeAttacking() && !this.isSummoning()) {
                    if (this.isStaying()) {
                        this.isStandingUp = MathHelper.secondsToTicks(1);
                        if (this.isSittingDown > 0) {
                            --this.isSittingDown;
                            this.setAnimationState(TO_SIT);
                        } else {
                            this.setAnimationState(SIT);
                        }
                    } else {
                        this.isSittingDown = MathHelper.secondsToTicks(1);
                        if (this.isStandingUp > 0) {
                            --this.isStandingUp;
                            this.setAnimationState(TO_STAND);
                        } else if (!this.isPostAttack && !this.isNovelty) {
                            this.setAnimationState(IDLE);
                        }
                    }
                }
                if (this.isAlive() && !this.isActivating()) {
                    if (this.isNovelty) {
                        this.jumping = false;
                        this.xxa = 0.0F;
                        this.zza = 0.0F;
                        ++this.noveltyTick;
                        if (this.noveltyTick == 8 || this.noveltyTick == 13 || this.noveltyTick == 18 || this.noveltyTick == 23 || this.noveltyTick == 28 || this.noveltyTick == 33) {
                            this.playSound(ModSounds.REDSTONE_GOLEM_CHEST.get());
                        }
                        if (this.noveltyTick == 42) {
                            this.playSound(ModSounds.REDSTONE_GOLEM_GROWL.get());
                            this.gameEvent(GameEvent.ENTITY_ROAR, this);
                        }
                        if (this.noveltyTick >= 92 || this.getTarget() != null || this.hurtTime > 0) {
                            this.isNovelty = false;
                        }
                    } else {
                        this.noveltyTick = 0;
                    }
                    if (!this.isMeleeAttacking() && !this.isSummoning() && !this.isMoving() && !this.isStaying()) {
                        ++this.idleTime;
                        if (this.level.random.nextFloat() <= 0.05F && this.hurtTime <= 0 && (this.getTarget() == null || this.getTarget().isDeadOrDying()) && !this.isNovelty && this.idleTime >= MathHelper.minutesToTicks(1)) {
                            this.idleTime = 0;
                            this.isNovelty = true;
                            this.setAnimationState(NOVELTY);
                        }
                    } else {
                        this.idleTime = 0;
                        this.isNovelty = false;
                    }
                    if (this.summonTick > 0) {
                        --this.summonTick;
                    }
                    if (this.summonCool > 0) {
                        --this.summonCool;
                    }
                    if (this.isMeleeAttacking()) {
                        ++this.attackTick;
                    }
                    if (this.isPostAttack) {
                        ++this.postAttackTick;
                    }
                    if (this.postAttackTick >= 15) {
                        if (!this.isSummoning()) {
                            this.setAnimationState(IDLE);
                        }
                        this.postAttackTick = 0;
                        this.isPostAttack = false;
                    }
                    if (this.isSummoning()) {
                        if (this.level instanceof ServerLevel serverLevel) {
                            for (int i = 0; i < 5; ++i) {
                                double d0 = serverLevel.random.nextGaussian() * 0.02D;
                                double d1 = serverLevel.random.nextGaussian() * 0.02D;
                                double d2 = serverLevel.random.nextGaussian() * 0.02D;
                                serverLevel.sendParticles(ModParticleTypes.BIG_ELECTRIC.get(), this.getRandomX(0.5D), this.getEyeY() - serverLevel.random.nextInt(5), this.getRandomZ(0.5D), 0, d0, d1, d2, 0.5F);
                            }
                        }
                        if (this.summonTick == MathHelper.secondsToTicks(SUMMON_SECONDS_TIME - 1)) {
                            CameraShake.cameraShake(this.level, this.position(), 10.0F, 0.1F, 0, 20);
                        }
                        if (this.summonTick <= (MathHelper.secondsToTicks(SUMMON_SECONDS_TIME - 1)) && this.mineCount > 0) {
                            int time = (int) (MathHelper.secondsToTicks(SUMMON_SECONDS_TIME - 1) / 14);
                            if (this.tickCount % time == 0 && this.onGround()) {
                                BlockPos blockPos = this.blockPosition();
                                blockPos = blockPos.offset(-8 + this.level.random.nextInt(16), 0, -8 + this.level.random.nextInt(16));
                                BlockPos blockPos2 = this.blockPosition().offset(-8 + this.level.random.nextInt(16), 0, -8 + this.level.random.nextInt(16));
                                Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
                                Vec3 vec32 = Vec3.atBottomCenterOf(blockPos2);
                                ScatterMine scatterMine = new ScatterMine(this.level, this, vec3);
                                if (!this.level.getEntitiesOfClass(ScatterMine.class, new AABB(blockPos)).isEmpty()) {
                                    scatterMine.setPos(vec32.x(), vec32.y(), vec32.z());
                                }
                                if (this.level.addFreshEntity(scatterMine)) {
                                    if (this.level.random.nextBoolean()) {
                                        scatterMine.playSound(ModSounds.REDSTONE_GOLEM_MINE_SPAWN.get());
                                    }
                                    --this.mineCount;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void glow() {
        this.getGlow = Mth.clamp(this.getGlow + this.glowAmount, 0, 1);
        if (this.getGlow == 0 || this.getGlow == 1) {
            this.glowAmount *= -1;
        }
    }

    public void handleEntityEvent(byte pId) {
        if (pId == 4){
            this.playSound(ModSounds.REDSTONE_GOLEM_SUMMON.get());
            this.summonTick = (int) (MathHelper.secondsToTicks(SUMMON_SECONDS_TIME) + 5);
            this.getGlow = 1.0F;
        } else if (pId == 5){
            this.attackTick = 0;
        } else if (pId == 6){
            this.playSound(ModSounds.REDSTONE_GOLEM_ATTACK.get());
        } else if (pId == 7){
            this.deathRotation = this.getYRot();
            this.playSound(ModSounds.REDSTONE_GOLEM_DEATH.get(), 1.0F, 1.0F);
        } else if (pId == 12){
            this.isSittingDown = MathHelper.secondsToTicks(1);
        } else if (pId == 13){
            this.isStandingUp = MathHelper.secondsToTicks(1);
        } else {
            super.handleEntityEvent(pId);
        }
    }

    public double getAttackReachSqr(LivingEntity enemy) {
        return (double)(this.getBbWidth() * 6.0F + enemy.getBbWidth()) + 1.0D;
    }

    public boolean targetClose(LivingEntity enemy, double distToEnemySqr){
        double reach = this.getAttackReachSqr(enemy);
        return distToEnemySqr <= reach || RedstoneGolem.this.getBoundingBox().intersects(enemy.getBoundingBox());
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (!this.level.isClientSide && !this.isMeleeAttacking()) {
            this.playSound(ModSounds.REDSTONE_GOLEM_PRE_ATTACK.get(), 1.5F, 1.0F);
            this.setMeleeAttacking(true);
        }
        return true;
    }

    @Override
    public void tryKill(Player player) {
        if (this.killChance <= 0){
            this.warnKill(player);
        } else {
            super.tryKill(player);
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity p_21544_) {
        if (!(p_21544_ instanceof Player) && p_21544_ != null && this.getTarget() != p_21544_ && this.summonCool <= 0){
            this.summonCool = MathHelper.secondsToTicks(SUMMON_SECONDS_TIME);
        }
        super.setTarget(p_21544_);
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand p_230254_2_) {
        if (!this.level.isClientSide) {
            ItemStack itemstack = pPlayer.getItemInHand(p_230254_2_);
            if (this.getTrueOwner() != null && pPlayer == this.getTrueOwner()) {
                if ((itemstack.is(ModBlocks.REINFORCED_REDSTONE_BLOCK.get().asItem())
                        || itemstack.is(Tags.Items.STORAGE_BLOCKS_REDSTONE)
                        || itemstack.is(Tags.Items.DUSTS_REDSTONE))
                        && this.getHealth() < this.getMaxHealth()) {
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    if (itemstack.is(ModBlocks.REINFORCED_REDSTONE_BLOCK.get().asItem())){
                        this.heal(this.getMaxHealth() / 2.0F);
                        this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, 1.5F);
                    } else if (itemstack.is(Tags.Items.STORAGE_BLOCKS_REDSTONE)){
                        this.heal(this.getMaxHealth() / 4.0F);
                        this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, 1.25F);
                    } else {
                        this.heal((this.getMaxHealth() / 4.0F) / 8.0F);
                        this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 0.25F, 1.0F);
                    }
                    if (this.level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 7; ++i) {
                            double d0 = serverLevel.random.nextGaussian() * 0.02D;
                            double d1 = serverLevel.random.nextGaussian() * 0.02D;
                            double d2 = serverLevel.random.nextGaussian() * 0.02D;
                            serverLevel.sendParticles(DustParticleOptions.REDSTONE, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), 0, d0, d1, d2, 0.5F);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * Attack and Melee Goals based of @Infamous-Misadventures codes to make animation sync up: <a href="https://github.com/Infamous-Misadventures/Dungeons-Mobs/blob/1.19/src/main/java/com/infamous/dungeons_mobs/entities/redstone/RedstoneGolemEntity.java#L92">...</a>
     */
    class AttackGoal extends MeleeAttackGoal {
        private final double moveSpeed;
        private int delayCounter;

        public AttackGoal(double moveSpeed) {
            super(RedstoneGolem.this, moveSpeed, true);
            this.moveSpeed = moveSpeed;
        }

        @Override
        public boolean canUse() {
            return RedstoneGolem.this.getTarget() != null && RedstoneGolem.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            RedstoneGolem.this.setAggressive(true);
            this.delayCounter = 0;
        }

        @Override
        public void stop() {
            RedstoneGolem.this.getNavigation().stop();
            if (RedstoneGolem.this.getTarget() == null) {
                RedstoneGolem.this.setAggressive(false);
            }
        }

        @Override
        public void tick() {
            LivingEntity livingentity = RedstoneGolem.this.getTarget();
            if (livingentity == null) {
                return;
            }

            RedstoneGolem.this.getLookControl().setLookAt(livingentity, RedstoneGolem.this.getMaxHeadYRot(), RedstoneGolem.this.getMaxHeadXRot());

            if (--this.delayCounter <= 0) {
                this.delayCounter = 10;
                RedstoneGolem.this.getNavigation().moveTo(livingentity, this.moveSpeed);
            }

            this.checkAndPerformAttack(livingentity, RedstoneGolem.this.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ()));
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
            if (RedstoneGolem.this.targetClose(enemy, distToEnemySqr) && !RedstoneGolem.this.isPostAttack) {
                RedstoneGolem.this.doHurtTarget(enemy);
            }
        }

    }

    class MeleeGoal extends Goal {
        public MeleeGoal() {
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return RedstoneGolem.this.getTarget() != null && RedstoneGolem.this.isMeleeAttacking();
        }

        /**
         * Is short so that the Redstone Golem can immediately move after attacking, else it would stay in place until animation is finished.
         */
        @Override
        public boolean canContinueToUse() {
            return RedstoneGolem.this.attackTick < 5;
        }

        @Override
        public void start() {
            RedstoneGolem.this.setMeleeAttacking(true);
        }

        /**
         * Using Post Attack Tick to ensure that attack animation is stopped properly.
         */
        @Override
        public void stop() {
            RedstoneGolem.this.setMeleeAttacking(false);
            RedstoneGolem.this.isPostAttack = true;
        }

        @Override
        public void tick() {
            if (RedstoneGolem.this.getTarget() != null && RedstoneGolem.this.getTarget().isAlive()) {
                LivingEntity livingentity = RedstoneGolem.this.getTarget();
                double d0 = RedstoneGolem.this.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                RedstoneGolem.this.getLookControl().setLookAt(livingentity, RedstoneGolem.this.getMaxHeadYRot(), RedstoneGolem.this.getMaxHeadXRot());
                if (RedstoneGolem.this.targetClose(livingentity, d0)){
                    RedstoneGolem.this.setYBodyRot(RedstoneGolem.this.getYHeadRot());
                    if (RedstoneGolem.this.attackTick == 1) {
                        RedstoneGolem.this.playSound(ModSounds.REDSTONE_GOLEM_ATTACK.get());
                        RedstoneGolem.this.setAnimationState(ATTACK);
                    }
                    if (RedstoneGolem.this.attackTick == 3) {
                        if (RedstoneGolem.this.targetClose(livingentity, d0)) {
                            this.hurtTarget(livingentity);
                            this.massiveSweep(RedstoneGolem.this, livingentity, 3.0D, 100.0D);
                        }
                    }
                }
            }
        }

        public void hurtTarget(Entity target) {
            float f = (float)RedstoneGolem.this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float f1 = (float)RedstoneGolem.this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);

            DamageSource damageSource = RedstoneGolem.this.getTrueOwner() != null ? ModDamageSource.summonAttack(RedstoneGolem.this, RedstoneGolem.this.getTrueOwner()) : RedstoneGolem.this.damageSources().mobAttack(RedstoneGolem.this);
            boolean flag = target.hurt(damageSource, f);
            if (flag) {
                if (f1 > 0.0F && target instanceof LivingEntity livingEntity) {
                    if (livingEntity.getBoundingBox().getSize() > RedstoneGolem.this.getBoundingBox().getSize()){
                        livingEntity.knockback((double)(f1 * 0.5F), (double)Mth.sin(RedstoneGolem.this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(RedstoneGolem.this.getYRot() * ((float)Math.PI / 180F))));
                    } else {
                        MobUtil.forcefulKnockBack(livingEntity, (double)(f1 * 0.5F), (double)Mth.sin(RedstoneGolem.this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(RedstoneGolem.this.getYRot() * ((float)Math.PI / 180F))), 0.5D);
                    }
                    RedstoneGolem.this.setDeltaMovement(RedstoneGolem.this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                }

                RedstoneGolem.this.doEnchantDamageEffects(RedstoneGolem.this, target);
                RedstoneGolem.this.setLastHurtMob(target);
            }
        }

        public void massiveSweep(LivingEntity source, LivingEntity exempt, double range, double arc){
            List<LivingEntity> hits = MobUtil.getAttackableLivingEntitiesNearby(source, range, 1.0F, range, range);
            for (LivingEntity target : hits) {
                float targetAngle = (float) ((Math.atan2(target.getZ() - source.getZ(), target.getX() - source.getX()) * (180 / Math.PI) - 90) % 360);
                float attackAngle = source.yBodyRot % 360;
                if (targetAngle < 0) {
                    targetAngle += 360;
                }
                if (attackAngle < 0) {
                    attackAngle += 360;
                }
                float relativeAngle = targetAngle - attackAngle;
                float hitDistance = (float) Math.sqrt((target.getZ() - source.getZ()) * (target.getZ() - source.getZ()) + (target.getX() - source.getX()) * (target.getX() - source.getX())) - target.getBbWidth() / 2f;
                if (target != exempt) {
                    if (hitDistance <= range && (relativeAngle <= arc / 2 && relativeAngle >= -arc / 2) || (relativeAngle >= 360 - arc / 2 || relativeAngle <= -360 + arc / 2)) {
                        this.hurtTarget(target);
                    }
                }
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    public class SummonMinesGoal extends Goal{

        @Override
        public boolean canUse() {
            LivingEntity livingentity = RedstoneGolem.this.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
                double d0 = RedstoneGolem.this.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                return RedstoneGolem.this.summonCool <= 0 && RedstoneGolem.this.onGround() && RedstoneGolem.this.targetClose(livingentity, d0);
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            super.start();
            RedstoneGolem.this.playSound(ModSounds.REDSTONE_GOLEM_SUMMON.get(), RedstoneGolem.this.getSoundVolume(), RedstoneGolem.this.getVoicePitch());
            RedstoneGolem.this.setAnimationState(SUMMON);
            RedstoneGolem.this.summonTick = (int) (MathHelper.secondsToTicks(SUMMON_SECONDS_TIME));
            RedstoneGolem.this.summonCool = (int) MathHelper.secondsToTicks(10 + SUMMON_SECONDS_TIME);
            RedstoneGolem.this.mineCount = 14;
        }
    }

    public Crackiness getCrackiness() {
        return Crackiness.byFraction(this.getHealth() / this.getMaxHealth());
    }

    public enum Crackiness {
        NONE(1.0F),
        LOW(0.75F),
        MEDIUM(0.5F),
        HIGH(0.25F);

        private static final List<Crackiness> BY_DAMAGE = Stream.of(values()).sorted(Comparator.comparingDouble((p_28904_) -> {
            return (double)p_28904_.fraction;
        })).collect(ImmutableList.toImmutableList());
        private final float fraction;

        Crackiness(float p_28900_) {
            this.fraction = p_28900_;
        }

        public static Crackiness byFraction(float p_28902_) {
            for(Crackiness irongolem$crackiness : BY_DAMAGE) {
                if (p_28902_ < irongolem$crackiness.fraction) {
                    return irongolem$crackiness;
                }
            }

            return NONE;
        }
    }

}
