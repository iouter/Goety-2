package com.Polarice3.Goety.client.render.block;

import com.Polarice3.Goety.common.blocks.entities.CursedInfuserBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CursedInfuserRenderer implements BlockEntityRenderer<CursedInfuserBlockEntity> {
    public CursedInfuserRenderer(BlockEntityRendererProvider.Context p_i226007_1_) {
    }

    public void render(CursedInfuserBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay) {
        NonNullList<ItemStack> nonnulllist = pBlockEntity.getItems();
        Minecraft minecraft = Minecraft.getInstance();
        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = nonnulllist.get(i);
            if (!itemstack.isEmpty()){
                pMatrixStack.pushPose();
                pMatrixStack.translate(0.5F, 0.5F, 0.5F);
                pMatrixStack.scale(1.0F, 1.0F, 1.0F);
                if (minecraft.level != null) {
                    pMatrixStack.mulPose(Axis.YP.rotationDegrees(3 * (minecraft.level.getGameTime() % 360 + pPartialTicks)));
                }
                minecraft.getItemRenderer().renderStatic(itemstack, ItemDisplayContext.GROUND, pCombinedLight, pCombinedOverlay, pMatrixStack, pBuffer, pBlockEntity.getLevel(), 0);
                pMatrixStack.popPose();
            }
        }
    }

}
