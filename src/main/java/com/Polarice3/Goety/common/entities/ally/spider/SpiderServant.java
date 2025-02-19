package com.Polarice3.Goety.common.entities.ally.spider;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.config.AttributesConfig;
import com.Polarice3.Goety.utils.EffectsUtil;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpiderServant extends Summoned implements PlayerRideable{
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(SpiderServant.class, EntityDataSerializers.BYTE);
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("2a3ee720-61cb-402c-affa-2eef9343910d");
    public static final AttributeModifier STOP_MODIFIER = new AttributeModifier(SPEED_MODIFIER_UUID, "Stop Moving Dammit", -1.0D, AttributeModifier.Operation.ADDITION);
    private static final UUID DETECTION_MODIFIER_UUID = UUID.fromString("858f6b2f-73e3-45a0-8bef-bb31e0d55be4");
    public static final AttributeModifier DETECTION_MODIFIER = new AttributeModifier(DETECTION_MODIFIER_UUID, "Light Is Blinding", -1.0D, AttributeModifier.Operation.ADDITION);

    public SpiderServant(EntityType<? extends Owned> type, Level worldIn) {
        super(type, worldIn);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WanderGoal<>(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.attackGoal();
    }

    public void attackGoal(){
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new SpiderAttackGoal(this));
    }

    public double getPassengersRidingOffset() {
        return (double)(this.getBbHeight() * 0.5F);
    }

    protected PathNavigation createNavigation(Level p_33802_) {
        return new WallClimberNavigation(this, p_33802_);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    public boolean canUpdateMove() {
        return true;
    }

    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            AttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.FOLLOW_RANGE);
            if (MobUtil.isInBrightLight(this)){
                if (modifiableattributeinstance != null) {
                    if (this.getAttribute(Attributes.FOLLOW_RANGE) != null) {
                        modifiableattributeinstance.removeModifier(DETECTION_MODIFIER);
                        modifiableattributeinstance.addTransientModifier(DETECTION_MODIFIER);
                    }
                }
            } else {
                if (modifiableattributeinstance != null) {
                    if (modifiableattributeinstance.hasModifier(DETECTION_MODIFIER)) {
                        modifiableattributeinstance.removeModifier(DETECTION_MODIFIER);
                    }
                }
            }
            this.setClimbing(this.horizontalCollision);
        }

    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, AttributesConfig.SpiderServantHealth.get())
                .add(Attributes.ATTACK_DAMAGE, AttributesConfig.SpiderServantDamage.get())
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), AttributesConfig.SpiderServantHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), AttributesConfig.SpiderServantDamage.get());
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource p_33814_) {
        return SoundEvents.SPIDER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    protected void playStepSound(BlockPos p_33804_, BlockState p_33805_) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    public boolean onClimbable() {
        return this.isClimbing();
    }

    public void makeStuckInBlock(BlockState p_33796_, Vec3 p_33797_) {
        if (!p_33796_.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(p_33796_, p_33797_);
        }

    }

    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    public boolean canBeAffected(MobEffectInstance p_33809_) {
        if (p_33809_.getEffect() == MobEffects.POISON) {
            net.minecraftforge.event.entity.living.MobEffectEvent.Applicable event = new net.minecraftforge.event.entity.living.MobEffectEvent.Applicable(this, p_33809_);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
            return event.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW;
        }
        return super.canBeAffected(p_33809_);
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean p_33820_) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (p_33820_) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, b0);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33790_, DifficultyInstance p_33791_, MobSpawnType p_33792_, @Nullable SpawnGroupData p_33793_, @Nullable CompoundTag p_33794_) {
        p_33793_ = super.finalizeSpawn(p_33790_, p_33791_, p_33792_, p_33793_, p_33794_);
        RandomSource randomsource = p_33790_.getRandom();
        if (p_33793_ == null) {
            p_33793_ = new SpiderEffectsGroupData();
            if (p_33790_.getDifficulty() == Difficulty.HARD && randomsource.nextFloat() < 0.1F * p_33791_.getSpecialMultiplier()) {
                ((SpiderEffectsGroupData)p_33793_).setRandomEffect(randomsource);
            }
        }

        if (p_33793_ instanceof SpiderEffectsGroupData) {
            MobEffect mobeffect = ((SpiderEffectsGroupData)p_33793_).effect;
            if (mobeffect != null) {
                this.addEffect(new MobEffectInstance(mobeffect, EffectsUtil.infiniteEffect()));
            }
        }

        return p_33793_;
    }

    protected float getStandingEyeHeight(Pose p_33799_, EntityDimensions p_33800_) {
        return 0.65F;
    }

    static class SpiderAttackGoal extends MeleeAttackGoal {
        public SpiderAttackGoal(SpiderServant p_33822_) {
            super(p_33822_, 1.0D, true);
        }

        public SpiderAttackGoal(SpiderServant p_33822_, double speed) {
            super(p_33822_, speed, true);
        }

        public boolean canUse() {
            return super.canUse() && !this.mob.isVehicle();
        }

        protected double getAttackReachSqr(LivingEntity p_33825_) {
            return (double)(4.0F + p_33825_.getBbWidth());
        }
    }

    public static class SpiderEffectsGroupData implements SpawnGroupData {
        @Nullable
        public MobEffect effect;

        public void setRandomEffect(RandomSource p_219119_) {
            int i = p_219119_.nextInt(5);
            if (i <= 1) {
                this.effect = MobEffects.MOVEMENT_SPEED;
            } else if (i == 2) {
                this.effect = MobEffects.DAMAGE_BOOST;
            } else if (i == 3) {
                this.effect = MobEffects.REGENERATION;
            } else if (i == 4) {
                this.effect = MobEffects.INVISIBILITY;
            }

        }
    }

    public boolean isFood(ItemStack p_30440_) {
        Item item = p_30440_.getItem();
        return item.isEdible() && p_30440_.getFoodProperties(this).isMeat();
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (this.getTrueOwner() != null && pPlayer == this.getTrueOwner()) {
            if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                FoodProperties foodProperties = itemstack.getFoodProperties(this);
                if (foodProperties != null){
                    this.heal((float)foodProperties.getNutrition());
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }

                    this.gameEvent(GameEvent.EAT, this);
                    this.eat(this.level, itemstack);
                    if (this.level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 7; ++i) {
                            double d0 = this.random.nextGaussian() * 0.02D;
                            double d1 = this.random.nextGaussian() * 0.02D;
                            double d2 = this.random.nextGaussian() * 0.02D;
                            serverLevel.sendParticles(ModParticleTypes.HEAL_EFFECT.get(), this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0, d0, d1, d2, 0.5F);
                        }
                    }
                    pPlayer.swing(pHand);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.mobInteract(pPlayer, pHand);
    }
}
