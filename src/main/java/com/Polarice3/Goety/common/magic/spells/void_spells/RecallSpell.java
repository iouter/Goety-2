package com.Polarice3.Goety.common.magic.spells.void_spells;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.items.magic.RecallFocus;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RecallSpell extends Spell {
    @Override
    public int defaultSoulCost() {
        return SpellConfig.RecallCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.RecallDuration.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.RecallCoolDown.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return null;
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.VOID;
    }

    @Override
    public SoundEvent loopSound(LivingEntity caster) {
        return SoundEvents.PORTAL_TRIGGER;
    }

    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster){
        if (caster instanceof ServerPlayer player) {
            if (RecallFocus.isValid(worldIn, WandUtil.findFocus(player))) {
                return true;
            } else if (!RecallFocus.hasRecall(WandUtil.findFocus(player))){
                player.displayClientMessage(Component.translatable("info.goety.focus.noPos"), true);
            } else {
                player.displayClientMessage(Component.translatable("info.goety.focus.PosInvalid"), true);
            }
        }
        return false;
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff){
        if (caster instanceof ServerPlayer player) {
            if (RecallFocus.isValid(worldIn, WandUtil.findFocus(player))) {
                RecallFocus.recall(player, WandUtil.findFocus(player));
            }
        }
    }
}
