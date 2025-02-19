package com.Polarice3.Goety.common.magic.spells;

import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.MagicBolt;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MagicBoltSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return SpellConfig.MagicBoltCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.MagicBoltDuration.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.MagicBoltCoolDown.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return ModSounds.CAST_SPELL.get();
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.DURATION.get());
        return list;
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff){
        Vec3 vector3d = caster.getViewVector( 1.0F);
        MagicBolt soulBolt = new MagicBolt(worldIn,
                caster.getX() + vector3d.x / 2,
                caster.getEyeY() - 0.2,
                caster.getZ() + vector3d.z / 2,
                vector3d.x,
                vector3d.y,
                vector3d.z);
        soulBolt.setOwner(caster);
        worldIn.addFreshEntity(soulBolt);
        worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), CastingSound(), this.getSoundSource(), 1.0F, 1.0F);
    }

}
