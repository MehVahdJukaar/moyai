package net.mehvahdjukaar.moyai;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.Locale;

public class MoyaiBlock extends FallingBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<RotationMode> MODE = EnumProperty.create("mode", RotationMode.class);


    public enum RotationMode implements StringRepresentable {
        STATIC, ROTATING_LEFT, ROTATING_RIGHT;

        public String toString() {
            return this.getSerializedName();
        }

        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    protected MoyaiBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.BASALT)
                .randomTicks()
                .strength(5, 4));
        this.registerDefaultState(this.stateDefinition.any().setValue(MODE, RotationMode.STATIC));
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
        return this.defaultBlockState().setValue(MODE, RotationMode.STATIC)
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
        pBuilder.add(MODE);
    }

    private static long LAST_GREETED_TIME = -24000;

    public static boolean maybeEatSoap(ItemStack stack, BlockState state, BlockPos pos, Level level, @Nullable Player player) {
        if (Registry.ITEM.getKey(stack.getItem()).getPath().equals("soap") && Moyai.SUPP_INSTALLED) {

            BlockPos facingPos = pos.relative(state.getValue(FACING));
            if (level.getBlockState(facingPos).isAir()) {
                if (player == null || player.isCreative()) stack.shrink(1);
                if (level.isClientSide && player != null) {

                    player.displayClientMessage(Component.translatable("message.moyai.soap"), true);
                } else {
                    level.setBlockAndUpdate(facingPos, Registry.BLOCK.get(
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
    public void onLand(Level level, BlockPos pos, BlockState p_48795_, BlockState state, FallingBlockEntity blockEntity) {
        if (!blockEntity.isSilent()) {
            level.levelEvent(1045, pos, 0);
            this.trySpawnGolem(level, pos, false);
        }
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

            irongolem.moveTo((double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.05D, (double) blockpos.getZ() + 0.5D, 0.0F, 0.0F);
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
            this.ironGolemBase = BlockPatternBuilder.start().aisle("~ ~", "###", "~#~").where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
        }

        return this.ironGolemBase;
    }

    private BlockPattern getOrCreateIronGolemFull() {
        if (this.ironGolemFull == null) {
            this.ironGolemFull = BlockPatternBuilder.start().aisle("~^~", "###", "~#~").where('^', BlockInWorld.hasState(b -> b.getBlock() == this)).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
        }

        return this.ironGolemFull;
    }

    @Nullable
    private BlockPattern ironGolemBase;
    @Nullable
    private BlockPattern ironGolemFull;

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return pState.getValue(MODE) == RotationMode.STATIC;
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        //only on full moon
        if (pLevel.getMoonPhase() == 0 && pLevel.isNight()) {
            if (pLevel.random.nextBoolean()) {
                pLevel.setBlock(pPos, pState.setValue(MODE, RotationMode.ROTATING_LEFT)
                        .setValue(FACING, pState.getValue(FACING).getCounterClockWise()), Block.UPDATE_CLIENTS);
            } else {
                pLevel.setBlock(pPos, pState.setValue(MODE, RotationMode.ROTATING_RIGHT)
                        .setValue(FACING, pState.getValue(FACING).getClockWise()), Block.UPDATE_CLIENTS);
            }

            pLevel.playSound(null, pPos, Moyai.MOYAI_ROTATE.get(), SoundSource.BLOCKS, 1, 1);

            pLevel.scheduleTick(pPos, this, 2 * 20 + pLevel.getRandom().nextInt(40));
        }
        super.randomTick(pState, pLevel, pPos, pRandom);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {

        var mode = pState.getValue(MODE);
        if (mode != RotationMode.STATIC) {
            if (mode == RotationMode.ROTATING_RIGHT) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(MODE, RotationMode.STATIC)
                        .setValue(FACING, pState.getValue(FACING).getCounterClockWise()));
            } else {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(MODE, RotationMode.STATIC)
                        .setValue(FACING, pState.getValue(FACING).getClockWise()));
            }
            pLevel.playSound(null, pPos, Moyai.MOYAI_ROTATE.get(), SoundSource.BLOCKS, 1, 0.8f);
        }
        super.tick(pState, pLevel, pPos, pRand);
    }

    @Override
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        if (pId == 0) {
            pLevel.addParticle(ParticleTypes.NOTE, (double) pPos.getX() + 0.5D, (double) pPos.getY() + 1.2D + 1, (double) pPos.getZ() + 0.5D, (double) pParam / 24.0D, 0.0D, 0.0D);
            if (pLevel.isClientSide){
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
