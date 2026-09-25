package net.mehvahdjukaar.moyai.platform;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moyai.Moyai;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;

public class MoyaiFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        Moyai.commonInit();

        BiomeModifications.addFeature(BiomeSelectors.tag(ConventionalBiomeTags.IS_MUSHROOM),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                ResourceKey.create(Registries.PLACED_FEATURE, Moyai.res("moyai_mushroom")));

        BiomeModifications.addFeature(BiomeSelectors.tag(Moyai.HAS_MOYAI_BEACH),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                ResourceKey.create(Registries.PLACED_FEATURE, Moyai.res("moyai_beach")));

    }
}
