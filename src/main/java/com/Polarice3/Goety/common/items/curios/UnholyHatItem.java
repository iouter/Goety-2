package com.Polarice3.Goety.common.items.curios;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.config.AttributesConfig;
import com.Polarice3.Goety.utils.CuriosFinder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.SlotContext;

@Mod.EventBusSubscriber(modid = Goety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnholyHatItem extends MagicCrownItem {

    public UnholyHatItem() {
        super(new Properties().fireResistant().stacksTo(1), SpellType.NETHER);
    }

    @SubscribeEvent
    public static void LivingEffects(LivingEvent.LivingTickEvent event){
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity != null){
            if (CuriosFinder.hasUnholyHat(livingEntity)){
                if (!livingEntity.level.isClientSide) {
                    if (livingEntity.hasEffect(GoetyEffects.BURN_HEX.get())){
                        livingEntity.removeEffect(GoetyEffects.BURN_HEX.get());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void HurtEvent(LivingHurtEvent event){
        LivingEntity victim = event.getEntity();
        float damage = event.getAmount();
        if (CuriosFinder.hasUnholyHat(victim)){
            if (victim.level.dimension() == Level.NETHER){
                damage /= 2.0F;
            }
            if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)){
                damage = Math.min(damage, AttributesConfig.ApostleDamageCap.get().floatValue());
            }
            event.setAmount(damage);
        }
    }

    @SubscribeEvent
    public static void PotionApplicationEvents(MobEffectEvent.Applicable event){
        if (event.getEffectInstance().getEffect() == GoetyEffects.BURN_HEX.get()){
            if (CuriosFinder.hasUnholyHat(event.getEntity())){
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @Override
    public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
