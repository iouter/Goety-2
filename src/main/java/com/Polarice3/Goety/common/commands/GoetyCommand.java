package com.Polarice3.Goety.common.commands;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.common.events.IllagerSpawner;
import com.Polarice3.Goety.common.events.WightSpawner;
import com.Polarice3.Goety.common.research.Research;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GoetyCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed"));
    private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed.uuid"));
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.summon.invalidPosition"));
    private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType(Component.translatable("commands.goety.soul.set.points.invalid"));
    private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID2 = new SimpleCommandExceptionType(Component.translatable("commands.goety.illager.rest.set.points.invalid"));
    private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID3 = new SimpleCommandExceptionType(Component.translatable("commands.goety.brew.level.set.failure"));
    private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID4 = new SimpleCommandExceptionType(Component.translatable("commands.goety.brew.xp.set.failure"));
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_RESEARCHES = (p_136344_, p_136345_) -> {
        Collection<Research> collection = ResearchList.getResearchIdList().values();
        return SharedSuggestionProvider.suggestResource(collection.stream().map(Research::getLocation), p_136345_);
    };

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext p_250122_) {
        pDispatcher.register(Commands.literal("goety")
                .requires((p_198442_0_) -> {
                    return p_198442_0_.hasPermission(2);
                })
                .then(Commands.literal("soul")
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer()).executes((p_198445_0_) -> {
                            return addSoulEnergy(p_198445_0_.getSource(), EntityArgument.getPlayers(p_198445_0_, "targets"), IntegerArgumentType.getInteger(p_198445_0_, "amount"));
                        }))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes((p_198439_0_) -> {
                            return setSoulEnergy(p_198439_0_.getSource(), EntityArgument.getPlayers(p_198439_0_, "targets"), IntegerArgumentType.getInteger(p_198439_0_, "amount"));
                        })))))
                .then(Commands.literal("illager")
                        .then(Commands.literal("spawn").executes((p_198352_0_) -> {
                            return spawnIllagers(p_198352_0_.getSource(), p_198352_0_.getSource().getPlayerOrException());
                        })
                                .then(Commands.argument("targets", EntityArgument.player()).executes((p_198435_0_) -> {
                                    return spawnIllagers(p_198435_0_.getSource(), EntityArgument.getPlayer(p_198435_0_, "targets"));
                                })))
                        .then(Commands.literal("rest")
                                .then(Commands.literal("get").executes((p_198352_0_) -> {
                                            return getRestPeriod(p_198352_0_.getSource(), p_198352_0_.getSource().getPlayerOrException());
                                        })
                                        .then(Commands.argument("targets", EntityArgument.player()).executes((p_198435_0_) -> {
                                            return getRestPeriod(p_198435_0_.getSource(), EntityArgument.getPlayer(p_198435_0_, "targets"));
                                        })))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("ticks", IntegerArgumentType.integer()).executes((p_198445_0_) -> {
                                                    return addRestPeriod(p_198445_0_.getSource(), EntityArgument.getPlayers(p_198445_0_, "targets"), IntegerArgumentType.getInteger(p_198445_0_, "ticks"));
                                                }))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("ticks", IntegerArgumentType.integer(0)).executes((p_198439_0_) -> {
                                                    return setRestPeriod(p_198439_0_.getSource(), EntityArgument.getPlayers(p_198439_0_, "targets"), IntegerArgumentType.getInteger(p_198439_0_, "ticks"));
                                                }))))))
                .then(Commands.literal("summons")
                        .then(Commands.literal("summon_noai")
                                .then(Commands.argument("entity", ResourceArgument.resource(p_250122_, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes((p_198738_0_) -> {
                                    return spawnNoAIEntity(p_198738_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198738_0_, "entity"), p_198738_0_.getSource().getPosition(), new CompoundTag(), true);
                                })
                                        .then(Commands.argument("pos", Vec3Argument.vec3()).executes((p_198735_0_) -> {
                                            return spawnNoAIEntity(p_198735_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198735_0_, "entity"), Vec3Argument.getVec3(p_198735_0_, "pos"), new CompoundTag(), true);
                                        })
                                                .then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes((p_198739_0_) -> {
                                                    return spawnNoAIEntity(p_198739_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198739_0_, "entity"), Vec3Argument.getVec3(p_198739_0_, "pos"), CompoundTagArgument.getCompoundTag(p_198739_0_, "nbt"), false);
                                                })))))
                        .then(Commands.literal("summon_persist")
                                .then(Commands.argument("entity", ResourceArgument.resource(p_250122_, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes((p_198738_0_) -> {
                                    return spawnPersistEntity(p_198738_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198738_0_, "entity"), p_198738_0_.getSource().getPosition(), new CompoundTag(), true);
                                })
                                        .then(Commands.argument("pos", Vec3Argument.vec3()).executes((p_198735_0_) -> {
                                            return spawnPersistEntity(p_198735_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198735_0_, "entity"), Vec3Argument.getVec3(p_198735_0_, "pos"), new CompoundTag(), true);
                                        })
                                                .then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes((p_198739_0_) -> {
                                                    return spawnPersistEntity(p_198739_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198739_0_, "entity"), Vec3Argument.getVec3(p_198739_0_, "pos"), CompoundTagArgument.getCompoundTag(p_198739_0_, "nbt"), false);
                                                })))))
                        .then(Commands.literal("summon_tamed")
                                .then(Commands.argument("entity", ResourceArgument.resource(p_250122_, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes((p_198738_0_) -> {
                                            return spawnTamedEntity(p_198738_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198738_0_, "entity"), p_198738_0_.getSource().getPosition(), new CompoundTag(), true);
                                        })
                                        .then(Commands.argument("pos", Vec3Argument.vec3()).executes((p_198735_0_) -> {
                                                    return spawnTamedEntity(p_198735_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198735_0_, "entity"), Vec3Argument.getVec3(p_198735_0_, "pos"), new CompoundTag(), true);
                                                })
                                                .then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes((p_198739_0_) -> {
                                                    return spawnTamedEntity(p_198739_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198739_0_, "entity"), Vec3Argument.getVec3(p_198739_0_, "pos"), CompoundTagArgument.getCompoundTag(p_198739_0_, "nbt"), false);
                                                })))))
                        .then(Commands.literal("summon_hostile")
                                .then(Commands.argument("entity", ResourceArgument.resource(p_250122_, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes((p_198738_0_) -> {
                                            return spawnHostileEntity(p_198738_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198738_0_, "entity"), p_198738_0_.getSource().getPosition(), new CompoundTag(), true);
                                        })
                                        .then(Commands.argument("pos", Vec3Argument.vec3()).executes((p_198735_0_) -> {
                                                    return spawnHostileEntity(p_198735_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198735_0_, "entity"), Vec3Argument.getVec3(p_198735_0_, "pos"), new CompoundTag(), true);
                                                })
                                                .then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes((p_198739_0_) -> {
                                                    return spawnHostileEntity(p_198739_0_.getSource(), ResourceArgument.getSummonableEntityType(p_198739_0_, "entity"), Vec3Argument.getVec3(p_198739_0_, "pos"), CompoundTagArgument.getCompoundTag(p_198739_0_, "nbt"), false);
                                                }))))))
                .then(Commands.literal("research")
                        .then(Commands.literal("get").executes((p_198352_0_) -> {
                            return getResearches(p_198352_0_.getSource(), p_198352_0_.getSource().getPlayerOrException());
                        })
                                .then(Commands.argument("targets", EntityArgument.player()).executes((p_198435_0_) -> {
                            return getResearches(p_198435_0_.getSource(), EntityArgument.getPlayer(p_198435_0_, "targets"));
                        })))
                        .then(Commands.literal("add").executes((p_198352_0_) -> {
                                    return getResearches(p_198352_0_.getSource(), p_198352_0_.getSource().getPlayerOrException());
                                })
                                .then(Commands.argument("targets", EntityArgument.player()).then(Commands.literal("only").then(Commands.argument("research", ResourceLocationArgument.id()).suggests(SUGGEST_RESEARCHES).executes((p_136363_) -> {
                                    return addResearch(p_136363_.getSource(), EntityArgument.getPlayers(p_136363_, "targets"), ResourceLocationArgument.getId(p_136363_, "research"));
                                })))
                                        .then(Commands.literal("all").executes(context -> {
                                            return addAllResearch(context.getSource(), EntityArgument.getPlayers(context, "targets"));
                                        }))))
                        .then(Commands.literal("remove").executes((p_198352_0_) -> {
                                    return getResearches(p_198352_0_.getSource(), p_198352_0_.getSource().getPlayerOrException());
                                })
                                .then(Commands.argument("targets", EntityArgument.player()).then(Commands.literal("only").then(Commands.argument("research", ResourceLocationArgument.id()).suggests(SUGGEST_RESEARCHES).executes((p_136363_) -> {
                                    return removeResearch(p_136363_.getSource(), EntityArgument.getPlayers(p_136363_, "targets"), ResourceLocationArgument.getId(p_136363_, "research"));
                                })))
                                        .then(Commands.literal("all").executes(context -> {
                                            return removeAllResearch(context.getSource(), EntityArgument.getPlayers(context, "targets"));
                                        })))))
                .then(Commands.literal("brew")
                        .then(Commands.literal("level")
                                .then(Commands.literal("get").executes((p_198352_0_) -> {
                                            return getBrewLevel(p_198352_0_.getSource(), p_198352_0_.getSource().getPlayerOrException());
                                        })
                                        .then(Commands.argument("targets", EntityArgument.player()).executes((p_198435_0_) -> {
                                            return getBrewLevel(p_198435_0_.getSource(), EntityArgument.getPlayer(p_198435_0_, "targets"));
                                        })))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer()).executes((p_198445_0_) -> {
                                                    return addBrewLevel(p_198445_0_.getSource(), EntityArgument.getPlayers(p_198445_0_, "targets"), IntegerArgumentType.getInteger(p_198445_0_, "amount"));
                                                }))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes((p_198439_0_) -> {
                                                    return setBrewLevel(p_198439_0_.getSource(), EntityArgument.getPlayers(p_198439_0_, "targets"), IntegerArgumentType.getInteger(p_198439_0_, "amount"));
                                                })))))
                        .then(Commands.literal("xp")
                                .then(Commands.literal("get").executes((p_198352_0_) -> {
                                            return getBrewBottling(p_198352_0_.getSource(), p_198352_0_.getSource().getPlayerOrException());
                                        })
                                        .then(Commands.argument("targets", EntityArgument.player()).executes((p_198435_0_) -> {
                                            return getBrewBottling(p_198435_0_.getSource(), EntityArgument.getPlayer(p_198435_0_, "targets"));
                                        })))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer()).executes((p_198445_0_) -> {
                                                    return addBrewBottling(p_198445_0_.getSource(), EntityArgument.getPlayers(p_198445_0_, "targets"), IntegerArgumentType.getInteger(p_198445_0_, "amount"));
                                                }))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes((p_198439_0_) -> {
                                                    return setBrewBottling(p_198439_0_.getSource(), EntityArgument.getPlayers(p_198439_0_, "targets"), IntegerArgumentType.getInteger(p_198439_0_, "amount"));
                                                }))))))
                .then(Commands.literal("spell")
                        .then(Commands.literal("cooldown")
                                .then(Commands.literal("held").executes((p_198352_0_) -> {
                                    if (p_198352_0_.getSource().isPlayer()) {
                                        List<ServerPlayer> list = new ArrayList<>();
                                        list.add(p_198352_0_.getSource().getPlayerOrException());
                                        return resetFocusCooldown(p_198352_0_.getSource(), list);
                                    }
                                    return 0;
                                })
                                        .then(Commands.argument("targets", EntityArgument.players()).executes((p_198435_0_) -> {
                                            return resetFocusCooldown(p_198435_0_.getSource(), EntityArgument.getPlayers(p_198435_0_, "targets"));
                                        })))
                                .then(Commands.literal("all").executes((p_198352_0_) -> {
                                            if (p_198352_0_.getSource().isPlayer()) {
                                                List<ServerPlayer> list = new ArrayList<>();
                                                list.add(p_198352_0_.getSource().getPlayerOrException());
                                                return resetAllFocusCooldowns(p_198352_0_.getSource(), list);
                                            }
                                            return 0;
                                        })
                                        .then(Commands.argument("targets", EntityArgument.players()).executes((p_198435_0_) -> {
                                            return resetAllFocusCooldowns(p_198435_0_.getSource(), EntityArgument.getPlayers(p_198435_0_, "targets"));
                                        })))))
                .then(Commands.literal("misc")
                        .then(Commands.literal("wight").executes((p_198352_0_) -> {
                            return spawnWight(p_198352_0_.getSource(), p_198352_0_.getSource().getPlayerOrException());
                        })
                                .then(Commands.argument("targets", EntityArgument.player()).executes((p_198435_0_) -> {
                                    return spawnWight(p_198435_0_.getSource(), EntityArgument.getPlayer(p_198435_0_, "targets"));
                                })))
                        .then(Commands.literal("repair")
                                .then(Commands.literal("held").executes((p_198352_0_) -> {
                                            if (p_198352_0_.getSource().isPlayer()) {
                                                List<ServerPlayer> list = new ArrayList<>();
                                                list.add(p_198352_0_.getSource().getPlayerOrException());
                                                return repairItem(p_198352_0_.getSource(), list);
                                            }
                                            return 0;
                                        })
                                        .then(Commands.argument("targets", EntityArgument.players()).executes((p_198435_0_) -> {
                                            return repairItem(p_198435_0_.getSource(), EntityArgument.getPlayers(p_198435_0_, "targets"));
                                        })))
                                .then(Commands.literal("inventory").executes((p_198352_0_) -> {
                                            if (p_198352_0_.getSource().isPlayer()) {
                                                List<ServerPlayer> list = new ArrayList<>();
                                                list.add(p_198352_0_.getSource().getPlayerOrException());
                                                return repairAllItems(p_198352_0_.getSource(), list);
                                            }
                                            return 0;
                                        })
                                        .then(Commands.argument("targets", EntityArgument.players()).executes((p_198435_0_) -> {
                                            return repairAllItems(p_198435_0_.getSource(), EntityArgument.getPlayers(p_198435_0_, "targets"));
                                        }))))
                        .then(Commands.literal("heal").executes((p_198352_0_) -> {
                                    if (p_198352_0_.getSource().isPlayer()) {
                                        List<ServerPlayer> list = new ArrayList<>();
                                        list.add(p_198352_0_.getSource().getPlayerOrException());
                                        return heal(p_198352_0_.getSource(), list);
                                    }
                                    return 0;
                                })
                                .then(Commands.argument("targets", EntityArgument.players()).executes((p_198435_0_) -> {
                                    return heal(p_198435_0_.getSource(), EntityArgument.getPlayers(p_198435_0_, "targets"));
                                })))));
    }

    private static int addSoulEnergy(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int pAmount) {
        for(ServerPlayer serverPlayer : pTargets) {
            if (SEHelper.getSoulsContainer(serverPlayer)) {
                SEHelper.increaseSouls(serverPlayer, pAmount);
            } else {
                pSource.sendFailure(Component.translatable("commands.goety.soul.failed", pTargets.iterator().next().getDisplayName()));
            }
        }

        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.soul.add"+ ".success.single", pAmount, pTargets.iterator().next().getDisplayName()), true);
        } else {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.soul.add" + ".success.multiple", pAmount, pTargets.size()), true);
        }

        return pTargets.size();
    }

    private static int setSoulEnergy(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int pAmount) throws CommandSyntaxException {
        int i = 0;

        for(ServerPlayer serverPlayer : pTargets) {
            if (SEHelper.getSoulsContainer(serverPlayer)) {
                SEHelper.setSoulsAmount(serverPlayer, pAmount);
                ++i;
            } else {
                pSource.sendFailure(Component.translatable("commands.goety.soul.failed", pTargets.iterator().next().getDisplayName()));
            }
        }

        if (i == 0) {
            throw ERROR_SET_POINTS_INVALID.create();
        } else {
            if (pTargets.size() == 1) {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.soul.set" + ".success.single", pAmount, pTargets.iterator().next().getDisplayName()), true);
            } else {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.soul.set" + ".success.multiple", pAmount, pTargets.size()), true);
            }

            return pTargets.size();
        }
    }

    private static int spawnIllagers(CommandSourceStack pSource, ServerPlayer pPlayer) {
        int i = SEHelper.getSoulAmountInt(pPlayer);
        if (i > MobsConfig.IllagerAssaultSEThreshold.get()){
            IllagerSpawner illagerSpawner = new IllagerSpawner();
            illagerSpawner.forceSpawn(pPlayer.serverLevel(), pPlayer, pSource);
            return 1;
        } else {
            pSource.sendFailure(Component.translatable("commands.goety.illager.spawn.failure", pPlayer.getDisplayName()));
        }
        return i;
    }

    private static int getRestPeriod(CommandSourceStack pSource, ServerPlayer pPlayer){
        int i = SEHelper.getRestPeriod(pPlayer);
        pSource.sendSuccess(() -> Component.translatable("commands.goety.illager.rest.get.success", pPlayer.getDisplayName(), StringUtil.formatTickDuration(i)), false);
        return 1;
    }

    private static int addRestPeriod(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int tick) {
        for(ServerPlayer serverPlayer : pTargets) {
            SEHelper.increaseRestPeriod(serverPlayer, tick);
        }

        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.illager.rest.add.success.single", tick, pTargets.iterator().next().getDisplayName()), true);
        } else {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.illager.rest.add.success.multiple", tick, pTargets.size()), true);
        }

        return pTargets.size();
    }

    private static int setRestPeriod(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int tick) throws CommandSyntaxException{
        int i = 0;

        for(ServerPlayer serverPlayer : pTargets) {
            SEHelper.setRestPeriod(serverPlayer, tick);
            ++i;
        }

        if (i == 0) {
            throw ERROR_SET_POINTS_INVALID2.create();
        } else {
            if (pTargets.size() == 1) {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.illager.rest.set.success.single", tick, pTargets.iterator().next().getDisplayName()), true);
            } else {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.illager.rest.set.success.multiple", tick, pTargets.size()), true);
            }

            return pTargets.size();
        }
    }

    private static int spawnNoAIEntity(CommandSourceStack pSource, Holder.Reference<EntityType<?>> pType, Vec3 pPos, CompoundTag pNbt, boolean pRandomizeProperties) throws CommandSyntaxException {
        BlockPos blockpos = BlockPos.containing(pPos);
        if (!Level.isInSpawnableBounds(blockpos)) {
            throw INVALID_POSITION.create();
        } else {
            CompoundTag compoundnbt = pNbt.copy();
            compoundnbt.putString("id", pType.key().location().toString());
            ServerLevel serverworld = pSource.getLevel();
            Entity entity = EntityType.loadEntityRecursive(compoundnbt, serverworld, (p_218914_1_) -> {
                p_218914_1_.moveTo(pPos.x, pPos.y, pPos.z, p_218914_1_.getYRot(), p_218914_1_.getXRot());
                return p_218914_1_;
            });
            if (entity == null) {
                throw ERROR_FAILED.create();
            } else {
                if (entity instanceof Mob mob){
                    mob.setNoAi(true);
                    mob.setPersistenceRequired();
                    if (pRandomizeProperties){
                        ForgeEventFactory.onFinalizeSpawn(mob, pSource.getLevel(), pSource.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null);
                    }
                }

                if (!serverworld.tryAddFreshEntityWithPassengers(entity)) {
                    throw ERROR_DUPLICATE_UUID.create();
                } else {
                    pSource.sendSuccess(() -> Component.translatable("commands.summon_noai.success", entity.getDisplayName()), true);
                    return 1;
                }
            }
        }
    }

    private static int spawnPersistEntity(CommandSourceStack pSource, Holder.Reference<EntityType<?>> pType, Vec3 pPos, CompoundTag pNbt, boolean pRandomizeProperties) throws CommandSyntaxException {
        BlockPos blockpos = BlockPos.containing(pPos);
        if (!Level.isInSpawnableBounds(blockpos)) {
            throw INVALID_POSITION.create();
        } else {
            CompoundTag compoundnbt = pNbt.copy();
            compoundnbt.putString("id", pType.key().location().toString());
            ServerLevel serverworld = pSource.getLevel();
            Entity entity = EntityType.loadEntityRecursive(compoundnbt, serverworld, (p_218914_1_) -> {
                p_218914_1_.moveTo(pPos.x, pPos.y, pPos.z, p_218914_1_.getYRot(), p_218914_1_.getXRot());
                return p_218914_1_;
            });
            if (entity == null) {
                throw ERROR_FAILED.create();
            } else {
                if (entity instanceof Mob mob){
                    mob.setPersistenceRequired();
                    if (pRandomizeProperties){
                        ForgeEventFactory.onFinalizeSpawn(mob, pSource.getLevel(), pSource.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null);
                    }
                }

                if (!serverworld.tryAddFreshEntityWithPassengers(entity)) {
                    throw ERROR_DUPLICATE_UUID.create();
                } else {
                    pSource.sendSuccess(() -> Component.translatable("commands.summon_persist.success", entity.getDisplayName()), true);
                    return 1;
                }
            }
        }
    }

    private static int spawnTamedEntity(CommandSourceStack pSource, Holder.Reference<EntityType<?>> pType, Vec3 pPos, CompoundTag pNbt, boolean pRandomizeProperties) throws CommandSyntaxException {
        BlockPos blockpos = BlockPos.containing(pPos);
        if (!Level.isInSpawnableBounds(blockpos)) {
            throw INVALID_POSITION.create();
        } else {
            CompoundTag compoundnbt = pNbt.copy();
            compoundnbt.putString("id", pType.key().location().toString());
            ServerLevel serverworld = pSource.getLevel();
            Entity entity = EntityType.loadEntityRecursive(compoundnbt, serverworld, (p_218914_1_) -> {
                p_218914_1_.moveTo(pPos.x, pPos.y, pPos.z, p_218914_1_.getYRot(), p_218914_1_.getXRot());
                return p_218914_1_;
            });
            if (entity == null) {
                throw ERROR_FAILED.create();
            } else {
                if (entity instanceof Mob mob){
                    MobUtil.summonTame(mob, pSource.getPlayerOrException());
                    if (pRandomizeProperties){
                        ForgeEventFactory.onFinalizeSpawn(mob, pSource.getLevel(), pSource.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null);
                    }
                }

                if (!serverworld.tryAddFreshEntityWithPassengers(entity)) {
                    throw ERROR_DUPLICATE_UUID.create();
                } else {
                    pSource.sendSuccess(() -> Component.translatable("commands.summon_tame.success", entity.getDisplayName()), true);
                    return 1;
                }
            }
        }
    }

    private static int spawnHostileEntity(CommandSourceStack pSource, Holder.Reference<EntityType<?>> pType, Vec3 pPos, CompoundTag pNbt, boolean pRandomizeProperties) throws CommandSyntaxException {
        BlockPos blockpos = BlockPos.containing(pPos);
        if (!Level.isInSpawnableBounds(blockpos)) {
            throw INVALID_POSITION.create();
        } else {
            CompoundTag compoundnbt = pNbt.copy();
            compoundnbt.putString("id", pType.key().location().toString());
            ServerLevel serverworld = pSource.getLevel();
            Entity entity = EntityType.loadEntityRecursive(compoundnbt, serverworld, (p_218914_1_) -> {
                p_218914_1_.moveTo(pPos.x, pPos.y, pPos.z, p_218914_1_.getYRot(), p_218914_1_.getXRot());
                return p_218914_1_;
            });
            if (entity == null) {
                throw ERROR_FAILED.create();
            } else {
                if (entity instanceof Mob mob){
                    mob.setPersistenceRequired();
                    if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null){
                        if (mob instanceof IOwned owned){
                            owned.setHostile(true);
                        } else {
                            mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(mob, Player.class, true));
                        }
                    }
                    if (pRandomizeProperties){
                        ForgeEventFactory.onFinalizeSpawn(mob, pSource.getLevel(), pSource.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null);
                    }
                }

                if (!serverworld.tryAddFreshEntityWithPassengers(entity)) {
                    throw ERROR_DUPLICATE_UUID.create();
                } else {
                    pSource.sendSuccess(() -> Component.translatable("commands.summon_hostile.success", entity.getDisplayName()), true);
                    return 1;
                }
            }
        }
    }

    private static int getResearches(CommandSourceStack pSource, ServerPlayer pPlayer){
        if (SEHelper.getResearch(pPlayer).isEmpty()){
            pSource.sendFailure(Component.translatable("commands.goety.research.get.empty", pPlayer.getDisplayName()));
        } else {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.research.get", pPlayer.getDisplayName()), true);
        }
        for (Research research : SEHelper.getResearch(pPlayer)){
            pSource.sendSuccess(() -> Component.literal(research.getId()), true);
        }
        return 1;
    }

    private static int addResearch(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, ResourceLocation string){
        for(ServerPlayer serverPlayer : pTargets) {
            if (ResearchList.getResearch(string) != null){
                Research research = ResearchList.getResearch(string);
                if (SEHelper.hasResearch(serverPlayer, research)){
                    pSource.sendFailure(Component.translatable("commands.goety.research.add.failure", serverPlayer.getDisplayName()));
                } else {
                    SEHelper.addResearch(serverPlayer, research);
                    pSource.sendSuccess(() -> Component.translatable("commands.goety.research.add.success", serverPlayer.getDisplayName()), true);
                }
            }
        }

        return 1;
    }

    private static int removeResearch(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, ResourceLocation string){
        for(ServerPlayer serverPlayer : pTargets) {
            if (ResearchList.getResearch(string) != null){
                Research research = ResearchList.getResearch(string);
                if (!SEHelper.hasResearch(serverPlayer, research)){
                    pSource.sendFailure(Component.translatable("commands.goety.research.remove.failure", serverPlayer.getDisplayName()));
                } else {
                    SEHelper.removeResearch(serverPlayer, research);
                    pSource.sendSuccess(() -> Component.translatable("commands.goety.research.remove.success", serverPlayer.getDisplayName()), true);
                }
            }
        }

        return 1;
    }

    private static int addAllResearch(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets){
        for(ServerPlayer serverPlayer : pTargets) {
            for (Research research : ResearchList.getResearchList().values()){
                if (!SEHelper.hasResearch(serverPlayer, research)){
                    SEHelper.addResearch(serverPlayer, research);
                }
            }
            pSource.sendSuccess(() -> Component.translatable("commands.goety.research.addAll", serverPlayer.getDisplayName()), true);
        }

        return 1;
    }

    private static int removeAllResearch(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets){
        for(ServerPlayer serverPlayer : pTargets) {
            for (Research research : ResearchList.getResearchList().values()){
                if (SEHelper.hasResearch(serverPlayer, research)){
                    SEHelper.removeResearch(serverPlayer, research);
                }
            }
            pSource.sendSuccess(() -> Component.translatable("commands.goety.research.removeAll", serverPlayer.getDisplayName()), true);
        }

        return 1;
    }

    private static int getBrewLevel(CommandSourceStack pSource, ServerPlayer pPlayer){
        int i = SEHelper.getBottleLevel(pPlayer);
        pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.level.get.success", pPlayer.getDisplayName(), i), false);
        return 1;
    }

    private static int addBrewLevel(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int level) {
        for(ServerPlayer serverPlayer : pTargets) {
            SEHelper.setBottleLevel(serverPlayer, SEHelper.getBottleLevel(serverPlayer) + level);
        }

        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.level.add.success.single", level, pTargets.iterator().next().getDisplayName()), true);
        } else {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.level.add.success.multiple", level, pTargets.size()), true);
        }

        return pTargets.size();
    }

    private static int setBrewLevel(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int level) throws CommandSyntaxException{
        int i = 0;

        for(ServerPlayer serverPlayer : pTargets) {
            SEHelper.setBottleLevel(serverPlayer, level);
            ++i;
        }

        if (i == 0) {
            throw ERROR_SET_POINTS_INVALID3.create();
        } else {
            if (pTargets.size() == 1) {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.level.set.success.single", level, pTargets.iterator().next().getDisplayName()), true);
            } else {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.level.set.success.multiple", level, pTargets.size()), true);
            }

            return pTargets.size();
        }
    }

    private static int getBrewBottling(CommandSourceStack pSource, ServerPlayer pPlayer){
        int i = SEHelper.getBottling(pPlayer);
        pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.xp.get.success", pPlayer.getDisplayName(), i), false);
        return 1;
    }

    private static int addBrewBottling(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int level) {
        for(ServerPlayer serverPlayer : pTargets) {
            SEHelper.increaseBottling(serverPlayer, level);
        }

        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.xp.add.success.single", level, pTargets.iterator().next().getDisplayName()), true);
        } else {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.xp.add.success.multiple", level, pTargets.size()), true);
        }

        return pTargets.size();
    }

    private static int setBrewBottling(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int level) throws CommandSyntaxException{
        int i = 0;

        for(ServerPlayer serverPlayer : pTargets) {
            SEHelper.setBottling(serverPlayer, level);
            ++i;
        }

        if (i == 0) {
            throw ERROR_SET_POINTS_INVALID4.create();
        } else {
            if (pTargets.size() == 1) {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.xp.set.success.single", level, pTargets.iterator().next().getDisplayName()), true);
            } else {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.brew.xp.set.success.multiple", level, pTargets.size()), true);
            }

            return pTargets.size();
        }
    }

    private static int resetFocusCooldown(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets) {
        int i = 0;
        for(ServerPlayer serverPlayer : pTargets) {
            if (!WandUtil.findFocus(serverPlayer).isEmpty()){
                Item item = WandUtil.findFocus(serverPlayer).getItem();
                if (SEHelper.getCooldowns(serverPlayer).containsKey(item)){
                    SEHelper.getFocusCoolDown(serverPlayer).removeCooldown(serverPlayer, pSource.getLevel(), item);
                    ++i;
                }
            }
        }

        if (i == 0){
            pSource.sendFailure(Component.translatable("commands.goety.spell.cooldown.held.failure"));
        } else {
            if (pTargets.size() == 1) {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.spell.cooldown.held.success.single", WandUtil.findFocus(pTargets.iterator().next()).getDisplayName()), true);
            } else {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.spell.cooldown.held.success.multiple", pTargets.size()), true);
            }
        }

        return i;
    }

    private static int resetAllFocusCooldowns(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets) {
        int i = 0;
        for(ServerPlayer serverPlayer : pTargets) {
            for (Item item : SEHelper.getCooldowns(serverPlayer).keySet()){
                SEHelper.getFocusCoolDown(serverPlayer).removeCooldown(serverPlayer, pSource.getLevel(), item);
            }
        }

        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.spell.cooldown.inventory.success.single", pTargets.iterator().next().getDisplayName()), true);
        } else {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.spell.cooldown.inventory.success.multiple", pTargets.size()), true);
        }

        return i;
    }

    private static int spawnWight(CommandSourceStack pSource, ServerPlayer pPlayer) {
        WightSpawner spawner = new WightSpawner();
        spawner.forceSpawn(pPlayer.serverLevel(), pPlayer, pSource);
        return 1;
    }

    private static int repairItem(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets) {
        int i = 0;
        for(ServerPlayer serverPlayer : pTargets) {
            if (!serverPlayer.getMainHandItem().isEmpty()){
                if (serverPlayer.getMainHandItem().isDamaged()){
                    serverPlayer.getMainHandItem().setDamageValue(0);
                    ++i;
                }
            }
        }

        if (i == 0){
            pSource.sendFailure(Component.translatable("commands.goety.misc.repair.held.failure"));
        } else {
            if (pTargets.size() == 1) {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.misc.repair.held.success.single", pTargets.iterator().next().getDisplayName(), pTargets.iterator().next().getMainHandItem().getDisplayName()), true);
            } else {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.misc.repair.held.success.multiple", pTargets.size()), true);
            }
        }
        return i;
    }

    private static int repairAllItems(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets) {
        int i0 = 0;
        for(ServerPlayer serverPlayer : pTargets) {
            for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); ++i){
                ItemStack itemStack = serverPlayer.getInventory().getItem(i);
                if (itemStack.isDamaged()){
                    itemStack.setDamageValue(0);
                    ++i0;
                }
            }
        }

        if (i0 == 0){
            pSource.sendFailure(Component.translatable("commands.goety.misc.repair.inventory.failure"));
        } else {
            if (pTargets.size() == 1) {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.misc.repair.inventory.success.single", pTargets.iterator().next().getDisplayName()), true);
            } else {
                pSource.sendSuccess(() -> Component.translatable("commands.goety.misc.repair.inventory.success.multiple", pTargets.size()), true);
            }
        }
        return i0;
    }

    private static int heal(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets) {
        for(ServerPlayer serverPlayer : pTargets) {
            serverPlayer.removeAllEffects();
            serverPlayer.heal(serverPlayer.getMaxHealth());
            serverPlayer.getFoodData().setFoodLevel(20);
            serverPlayer.getFoodData().setSaturation(20);
            serverPlayer.getFoodData().setExhaustion(0);
        }

        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.misc.heal.success.single", pTargets.iterator().next().getDisplayName()), true);
        } else {
            pSource.sendSuccess(() -> Component.translatable("commands.goety.misc.heal.success.multiple", pTargets.size()), true);
        }
        return 1;
    }
}
