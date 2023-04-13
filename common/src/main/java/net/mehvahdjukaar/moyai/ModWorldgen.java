package net.mehvahdjukaar.moyai;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.*;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class ModWorldgen {

    protected static void init() {
    }

    //helper
    private static RandomPatchConfiguration makeRandomPatch(int tries, int xzSpread, int ySpread, ConfiguredFeature<?, ?> feature, BlockPredicate placementRule) {
        return new RandomPatchConfiguration(tries, xzSpread, ySpread, PlacementUtils.inlinePlaced(Holder.direct(feature),
                BlockPredicateFilter.forPredicate(placementRule)));
    }

    private static Holder<PlacedFeature> makeSimpleMoyaiFeature(Direction dir) {
        return PlacementUtils.filtered(
                Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(SimpleStateProvider.simple(Moyai.MOYAI_BLOCK.get().defaultBlockState().setValue(MoyaiBlock.FACING, dir))),
                BlockPredicate.wouldSurvive(Moyai.MOYAI_BLOCK.get().defaultBlockState()
                        .setValue(MoyaiBlock.FACING, dir).setValue(MoyaiBlock.MODE, MoyaiBlock.RotationMode.STATIC), Vec3i.ZERO));
    }

    private static final BlockPredicate BEACH_PLACEMENT = BlockPredicate.allOf(
            BlockPredicate.ONLY_IN_AIR_PREDICATE,
            BlockPredicate.matchesTag(BlockPos.ZERO.below(), BlockTags.SAND)
    );


    private static final BlockPredicate MUSHROOM_PLACEMENT = BlockPredicate.allOf(
            BlockPredicate.ONLY_IN_AIR_PREDICATE,
            BlockPredicate.anyOf(
                    BlockPredicate.matchesTag(BlockPos.ZERO.below(), BlockTags.SAND),
                    BlockPredicate.matchesBlocks(BlockPos.ZERO.below(), Blocks.MYCELIUM)
            )
    );

    //configured features

    //single block feature. not registered as it's nested
    private static final Supplier<ConfiguredFeature<SimpleRandomFeatureConfiguration, ?>> MOYAI_FEATURE = Suppliers.memoize(() -> new ConfiguredFeature<>(Feature.SIMPLE_RANDOM_SELECTOR,
            new SimpleRandomFeatureConfiguration(HolderSet.direct(
                    makeSimpleMoyaiFeature(Direction.NORTH),
                    makeSimpleMoyaiFeature(Direction.SOUTH),
                    makeSimpleMoyaiFeature(Direction.EAST),
                    makeSimpleMoyaiFeature(Direction.WEST)))));

    public static final RegSupplier<ConfiguredFeature<RandomPatchConfiguration, Feature<RandomPatchConfiguration>>> MUSHROOM_MOYAI_PATCH =
            RegHelper.registerConfiguredFeature(Moyai.res("mushroom_moyai"), () -> Feature.RANDOM_PATCH,
                    () -> makeRandomPatch(
                            40,
                            9, 3,
                            MOYAI_FEATURE.get(),
                            MUSHROOM_PLACEMENT));

    public static final RegSupplier<ConfiguredFeature<RandomPatchConfiguration, Feature<RandomPatchConfiguration>>> BEACH_MOYAI_PATCH =
            RegHelper.registerConfiguredFeature(Moyai.res("beach_moyai"), () -> Feature.RANDOM_PATCH,
                    () -> makeRandomPatch(
                            35,
                            10, 3,
                            MOYAI_FEATURE.get(),
                            BEACH_PLACEMENT));


    //placed features


    public static final RegSupplier<PlacedFeature> BEACH_MOYAI =
            RegHelper.registerPlacedFeature(Moyai.res("beach_moyai"),
                    BEACH_MOYAI_PATCH,
                    () -> List.of(
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            RarityFilter.onAverageOnceEvery(45),
                            InSquarePlacement.spread(),
                            BiomeFilter.biome()));


    public static final RegSupplier<PlacedFeature> MUSHROOM_MOYAI =
            RegHelper.registerPlacedFeature(Moyai.res("mushroom_moyai"),
                    MUSHROOM_MOYAI_PATCH,
                    () -> List.of(
                            HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(63 + 64), VerticalAnchor.aboveBottom(64 + 64 + 9)),
                            RarityFilter.onAverageOnceEvery(6),
                            InSquarePlacement.spread(),
                            BiomeFilter.biome()));

//todo:json
}
