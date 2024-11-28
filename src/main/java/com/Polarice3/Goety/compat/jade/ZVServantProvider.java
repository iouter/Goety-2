package com.Polarice3.Goety.compat.jade;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.common.entities.ally.undead.zombie.ZombieVillagerServant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum ZVServantProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private ZVServantProvider() {
    }

    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("ConversionTime", 3)) {
            int time = accessor.getServerData().getInt("ConversionTime");
            if (time > 0) {
                tooltip.add(Component.translatable("jade.zombieConversion.time", new Object[]{IThemeHelper.get().seconds(time)}));
            }

        }
    }

    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        ZombieVillagerServant entity = (ZombieVillagerServant)accessor.getEntity();
        if (entity.villagerConversionTime > 0) {
            tag.putInt("ConversionTime", entity.villagerConversionTime);
        }

    }

    public ResourceLocation getUid() {
        return Goety.location("zombie_villager");
    }
}
