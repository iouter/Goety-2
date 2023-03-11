package com.Polarice3.Goety.client.render;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.client.render.model.SummonCircleModel;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.common.entities.util.SummonCircleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SummonCircleRenderer extends EntityRenderer<SummonCircleEntity> {
    private static final ResourceLocation TEXTURES = new ResourceLocation(Goety.MOD_ID,"textures/entity/summon_circle.png");
    private static final ResourceLocation TEXTURES_APOSTLE = new ResourceLocation(Goety.MOD_ID,"textures/entity/cultist/apostle_summon_circle.png");
    private final SummonCircleModel model;

    public SummonCircleRenderer(EntityRendererProvider.Context p_i46179_1_) {
        super(p_i46179_1_);
        this.model = new SummonCircleModel(p_i46179_1_.bakeLayer(ModModelLayer.SUMMON_CIRCLE));
    }

    public void render(SummonCircleEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(this.model.renderType(this.getTextureLocation(entityIn)));
        this.model.setupAnim(entityIn, 0.0F, 0.0F, partialTicks/10, entityIn.getYRot(), entityIn.getXRot());
        matrixStackIn.scale(1.5F, 1.5F, 1.5F);
        matrixStackIn.translate(0.0D, 1.6D, 0.0D);
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        this.model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.15F);
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(SummonCircleEntity pEntity) {
        if(pEntity.getTrueOwner() instanceof Apostle){
            return TEXTURES_APOSTLE;
        }
        return TEXTURES;
    }
}
