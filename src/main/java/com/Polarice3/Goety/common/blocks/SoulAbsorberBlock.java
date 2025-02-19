package com.Polarice3.Goety.common.blocks;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.blocks.entities.SoulAbsorberBlockEntity;
import com.Polarice3.Goety.common.crafting.SoulAbsorberRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.ToIntFunction;

public class SoulAbsorberBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public SoulAbsorberBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.5F)
                .sound(SoundType.STONE)
                .lightLevel(litBlockEmission())
                .noOcclusion()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(LIT, Boolean.FALSE));
    }

    private static ToIntFunction<BlockState> litBlockEmission() {
        return (state) -> state.getValue(BlockStateProperties.LIT) ? 10 : 0;
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity tileentity = pLevel.getBlockEntity(pPos);
        if (tileentity instanceof SoulAbsorberBlockEntity burnerTileEntity) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            Optional<SoulAbsorberRecipes> optional = burnerTileEntity.getRecipes(itemstack);
            if (optional.isPresent()) {
                if (!pLevel.isClientSide && burnerTileEntity.placeItem(pPlayer.getAbilities().instabuild ? itemstack.copy() : itemstack, optional.get().getCookingTime())) {
                    return InteractionResult.SUCCESS;
                }

                return InteractionResult.CONSUME;
            }
            if (itemstack.getItem() instanceof ITotem){
                if (ITotem.currentSouls(itemstack) > 0){
                    if (!pLevel.isClientSide && burnerTileEntity.placeItem(pPlayer.getAbilities().instabuild ? itemstack.copy() : itemstack, 9999)) {
                        return InteractionResult.SUCCESS;
                    }
                }

                return InteractionResult.CONSUME;
            }
            if (itemstack.isEmpty() || itemstack == burnerTileEntity.getItem(0)){
                if (!burnerTileEntity.getItem(0).isEmpty()){
                    dropItemStack(pLevel, pPlayer.blockPosition(), burnerTileEntity.getItem(0));
                    burnerTileEntity.removeItem(0, 1);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    public static void dropItemStack(Level pLevel, BlockPos pPos, ItemStack pStack) {
        double d0 = (double) EntityType.ITEM.getWidth();
        double d1 = 1.0D - d0;
        double d2 = d0 / 2.0D;
        double d3 = Math.floor(pPos.getX()) + pLevel.random.nextDouble() * d1 + d2;
        double d4 = Math.floor(pPos.getY()) + pLevel.random.nextDouble() * d1;
        double d5 = Math.floor(pPos.getZ()) + pLevel.random.nextDouble() * d1 + d2;

        while(!pStack.isEmpty()) {
            ItemEntity itementity = new ItemEntity(pLevel, d3, d4, d5, pStack.split(pLevel.random.nextInt(21) + 10));
            float f = 0.05F;
            itementity.setDeltaMovement(pLevel.random.nextGaussian() * (double)f, pLevel.random.nextGaussian() * (double)f + (double)0.2F, pLevel.random.nextGaussian() * (double)f);
            pLevel.addFreshEntity(itementity);
        }

    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity tileentity = pLevel.getBlockEntity(pPos);
            if (tileentity instanceof SoulAbsorberBlockEntity) {
                dropItemStack(pLevel, pPos, ((SoulAbsorberBlockEntity) tileentity).getItem(0));
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        LevelAccessor iworld = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        boolean flag = iworld.getFluidState(blockpos).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
        if (!pState.getValue(BlockStateProperties.WATERLOGGED) && pFluidState.getType() == Fluids.WATER) {
            pLevel.setBlock(pPos, pState.setValue(WATERLOGGED, Boolean.TRUE), 3);
            pLevel.scheduleTick(pPos, pFluidState.getType(), pFluidState.getType().getTickDelay(pLevel));
            return true;
        } else {
            return false;
        }
    }

    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WATERLOGGED, LIT);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new SoulAbsorberBlockEntity(p_153215_, p_153216_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152755_, BlockState p_152756_, BlockEntityType<T> p_152757_) {
        return (world, pos, state, blockEntity) -> {
            if (blockEntity instanceof SoulAbsorberBlockEntity arcaBlock)
                arcaBlock.tick();
        };
    }
}
