package net.mehvahdjukaar.moyai;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Author: MehVahdJukaar
 */
public class Moyai {
    public static final String MOD_ID = "moyai";

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static final Supplier<SoundEvent> MOYAI_BOOM_SOUND = RegHelper.registerSound(res("record.moyai_boom"));
    public static final Supplier<SoundEvent> MOYAI_ROTATE = RegHelper.registerSound(res("block.moyai_rotate"));
    public static final Supplier<SoundEvent> MOYAI_THINK = RegHelper.registerSound(res("block.moyai_think"));
    public static final Supplier<Block> MOYAI_BLOCK = RegHelper.registerBlock(res("moyai"), MoyaiBlock::new);
    public static final Supplier<BlockItem> MOYAI_ITEM = RegHelper.registerItem(res("moyai"), () ->
            new BlockItem(MOYAI_BLOCK.get(), (new Item.Properties()).rarity(Rarity.RARE)));

    public static final Supplier<PoiType> MOYAI_POI = RegHelper.register(res("moyai"), () ->
            new PoiType(ImmutableSet.<BlockState>builder().addAll(MOYAI_BLOCK.get().getStateDefinition().getPossibleStates()).build(),
            1, 1), Registries.POINT_OF_INTEREST_TYPE);

    public static final TagKey<PoiType> MOYAI_POI_TAG = TagKey.create(Registries.POINT_OF_INTEREST_TYPE, res("moyai"));

    public static final Supplier<GameEvent> MOYAI_BOOM_EVENT = RegHelper.register(res("moyai_boom"),
            () -> new GameEvent("moyai_boom", 16), Registries.GAME_EVENT);

    public static final boolean SUPP_INSTALLED = PlatHelper.isModLoaded("supplementaries");

    public static void commonInit() {
        RegHelper.addItemsToTabsRegistration(Moyai::onAddItemToTabs);
    }

    private static void onAddItemToTabs(RegHelper.ItemToTabEvent event) {
        event.add(CreativeModeTabs.FUNCTIONAL_BLOCKS, MOYAI_BLOCK.get());
        event.add(CreativeModeTabs.NATURAL_BLOCKS, MOYAI_BLOCK.get());
    }

    public static void commonSetup() {
        Optional<Item> i = BuiltInRegistries.ITEM.getOptional(new ResourceLocation("supplementaries:soap"));
        i.ifPresent(item -> DispenserBlock.registerBehavior(item, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                BlockState state = source.getLevel().getBlockState(pos);
                if (state.is(MOYAI_BLOCK.get())) {
                    if (MoyaiBlock.maybeEatSoap(stack, state, pos, source.getLevel(), null)) {
                        return stack;
                    }
                }
                return super.execute(source, stack);
            }
        }));
    }


    public static boolean onNotePlayed(LevelAccessor level, BlockPos pos, BlockState blockState) {
        if (blockState.getValue(NoteBlock.INSTRUMENT) == NoteBlockInstrument.BASEDRUM) {
            BlockState below = level.getBlockState(pos.below());
            if (below.getBlock() instanceof MoyaiBlock && level instanceof ServerLevel serverLevel) {
                level.gameEvent(MOYAI_BOOM_EVENT.get(), pos, new GameEvent.Context(null, blockState));

                int i = blockState.getValue(NoteBlock.NOTE);
                float f = (float) Math.pow(2.0D, (i - 12) / 12.0D);
                level.playSound(null, pos, MOYAI_BOOM_SOUND.get(), SoundSource.RECORDS, 0.5F, f);
                serverLevel.blockEvent(pos.below(), below.getBlock(), 0, i);
                return true;
            }
        }
        return false;
    }
}