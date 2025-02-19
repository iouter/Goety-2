package com.Polarice3.Goety.utils;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.api.items.armor.ISoulDiscount;
import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.capabilities.soulenergy.*;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.hostile.illagers.Ripper;
import com.Polarice3.Goety.common.entities.util.SurveyEye;
import com.Polarice3.Goety.common.events.ArcaTeleporter;
import com.Polarice3.Goety.common.items.ModTiers;
import com.Polarice3.Goety.common.items.equipment.FangedDaggerItem;
import com.Polarice3.Goety.common.listeners.SoulTakenListener;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket;
import com.Polarice3.Goety.common.research.Research;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.compat.minecolonies.MinecoloniesLoaded;
import com.Polarice3.Goety.config.BrewConfig;
import com.Polarice3.Goety.config.MainConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class SEHelper {

    public static ISoulEnergy getCapability(Player player) {
        return player.getCapability(SEProvider.CAPABILITY).orElse(new SEImp());
    }

    public static boolean getSEActive(Player player) {
        return getCapability(player).getSEActive();
    }

    public static void setSEActive(Player player, boolean active){
        getCapability(player).setSEActive(active);
    }

    public static int getSESouls(Player player){
        return getCapability(player).getSoulEnergy();
    }

    public static void setSESouls(Player player, int souls){
        getCapability(player).setSoulEnergy(souls);
    }

    public static void setSoulsAmount(Player player, int souls){
        if (SEHelper.getSEActive(player)){
            SEHelper.setSESouls(player, souls);
            SEHelper.sendSEUpdatePacket(player);
        } else if (!TotemFinder.FindTotem(player).isEmpty()){
            ITotem.setSoulsamount(TotemFinder.FindTotem(player), souls);
        }
    }

    @Nullable
    public static BlockPos getArcaBlock(Player player){
        return getCapability(player).getArcaBlock();
    }

    public static ResourceKey<Level> getArcaDimension(Player player){
        return getCapability(player).getArcaBlockDimension();
    }

    public static void setEndWalk(Player player, @Nullable BlockPos blockPos, @Nullable ResourceKey<Level> dimension){
        getCapability(player).setEndWalkPos(blockPos);
        getCapability(player).setEndWalkDimension(dimension);
    }

    public static void removeEndWalk(Player player){
        setEndWalk(player, null, null);
    }

    @Nullable
    public static BlockPos getEndWalkPos(Player player){
        return getCapability(player).getEndWalkPos();
    }

    @Nullable
    public static ResourceKey<Level> getEndWalkDimension(Player player){
        return getCapability(player).getEndWalkDimension();
    }

    public static boolean hasEndWalk(Player player){
        return getEndWalkPos(player) != null;
    }

    public static boolean decreaseSESouls(Player player, int souls){
        return getCapability(player).decreaseSE(souls);
    }

    public static boolean increaseSESouls(Player player, int souls){
        return getCapability(player).increaseSE(souls);
    }

    public static boolean apostleWarned(Player player){
        return getCapability(player).apostleWarned();
    }

    public static void setApostleWarned(Player player, boolean warned){
        getCapability(player).setApostleWarned(warned);
    }

    public static boolean getSoulsContainer(Player player){
        if(SEHelper.getSEActive(player)){
            return true;
        } else return !TotemFinder.FindTotem(player).isEmpty();
    }

    public static boolean getSoulsAmount(Player player, int souls){
        if (SEHelper.getSEActive(player) && SEHelper.getSESouls(player) >= souls){
            return true;
        } else {
            return !TotemFinder.FindTotem(player).isEmpty() && ITotem.currentSouls(TotemFinder.FindTotem(player)) >= souls && !SEHelper.getSEActive(player);
        }
    }

    public static int getSoulAmountInt(Player player){
        if (SEHelper.getSEActive(player)){
            return SEHelper.getSESouls(player);
        } else if (!TotemFinder.FindTotem(player).isEmpty()){
            return ITotem.currentSouls(TotemFinder.FindTotem(player));
        }
        return 0;
    }

    public static int getSoulGiven(LivingEntity victim){
        if (victim != null){
            boolean flag = true;
            if (victim instanceof IOwned owned && owned.getTrueOwner() != null){
                flag = false;
            } else if (victim.isBaby() && !(victim instanceof Enemy)){
                flag = false;
            }
            if (flag) {
                if (SoulTakenListener.getSoulAmount(victim) > 0){
                    return SoulTakenListener.getSoulAmount(victim);
                } else if (victim.getMobType() == MobType.UNDEAD) {
                    return MainConfig.UndeadSouls.get();
                } else if (victim.getMobType() == MobType.ARTHROPOD) {
                    return MainConfig.AnthropodSouls.get();
                } else if (victim instanceof Animal) {
                    return MainConfig.AnimalSouls.get();
                } else if (victim instanceof Raider && !(victim instanceof Ripper)) {
                    return MainConfig.IllagerSouls.get();
                } else if (victim instanceof VillagerDataHolder
                        || victim instanceof ReputationEventHandler
                        || victim instanceof Npc
                        || victim instanceof Merchant) {
                    return MainConfig.VillagerSouls.get();
                } else if (victim instanceof AbstractPiglin) {
                    return MainConfig.PiglinSouls.get();
                } else if (victim instanceof EnderMan){
                    return MainConfig.EndermanSouls.get();
                } else if (victim instanceof Player) {
                    return MainConfig.PlayerSouls.get();
                } else if (MinecoloniesLoaded.MINECOLONIES.isLoaded()
                        && victim.getType().getDescriptionId().contains("minecolonies")
                        && victim.getType().getCategory() != MobCategory.MISC){
                    return MainConfig.VillagerSouls.get();
                } else {
                    return MainConfig.DefaultSouls.get();
                }
            }
        }
        return 0;
    }

    public static void rawHandleKill(LivingEntity killer, LivingEntity victim, int soulEater, DamageSource source) {
        Player player = null;
        int multi = Mth.clamp(MainConfig.SoulTakenMultiplier.get(), 1, Integer.MAX_VALUE);
        float extra = soulEater;
        if (killer instanceof Player){
            player = (Player) killer;
        } else if (killer instanceof IOwned summonedEntity){
            if (summonedEntity.getTrueOwner() instanceof Player){
                player = (Player) summonedEntity.getTrueOwner();
            }
        }
        if (player != null) {
            if (ModDamageSource.physicalAttacks(source)) {
                ItemStack itemStack = killer.getMainHandItem();
                Item item = itemStack.getItem();
                if (item instanceof FangedDaggerItem
                        || (item instanceof TieredItem tieredItem && tieredItem.getTier() == ModTiers.DARK)) {
                    extra *= 1.5F;
                }
            }
            int soulGain = Mth.floor(getSoulGiven(victim) * extra) * multi;
            increaseSouls(player, soulGain);
        }
    }

    public static void handleKill(LivingEntity killer, LivingEntity victim, DamageSource source) {
        SEHelper.rawHandleKill(killer, victim, SEHelper.SoulMultiply(killer, source), source);
    }

    public static void increaseSouls(Player player, int souls){
        if (getSEActive(player)) {
            increaseSESouls(player, souls);
            SEHelper.sendSEUpdatePacket(player);
        } else {
            ItemStack foundStack = TotemFinder.FindTotem(player);
            if (foundStack != null){
                ITotem.increaseSouls(foundStack, souls);
            }
        }
    }

    public static float soulDiscount(LivingEntity living){
        float init = 1.0F;
        for (ItemStack itemStack : living.getArmorSlots()){
            if (itemStack.getItem() instanceof ISoulDiscount soulDiscount){
                init -= (soulDiscount.getSoulDiscount(LivingEntity.getEquipmentSlotForItem(itemStack)) / 100.0F);
            }
        }

        return init;
    }

    public static void decreaseSouls(Player player, int souls){
        souls *= soulDiscount(player);
        if (getSEActive(player)) {
            decreaseSESouls(player, souls);
            SEHelper.sendSEUpdatePacket(player);
        } else {
            ItemStack foundStack = TotemFinder.FindTotem(player);
            if (foundStack != null){
                ITotem.decreaseSouls(foundStack, souls);
            }
        }
    }

    public static int SoulMultiply(LivingEntity livingEntity, DamageSource source){
        ItemStack weapon= livingEntity.getMainHandItem();
        int multiply = 1;
        if (ModDamageSource.physicalAttacks(source)) {
            int i = weapon.getEnchantmentLevel(ModEnchantments.SOUL_EATER.get());
            if (i > 0) {
                multiply = Mth.clamp(i + 1, 1, 10);
            }
        }
        return multiply;
    }

    public static int getRecoil(Player player){
        return getCapability(player).getRecoil();
    }

    public static void setRecoil(Player player, int recoil){
        getCapability(player).setRecoil(recoil);
    }

    public static void increaseRecoil(Player player, int increase){
        setRecoil(player, Math.min(getRecoil(player) + increase, 100));
    }

    public static void decreaseRecoil(Player player, int decrease){
        setRecoil(player, Math.max(getRecoil(player) - decrease, 0));
    }

    public static int getRestPeriod(Player player){
        return getCapability(player).getRestPeriod();
    }

    public static void setRestPeriod(Player player, int restPeriod){
        getCapability(player).setRestPeriod(restPeriod);
    }

    public static boolean increaseRestPeriod(Player player, int increase){
        return getCapability(player).increaseRestPeriod(increase);
    }

    public static boolean decreaseRestPeriod(Player player, int decrease){
        return getCapability(player).decreaseRestPeriod(decrease);
    }

    public static boolean teleportToArca(Player player){
        ISoulEnergy soulEnergy = SEHelper.getCapability(player);
        BlockPos blockPos = SEHelper.getArcaBlock(player);
        BlockPos blockPos1 = BlockPos.containing(blockPos.getX() + 0.5F, blockPos.getY() + 0.5F, blockPos.getZ() + 0.5F);
        if (soulEnergy.getArcaBlockDimension() == player.level.dimension()) {
            Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, player.level, blockPos1);
            if (optional.isPresent()) {
                player.teleportTo(optional.get().x, optional.get().y, optional.get().z);
                return true;
            }
        } else {
            if (soulEnergy.getArcaBlockDimension() != null) {
                if (player.getServer() != null) {
                    ServerLevel serverWorld = player.getServer().getLevel(soulEnergy.getArcaBlockDimension());
                    if (serverWorld != null) {
                        Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, serverWorld, blockPos1);
                        if (optional.isPresent()) {
                            player.changeDimension(serverWorld, new ArcaTeleporter(optional.get()));
                            player.teleportTo(optional.get().x, optional.get().y, optional.get().z);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean addGrudgeEntity(Player owner, LivingEntity target){
        if (target != owner) {
            if (!getGrudgeEntities(owner).contains(target)) {
                getCapability(owner).addGrudge(target.getUUID());
                return true;
            }
        }
        return false;
    }

    public static boolean removeGrudgeEntity(Player owner, LivingEntity target){
        if (target != owner) {
            if (getGrudgeEntities(owner).contains(target)) {
                getCapability(owner).removeGrudge(target.getUUID());
                return true;
            }
        }
        return false;
    }

    public static List<LivingEntity> getGrudgeEntities(Player owner){
        List<LivingEntity> livingEntities = new ArrayList<>();
        if (!getCapability(owner).grudgeList().isEmpty()){
            for (UUID uuid : getCapability(owner).grudgeList()){
                Entity entity = EntityFinder.getLivingEntityByUuiD(uuid);
                if (entity instanceof LivingEntity target && !livingEntities.contains(target) && target != owner){
                    livingEntities.add(target);
                }
            }
        }
        return livingEntities;
    }

    public static boolean addGrudgeEntityType(Player owner, EntityType<?> target){
        if (!getGrudgeEntityTypes(owner).contains(target)) {
            getCapability(owner).addGrudgeType(target);
            return true;
        }
        return false;
    }

    public static boolean removeGrudgeEntityType(Player owner, EntityType<?> target){
        if (getGrudgeEntityTypes(owner).contains(target)) {
            getCapability(owner).removeGrudgeType(target);
            return true;
        }
        return false;
    }

    public static List<EntityType<?>> getGrudgeEntityTypes(Player owner){
        List<EntityType<?>> entityTypes = new ArrayList<>();
        if (!getCapability(owner).grudgeTypeList().isEmpty()){
            for (EntityType<?> entityType : getCapability(owner).grudgeTypeList()){
                if (!entityTypes.contains(entityType)){
                    entityTypes.add(entityType);
                }
            }
        }
        return entityTypes;
    }

    public static boolean addAllyEntity(Player owner, LivingEntity target){
        if (target != owner) {
            if (!getAllyEntities(owner).contains(target)) {
                getCapability(owner).addAlly(target.getUUID());
                return true;
            }
        }
        return false;
    }

    public static boolean removeAllyEntity(Player owner, LivingEntity target){
        if (target != owner) {
            if (getAllyEntities(owner).contains(target)) {
                getCapability(owner).removeAlly(target.getUUID());
                return true;
            }
        }
        return false;
    }

    public static List<LivingEntity> getAllyEntities(Player owner){
        List<LivingEntity> livingEntities = new ArrayList<>();
        if (!getCapability(owner).allyList().isEmpty()){
            for (UUID uuid : getCapability(owner).allyList()){
                Entity entity = EntityFinder.getLivingEntityByUuiD(uuid);
                if (entity instanceof LivingEntity target && !livingEntities.contains(target) && target != owner){
                    livingEntities.add(target);
                }
            }
        }
        return livingEntities;
    }

    public static boolean addAllyEntityType(Player owner, EntityType<?> target){
        if (!getAllyEntityTypes(owner).contains(target)) {
            getCapability(owner).addAllyType(target);
            return true;
        }
        return false;
    }

    public static boolean removeAllyEntityType(Player owner, EntityType<?> target){
        if (getAllyEntityTypes(owner).contains(target)) {
            getCapability(owner).removeAllyType(target);
            return true;
        }
        return false;
    }

    public static List<EntityType<?>> getAllyEntityTypes(Player owner){
        List<EntityType<?>> entityTypes = new ArrayList<>();
        if (!getCapability(owner).allyTypeList().isEmpty()){
            for (EntityType<?> entityType : getCapability(owner).allyTypeList()){
                if (!entityTypes.contains(entityType)){
                    entityTypes.add(entityType);
                }
            }
        }
        return entityTypes;
    }

    public static boolean addGroundedEntity(Player owner, LivingEntity target){
        if (target != owner) {
            if (!getGroundedEntities(owner).contains(target)) {
                getCapability(owner).addGrounded(target.getUUID());
                return true;
            }
        }
        return false;
    }

    public static boolean removeGroundedEntity(Player owner, LivingEntity target){
        if (target != owner) {
            if (getGroundedEntities(owner).contains(target)) {
                getCapability(owner).removeGrounded(target.getUUID());
                return true;
            }
        }
        return false;
    }

    public static List<LivingEntity> getGroundedEntities(Player owner){
        List<LivingEntity> livingEntities = new ArrayList<>();
        if (!getCapability(owner).groundedList().isEmpty()){
            for (UUID uuid : getCapability(owner).groundedList()){
                Entity entity = EntityFinder.getLivingEntityByUuiD(uuid);
                if (entity instanceof LivingEntity target && !livingEntities.contains(target) && target != owner){
                    livingEntities.add(target);
                }
            }
        }
        return livingEntities;
    }

    public static boolean addGroundedEntityType(Player owner, EntityType<?> target){
        if (!getGroundedEntityTypes(owner).contains(target)) {
            getCapability(owner).addGroundedType(target);
            return true;
        }
        return false;
    }

    public static boolean removeGroundedEntityType(Player owner, EntityType<?> target){
        if (getGroundedEntityTypes(owner).contains(target)) {
            getCapability(owner).removeGroundedType(target);
            return true;
        }
        return false;
    }

    public static List<EntityType<?>> getGroundedEntityTypes(Player owner){
        List<EntityType<?>> entityTypes = new ArrayList<>();
        if (!getCapability(owner).groundedTypeList().isEmpty()){
            for (EntityType<?> entityType : getCapability(owner).groundedTypeList()){
                if (!entityTypes.contains(entityType)){
                    entityTypes.add(entityType);
                }
            }
        }
        return entityTypes;
    }

    public static boolean isGrounded(LivingEntity owner, LivingEntity livingEntity){
        if (owner instanceof Player player){
            return isGrounded(player, livingEntity);
        }
        return false;
    }

    public static boolean isGrounded(Player owner, LivingEntity livingEntity){
        return getGroundedEntities(owner).contains(livingEntity) || getGroundedEntityTypes(owner).contains(livingEntity.getType());
    }

    public static boolean addResearch(Player player, Research research){
        if (!getResearch(player).contains(research)) {
            getCapability(player).addResearch(research);
            SEHelper.sendSEUpdatePacket(player);
            return true;
        }
        return false;
    }

    public static boolean removeResearch(Player player, Research research){
        if (getResearch(player).contains(research)) {
            getCapability(player).removeResearch(research);
            SEHelper.sendSEUpdatePacket(player);
            return true;
        }
        return false;
    }

    public static List<Research> getResearch(Player player){
        return getCapability(player).getResearch();
    }

    public static boolean hasResearch(Player player, Research research){
        return getResearch(player).contains(research);
    }

    public static FocusCooldown getFocusCoolDown(Player player){
        return getCapability(player).cooldowns();
    }

    public static void addCooldown(Player player, Item item, int duration){
        getFocusCoolDown(player).addCooldown(player, player.level, item, duration);
    }

    public static FocusCooldown.CooldownInstance getCooldownInstance(Player player, Item item){
        return getFocusCoolDown(player).getInstance(item);
    }

    public static Map<Item, FocusCooldown.CooldownInstance> getCooldowns(Player player){
        return getFocusCoolDown(player).getCooldowns();
    }

    @Nullable
    public static Projectile getGrappling(Player player){
        return getCapability(player).getGrappling();
    }

    public static void setGrappling(Player player, @Nullable Projectile projectile){
        getCapability(player).setGrappling(projectile);
    }

    public static int getBottling(Player player){
        return getCapability(player).bottling();
    }

    public static int getBottleLevel(Player player){
        return getCapability(player).bottling() / BrewConfig.BottlingLevelReq.get();
    }

    public static void setBottleLevel(Player player, int level){
        getCapability(player).setBottling(BrewConfig.BottlingLevelReq.get() * level);
        SEHelper.sendSEUpdatePacket(player);
    }

    public static void setBottling(Player player, int bottling){
        getCapability(player).setBottling(bottling);
        SEHelper.sendSEUpdatePacket(player);
    }

    public static void increaseBottling(Player player){
        increaseBottling(player, 1);
    }

    public static void increaseBottling(Player player, int increase){
        if (BrewConfig.MaxBottlingLevel.get() > 0) {
            if (getBottling(player) < BrewConfig.MaxBottlingLevel.get() * BrewConfig.BottlingLevelReq.get()) {
                setBottling(player, getBottling(player) + increase);
                if (getBottling(player) > 0 && getBottling(player) % BrewConfig.BottlingLevelReq.get() == 0) {
                    if (!player.level.isClientSide){
                        ModNetwork.sendTo(player, new SPlayPlayerSoundPacket(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.5F));
                    }
                    if (getBottling(player) >= BrewConfig.MaxBottlingLevel.get() * BrewConfig.BottlingLevelReq.get()){
                        player.displayClientMessage(Component.translatable("info.goety.brew.max_level").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    } else {
                        player.displayClientMessage(Component.translatable("info.goety.brew.level_up").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    }
                }
            }
        }
    }

    public static int getWardingLeft(Player player){
        return getCapability(player).wardingLeft();
    }

    public static int getMaxWarding(Player player){
        return getCapability(player).maxWarding();
    }

    public static void setCurrentWarding(Player player, int ward){
        getCapability(player).setWarding(ward);
        SEHelper.sendSEUpdatePacket(player);
    }

    public static void increaseWarding(Player player, int amount){
        if (getWardingLeft(player) < getMaxWarding(player)){
            setCurrentWarding(player, getWardingLeft(player) + amount);
        }
    }

    public static void damageWarding(Player player, int amount){
        if (getWardingLeft(player) > 0){
            if ((getWardingLeft(player) - amount) <= 0){
                setCurrentWarding(player, 0);
                player.stopUsingItem();
                player.addEffect(new MobEffectInstance(GoetyEffects.STUNNED.get(), 40));
                if (!player.level.isClientSide){
                    ModNetwork.sendTo(player, new SPlayPlayerSoundPacket(SoundEvents.SHIELD_BREAK, 1.0F, 1.0F));
                }
            } else {
                setCurrentWarding(player, getWardingLeft(player) - amount);
            }
        }
    }

    public static void setMaxWarding(Player player, int ward){
        getCapability(player).setMaxWarding(ward);
        SEHelper.sendSEUpdatePacket(player);
    }

    //Air Jumps codes based on Zepalesque's codes: https://github.com/Zepalesque/The-Aether-Redux/blob/1.20.1/src/main/java/net/zepalesque/redux/capability/player/ReduxPlayerCapability.java
    public static int getTicksInAir(Player player){
        return getCapability(player).getTicksInAir();
    }

    public static int getAirJumps(Player player){
        return getCapability(player).getAirJumps();
    }

    public static int getAirJumpCooldown(Player player){
        return getCapability(player).getAirJumpCooldown();
    }

    public static void setTicksInAir(Player player, int tick){
        getCapability(player).setTicksInAir(tick);
        SEHelper.sendSEUpdatePacket(player);
    }

    public static void setAirJumps(Player player, int jump){
        getCapability(player).setAirJumps(jump);
        SEHelper.sendSEUpdatePacket(player);
    }

    public static boolean increaseAirJumpCount(Player player){
        MobEffectInstance instance = player.getEffect(GoetyEffects.FROG_LEG.get());
        if (instance != null) {
            if (getAirJumpCooldown(player) <= 0 && getAirJumps(player) < (instance.getAmplifier() + 1)) {
                setAirJumps(player, getAirJumps(player) + 1);
                setAirJumpCooldown(player, 4);
                SEHelper.sendSEUpdatePacket(player);
                return true;
            }
        }
        return false;
    }

    public static void setAirJumpCooldown(Player player, int cool){
        getCapability(player).setAirJumpCooldown(cool);
        SEHelper.sendSEUpdatePacket(player);
    }

    public static boolean doubleJump(Player player) {
        if (increaseAirJumpCount(player)) {
            doDoubleJumpMovement(player);
            return true;
        } else {
            return false;
        }
    }

    public static void doDoubleJumpMovement(LivingEntity entity) {
        double xDelta = entity.getDeltaMovement().x() * 1.4D;
        double yDelta = 0.42D + entity.getJumpBoostPower();
        double zDelta = entity.getDeltaMovement().z() * 1.4D;
        entity.setDeltaMovement(xDelta, yDelta, zDelta);
        entity.resetFallDistance();
        spawnDoubleJumpParticles(entity.level, entity.position(), 1.5D, 12);
        entity.level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.FROG_LONG_JUMP, SoundSource.PLAYERS, 1.0F, 0.9F + entity.level.random.nextFloat() * 0.2F);
    }

    public static void spawnDoubleJumpParticles(Level level, Vec3 vec3, double radius, int quantity) {
        for (int i = 0; i < quantity; i++) {
            double x2 = vec3.x + (level.random.nextDouble() * radius) - (radius * 0.5D);
            double y2 = vec3.y + (level.random.nextDouble() * 0.4D);
            double z2 = vec3.z + (level.random.nextDouble() * radius) - (radius * 0.5D);
            level.addParticle(ParticleTypes.CLOUD, x2, y2, z2, 0.0D, level.random.nextDouble() * 0.03D, 0.0D);
        }
    }

    public static int getMiningProgress(Player player){
        return getCapability(player).getMiningProgress();
    }

    public static void setMiningProgress(Player player, int progress){
        getCapability(player).setMiningProgress(progress);
    }

    public static void increaseMiningProgress(Player player){
        setMiningProgress(player, getMiningProgress(player) + 1);
    }

    @Nullable
    public static BlockPos getMiningPos(Player player){
        return getCapability(player).getMiningPos();
    }

    public static void setMiningPos(Player player, BlockPos blockPos){
        getCapability(player).setMiningPos(blockPos);
    }

    public static boolean hasCamera(Player player){
        return getCapability(player).getCameraUUID() != null;
    }

    public static void setCamera(Player player, @Nullable Entity target){
        setCamera(player, target, null);
    }

    public static void setCamera(Player player, @Nullable Entity target, @Nullable BlockPos blockPos){
        if (target != null || blockPos != null) {
            if (target == player || blockPos != null){
                double Y;
                SurveyEye surveyEye = new SurveyEye(ModEntityType.SURVEY_EYE.get(), player.level);
                BlockPos.MutableBlockPos blockpos$mutable;
                if (target == player){
                    Y = player.getY();
                    blockpos$mutable = new BlockPos.MutableBlockPos(player.getX(), player.getY() + 8, player.getZ());
                } else {
                    Y = blockPos.getY();
                    blockpos$mutable = new BlockPos.MutableBlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
                }

                while(blockpos$mutable.getY() < Y + 16.0D && !player.level.getBlockState(blockpos$mutable).blocksMotion()) {
                    blockpos$mutable.move(Direction.UP);
                }
                surveyEye.setPos(Vec3.atCenterOf(blockpos$mutable));
                if (target == player) {
                    surveyEye.lookAt(player, 90.0F, 90.0F);
                } else {
                    surveyEye.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atBottomCenterOf(blockpos$mutable));
                }
                surveyEye.setOwner(player);
                if (player.level.addFreshEntity(surveyEye)){
                    getCapability(player).setCameraUUID(surveyEye.getUUID());
                    target = surveyEye;
                }
            } else {
                getCapability(player).setCameraUUID(target.getUUID());
            }
        } else {
            getCapability(player).setCameraUUID(null);
            target = player;
        }
        if (player instanceof ServerPlayer serverPlayer && target != null) {
            serverPlayer.connection.send(new ClientboundSetCameraPacket(target));
        }
        sendSEUpdatePacket(player);
    }

    public static void sendSEUpdatePacket(Player player) {
        if (!player.level.isClientSide()) {
            ModNetwork.sendTo(player, new SEUpdatePacket(player));
        }
    }

    public static CompoundTag save(CompoundTag tag, ISoulEnergy soulEnergy) {
        tag.putBoolean("seActive", soulEnergy.getSEActive());
        tag.putInt("soulEnergy", soulEnergy.getSoulEnergy());
        tag.putInt("recoil", soulEnergy.getRecoil());
        tag.putInt("restPeriod", soulEnergy.getRestPeriod());
        tag.putBoolean("apostleWarned", soulEnergy.apostleWarned());
        tag.putInt("bottling", soulEnergy.bottling());
        tag.putInt("warding", soulEnergy.wardingLeft());
        tag.putInt("maxWarding", soulEnergy.maxWarding());
        tag.putInt("airTick", soulEnergy.getTicksInAir());
        tag.putInt("airJumps", soulEnergy.getAirJumps());
        tag.putInt("airJumpCoolDown", soulEnergy.getAirJumpCooldown());
        tag.putInt("miningProgress", soulEnergy.getMiningProgress());
        if (soulEnergy.getCameraUUID() != null) {
            tag.putUUID("cameraUUID", soulEnergy.getCameraUUID());
        }
        if (soulEnergy.getArcaBlock() != null) {
            tag.putInt("arcax", soulEnergy.getArcaBlock().getX());
            tag.putInt("arcay", soulEnergy.getArcaBlock().getY());
            tag.putInt("arcaz", soulEnergy.getArcaBlock().getZ());
            ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, soulEnergy.getArcaBlockDimension().location()).resultOrPartial(Goety.LOGGER::error).ifPresent(
                    (p_241148_1_) -> tag.put("dimension", p_241148_1_));
        }
        if (soulEnergy.getMiningPos() != null) {
            tag.putInt("miningPosX", soulEnergy.getMiningPos().getX());
            tag.putInt("miningPosY", soulEnergy.getMiningPos().getY());
            tag.putInt("miningPosZ", soulEnergy.getMiningPos().getZ());
        }

        if (soulEnergy.grudgeList() != null) {
            ListTag listTag = new ListTag();
            if (!soulEnergy.grudgeList().isEmpty()) {
                for (UUID uuid : soulEnergy.grudgeList()) {
                    listTag.add(NbtUtils.createUUID(uuid));
                }
                tag.put("grudgeList", listTag);
            }
        }

        if (soulEnergy.grudgeTypeList() != null){
            ListTag listTag = new ListTag();
            if (!soulEnergy.grudgeTypeList().isEmpty()) {
                for (EntityType<?> entityType : soulEnergy.grudgeTypeList()){
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putString("id", EntityType.getKey(entityType).toString());
                    listTag.add(compoundTag);
                }
                tag.put("grudgeTypeList", listTag);
            }
        }

        if (soulEnergy.allyList() != null) {
            ListTag listTag = new ListTag();
            if (!soulEnergy.allyList().isEmpty()) {
                for (UUID uuid : soulEnergy.allyList()) {
                    listTag.add(NbtUtils.createUUID(uuid));
                }
                tag.put("allyList", listTag);
            }
        }

        if (soulEnergy.allyTypeList() != null){
            ListTag listTag = new ListTag();
            if (!soulEnergy.allyTypeList().isEmpty()) {
                for (EntityType<?> entityType : soulEnergy.allyTypeList()){
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putString("id", EntityType.getKey(entityType).toString());
                    listTag.add(compoundTag);
                }
                tag.put("allyTypeList", listTag);
            }
        }

        if (soulEnergy.groundedList() != null) {
            ListTag listTag = new ListTag();
            if (!soulEnergy.groundedList().isEmpty()) {
                for (UUID uuid : soulEnergy.groundedList()) {
                    listTag.add(NbtUtils.createUUID(uuid));
                }
                tag.put("groundedList", listTag);
            }
        }

        if (soulEnergy.groundedTypeList() != null){
            ListTag listTag = new ListTag();
            if (!soulEnergy.groundedTypeList().isEmpty()) {
                for (EntityType<?> entityType : soulEnergy.groundedTypeList()){
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putString("id", EntityType.getKey(entityType).toString());
                    listTag.add(compoundTag);
                }
                tag.put("groundedTypeList", listTag);
            }
        }

        if (soulEnergy.getResearch() != null){
            ListTag listTag = new ListTag();
            if (!soulEnergy.getResearch().isEmpty()){
                for (Research research : soulEnergy.getResearch()){
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putString("research", research.getId());
                    listTag.add(compoundTag);
                }
                tag.put("researchList", listTag);
            }
        }

        if (soulEnergy.cooldowns() != null){
            ListTag listTag = new ListTag();
            soulEnergy.cooldowns().save(listTag);
            tag.put("coolDowns", listTag);
        }
        if (soulEnergy.getEndWalkPos() != null) {
            tag.putInt("EndWalkX", soulEnergy.getEndWalkPos().getX());
            tag.putInt("EndWalkY", soulEnergy.getEndWalkPos().getY());
            tag.putInt("EndWalkZ", soulEnergy.getEndWalkPos().getZ());
            ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, soulEnergy.getEndWalkDimension().location()).resultOrPartial(Goety.LOGGER::error).ifPresent(
                    (p_241148_1_) -> tag.put("EndWalkDim", p_241148_1_));
        }
        return tag;
    }

    public static ISoulEnergy load(CompoundTag tag, ISoulEnergy soulEnergy) {
        soulEnergy.setSEActive(tag.getBoolean("seActive"));
        if (tag.contains("arcax") && tag.contains("arcay") && tag.contains("arcaz")) {
            soulEnergy.setArcaBlock(new BlockPos(tag.getInt("arcax"), tag.getInt("arcay"), tag.getInt("arcaz")));
        }
        if (tag.contains("dimension")) {
            soulEnergy.setArcaBlockDimension(Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get("dimension")).resultOrPartial(Goety.LOGGER::error).orElse(Level.OVERWORLD));
        } else {
            soulEnergy.setArcaBlockDimension(Level.OVERWORLD);
        }
        if (tag.contains("EndWalkX") && tag.contains("EndWalkY") && tag.contains("EndWalkZ") && tag.contains("EndWalkDim")) {
            soulEnergy.setEndWalkPos(new BlockPos(tag.getInt("EndWalkX"), tag.getInt("EndWalkY"), tag.getInt("EndWalkZ")));
            soulEnergy.setEndWalkDimension(Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get("EndWalkDim")).resultOrPartial(Goety.LOGGER::error).orElse(Level.OVERWORLD));
        }
        if (tag.contains("miningPosX") && tag.contains("miningPosY") && tag.contains("miningPosZ")) {
            soulEnergy.setMiningPos(new BlockPos(tag.getInt("miningPosX"), tag.getInt("miningPosY"), tag.getInt("miningPosZ")));
        }
        soulEnergy.setSoulEnergy(tag.getInt("soulEnergy"));
        soulEnergy.setRecoil(tag.getInt("recoil"));
        soulEnergy.setRestPeriod(tag.getInt("restPeriod"));
        soulEnergy.setApostleWarned(tag.getBoolean("apostleWarned"));
        soulEnergy.setBottling(tag.getInt("bottling"));
        soulEnergy.setWarding(tag.getInt("warding"));
        soulEnergy.setMaxWarding(tag.getInt("maxWarding"));
        soulEnergy.setTicksInAir(tag.getInt("airTick"));
        soulEnergy.setAirJumps(tag.getInt("airJumps"));
        soulEnergy.setAirJumpCooldown(tag.getInt("airJumpCoolDown"));
        soulEnergy.setMiningProgress(tag.getInt("miningProgress"));
        if (tag.contains("cameraUUID")) {
            soulEnergy.setCameraUUID(tag.getUUID("cameraUUID"));
        } else {
            soulEnergy.setCameraUUID(null);
        }
        if (tag.contains("grudgeList", 9)) {
            ListTag listtag = tag.getList("grudgeList", 11);
            for (Tag value : listtag) {
                UUID uuid = NbtUtils.loadUUID(value);
                if (!soulEnergy.grudgeList().contains(uuid)){
                    soulEnergy.addGrudge(NbtUtils.loadUUID(value));
                }
            }
        }
        if (tag.contains("grudgeTypeList", Tag.TAG_LIST)) {
            ListTag listtag = tag.getList("grudgeTypeList", Tag.TAG_COMPOUND);
            for (int i = 0; i < listtag.size(); ++i) {
                String string = listtag.getCompound(i).getString("id");
                if (EntityType.byString(string).isPresent()){
                    EntityType<?> entityType = EntityType.byString(string).get();
                    if (!soulEnergy.grudgeTypeList().contains(entityType)) {
                        soulEnergy.addGrudgeType(EntityType.byString(string).get());
                    }
                }
            }
        }
        if (tag.contains("allyList", 9)) {
            ListTag listtag = tag.getList("allyList", 11);
            for (Tag value : listtag) {
                UUID uuid = NbtUtils.loadUUID(value);
                if (!soulEnergy.allyList().contains(uuid)) {
                    soulEnergy.addAlly(NbtUtils.loadUUID(value));
                }
            }
        }
        if (tag.contains("allyTypeList", Tag.TAG_LIST)) {
            ListTag listtag = tag.getList("allyTypeList", Tag.TAG_COMPOUND);
            for (int i = 0; i < listtag.size(); ++i) {
                String string = listtag.getCompound(i).getString("id");
                if (EntityType.byString(string).isPresent()){
                    EntityType<?> entityType = EntityType.byString(string).get();
                    if (!soulEnergy.allyTypeList().contains(entityType)) {
                        soulEnergy.addAllyType(EntityType.byString(string).get());
                    }
                }
            }
        }
        if (tag.contains("groundedList", 9)) {
            ListTag listtag = tag.getList("groundedList", 11);
            for (Tag value : listtag) {
                UUID uuid = NbtUtils.loadUUID(value);
                if (!soulEnergy.groundedList().contains(uuid)) {
                    soulEnergy.addGrounded(NbtUtils.loadUUID(value));
                }
            }
        }
        if (tag.contains("groundedTypeList", Tag.TAG_LIST)) {
            ListTag listtag = tag.getList("groundedTypeList", Tag.TAG_COMPOUND);
            for (int i = 0; i < listtag.size(); ++i) {
                String string = listtag.getCompound(i).getString("id");
                if (EntityType.byString(string).isPresent()){
                    EntityType<?> entityType = EntityType.byString(string).get();
                    if (!soulEnergy.groundedTypeList().contains(entityType)) {
                        soulEnergy.addGroundedType(EntityType.byString(string).get());
                    }
                }
            }
        }
        if (tag.contains("researchList", Tag.TAG_LIST)) {
            ListTag listtag = tag.getList("researchList", Tag.TAG_COMPOUND);
            for (int i = 0; i < listtag.size(); ++i) {
                String string = listtag.getCompound(i).getString("research");
                if (ResearchList.getResearch(string) != null) {
                    Research research = ResearchList.getResearch(string);
                    if (!soulEnergy.getResearch().contains(research)) {
                        soulEnergy.addResearch(ResearchList.getResearch(string));
                    }
                }
            }
        }
        if (tag.contains("coolDowns", Tag.TAG_LIST)){
            ListTag listTag = (ListTag) tag.get("coolDowns");
            soulEnergy.cooldowns().load(listTag);
        }
        return soulEnergy;
    }
}
