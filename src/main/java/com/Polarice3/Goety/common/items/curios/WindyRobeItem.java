package com.Polarice3.Goety.common.items.curios;

import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.compat.iron.IronAttributes;
import com.Polarice3.Goety.compat.iron.IronLoaded;
import com.Polarice3.Goety.config.MainConfig;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

public class WindyRobeItem extends SingleStackItem{

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                        UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        if (IronLoaded.IRON_SPELLBOOKS.isLoaded()){
            if (MainConfig.RobesIronResist.get()) {
                if (stack.is(ModItems.STORM_ROBE.get())){
                    map.put(IronAttributes.LIGHTNING_MAGIC_RESIST, new AttributeModifier(UUID.fromString("ed6bb7e0-c849-4e25-849a-e288e2b76961"), "Robes Iron Spell Resist", 0.5F, AttributeModifier.Operation.ADDITION));
                }
            }
        }
        return map;
    }
}
