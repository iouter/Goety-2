package com.Polarice3.Goety.common.magic.spells.abyss;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.SpellHurtingProjectile;
import com.Polarice3.Goety.common.entities.projectiles.SteamMissile;
import com.Polarice3.Goety.common.magic.ChargingSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SteamSpell extends ChargingSpell {

    public int defaultSoulCost() {
        return SpellConfig.SoulBoltCost.get();
    }

    @Override
    public int Cooldown() {
        return 2;
    }

    @Override
    public int defaultSpellCooldown() {
        return 10;
    }

    public SoundEvent CastingSound() {
        return ModSounds.CAST_STEAM.get();
    }

    @Override
    public float castingVolume() {
        return 1.0F;
    }

    @Override
    public int shotsNumber() {
        return 5;
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.ABYSS;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.VELOCITY.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff) {
        Vec3 vector3d = caster.getViewVector( 1.0F);
        SpellHurtingProjectile steamMissile = new SteamMissile(
                caster.getX() + vector3d.x / 2,
                caster.getEyeY() - 0.2,
                caster.getZ() + vector3d.z / 2,
                vector3d.x,
                vector3d.y,
                vector3d.z, worldIn);
        steamMissile.setExtraDamage(WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster));
        steamMissile.setBoltSpeed(WandUtil.getLevels(ModEnchantments.VELOCITY.get(), caster));
        steamMissile.setOwner(caster);
        worldIn.addFreshEntity(steamMissile);
    }
}
