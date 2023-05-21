package net.mehvahdjukaar.moyai.forge;

import net.mehvahdjukaar.moyai.Moyai;
import net.minecraft.tags.BiomeTags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.NoteBlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moyai.MOD_ID)
public class MoyaiForge {

    public MoyaiForge() {
        Moyai.commonInit();

        MinecraftForge.EVENT_BUS.addListener(MoyaiForge::onNoteBlockPlayer);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Moyai::commonSetup);
    }

    private static void onNoteBlockPlayer(NoteBlockEvent event) {
        if (Moyai.onNotePlayed(event.getLevel(), event.getPos(), event.getState())) {
            event.setCanceled(true);
        }
    }


}

