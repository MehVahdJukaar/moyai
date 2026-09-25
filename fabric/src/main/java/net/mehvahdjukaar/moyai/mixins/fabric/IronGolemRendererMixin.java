package net.mehvahdjukaar.moyai.mixins.fabric;

import net.mehvahdjukaar.moyai.MoyaiHeadLayer;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.world.entity.animal.golem.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolemRenderer.class)
public abstract class IronGolemRendererMixin extends MobRenderer<IronGolem, IronGolemRenderState, IronGolemModel> {

    public IronGolemRendererMixin(EntityRendererProvider.Context context, IronGolemModel entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void prepareMobModel(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.addLayer(new MoyaiHeadLayer<>(this));
    }

}
