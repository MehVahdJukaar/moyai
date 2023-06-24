package net.mehvahdjukaar.moyai.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moyai.Moyai;
import net.mehvahdjukaar.moyai.MoyaiHeadLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Moyai.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MoyaiClientForge {


    @Mod.EventBusSubscriber(modid = Moyai.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Rumbler {
        private static final int SHAKE_DURATION = 2 * 20;
        private static final int MAX_DIST = 16;
        /*
        private static final LoadingCache<BlockPos,Integer> CACHED_POS = CacheBuilder.newBuilder()
                .expireAfterAccess(SHAKE_DURATION/20, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public Integer load(BlockPos key) {
                        return null;
                    }
                });
                */
        private static final Map<BlockPos, Float> CACHED_POS = new HashMap<>();

        private static float animationCounter = 0;

        public static void setShaking(BlockPos pos, int note) {
            float n = 1- note/24f;
            Player p = Minecraft.getInstance().player;
            if (p != null) {
                CACHED_POS.put(pos, (float) SHAKE_DURATION*(0.75f*(n-0.5f)+1));
            }
        }

        private static double getIntensity(BlockPos pos, Player player) {
            double dist = player.position().distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            return Math.max(0, (MAX_DIST * MAX_DIST) - dist) / (MAX_DIST * MAX_DIST);
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event){
            if(event.phase == TickEvent.Phase.END){
                animationCounter++;
            }
        }

        @SubscribeEvent
        public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
            Player p = Minecraft.getInstance().player;

            if (p != null && !Minecraft.getInstance().isPaused()) {
                double maxIntensity = 0;

                Set<BlockPos> toRemove = new HashSet<>();
                for (var pos : CACHED_POS.keySet()) {

                    maxIntensity = Math.max(maxIntensity, getIntensity(pos, p));


                    float duration = CACHED_POS.get(pos);
                    duration-=event.getPartialTick();
                    if(duration<0){
                        toRemove.add(pos);
                    }else{
                        CACHED_POS.put(pos,duration);
                    }
                }
                toRemove.forEach(CACHED_POS::remove);
                if(maxIntensity != 0){
                    //float a = (float) (Mth.sin((float) (((animationCounter)*1.5f)%Math.PI)) * 1.8 * maxIntensity ); //* Math.min(1, animationCounter / 25)
                    event.setRoll((float) (event.getRoll() +
                            Mth.sin((float) ((((animationCounter+event.getPartialTick())/3f)%1)*2*Math.PI))* 1.5f * maxIntensity));
                }
            }
        }

    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        addLayer(event.getRenderer(EntityType.IRON_GOLEM));
    }

    private static <T extends LivingEntity, M extends HierarchicalModel<T>, R extends LivingEntityRenderer<T, M>> void addLayer(
            @Nullable R renderer) {
        if (renderer != null) {
            renderer.addLayer(new MoyaiHeadLayer<>(renderer));
        }
    }


}
