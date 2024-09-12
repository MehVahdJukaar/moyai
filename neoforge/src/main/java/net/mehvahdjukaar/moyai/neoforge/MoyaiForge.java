package net.mehvahdjukaar.moyai.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moyai.Moyai;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.FireworkExplosion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.FireworkShapeFactoryRegistry;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.NoteBlockEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moyai.MOD_ID)
public class MoyaiForge {

    public static final FireworkExplosion.Shape MOYAI_FIREWORK = FireworkExplosion.Shape.valueOf("MOYAI_MOYAI");

    public MoyaiForge(IEventBus bus) {
        RegHelper.startRegisteringFor(bus);
        Moyai.commonInit();
        NeoForge.EVENT_BUS.addListener(MoyaiForge::onNoteBlockPlayer);

        PlatHelper.addCommonSetup(() -> RegHelper.registerFireworkRecipe(MOYAI_FIREWORK, Moyai.MOYAI_ITEM.get()));
        if (PlatHelper.getPhysicalSide().isClient()) {
            FireworkShapeFactoryRegistry.register(MOYAI_FIREWORK, MoyaiFireworkShape::create);
        }
    }

    private static void onNoteBlockPlayer(NoteBlockEvent.Play event) {
        if (Moyai.onNotePlayed(event.getLevel(), event.getPos(), event.getState())) {
            event.setCanceled(true);
        }
    }


}

