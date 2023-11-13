package net.mehvahdjukaar.moyai.forge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moyai.Moyai;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraftforge.client.FireworkShapeFactoryRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.NoteBlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moyai.MOD_ID)
public class MoyaiForge {

    public static final FireworkRocketItem.Shape MOYAI_FIREWORK =FireworkRocketItem.Shape.create(
            "moyai", FireworkRocketItem.Shape.values().length, "moyai");

    public MoyaiForge() {
        Moyai.commonInit();
        MinecraftForge.EVENT_BUS.addListener(MoyaiForge::onNoteBlockPlayer);

        PlatHelper.addCommonSetup(()->RegHelper.registerFireworkRecipe(MOYAI_FIREWORK, Moyai.MOYAI_ITEM.get()));
        if(PlatHelper.getPhysicalSide().isClient()){
            FireworkShapeFactoryRegistry.register(MOYAI_FIREWORK, MoyaiFireworkShape::create);
        }
    }

    private static void onNoteBlockPlayer(NoteBlockEvent event) {
        if (Moyai.onNotePlayed(event.getLevel(), event.getPos(), event.getState())) {
            event.setCanceled(true);
        }
    }


}

