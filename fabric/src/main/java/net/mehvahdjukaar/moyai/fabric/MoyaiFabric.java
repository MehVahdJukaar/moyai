package net.mehvahdjukaar.moyai.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moyai.Moyai;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

public class MoyaiFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        Moyai.commonInit();

        PlatHelper.addCommonSetup(Moyai::commonSetup);

        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.MUSHROOM_FIELDS),
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                ResourceKey.create(Registries.PLACED_FEATURE, Moyai.res("moyai_mushroom")));

        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_BEACH),
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                ResourceKey.create(Registries.PLACED_FEATURE, Moyai.res("moyai_beach")));

    }
}
