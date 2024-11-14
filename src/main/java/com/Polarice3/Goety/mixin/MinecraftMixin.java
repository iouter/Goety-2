package com.Polarice3.Goety.mixin;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At(value = "HEAD"), cancellable = true)
    public void shouldEntityAppearGlowing(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null
                && player.hasEffect(GoetyEffects.TREMOR_SENSE.get())
                && pEntity instanceof LivingEntity livingEntity
                && player.onGround()
                && livingEntity.onGround()) {
            MobEffectInstance instance = player.getEffect(GoetyEffects.TREMOR_SENSE.get());
            if (instance != null){
                float amp = (instance.getAmplifier() + 1.0F) / 2.0F;
                if (player.distanceTo(livingEntity) <= 32.0F * amp) {
                    cir.setReturnValue(true);
                }
            }

        }
    }
}
