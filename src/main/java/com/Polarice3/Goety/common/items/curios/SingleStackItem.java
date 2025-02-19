package com.Polarice3.Goety.common.items.curios;

import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.ItemConfig;
import com.Polarice3.Goety.init.ModKeybindings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SingleStackItem extends Item implements ICurioItem {

    public SingleStackItem() {
        this(new Properties().stacksTo(1));
    }

    public SingleStackItem(Properties properties){
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        if (stack.is(ModItems.CRONE_HAT.get())){
            return true;
        }
        return super.hasCraftingRemainingItem(stack);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        if (itemStack.is(ModItems.CRONE_HAT.get())) {
            return itemStack.copy();
        }
        return super.getCraftingRemainingItem(itemStack);
    }

    public boolean isEnchantable(ItemStack pStack) {
        return pStack.getCount() == 1;
    }

    public int getEnchantmentValue() {
        return 15;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        if (stack.getItem() == ModItems.SPITEFUL_BELT.get()) {
            return enchantment == Enchantments.THORNS;
        }
        return false;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        ChatFormatting main = ChatFormatting.DARK_PURPLE;
        ChatFormatting secondary = ChatFormatting.BLUE;

        if (stack.getItem() instanceof SingleStackItem) {
            if (stack.is(ModItems.AMETHYST_NECKLACE.get())) {
                tooltip.add(Component.translatable("info.goety.amethyst_necklace").withStyle(main));
                tooltip.add(Component.translatable("info.goety.amethyst_necklace_discount").withStyle(secondary));
            }
            if (stack.getItem() instanceof WitchHatItem) {
                tooltip.add(Component.translatable("info.goety.witch_hat").withStyle(main));
                if (stack.is(ModItems.CRONE_HAT.get())){
                    tooltip.add(Component.translatable("info.goety.crone_hat_potion").withStyle(secondary));
                } else {
                    tooltip.add(Component.translatable("info.goety.witch_hat_potion").withStyle(secondary));
                }
            }
            if (stack.getItem() instanceof NecroGarbs.NecroCrownItem crownItem) {
                tooltip.add(Component.translatable("info.goety.necro_crown").withStyle(main));
                if (!crownItem.isNameless) {
                    tooltip.add(Component.translatable("info.goety.necro_crown_cast").withStyle(secondary));
                    if (ItemConfig.NecroCrownWeakness.get()){
                        tooltip.add(Component.translatable("info.goety.necro_crown_weakness").withStyle(secondary));
                    }
                } else {
                    tooltip.add(Component.translatable("info.goety.dark_hat").withStyle(secondary));
                }
            }
            if (stack.getItem() instanceof MagicHatItem) {
                if (stack.is(ModItems.GRAND_TURBAN.get())) {
                    tooltip.add(Component.translatable("info.goety.grand_turban").withStyle(main));
                }
                tooltip.add(Component.translatable("info.goety.dark_hat").withStyle(secondary));
            }
            if (stack.is(ModItems.FROST_CROWN.get())){
                tooltip.add(Component.translatable("info.goety.frost_crown").withStyle(main));
                tooltip.add(Component.translatable("info.goety.frost_crown_cast").withStyle(secondary));
            }
            if (stack.is(ModItems.WILD_CROWN.get())){
                tooltip.add(Component.translatable("info.goety.wild_crown").withStyle(main));
                tooltip.add(Component.translatable("info.goety.wild_crown_cast").withStyle(secondary));
            }
            if (stack.is(ModItems.NETHER_CROWN.get())){
                tooltip.add(Component.translatable("info.goety.nether_crown").withStyle(main));
                tooltip.add(Component.translatable("info.goety.nether_crown_cast").withStyle(secondary));
            }
            if (stack.getItem() instanceof UnholyHatItem){
                tooltip.add(Component.translatable("info.goety.unholy_hat").withStyle(main));
                tooltip.add(Component.translatable("info.goety.unholy_hat_cast").withStyle(secondary));
            }
            if (stack.getItem() instanceof NecroGarbs.NecroCapeItem capeItem) {
                tooltip.add(Component.translatable("info.goety.necro_cape").withStyle(main));
                tooltip.add(Component.translatable("info.goety.necro_cape_power").withStyle(secondary));
                if (!capeItem.isNameless) {
                    if (ItemConfig.NecroCapeHunger.get()){
                        tooltip.add(Component.translatable("info.goety.necro_cape_hunger").withStyle(secondary));
                    }
                }
            }
            if (stack.getItem() instanceof MagicRobeItem) {
                if (stack.is(ModItems.GRAND_ROBE.get())) {
                    tooltip.add(Component.translatable("info.goety.grand_robe").withStyle(main));
                }
                tooltip.add(Component.translatable("info.goety.dark_robe").withStyle(secondary));
            }
            if (stack.getItem() instanceof IllusionRobeItem) {
                tooltip.add(Component.translatable("info.goety.illusion_robe").withStyle(secondary));
            }
            if (stack.getItem() instanceof FrostRobeItem) {
                if (ItemConfig.FrostRobeResistance.get() > 0) {
                    tooltip.add(Component.translatable("info.goety.frost_robe", ItemConfig.FrostRobeResistance.get()).withStyle(main));
                }
                tooltip.add(Component.translatable("info.goety.frost_robe_discount").withStyle(secondary));
            }
            if (stack.getItem() instanceof WindyRobeItem) {
                tooltip.add(Component.translatable("info.goety.wind_robe").withStyle(main));
                if (stack.is(ModItems.WIND_ROBE.get())) {
                    tooltip.add(Component.translatable("info.goety.wind_robe_discount").withStyle(secondary));
                }
                if (stack.is(ModItems.STORM_ROBE.get())) {
                    if (ItemConfig.StormRobeResistance.get() > 0) {
                        tooltip.add(Component.translatable("info.goety.storm_robe", ItemConfig.StormRobeResistance.get()).withStyle(main));
                    }
                    tooltip.add(Component.translatable("info.goety.storm_robe_discount").withStyle(secondary));
                }
            }
            if (stack.getItem() instanceof WildRobeItem) {
                tooltip.add(Component.translatable("info.goety.wild_robe").withStyle(main));
                tooltip.add(Component.translatable("info.goety.wild_robe_discount").withStyle(secondary));
            }
            if (stack.getItem() instanceof WitchRobeItem) {
                tooltip.add(Component.translatable("info.goety.witch_robe_brew", ModKeybindings.keyBindings[3].getTranslatedKeyMessage().getString()).withStyle(main));
                if (ItemConfig.WitchRobeResistance.get() > 0) {
                    tooltip.add(Component.translatable("info.goety.witch_robe", ItemConfig.WitchRobeResistance.get()).withStyle(secondary));
                }
            }
            if (stack.getItem() instanceof WarlockGarmentItem) {
                tooltip.add(Component.translatable("info.goety.warlock_garment").withStyle(main));
                if (stack.getItem() instanceof WarlockRobeItem && ItemConfig.WarlockRobeResistance.get() > 0) {
                    tooltip.add(Component.translatable("info.goety.warlock_robe", ItemConfig.WarlockRobeResistance.get()).withStyle(secondary));
                }
            }
            if (stack.getItem() instanceof NetherRobeItem) {
                if (ItemConfig.NetherRobeResistance.get() > 0) {
                    tooltip.add(Component.translatable("info.goety.nether_robe", ItemConfig.NetherRobeResistance.get()).withStyle(main));
                }
                tooltip.add(Component.translatable("info.goety.nether_robe_discount").withStyle(secondary));
            }
            if (stack.getItem() instanceof UnholyRobeItem) {
                tooltip.add(Component.translatable("info.goety.unholy_robe").withStyle(main));
                tooltip.add(Component.translatable("info.goety.unholy_robe_discount").withStyle(secondary));
            }
            if (stack.is(ModItems.RING_OF_WANT.get())){
                tooltip.add(Component.translatable("info.goety.ring_of_want").withStyle(secondary));
            }
            if (stack.is(ModItems.RING_OF_FORCE.get())){
                tooltip.add(Component.translatable("info.goety.ring_of_force").withStyle(secondary));
            }
            if (stack.is(ModItems.RING_OF_THE_FORGE.get())){
                tooltip.add(Component.translatable("info.goety.ring_of_the_forge").withStyle(secondary));
            }
            if (stack.is(ModItems.RING_OF_THE_DRAGON.get())){
                tooltip.add(Component.translatable("info.goety.ring_of_the_dragon").withStyle(secondary));
            }
            if (stack.is(ModItems.GRAVE_GLOVE.get())) {
                tooltip.add(Component.translatable("info.goety.grave_gloves").withStyle(secondary));
            }
            if (stack.is(ModItems.THRASH_GLOVE.get())) {
                tooltip.add(Component.translatable("info.goety.thrash_gloves").withStyle(secondary));
            }
            if (stack.is(ModItems.WAYFARERS_BELT.get())) {
                tooltip.add(Component.translatable("info.goety.wayfarers_belt").withStyle(secondary));
            }
            if (stack.is(ModItems.SPITEFUL_BELT.get())) {
                tooltip.add(Component.translatable("info.goety.spiteful_belt").withStyle(secondary));
            }
            if (stack.is(ModItems.STAR_AMULET.get())) {
                tooltip.add(Component.translatable("info.goety.star_amulet").withStyle(secondary));
            }
            if (stack.is(ModItems.ALARMING_CHARM.get())) {
                tooltip.add(Component.translatable("info.goety.alarming_charm").withStyle(secondary));
            }
        }
    }
}
