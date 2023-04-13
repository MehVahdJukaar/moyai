package net.mehvahdjukaar.moyai.mixins;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.IronGolem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolemModel.class)
public abstract class IronGolemLayerMixin<T extends IronGolem> extends HierarchicalModel<T> {

    @Shadow @Final private ModelPart head;

    @Inject(method = "prepareMobModel(Lnet/minecraft/world/entity/animal/IronGolem;FFF)V", at = @At("HEAD"))
    public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, CallbackInfo cb) {
        this.head.visible = pEntity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
    }


}
