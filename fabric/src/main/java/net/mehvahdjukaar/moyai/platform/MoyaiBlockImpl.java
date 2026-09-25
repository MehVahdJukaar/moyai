package net.mehvahdjukaar.moyai.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public class MoyaiBlockImpl {
    public static boolean isValidBiome(Holder<Biome> biome) {
        return biome.value().getBaseTemperature() >= 0.15F;
    }

    public static void setShaking(BlockPos pPos, int pParam) {
    }
}
