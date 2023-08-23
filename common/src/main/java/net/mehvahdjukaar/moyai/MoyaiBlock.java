package net.mehvahdjukaar.moyai;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Locale;

public class MoyaiBlock extends FallingBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;
    public static final EnumProperty<RotationMode> MODE = EnumProperty.create("mode", RotationMode.class);


    public enum RotationMode implements StringRepresentable {
        STATIC, ROTATING_LEFT, ROTATING_RIGHT;

        @Override
        public String toString() {
            return this.getSerializedName();
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    protected MoyaiBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.BASALT)
                .randomTicks()
                .strength(5, 4));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BOTTOM, false)
                .setValue(MODE, RotationMode.STATIC));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        level.scheduleTick(currentPos, this, this.getDelayAfterPlace());
        if (direction == Direction.UP) {
            boolean shouldBeBottom = isMoyaiSameFacing(state, neighborState) &&
                    !isMoyaiSameFacing(state, level.getBlockState(currentPos.below()));
            if (state.getValue(BOTTOM) != shouldBeBottom) {
                return state.setValue(BOTTOM, shouldBeBottom);
            }
        } else if (direction == Direction.DOWN) {
            boolean shouldNotBeBottom = isMoyaiSameFacing(state, neighborState);
            if (shouldNotBeBottom) {
                if (state.getValue(BOTTOM)) {
                    return state.setValue(BOTTOM, false);
                }
            } else if (isMoyaiSameFacing(state, level.getBlockState(currentPos.above()))) {
                return state.setValue(BOTTOM, true);
            }

        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    private boolean isMoyaiSameFacing(BlockState state, BlockState neighborState) {
        return neighborState.is(this) &&
                neighborState.getValue(FACING) == state.getValue(FACING);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Level level = pContext.getLevel();
        BlockState state = this.defaultBlockState().setValue(MODE, RotationMode.STATIC)
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite());
        BlockPos pos = pContext.getClickedPos();
        boolean bottom = isMoyaiSameFacing(state, level.getBlockState(pos.above())) &&
                !isMoyaiSameFacing(state, level.getBlockState(pos.below()));
        return state.setValue(BOTTOM, bottom);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, MODE, BOTTOM);
    }

    private static long LAST_GREETED_TIME = -24000;

    public static boolean maybeEatSoap(ItemStack stack, BlockState state, BlockPos pos, Level level, @Nullable Player player) {
        if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals("soap") && Moyai.SUPP_INSTALLED) {

            BlockPos facingPos = pos.relative(state.getValue(FACING));
            if (level.getBlockState(facingPos).isAir()) {
                if (player == null || player.isCreative()) stack.shrink(1);
                if (level.isClientSide && player != null) {

                    player.displayClientMessage(Component.translatable("message.moyai.soap"), true);
                } else {
                    level.setBlockAndUpdate(facingPos, BuiltInRegistries.BLOCK.get(
                            new ResourceLocation("supplementaries:bubble_block")).defaultBlockState());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (Utils.getID(stack.getItem()).toString().equals("yippee:moyai_statue")) {
            if (pLevel.isClientSide) {
                pPlayer.displayClientMessage(Component.translatable("message.moyai.child"), true);
            }
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }

        if (maybeEatSoap(stack, pState, pPos, pLevel, pPlayer)) {
            //TODO: finish this
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }

        if (pLevel.isClientSide) {
            long time = pLevel.getDayTime();
            if (Math.abs(time - LAST_GREETED_TIME) >= 12000) {
                LAST_GREETED_TIME = time;
                pPlayer.displayClientMessage(Component.translatable("message.moyai.angelo"), true);
                pPlayer.swing(pHand);
            }
            //doest return success since its client only and we want to be able to place blocks with sounds & stuff
            // return InteractionResult.SUCCESS;
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    //only called by worldgen
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level instanceof WorldGenRegion) {
            //if this is called during world gen
            if (!isValidBiome(level.getBiome(pos))) return false;

            Direction direction = state.getValue(FACING);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockState s = level.getBlockState(pos.relative(dir));
                if (dir == direction && !s.isAir()) return false;
                else if (s.is(this)) {
                    if (s.getValue(FACING) == dir.getOpposite()) return false;
                }
            }
        }
        return true;
    }

    @ExpectPlatform
    private static boolean isValidBiome(Holder<Biome> biome) {
        throw new AssertionError();
    }


    @Override
    public int getDustColor(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return pState.getMapColor(pReader, pPos).col;
    }

    @Override
    protected void falling(FallingBlockEntity pFallingEntity) {
        pFallingEntity.setHurtsEntities(2.0F, 40);
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState oldState, FallingBlockEntity blockEntity) {
        if (!blockEntity.isSilent()) {
            level.levelEvent(1045, pos, 0);
            this.trySpawnGolem(level, pos, false);
        }
        BlockState newState = this.updateShape(state, Direction.UP, level.getBlockState(pos.above()), level, pos, pos.above());
        if (newState != state) level.setBlockAndUpdate(pos, newState);
    }

    //golem


    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @org.jetbrains.annotations.Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        pLevel.scheduleTick(pPos, this, this.getDelayAfterPlace());
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (!pOldState.is(pState.getBlock())) {
            this.trySpawnGolem(pLevel, pPos, true);
        }
    }

    public boolean canSpawnGolem(LevelReader pLevel, BlockPos pPos) {
        return this.getOrCreateIronGolemBase().find(pLevel, pPos) != null;
    }

    private boolean trySpawnGolem(Level pLevel, BlockPos pPos, boolean playerCreated) {
        var pattern = this.getOrCreateIronGolemFull();
        BlockPattern.BlockPatternMatch patternMatch = pattern.find(pLevel, pPos);
        if (patternMatch != null) {
            for (int j = 0; j < pattern.getWidth(); ++j) {
                for (int k = 0; k < pattern.getHeight(); ++k) {
                    BlockInWorld matchBlock = patternMatch.getBlock(j, k, 0);
                    pLevel.setBlock(matchBlock.getPos(), Blocks.AIR.defaultBlockState(), 2);
                    pLevel.levelEvent(2001, matchBlock.getPos(), Block.getId(matchBlock.getState()));
                }
            }

            BlockPos blockpos = patternMatch.getBlock(1, 2, 0).getPos();
            IronGolem irongolem = EntityType.IRON_GOLEM.create(pLevel);
            irongolem.setPlayerCreated(playerCreated);
            irongolem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(this));

            irongolem.moveTo(blockpos.getX() + 0.5D, blockpos.getY() + 0.05D, blockpos.getZ() + 0.5D, 0.0F, 0.0F);
            pLevel.addFreshEntity(irongolem);

            for (ServerPlayer player : pLevel.getEntitiesOfClass(ServerPlayer.class, irongolem.getBoundingBox().inflate(5.0D))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(player, irongolem);
            }

            for (int i1 = 0; i1 < pattern.getWidth(); ++i1) {
                for (int j1 = 0; j1 < pattern.getHeight(); ++j1) {
                    BlockInWorld matchBlock = patternMatch.getBlock(i1, j1, 0);
                    pLevel.blockUpdated(matchBlock.getPos(), Blocks.AIR);
                }
            }
            return true;
        }
        return false;
    }

    private BlockPattern getOrCreateIronGolemBase() {
        if (this.ironGolemBase == null) {
            this.ironGolemBase = BlockPatternBuilder.start().aisle("~ ~", "###", "~#~").where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', BlockInWorld.hasState(BlockStateBase::isAir)).build();
        }

        return this.ironGolemBase;
    }

    private BlockPattern getOrCreateIronGolemFull() {
        if (this.ironGolemFull == null) {
            this.ironGolemFull = BlockPatternBuilder.start().aisle("~^~", "###", "~#~").where('^', BlockInWorld.hasState(b -> b.getBlock() == this)).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', BlockInWorld.hasState(BlockState::isAir)).build();
        }

        return this.ironGolemFull;
    }

    @Nullable
    private BlockPattern ironGolemBase;
    @Nullable
    private BlockPattern ironGolemFull;

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return !pState.getValue(BOTTOM);
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        //only on full moon
        if (pState.getValue(MODE) == RotationMode.STATIC && pLevel.getMoonPhase() == 0 && pLevel.isNight()) {
            BlockState below = pLevel.getBlockState(pPos.below());
            Direction facing = pState.getValue(FACING);
            boolean moved = false;
            if (pLevel.random.nextBoolean() && canSee(pLevel, pPos, facing.getCounterClockWise())) {
                Direction dir = facing.getCounterClockWise();
                rotateWithBelow(pState, pLevel, pPos, below, dir, RotationMode.ROTATING_LEFT);
                moved = true;
            } else if (canSee(pLevel, pPos, facing.getClockWise())) {
                Direction dir = facing.getClockWise();
                rotateWithBelow(pState, pLevel, pPos, below, dir, RotationMode.ROTATING_RIGHT);
                moved = true;
            }
            if (moved) {
                pLevel.playSound(null, pPos, Moyai.MOYAI_ROTATE.get(), SoundSource.BLOCKS, 1, 1);
                pLevel.scheduleTick(pPos, this, 2 * 20 + pLevel.getRandom().nextInt(40));
            }
            return;
        }
        if (pLevel.random.nextFloat() < 0.3) {
            long count = pLevel.getPoiManager().getCountInRange(p -> p.is(Moyai.MOYAI_POI_TAG), pPos, 10, PoiManager.Occupancy.ANY);
            if (count >= 5) {
                pLevel.playSound(null, pPos, Moyai.MOYAI_THINK.get(), SoundSource.BLOCKS, 0.5f,
                        1 + pLevel.random.nextFloat() * 0.1f - pLevel.random.nextFloat() * 0.07f);
            }
        }
    }

    @NotNull
    private static boolean canSee(ServerLevel pLevel, BlockPos pos, Direction dir) {
        return !pLevel.getBlockState(pos.relative(dir)).isRedstoneConductor(pLevel, pos);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
        var mode = pState.getValue(MODE);
        boolean bottom = pState.getValue(BOTTOM);
        if (mode != RotationMode.STATIC && !bottom) {
            BlockState below = pLevel.getBlockState(pPos.below());
            Direction dir;
            if (mode == RotationMode.ROTATING_RIGHT) {
                dir = pState.getValue(FACING).getCounterClockWise();
            } else {
                dir = pState.getValue(FACING).getClockWise();
            }
            rotateWithBelow(pState, pLevel, pPos, below, dir, RotationMode.STATIC);
            pLevel.playSound(null, pPos, Moyai.MOYAI_ROTATE.get(), SoundSource.BLOCKS, 1, 0.8f);
        }
        super.tick(pState, pLevel, pPos, pRand);
        if (bottom) {
            BlockState above = pLevel.getBlockState(pPos.above());
            if (above.is(this)) {
                above.tick(pLevel, pPos.above(), pRand);
            }
        }

    }

    private void rotateWithBelow(BlockState pState, ServerLevel pLevel, BlockPos pPos, BlockState below, Direction dir, RotationMode mode) {
        pLevel.setBlock(pPos, pState.setValue(MODE, mode).setValue(FACING, dir), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        if (isMoyaiSameFacing(pState, below) && below.getValue(BOTTOM)) {
            pLevel.setBlock(pPos.below(), below.setValue(FACING, dir), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        }

    }

    @Override
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        if (pId == 0) {
            pLevel.addParticle(ParticleTypes.NOTE, pPos.getX() + 0.5D, pPos.getY() + 1.2D + 1, pPos.getZ() + 0.5D, pParam / 24.0D, 0.0D, 0.0D);
            if (pLevel.isClientSide) {
                setShaking(pPos, pParam);
            }
            return true;
        }
        return super.triggerEvent(pState, pLevel, pPos, pId, pParam);
    }

    @ExpectPlatform
    private static void setShaking(BlockPos pPos, int pParam) {
    }
}
