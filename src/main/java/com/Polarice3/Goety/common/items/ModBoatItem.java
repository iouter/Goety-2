package com.Polarice3.Goety.common.items;

import com.Polarice3.Goety.common.entities.vehicle.ModBoat;
import com.Polarice3.Goety.common.entities.vehicle.ModChestBoat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class ModBoatItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final ModBoat.Type type;
    private final boolean hasChest;

    public ModBoatItem(boolean p_220013_, ModBoat.Type pType, Properties pProperties) {
        super(pProperties);
        this.hasChest = p_220013_;
        this.type = pType;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        HitResult raytraceresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.ANY);
        if (raytraceresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            Vec3 vector3d = pPlayer.getViewVector(1.0F);
            double d0 = 5.0D;
            List<Entity> list = pLevel.getEntities(pPlayer, pPlayer.getBoundingBox().expandTowards(vector3d.scale(5.0D)).inflate(1.0D), ENTITY_PREDICATE);
            if (!list.isEmpty()) {
                Vec3 vector3d1 = pPlayer.getEyePosition(1.0F);

                for(Entity entity : list) {
                    AABB axisalignedbb = entity.getBoundingBox().inflate((double)entity.getPickRadius());
                    if (axisalignedbb.contains(vector3d1)) {
                        return InteractionResultHolder.pass(itemstack);
                    }
                }
            }

            if (raytraceresult.getType() == HitResult.Type.BLOCK) {
                ModBoat boatentity = this.getBoat(pLevel, raytraceresult);
                boatentity.setType(this.type);
                boatentity.setYRot(pPlayer.getYRot());
                if (!pLevel.noCollision(boatentity, boatentity.getBoundingBox().inflate(-0.1D))) {
                    return InteractionResultHolder.fail(itemstack);
                } else {
                    if (!pLevel.isClientSide) {
                        pLevel.addFreshEntity(boatentity);
                        if (!pPlayer.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                    }

                    pPlayer.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
                }
            } else {
                return InteractionResultHolder.pass(itemstack);
            }
        }
    }

    private ModBoat getBoat(Level p_220017_, HitResult p_220018_) {
        return (ModBoat)(this.hasChest ? new ModChestBoat(p_220017_, p_220018_.getLocation().x, p_220018_.getLocation().y, p_220018_.getLocation().z) : new ModBoat(p_220017_, p_220018_.getLocation().x, p_220018_.getLocation().y, p_220018_.getLocation().z));
    }
}
