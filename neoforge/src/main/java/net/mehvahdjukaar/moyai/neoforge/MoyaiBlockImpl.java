package net.mehvahdjukaar.moyai.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

public class MoyaiBlockImpl {
    public static boolean isValidBiome(Holder<Biome> biome) {
        return !biome.is(Tags.Biomes.IS_COLD);
    }

    public static void setShaking(BlockPos pPos, int pParam) {
        MoyaiClientForge.Rumbler.setShaking(pPos, pParam);
    }
}
