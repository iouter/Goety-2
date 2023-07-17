package com.Polarice3.Goety.common.entities.neutral;

import net.minecraft.world.item.ItemStack;

public interface IRavager {
    int getAttackTick();
    int getStunnedTick();
    int getRoarTick();
    ItemStack getArmor();
}
