package net.mehvahdjukaar.moyai;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class MoyaiHeadLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {

    private final ModelPart head;

    public MoyaiHeadLayer(RenderLayerParent<S, M> parent) {
        super(parent);
        this.head = this.getParentModel().root().getChild("head");
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        if (!state.headItem.isEmpty()) {

            poseStack.pushPose();

            this.head.translateAndRotate(poseStack);

            translateToHead(poseStack);
            state.headItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);

            poseStack.popPose();
        }
    }

    private static void translateToHead(PoseStack stack) {
        stack.translate(0.0D, -0.25D, 0.0D);
        stack.mulPose(RotHlpr.Y180);
        stack.scale(0.625F, -0.625F, -0.625F);

        stack.translate(0.0D, 0.25 + 3 / 64f, -0.125D);
    }
}
