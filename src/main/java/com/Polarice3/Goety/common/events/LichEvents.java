package com.Polarice3.Goety.common.events;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.client.particles.ShockwaveParticleOption;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.compat.iron.IronAttributes;
import com.Polarice3.Goety.compat.iron.IronLoaded;
import com.Polarice3.Goety.config.MainConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import static net.minecraftforge.event.entity.living.LivingChangeTargetEvent.LivingTargetType.MOB_TARGET;

@Mod.EventBusSubscriber(modid = Goety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LichEvents {

    @SubscribeEvent
    public static void onPlayerLichdom(TickEvent.PlayerTickEvent event){
        Player player = event.player;
        Level world = player.level;
        if (LichdomHelper.isLich(player)){
            player.getFoodData().setFoodLevel(17);
            player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            boolean burn = MobUtil.isInSunlight(player) && !world.isRaining();

            if (burn){
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                if (!helmet.isEmpty()) {
                    if (!player.isCreative()) {
                        if (!player.hasEffect(MobEffects.FIRE_RESISTANCE) && !player.fireImmune()) {
                            if (MainConfig.LichDamageHelmet.get()) {
                                if (helmet.isDamageableItem()) {
                                    helmet.setDamageValue(helmet.getDamageValue() + world.random.nextInt(2));
                                    if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
                                        player.broadcastBreakEvent(EquipmentSlot.HEAD);
                                        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                                    }
                                }
                            }
                        }
                    }
                    burn = false;
                }
                if (burn){
                    if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)){
                        player.setSecondsOnFire(8);
                    }
                }
            }
            player.getActiveEffects().removeIf(effectInstance -> !EffectsUtil.canAffectLich(effectInstance, world));
            if (player.hasEffect(GoetyEffects.SOUL_HUNGER.get())){
                if (SEHelper.getSoulsAmount(player, MainConfig.MaxSouls.get())){
                    player.removeEffect(GoetyEffects.SOUL_HUNGER.get());
                }
            }
            if (MainConfig.LichSoulHeal.get()) {
                if (!player.isOnFire() && LichdomHelper.smited(player) <= 0) {
                    if (player.getHealth() < player.getMaxHealth()) {
                        if (player.tickCount % (MathHelper.secondsToTicks(MainConfig.LichHealSeconds.get()) + 1) == 0 && SEHelper.getSoulsAmount(player, MainConfig.LichHealCost.get())) {
                            player.heal(MainConfig.LichHealAmount.get().floatValue());
                            Vec3 vector3d = player.getDeltaMovement();
                            if (!player.level.isClientSide) {
                                ServerLevel serverWorld = (ServerLevel) player.level;
                                serverWorld.sendParticles(ParticleTypes.SCULK_SOUL, player.getRandomX(0.5D), player.getRandomY(), player.getRandomZ(0.5D), 0, vector3d.x * -0.2D, 0.1D, vector3d.z * -0.2D, 0.5F);
                            }
                            SEHelper.decreaseSouls(player, MainConfig.LichHealCost.get());
                        }
                    }
                }
            }
            if (MainConfig.LichVillagerHate.get() && player.tickCount % 20 == 0) {
                for (Villager villager : player.level.getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(16.0D))) {
                    if (villager.getPlayerReputation(player) > -200 && villager.getPlayerReputation(player) < 100) {
                        villager.getGossips().add(player.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
                    }
                }
                for (IronGolem ironGolem : player.level.getEntitiesOfClass(IronGolem.class, player.getBoundingBox().inflate(16.0D))) {
                    if (!ironGolem.isPlayerCreated() && ironGolem.getTarget() != player && TargetingConditions.forCombat().range(16.0F).test(ironGolem, player)) {
                        ironGolem.setTarget(player);
                    }
                }
            }
            if (LichdomHelper.isInLichMode(player)) {
                if (player.tickCount % 5 == 0) {
                    if (world.isClientSide) {
                        world.addParticle(ModParticleTypes.LICH.get(), player.getRandomX(0.5D), player.getY(), player.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
                    }
                }
                if (MainConfig.LichModeSounds.get()) {
                    if (event.phase == TickEvent.Phase.END) {
                        if (player.isAlive()) {
                            MiscCapHelper.doAmbientSoundTime(player);
                            if (MiscCapHelper.getAmbientSoundTime(player) > player.getRandom().nextInt(1000)) {
                                MiscCapHelper.resetAmbientSoundTime(player, MathHelper.secondsToTicks(8));
                                player.playSound(ModSounds.LICH_AMBIENT.get(), 1.0F, player.getVoicePitch());
                            }
                        }
                    }
                }
            }
            if (player.isAlive()){
                if (!player.level.isClientSide) {
                    if (LichdomHelper.nightVision(player) && MainConfig.LichNightVision.get()) {
                        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, false, false, false));
                    } else {
                        if (player.hasEffect(MobEffects.NIGHT_VISION)) {
                            player.removeEffect(MobEffects.NIGHT_VISION);
                        }
                    }
                }
            }
            if (LichdomHelper.smited(player) > 0){
                LichdomHelper.setSmited(player, LichdomHelper.smited(player) - 1);
            }
        } else {
            LichdomHelper.setLichMode(player, false);
            LichdomHelper.setNightVision(player, false);
        }

        if (IronLoaded.IRON_SPELLBOOKS.isLoaded()){
            AttributeInstance bloodResist = player.getAttribute(IronAttributes.BLOOD_MAGIC_RESIST);
            AttributeModifier attributemodifier = new AttributeModifier(UUID.fromString("1d0bd7da-03e8-4b25-be6d-014d73689417"), "Lich Blood Resistance", 0.5F, AttributeModifier.Operation.ADDITION);
            if (bloodResist != null){
                if (LichdomHelper.isLich(player)){
                    if (!bloodResist.hasModifier(attributemodifier)){
                        bloodResist.addPermanentModifier(attributemodifier);
                    }
                } else {
                    if (bloodResist.hasModifier(attributemodifier)){
                        bloodResist.removeModifier(attributemodifier);
                    }
                }
            }

            AttributeInstance holyResist = player.getAttribute(IronAttributes.HOLY_MAGIC_RESIST);
            AttributeModifier attributemodifier1 = new AttributeModifier(UUID.fromString("5290681e-7020-4de5-bba2-a659242d45a9"), "Lich Holy Weakness", -0.5F, AttributeModifier.Operation.ADDITION);
            if (holyResist != null){
                if (LichdomHelper.isLich(player)){
                    if (!holyResist.hasModifier(attributemodifier1)){
                        holyResist.addPermanentModifier(attributemodifier1);
                    }
                } else {
                    if (holyResist.hasModifier(attributemodifier1)){
                        holyResist.removeModifier(attributemodifier1);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void SpecialPotionEffects(MobEffectEvent.Applicable event){
        if (event.getEntity() instanceof Player player){
            if (LichdomHelper.isLich(player)){
                if (!EffectsUtil.canAffectLich(event.getEffectInstance(), player.level)) {
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void UndeadFriendly(LivingChangeTargetEvent event){
        if (MainConfig.LichUndeadFriends.get()) {
            if (event.getEntity() instanceof Enemy) {
                if (event.getEntity().getMobType() == MobType.UNDEAD
                        || event.getEntity().getType().is(ModTags.EntityTypes.LICH_NEUTRAL)) {
                    if (event.getOriginalTarget() != null) {
                        if (event.getOriginalTarget() instanceof Player player) {
                            if (LichdomHelper.isLich(player)) {
                                if (MainConfig.LichPowerfulFoes.get()) {
                                    if (event.getEntity().getMaxHealth() <= MainConfig.LichPowerfulFoesHealth.get()) {
                                        if (event.getTargetType() == MOB_TARGET) {
                                            event.setNewTarget(null);
                                            if (event.getEntity() instanceof NeutralMob){
                                                event.setNewTarget(null);
                                            }
                                        } else {
                                            event.setCanceled(true);
                                        }
                                    }
                                } else {
                                    if (event.getTargetType() == MOB_TARGET) {
                                        event.setNewTarget(null);
                                        if (event.getEntity() instanceof NeutralMob){
                                            event.setNewTarget(null);
                                        }
                                    } else {
                                        event.setCanceled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void HurtEvent(LivingHurtEvent event){
        if (event.getEntity() instanceof Player player) {
            if (LichdomHelper.isLich(player)){
                if (MainConfig.LichMagicResist.get()) {
                    if (event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                        event.setAmount(event.getAmount() * 0.15F);
                    }
                }
                if (ModDamageSource.freezeAttacks(event.getSource()) || event.getSource().is(DamageTypeTags.IS_FREEZING)){
                    event.setAmount(event.getAmount()/2);
                }
                if (MainConfig.LichUndeadFriends.get()) {
                    if (CuriosFinder.hasUndeadSet(player) && event.getSource().getEntity() != null) {
                        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                            for (Mob undead : player.level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(16))) {
                                if (undead != attacker) {
                                    if (undead.getMobType() == MobType.UNDEAD) {
                                        if (undead.getTarget() != player) {
                                            if (MainConfig.LichPowerfulFoes.get()) {
                                                if (undead.getMaxHealth() <= MainConfig.LichPowerfulFoesHealth.get()) {
                                                    undead.setTarget(attacker);
                                                }
                                            } else {
                                                undead.setTarget(attacker);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (LichdomHelper.isInLichMode(player)){
                    if (MainConfig.LichModeSounds.get()) {
                        if (player.isAlive()) {
                            if (event.getAmount() > 0.0F) {
                                if (!player.level.isClientSide) {
                                    player.level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LICH_HURT.get(), player.getSoundSource(), 1.0F, player.getVoicePitch());
                                    MiscCapHelper.resetAmbientSoundTime(player, MathHelper.secondsToTicks(8));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (event.getSource().getDirectEntity() instanceof Player player){
            if (LichdomHelper.isLich(player) && MainConfig.LichTouch.get()){
                if (ModDamageSource.physicalAttacks(event.getSource()) && event.getEntity() != player){
                    if (player.getMainHandItem().isEmpty()) {
                        event.getEntity().addEffect(new MobEffectInstance(GoetyEffects.FREEZING.get(), 900));
                    }
                    if (event.getEntity().getMobType() != MobType.UNDEAD && player.getMainHandItem().is(ModTags.Items.LICH_WITHER_ITEMS)){
                        event.getEntity().addEffect(new MobEffectInstance(MobEffects.WITHER, MathHelper.secondsToTicks(5)));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (LichdomHelper.isLich(livingEntity)){
            if (LichdomHelper.isInLichMode(livingEntity)){
                if (!event.isCanceled()){
                    if (MainConfig.LichModeSounds.get()) {
                        livingEntity.playSound(ModSounds.LICH_DEATH.get(), 1.0F, livingEntity.getVoicePitch());
                    }
                    if (livingEntity.level instanceof ServerLevel serverLevel){
                        ColorUtil colorUtil = new ColorUtil(0x36e416);
                        serverLevel.sendParticles(new ShockwaveParticleOption(0, colorUtil.red(), colorUtil.green(), colorUtil.blue(), 20, 0, true), livingEntity.getX(), livingEntity.getY() + 0.5F, livingEntity.getZ(), 0, 0, 0, 0, 0.5F);
                    }
                }
            }
        }
    }

}
