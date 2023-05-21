package net.mehvahdjukaar.moyai.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.mehvahdjukaar.moonlight.fabric.FabricSetupCallbacks;
import net.mehvahdjukaar.moyai.ModWorldgen;
import net.mehvahdjukaar.moyai.Moyai;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

public class MoyaiFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        Moyai.commonInit();

        FabricSetupCallbacks.COMMON_SETUP.add(Moyai::commonSetup);

        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.MUSHROOM_FIELDS),
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, ModWorldgen.MUSHROOM_MOYAI.getId()));

        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_BEACH),
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, ModWorldgen.BEACH_MOYAI.getId()));
    }
}
