package com.Polarice3.Goety.common.entities.ally;

import com.Polarice3.Goety.api.entities.ally.IServant;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.entities.ai.SummonTargetGoal;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.init.ModMobType;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.Polarice3.Goety.utils.ItemHelper;
import com.Polarice3.Goety.utils.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class Summoned extends Owned implements IServant {
    protected static final EntityDataAccessor<Byte> SUMMONED_FLAGS = SynchedEntityData.defineId(Summoned.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> UPGRADE_FLAGS = SynchedEntityData.defineId(Summoned.class, EntityDataSerializers.BYTE);
    public LivingEntity commandPosEntity;
    public BlockPos commandPos;
    public BlockPos boundPos;
    public int commandTick;
    public int killChance;
    public int noHealTime;

    protected Summoned(EntityType<? extends Owned> type, Level worldIn) {
        super(type, worldIn);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.followGoal();
        this.targetSelectGoal();
    }

    public void followGoal(){
        this.goalSelector.addGoal(5, new FollowOwnerGoal<>(this, 1.0D, 10.0F, 2.0F));
    }

    public void targetSelectGoal(){
        this.targetSelector.addGoal(1, new SummonTargetGoal(this));
    }

    protected void updateControlFlags() {
        boolean flag = !(this.getControllingPassenger() instanceof Mob);
        boolean flag1 = !(this.getVehicle() instanceof Boat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag && flag1);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
        this.goalSelector.setControlFlag(Goal.Flag.TARGET, flag);
    }

    public void checkDespawn() {
        if (this.isHostile()){
            super.checkDespawn();
        }
    }

    public ItemStack getProjectile(ItemStack pShootable) {
        if (pShootable.getItem() instanceof ProjectileWeaponItem) {
            Predicate<ItemStack> predicate = ((ProjectileWeaponItem)pShootable.getItem()).getSupportedHeldProjectiles();
            ItemStack itemstack = ProjectileWeaponItem.getHeldProjectile(this, predicate);
            return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void tick(){
        super.tick();
        this.servantTick();
    }

    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    public boolean isSunSensitive2() {
        return this.isSunSensitive();
    }

    @Override
    public boolean burnSunTick() {
        return this.isSunBurnTick();
    }

    public void setTarget(@Nullable LivingEntity p_21544_) {
        if (this.isPatrolling()){
            if (p_21544_ != null) {
                if (p_21544_.distanceToSqr(this.vec3BoundPos()) <= Mth.square(PATROL_RANGE)) {
                    this.normalSetTarget(p_21544_);
                }
            } else {
                this.normalSetTarget(null);
            }
        } else {
            this.normalSetTarget(p_21544_);
        }
    }

    public void normalSetTarget(@Nullable LivingEntity p_21544_){
        super.setTarget(p_21544_);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        if (pReason == MobSpawnType.MOB_SUMMONED && this.getTrueOwner() != null && this.getMobType() == MobType.UNDEAD){
            for (int i = 0; i < pLevel.getLevel().random.nextInt(10) + 10; ++i) {
                pLevel.getLevel().sendParticles(ModParticleTypes.SUMMON.get(), this.getRandomX(1.5D), this.getRandomY(), this.getRandomZ(1.5D), 0, 0.0F, 0.0F, 0.0F, 1.0F);
            }
            pLevel.getLevel().sendParticles(ModParticleTypes.SOUL_EXPLODE.get(), this.getX(), this.getY(), this.getZ(), 0, 0, 2.0D, 0, 1.0F);
        }
        if (this.getTrueOwner() != null){
            this.spawnUpgraded();
        }
        this.setWandering(this.getTrueOwner() == null);
        this.setStaying(false);
        this.setBoundPos(null);
        return pSpawnData;
    }

    public boolean canSpawnArmor(){
        return this.getTrueOwner() != null && CuriosFinder.hasCurio(this.getTrueOwner(), ModItems.RING_OF_THE_FORGE.get());
    }

    protected void populateDefaultEquipmentSlots(RandomSource p_217055_, DifficultyInstance p_217056_) {
        if (this.canSpawnArmor()){
            for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
                    int i = p_217055_.nextInt(2);
                    if (p_217055_.nextFloat() < 0.095F) {
                        ++i;
                    }

                    if (p_217055_.nextFloat() < 0.095F) {
                        ++i;
                    }

                    if (p_217055_.nextFloat() < 0.095F) {
                        ++i;
                    }
                    ItemStack itemstack = this.getItemBySlot(equipmentslot);
                    if (itemstack.isEmpty()) {
                        Item item = getEquipmentForSlot(equipmentslot, i);
                        if (item != null) {
                            this.setItemSlot(equipmentslot, new ItemStack(item));
                            this.setDropChance(equipmentslot, 0.0F);
                        }
                    }
                }
            }
        } else {
            super.populateDefaultEquipmentSlots(p_217055_, p_217056_);
        }
    }

    public void die(DamageSource pCause) {
        if (!this.level.isClientSide && this.hasCustomName() && this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getTrueOwner() instanceof ServerPlayer) {
            this.getTrueOwner().sendSystemMessage(this.getCombatTracker().getDeathMessage());
        }
        super.die(pCause);
    }

    public boolean hurt(DamageSource source, float amount) {
        if (MobsConfig.MinionsMasterImmune.get()) {
            if (source.getEntity() instanceof Summoned summoned) {
                if (!summoned.isHostile() && !this.isHostile()) {
                    if (this.getTrueOwner() != null && summoned.getTrueOwner() == this.getTrueOwner()) {
                        return false;
                    }
                }
            }
        }
        boolean flag = super.hurt(source, amount);
        if (flag){
            this.noHealTime = MathHelper.secondsToTicks(MobsConfig.ServantHealHalt.get());
        }
        return flag;
    }

    public boolean doHurtTarget(Entity entityIn) {
        boolean flag = super.doHurtTarget(entityIn);
        if (flag) {
            if (this.getMobType() == MobType.UNDEAD){
                float f = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
                if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
                    entityIn.setSecondsOnFire(2 * (int)f);
                }
            }
            if (!this.getMainHandItem().isEmpty() && this.getMainHandItem().isDamageableItem()){
                ItemHelper.hurtAndBreak(this.getMainHandItem(), 1, this);
            }
        }

        return flag;
    }

    protected void hurtArmor(DamageSource pDamageSource, float pDamage) {
        if (!(pDamage <= 0.0F)) {
            pDamage = pDamage / 4.0F;
            if (pDamage < 1.0F) {
                pDamage = 1.0F;
            }

            for(EquipmentSlot equipmentSlotType : EquipmentSlot.values()) {
                if (equipmentSlotType.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack itemstack = this.getItemBySlot(equipmentSlotType);
                    if ((!pDamageSource.is(DamageTypeTags.IS_FIRE) || !itemstack.getItem().isFireResistant()) && itemstack.getItem() instanceof ArmorItem) {
                        itemstack.hurtAndBreak((int) pDamage, this, (p_214023_1_) -> {
                            p_214023_1_.broadcastBreakEvent(equipmentSlotType);
                        });
                    }
                }
            }

        }
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SUMMONED_FLAGS, (byte)0);
        this.entityData.define(UPGRADE_FLAGS, (byte)0);
    }

    private boolean getFlag(int mask) {
        int i = this.entityData.get(SUMMONED_FLAGS);
        return (i & mask) != 0;
    }

    private void setFlags(int mask, boolean value) {
        int i = this.entityData.get(SUMMONED_FLAGS);
        if (value) {
            i = i | mask;
        } else {
            i = i & ~mask;
        }

        this.entityData.set(SUMMONED_FLAGS, (byte)(i & 255));
    }

    public boolean isWandering() {
        return this.getFlag(1);
    }

    public void setWandering(boolean wandering) {
        this.setFlags(1, wandering);
    }

    public boolean isStaying(){
        return this.getFlag(2) && !this.isCommanded() && !this.isVehicle();
    }

    public void setStaying(boolean staying){
        this.setFlags(2, staying);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.readServantData(compound);
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.saveServantData(compound);
    }

    public boolean canUpdateMove(){
        return this.getMobType() == MobType.UNDEAD || this.getMobType() == ModMobType.NATURAL;
    }

    public boolean isUpgraded() {
        return (this.entityData.get(UPGRADE_FLAGS) & 4) != 0;
    }

    public void setUpgraded(boolean upgraded) {
        byte b0 = this.entityData.get(UPGRADE_FLAGS);
        if (upgraded) {
            this.entityData.set(UPGRADE_FLAGS, (byte)(b0 | 4));
        } else {
            this.entityData.set(UPGRADE_FLAGS, (byte)(b0 & -5));
        }
    }

    public void upgrade(){
        this.setUpgraded(true);
    }

    public void downgrade(){
        this.setUpgraded(false);
    }

    public void setCommandPos(BlockPos blockPos, boolean removeEntity) {
        if (removeEntity) {
            this.commandPosEntity = null;
        }
        this.commandPos = blockPos;
        this.setCommandTick(MathHelper.secondsToTicks(10));
    }

    public BlockPos getCommandPos(){
        return this.commandPos;
    }

    public void setCommandPosEntity(@Nullable LivingEntity living){
        this.commandPosEntity = living;
        if (living != null) {
            this.setCommandPos(living.blockPosition(), false);
        }
    }

    @Nullable
    public LivingEntity getCommandPosEntity(){
        return this.commandPosEntity;
    }

    public int getCommandTick(){
        return this.commandTick;
    }

    @Override
    public void setCommandTick(int commandTick) {
        this.commandTick = commandTick;
    }

    public boolean isCommanded(){
        return this.commandPos != null;
    }

    public BlockPos getBoundPos(){
        return this.boundPos;
    }

    public void setBoundPos(BlockPos blockPos){
        this.boundPos = blockPos;
    }

    public void dropEquipment(EquipmentSlot equipmentSlot, ItemStack stack){
        if (this.getEquipmentDropChance(equipmentSlot) > 0.0F) {
            this.spawnAtLocation(stack);
        }
    }

    public boolean isMoving() {
        return !(this.walkAnimation.speed() < 0.01F);
    }

    public int getNoHealTime() {
        return this.noHealTime;
    }

    @Override
    public void setNoHealTime(int time) {
        this.noHealTime = time;
    }

    @Override
    public int getKillChance() {
        return this.killChance;
    }

    @Override
    public void setKillChance(int killChance) {
        this.killChance = killChance;
    }

    public void warnKill(Player player){
        this.killChance = 60;
        player.displayClientMessage(Component.translatable("info.goety.servant.tryKill", this.getDisplayName()), true);
    }

    public void tryKill(Player player){
        this.kill();
    }

    public static class FollowOwnerGoal<T extends Mob & IServant> extends Goal {
        private final T summonedEntity;
        private LivingEntity owner;
        private final LevelReader level;
        private final double followSpeed;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;
        private float oldWaterCost;

        public FollowOwnerGoal(T summonedEntity, double speed, float startDistance, float stopDistance) {
            this.summonedEntity = summonedEntity;
            this.level = summonedEntity.level;
            this.followSpeed = speed;
            this.navigation = summonedEntity.getNavigation();
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            if (!(summonedEntity.getNavigation() instanceof GroundPathNavigation) && !(summonedEntity.getNavigation() instanceof FlyingPathNavigation)) {
                throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
            }
        }

        public boolean canUse() {
            LivingEntity livingentity = this.summonedEntity.getTrueOwner();
            if (livingentity == null) {
                return false;
            } else if (livingentity.isSpectator()) {
                return false;
            } else if (this.summonedEntity.distanceToSqr(livingentity) < (double)(Mth.square(this.startDistance))) {
                return false;
            } else if (!this.summonedEntity.isFollowing() || this.summonedEntity.isCommanded()) {
                return false;
            } else if (this.summonedEntity.getTarget() != null) {
                return false;
            } else {
                this.owner = livingentity;
                return true;
            }
        }

        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else if (this.summonedEntity.getTarget() != null){
                return false;
            } else {
                return !(this.summonedEntity.distanceToSqr(this.owner) <= (double)(Mth.square(this.stopDistance)));
            }
        }

        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.summonedEntity.getPathfindingMalus(BlockPathTypes.WATER);
            this.summonedEntity.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.summonedEntity.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        }

        public void tick() {
            if (this.owner != null) {
                this.summonedEntity.getLookControl().setLookAt(this.owner, 10.0F, (float) this.summonedEntity.getMaxHeadXRot());
                if (this.summonedEntity.getControlledVehicle() != null){
                    this.navigation.moveTo(this.owner, this.followSpeed + 0.25D);
                    if (this.summonedEntity.getControlledVehicle() instanceof Mob mob){
                        mob.getNavigation().moveTo(this.owner, this.followSpeed + 0.25D);
                    }
                } else if (--this.timeToRecalcPath <= 0) {
                    this.timeToRecalcPath = 10;
                    if (!this.summonedEntity.isLeashed() && !this.summonedEntity.isPassenger()) {
                        if (this.summonedEntity.distanceToSqr(this.owner) >= 144.0D && MobsConfig.UndeadTeleport.get()) {
                            this.tryToTeleportNearEntity();
                        } else {
                            this.navigation.moveTo(this.owner, this.followSpeed);
                        }
                    }
                }
            }
        }

        private void tryToTeleportNearEntity() {
            BlockPos blockpos = this.owner.blockPosition();

            for(int i = 0; i < 10; ++i) {
                int j = this.getRandomNumber(-3, 3);
                int k = this.getRandomNumber(-1, 1);
                int l = this.getRandomNumber(-3, 3);
                boolean flag = this.tryToTeleportToLocation(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag) {
                    return;
                }
            }

        }

        private boolean tryToTeleportToLocation(int x, int y, int z) {
            if (Math.abs((double)x - this.owner.getX()) < 2.0D && Math.abs((double)z - this.owner.getZ()) < 2.0D) {
                return false;
            } else if (!this.isTeleportFriendlyBlock(new BlockPos(x, y, z))) {
                return false;
            } else {
                this.summonedEntity.moveTo((double)x + 0.5D, (double)y, (double)z + 0.5D, this.summonedEntity.getYRot(), this.summonedEntity.getXRot());
                this.navigation.stop();
                return true;
            }
        }

        private boolean isTeleportFriendlyBlock(BlockPos pos) {
            BlockPathTypes pathnodetype = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pos.mutable());
            if (pathnodetype != BlockPathTypes.WALKABLE) {
                return false;
            } else {
                BlockState blockstate = this.level.getBlockState(pos.below());
                if (blockstate.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockpos = pos.subtract(this.summonedEntity.blockPosition());
                    return this.level.noCollision(this.summonedEntity, this.summonedEntity.getBoundingBox().move(blockpos));
                }
            }
        }

        private int getRandomNumber(int min, int max) {
            return this.summonedEntity.getRandom().nextInt(max - min + 1) + min;
        }
    }

    public static class FollowOwnerWaterGoal extends Goal {
        protected final Summoned summonedEntity;
        private LivingEntity owner;
        private final LevelReader level;
        private final double followSpeed;
        private Path path;
        private double pathedTargetX;
        private double pathedTargetY;
        private double pathedTargetZ;
        private int ticksUntilNextPathRecalculation;
        private final PathNavigation navigation;
        private final float maxDist;
        private final float minDist;

        public FollowOwnerWaterGoal(Summoned summonedEntity, double speed, float minDist, float maxDist) {
            this.summonedEntity = summonedEntity;
            this.level = summonedEntity.level;
            this.followSpeed = speed;
            this.navigation = summonedEntity.getNavigation();
            this.minDist = minDist;
            this.maxDist = maxDist;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.summonedEntity.getTrueOwner();
            if (livingentity == null) {
                return false;
            } else if (livingentity.isSpectator()) {
                return false;
            } else if (this.summonedEntity.distanceToSqr(livingentity) < (double)(this.minDist * this.minDist)) {
                return false;
            } else if (!this.summonedEntity.isFollowing()) {
                return false;
            } else if (this.summonedEntity.getTarget() != null) {
                return false;
            } else {
                this.owner = livingentity;
                if (!livingentity.isAlive()) {
                    return false;
                } else {
                    this.path = this.summonedEntity.getNavigation().createPath(livingentity, 0);
                    if (this.path != null) {
                        return true;
                    }
                }
                return true;
            }
        }

        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else if (this.summonedEntity.getTarget() != null){
                return false;
            } else {
                return !(this.summonedEntity.distanceToSqr(this.owner) <= (double)(this.maxDist * this.maxDist));
            }
        }

        public void start() {
            this.summonedEntity.getNavigation().moveTo(this.path, this.followSpeed);
            this.ticksUntilNextPathRecalculation = 0;
        }

        public void stop() {
            this.owner = null;
            this.navigation.stop();
        }

        public void tick() {
            this.summonedEntity.getLookControl().setLookAt(this.owner, 30.0F, 30.0F);
            double d0 = this.summonedEntity.distanceToSqr(this.owner.getX(), this.owner.getY(), this.owner.getZ());
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            if (this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D || this.owner.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D || this.summonedEntity.getRandom().nextFloat() < 0.05F)) {
                this.pathedTargetX = this.owner.getX();
                this.pathedTargetY = this.owner.getY();
                this.pathedTargetZ = this.owner.getZ();
                this.ticksUntilNextPathRecalculation = 4 + this.summonedEntity.getRandom().nextInt(7);
                if (d0 > 144.0D && MobsConfig.UndeadTeleport.get()){
                    this.tryToTeleportNearEntity();
                }
                if (d0 > 1024.0D) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (d0 > 256.0D) {
                    this.ticksUntilNextPathRecalculation += 5;
                }

                if (!this.summonedEntity.getNavigation().moveTo(this.owner, this.followSpeed)) {
                    this.ticksUntilNextPathRecalculation += 15;
                }
            }
        }

        private void tryToTeleportNearEntity() {
            BlockPos blockpos = this.owner.blockPosition();

            for(int i = 0; i < 10; ++i) {
                int j = this.getRandomNumber(-3, 3);
                int k = this.getRandomNumber(-1, 1);
                int l = this.getRandomNumber(-3, 3);
                boolean flag = this.tryToTeleportToLocation(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag) {
                    return;
                }
            }

        }

        private boolean tryToTeleportToLocation(int x, int y, int z) {
            if (Math.abs((double)x - this.owner.getX()) < 2.0D && Math.abs((double)z - this.owner.getZ()) < 2.0D) {
                return false;
            } else if (!this.isTeleportFriendlyBlock(new BlockPos(x, y, z))) {
                return false;
            } else {
                this.summonedEntity.moveTo((double)x + 0.5D, (double)y, (double)z + 0.5D, this.summonedEntity.getYRot(), this.summonedEntity.getXRot());
                this.navigation.stop();
                return true;
            }
        }

        private boolean isTeleportFriendlyBlock(BlockPos pos) {
            BlockPathTypes pathnodetype = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pos.mutable());
            if (pathnodetype != BlockPathTypes.WALKABLE) {
                return false;
            } else {
                BlockState blockstate = this.level.getBlockState(pos.below());
                if (blockstate.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockpos = pos.subtract(this.summonedEntity.blockPosition());
                    return this.level.noCollision(this.summonedEntity, this.summonedEntity.getBoundingBox().move(blockpos));
                }
            }
        }

        private int getRandomNumber(int min, int max) {
            return this.summonedEntity.getRandom().nextInt(max - min + 1) + min;
        }
    }

    public static class WanderGoal<T extends PathfinderMob & IServant> extends RandomStrollGoal {
        private final T summonedEntity;
        protected final float probability;

        public WanderGoal(T p_i47301_1_, double p_i47301_2_) {
            this(p_i47301_1_, p_i47301_2_, 0.001F);
        }

        public WanderGoal(T entity, double speedModifier, float probability) {
            this(entity, speedModifier, 120, probability);
        }

        public WanderGoal(T entity, double speedModifier, int interval, float probability) {
            super(entity, speedModifier, interval, false);
            this.summonedEntity = entity;
            this.probability = probability;
        }

        public boolean canUse() {
            if (super.canUse()){
                return (!this.summonedEntity.isStaying() && !this.summonedEntity.isCommanded() || this.summonedEntity.getTrueOwner() == null) && !(this.summonedEntity.getNavigation() instanceof WaterBoundPathNavigation);
            } else {
                return false;
            }
        }

        @Nullable
        protected Vec3 getPosition() {
            if (this.summonedEntity.isPatrolling()){
                return randomBoundPos();
            } else if (this.mob.isInWaterOrBubble()) {
                Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
                return vec3 == null ? super.getPosition() : vec3;
            } else {
                return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
            }
        }

        public Vec3 randomBoundPos(){
            Vec3 vec3 = null;
            int range = PATROL_RANGE / 2;

            for (int i = 0; i < 10; ++i){
                BlockPos blockPos = this.summonedEntity.getBoundPos()
                        .offset(this.summonedEntity.getRandom().nextIntBetweenInclusive(-range, range),
                                this.summonedEntity.getRandom().nextIntBetweenInclusive(-range, range),
                                this.summonedEntity.getRandom().nextIntBetweenInclusive(-range, range));
                BlockPos blockPos1 = LandRandomPos.movePosUpOutOfSolid(this.summonedEntity, blockPos);
                if (blockPos1 != null){
                    vec3 = Vec3.atBottomCenterOf(blockPos1);
                    break;
                }
            }

            return vec3;
        }
    }

    public class WaterWanderGoal<T extends PathfinderMob & IServant> extends RandomStrollGoal {
        private final T summonedEntity;

        public WaterWanderGoal(T entity) {
            super(entity, 1.0D);
            this.summonedEntity = entity;
        }

        @Nullable
        protected Vec3 getPosition() {
            if (this.summonedEntity.isPatrolling()){
                return randomBoundPos();
            }
            return super.getPosition();
        }

        public Vec3 randomBoundPos(){
            Vec3 vec3 = null;
            int range = PATROL_RANGE / 2;

            for (int i = 0; i < 10; ++i){
                BlockPos blockPos = this.summonedEntity.getBoundPos()
                        .offset(this.summonedEntity.getRandom().nextIntBetweenInclusive(-range, range),
                                this.summonedEntity.getRandom().nextIntBetweenInclusive(-range, range),
                                this.summonedEntity.getRandom().nextIntBetweenInclusive(-range, range));
                if (this.summonedEntity.getNavigation() instanceof WaterBoundPathNavigation){
                    if (GoalUtils.isWater(this.summonedEntity, blockPos)){
                        vec3 = Vec3.atBottomCenterOf(blockPos);
                        break;
                    }
                } else {
                    BlockPos blockPos1 = LandRandomPos.movePosUpOutOfSolid(this.summonedEntity, blockPos);
                    if (blockPos1 != null){
                        vec3 = Vec3.atBottomCenterOf(blockPos1);
                        break;
                    }
                }
            }

            return vec3;
        }

        public boolean canUse() {
            if (super.canUse()){
                return !Summoned.this.isStaying() || Summoned.this.getTrueOwner() == null;
            } else {
                return false;
            }
        }
    }

    public static class GoToWaterGoal extends Goal {
        private final Summoned mob;
        private double wantedX;
        private double wantedY;
        private double wantedZ;
        private final double speedModifier;
        private final Level level;

        public GoToWaterGoal(Summoned p_i48910_1_, double p_i48910_2_) {
            this.mob = p_i48910_1_;
            this.speedModifier = p_i48910_2_;
            this.level = p_i48910_1_.level;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            if (this.mob.getTrueOwner() != null){
                if (!this.mob.getTrueOwner().isInWater()){
                    return false;
                }
            } else if (!this.level.isDay()) {
                return false;
            }
            if (this.mob.isInWater()) {
                return false;
            }
            Vec3 vector3d = this.getWaterPos();
            if (vector3d == null) {
                return false;
            } else {
                this.wantedX = vector3d.x;
                this.wantedY = vector3d.y;
                this.wantedZ = vector3d.z;
                return true;
            }
        }

        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone();
        }

        public void start() {
            this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }

        @Nullable
        private Vec3 getWaterPos() {
            RandomSource random = this.mob.getRandom();
            BlockPos blockpos = this.mob.blockPosition();

            for(int i = 0; i < 10; ++i) {
                BlockPos blockpos1 = blockpos.offset(random.nextInt(20) - 10, 2 - random.nextInt(8), random.nextInt(20) - 10);
                if (this.level.getBlockState(blockpos1).is(Blocks.WATER)) {
                    return Vec3.atBottomCenterOf(blockpos1);
                }
            }

            return null;
        }
    }

    public static class NaturalAttackGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        protected Summoned summoned;

        public NaturalAttackGoal(Summoned summoned, Class<T> tClass) {
            this(summoned, tClass, 10, true, null);
        }

        public NaturalAttackGoal(Summoned summoned, Class<T> tClass, boolean pMustSee) {
            this(summoned, tClass, 10, pMustSee, null);
        }

        public NaturalAttackGoal(Summoned summoned, Class<T> tClass, boolean pMustSee, @Nullable Predicate<LivingEntity> predicate) {
            this(summoned, tClass, 10, pMustSee, predicate);
        }

        public NaturalAttackGoal(Summoned summoned, Class<T> tClass, int time, boolean pMustSee, @Nullable Predicate<LivingEntity> predicate) {
            this(summoned, tClass, time, pMustSee, false, predicate);
        }

        public NaturalAttackGoal(Summoned summoned, Class<T> tClass, int time, boolean pMustSee, boolean pMustReach, Predicate<LivingEntity> predicate) {
            super(summoned, tClass, time, pMustSee, pMustReach, predicate);
            this.summoned = summoned;
        }

        public boolean canUse() {
            return super.canUse() && this.summoned.isNatural() && this.summoned.getTrueOwner() == null && this.target != null;
        }
    }
}
