package com.Polarice3.Goety.common.effects.brew;

import com.Polarice3.Goety.utils.ItemHelper;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nullable;

public class FlayingBrewEffect extends BrewEffect {
    public FlayingBrewEffect() {
        super("flaying", MobEffectCategory.BENEFICIAL, 0x9e492a);
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    @Override
    public boolean canLinger() {
        return true;
    }

    public void applyEntityEffect(LivingEntity pTarget, @Nullable Entity pSource, @Nullable Entity pIndirectSource, int pAmplifier){
        LivingEntity livingEntity = pIndirectSource instanceof LivingEntity living ? living : null;
        DamageSource damageSource = livingEntity != null ? pTarget.damageSources().mobAttack(livingEntity) : pTarget.damageSources().sweetBerryBush();
        if (pTarget instanceof Animal animal){
            if (animal.level.getServer() != null) {
                LootTable loottable = animal.level.getServer().getLootData().getLootTable(animal.getLootTable());
                LootParams.Builder lootcontext$builder = MobUtil.createLootContext(damageSource, animal).withLuck(pAmplifier);
                LootParams ctx = lootcontext$builder.create(LootContextParamSets.ENTITY);
                for (int i = 0; i < pAmplifier + 1; ++i) {
                    for (ItemStack itemStack : loottable.getRandomItems(ctx)) {
                        if (!itemStack.getItem().isEdible() && !itemStack.isEmpty()) {
                            if (animal.hurt(damageSource, animal.getMaxHealth() / 4.0F)) {
                                itemStack.setCount(1);
                                ItemEntity item = ItemHelper.itemEntityDrop(animal, itemStack);
                                item.setDefaultPickUpDelay();
                                animal.level.addFreshEntity(item);
                            }
                        }
                    }
                }
            }
        }
    }
}
