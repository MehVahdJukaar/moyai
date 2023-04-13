package net.mehvahdjukaar.moyai;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MoyaiHeadLayer<T extends LivingEntity, M extends HierarchicalModel<T>> extends RenderLayer<T, M> {

    private final ModelPart head;
    private final ItemInHandRenderer itemRenderer;

    public MoyaiHeadLayer(RenderLayerParent<T, M> parent) {
        super(parent);
        this.itemRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        this.head = this.getParentModel().root().getChild("head");
    }

    @Override
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (!itemstack.isEmpty()) {

            pMatrixStack.pushPose();

            this.head.translateAndRotate(pMatrixStack);

            translateToHead(pMatrixStack);
            itemRenderer.renderItem(pLivingEntity, itemstack, ItemDisplayContext.HEAD, false, pMatrixStack, pBuffer, pPackedLight);

            pMatrixStack.popPose();
        }
    }

    public static void translateToHead(PoseStack stack) {
        stack.translate(0.0D, -0.25D, 0.0D);
        stack.mulPose(RotHlpr.Y180);
        stack.scale(0.625F, -0.625F, -0.625F);

        stack.translate(0.0D, 0.25 + 3 / 64f, -0.125D);
    }
}
