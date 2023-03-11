package com.Polarice3.Goety.common.events;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.MainConfig;
import com.Polarice3.Goety.common.effects.ModEffects;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.utils.LichdomHelper;
import com.Polarice3.Goety.utils.ModMathHelper;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Goety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LichEvents {

    @SubscribeEvent
    public static void onPlayerLichdom(TickEvent.PlayerTickEvent event){
        List<BlockState> result = new ArrayList<>();
        Player player = event.player;
        Level world = player.level;
        if (LichdomHelper.isLich(player)){
            player.getFoodData().setFoodLevel(17);
            player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            boolean burn = false;
            if (!world.isClientSide && world.isDay()) {
                float f = player.getLightLevelDependentMagicValue();
                BlockPos blockpos = player.getVehicle() instanceof Boat ? (new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ())).above() : new BlockPos(player.getX(), (double) Math.round(player.getY()), player.getZ());
                if (f > 0.5F && world.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && world.canSeeSky(blockpos)) {
                    burn = true;
                }
            }

            if (burn){
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                if (!helmet.isEmpty()) {
                    if (!player.isCreative()) {
                        if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                            if (helmet.isDamageableItem()) {
                                helmet.setDamageValue(helmet.getDamageValue() + world.random.nextInt(2));
                                if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
                                    player.broadcastBreakEvent(EquipmentSlot.HEAD);
                                    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                                }
                            }
                        }
                    }
                    burn = false;
                }
                if (burn){
                    if (player.hasEffect(MobEffects.FIRE_RESISTANCE) && !player.isCreative() && !player.isSpectator()){
                        player.addEffect(new MobEffectInstance(ModEffects.SAPPED.get(), 100, 1, false, false));
                    } else {
                        player.setSecondsOnFire(8);
                    }
                }
            }

            for (MobEffectInstance effectInstance : player.getActiveEffects()){
                MobEffect effect = effectInstance.getEffect();
                if (!new Zombie(world).canBeAffected(effectInstance)){
                    player.removeEffectNoUpdate(effect);
                }
                if (effect == MobEffects.BLINDNESS || effect == MobEffects.CONFUSION
                        || effect == MobEffects.HUNGER || effect == MobEffects.SATURATION){
                    player.removeEffectNoUpdate(effect);
                }
            }
            if (player.hasEffect(ModEffects.SOUL_HUNGER.get())){
                if (SEHelper.getSoulsAmount(player, MainConfig.MaxSouls.get())){
                    player.removeEffectNoUpdate(ModEffects.SOUL_HUNGER.get());
                }
            }
            if (!player.isOnFire()) {
                if (player.getHealth() < player.getMaxHealth()) {
                    if (player.tickCount % 20 == 0 && SEHelper.getSoulsAmount(player, MainConfig.LichHealCost.get())) {
                        player.heal(1.0F);
                        Vec3 vector3d = player.getDeltaMovement();
                        if (!player.level.isClientSide){
                            ServerLevel serverWorld = (ServerLevel) player.level;
                            serverWorld.sendParticles(ParticleTypes.SOUL, player.getRandomX(0.5D), player.getRandomY(), player.getRandomZ(0.5D), 0, vector3d.x * -0.2D, 0.1D, vector3d.z * -0.2D, 0.5F);
                        }
                        SEHelper.decreaseSouls(player, MainConfig.LichHealCost.get());
                    }
                }
            }
            for (Villager villager : player.level.getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(16.0D))) {
                if (villager.getPlayerReputation(player) > -100 && villager.getPlayerReputation(player) < 100) {
                    if (player.tickCount % 20 == 0) {
                        villager.getGossips().add(player.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
                    }
                }
            }
            for (IronGolem ironGolem : player.level.getEntitiesOfClass(IronGolem.class, player.getBoundingBox().inflate(16.0D))) {
                if (!ironGolem.isPlayerCreated() && ironGolem.getTarget() != player) {
                    ironGolem.setTarget(player);
                }
            }
            if (player.isAlive()){
                player.setAirSupply(player.getMaxAirSupply());
                if (MainConfig.LichNightVision.get()) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
                }
            }
        }
    }

    @SubscribeEvent
    public static void SpecialPotionEffects(MobEffectEvent.Applicable event){
        if (event.getEntity() instanceof Player player){
            if (LichdomHelper.isLich(player)){
                if (!new Zombie(player.level).canBeAffected(event.getEffectInstance())){
                    event.setResult(Event.Result.DENY);
                }
                if (event.getEffectInstance().getEffect() == MobEffects.BLINDNESS){
                    event.setResult(Event.Result.DENY);
                }
                if (event.getEffectInstance().getEffect() == MobEffects.CONFUSION){
                    event.setResult(Event.Result.DENY);
                }
                if (event.getEffectInstance().getEffect() == MobEffects.HUNGER){
                    event.setResult(Event.Result.DENY);
                }
                if (event.getEffectInstance().getEffect() == MobEffects.SATURATION){
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void UndeadFriendly(LivingChangeTargetEvent event){
        if (MainConfig.LichUndeadFriends.get()) {
            if (event.getEntity() instanceof Monster) {
                if (event.getEntity().getMobType() == MobType.UNDEAD) {
                    if (event.getOriginalTarget() != null) {
                        if (event.getOriginalTarget() instanceof Player player) {
                            if (LichdomHelper.isLich(player)) {
                                if (MainConfig.LichPowerfulFoes.get()) {
                                    if (event.getEntity().getMaxHealth() < 100) {
                                        if (!(event.getEntity() instanceof Owned)) {
                                            event.setNewTarget(null);
                                        }
                                    }
                                } else {
                                    ((Monster) event.getEntity()).setTarget(null);
                                    if (event.getEntity() instanceof NeutralMob){
                                        event.setNewTarget(null);
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
                if (event.getSource().getEntity() instanceof LivingEntity attacker){
                    if (attacker.getMainHandItem().isEnchanted()){
                        ItemStack weapon = attacker.getMainHandItem();
                        event.setAmount((float) (EnchantmentHelper.getDamageBonus(weapon, MobType.UNDEAD) + attacker.getAttributeValue(Attributes.ATTACK_DAMAGE)));
                    }
                }
                if (event.getSource() == DamageSource.DROWN){
                    event.setCanceled(true);
                }
                if (event.getSource().isMagic()){
                    event.setAmount(event.getAmount() * 0.15F);
                }
/*                if (ModDamageSource.frostAttacks(event.getSource())){
                    event.setAmount(event.getAmount()/2);
                }
                if (MainConfig.LichUndeadFriends.get()) {
                    if (RobeArmorFinder.FindNecroSet(player) && event.getSource().getEntity() != null) {
                        if (event.getSource().getEntity() instanceof LivingEntity) {
                            LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
                            for (MonsterEntity undead : player.level.getEntitiesOfClass(MonsterEntity.class, player.getBoundingBox().inflate(16))) {
                                if (undead.getMobType() == CreatureAttribute.UNDEAD && undead.getMaxHealth() < 100) {
                                    undead.setTarget(attacker);
                                }
                            }
                        }
                    }
                }*/
            }
        }
        if (event.getSource().getDirectEntity() instanceof Player){
            Player player = (Player) event.getSource().getDirectEntity();
            if (LichdomHelper.isLich(player)){
                if (player.getMainHandItem().isEmpty() && event.getEntity() != player){
                    event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200));
                }
                if (event.getEntity().getMobType() != MobType.UNDEAD){
                    event.getEntity().addEffect(new MobEffectInstance(MobEffects.WITHER, ModMathHelper.ticksToSeconds(5)));
                }
            }
        }
    }

    @SubscribeEvent
    public static void AttackEvent(LivingAttackEvent event){
        if (event.getEntity() instanceof Player player) {
            if (LichdomHelper.isLich(player)) {
                if (!new Zombie(player.level).hurt(event.getSource(), event.getAmount())) {
                    event.setCanceled(true);
                }
                if (event.getSource() == DamageSource.DROWN){
                    event.setCanceled(true);
                }
            }
        }
    }

}
