package net.mehvahdjukaar.moyai.platform;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public class MoyaiBlockImpl {
    public static boolean isValidBiome(Holder<Biome> biome) {
        return !biome.is(ConventionalBiomeTags.IS_COLD);
    }

    public static void setShaking(BlockPos pPos, int pParam) {
    }
}
