package com.Polarice3.Goety.client.render.block;

import com.Polarice3.Goety.common.blocks.entities.PedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
    public PedestalRenderer(BlockEntityRendererProvider.Context p_i226007_1_) {
    }

    public void render(PedestalBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay) {
        pBlockEntity.itemStackHandler.ifPresent(handler -> {
            ItemStack stack = handler.getStackInSlot(0);
            Minecraft minecraft = Minecraft.getInstance();
            if (!stack.isEmpty()) {
                pMatrixStack.pushPose();
                pMatrixStack.translate(0.5F, 1.1F, 0.5F);
                pMatrixStack.scale(1.0F, 1.0F, 1.0F);
                if (minecraft.level != null){
                    pMatrixStack.mulPose(Axis.YP.rotationDegrees(3 * (minecraft.level.getGameTime() % 360 + pPartialTicks)));
                }
                minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, pCombinedLight, pCombinedOverlay, pMatrixStack, pBuffer, pBlockEntity.getLevel(), 0);
                pMatrixStack.popPose();
            }
        });
    }

}
