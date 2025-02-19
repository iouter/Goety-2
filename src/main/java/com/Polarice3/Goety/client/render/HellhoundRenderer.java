package com.Polarice3.Goety.client.render;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.client.render.model.BlackWolfModel;
import com.Polarice3.Goety.common.entities.ally.BlackWolf;
import com.Polarice3.Goety.common.entities.ally.Hellhound;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class HellhoundRenderer extends MobRenderer<Hellhound, BlackWolfModel<Hellhound>> {
    private static final ResourceLocation TEXTURES = Goety.location("textures/entity/servants/black_wolf/hellhound.png");

    public HellhoundRenderer(EntityRendererProvider.Context p_174452_) {
        super(p_174452_, new BlackWolfModel<>(p_174452_.bakeLayer(ModModelLayer.BLACK_WOLF)), 0.5F);
        this.addLayer(new WolfEyesLayer<>(this));
        this.addLayer(new ChainsLayer<>(this, p_174452_.getModelSet()));
        this.addLayer(new BigFurLayer<>(this, p_174452_.getModelSet()));
    }

    @Override
    protected void scale(Hellhound p_115314_, PoseStack p_115315_, float p_115316_) {
        int i = p_115314_.isUpgraded() ? 1 : 0;
        float f = 1.0F + 0.15F * (float)i;
        p_115315_.scale(f, f, f);
    }

    protected float getBob(Hellhound p_116528_, float p_116529_) {
        return p_116528_.getTailAngle();
    }

    public void render(Hellhound p_116531_, float p_116532_, float p_116533_, PoseStack p_116534_, MultiBufferSource p_116535_, int p_116536_) {
        if (p_116531_.isWet()) {
            float f = p_116531_.getWetShade(p_116533_);
            this.model.setColor(f, f, f);
        }

        super.render(p_116531_, p_116532_, p_116533_, p_116534_, p_116535_, p_116536_);
        if (p_116531_.isWet()) {
            this.model.setColor(1.0F, 1.0F, 1.0F);
        }

    }

    public ResourceLocation getTextureLocation(Hellhound p_116526_) {
        return TEXTURES;
    }

    public static class ChainsLayer<T extends Hellhound, M extends BlackWolfModel<T>> extends RenderLayer<T, M> {
        private static final ResourceLocation TEXTURES = Goety.location("textures/entity/servants/black_wolf/hellhound_chains.png");
        private final BlackWolfModel<T> layerModel;

        public ChainsLayer(RenderLayerParent<T, M> p_117346_, EntityModelSet p_174555_) {
            super(p_117346_);
            this.layerModel = new BlackWolfModel<>(p_174555_.bakeLayer(ModModelLayer.BLACK_WOLF));
        }

        @Override
        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T blackHound, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!blackHound.isNatural()) {
                coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, TEXTURES, matrixStackIn, bufferIn, packedLightIn, blackHound, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    public static class BigFurLayer<T extends BlackWolf, M extends BlackWolfModel<T>> extends RenderLayer<T, M> {
        private static final ResourceLocation TEXTURES = Goety.location("textures/entity/servants/black_wolf/hellhound_fur.png");

        private final BlackWolfModel<T> layerModel;

        public BigFurLayer(RenderLayerParent<T, M> p_117346_, EntityModelSet p_174555_) {
            super(p_117346_);
            this.layerModel = new BlackWolfModel<>(p_174555_.bakeLayer(ModModelLayer.BLACK_WOLF));
        }

        @Override
        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T blackHound, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (blackHound.isUpgraded()) {
                coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, TEXTURES, matrixStackIn, bufferIn, packedLightIn, blackHound, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    public static class WolfEyesLayer<T extends Hellhound, M extends BlackWolfModel<T>> extends EyesLayer<T, M> {
        private static final ResourceLocation TEXTURES = Goety.location("textures/entity/servants/black_wolf/hellhound_eyes.png");

        public WolfEyesLayer(RenderLayerParent<T, M> p_117346_) {
            super(p_117346_);
        }

        @Override
        public @NotNull RenderType renderType() {
            return RenderType.eyes(TEXTURES);
        }
    }
}
