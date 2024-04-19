// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;

public class BlockRepository
{
    public static Block air;
    public static Block voidAir;
    public static int airID;
    public static int voidAirID;
    public static MovingPistonBlock pistonTechBlock;
    public static Block water;
    public static Block lava;
    public static Block ice;
    public static Block grassBlock;
    public static Block oakLeaves;
    public static Block spruceLeaves;
    public static Block birchLeaves;
    public static Block jungleLeaves;
    public static Block acaciaLeaves;
    public static Block darkOakLeaves;
    public static Block grass;
    public static Block fern;
    public static Block tallGrass;
    public static Block largeFern;
    public static Block reeds;
    public static Block vine;
    public static Block lilypad;
    public static Block tallFlower;
    public static Block cobweb;
    public static Block stickyPiston;
    public static Block piston;
    public static Block redstone;
    public static Block ladder;
    public static Block barrier;
    public static Block chorusPlant;
    public static Block chorusFlower;
    public static FluidState dry;
    public static HashSet<Block> biomeBlocks;
    public static Block[] biomeBlocksArray;
    public static HashSet<Block> shapedBlocks;
    public static Block[] shapedBlocksArray;
    private static ConcurrentHashMap<BlockState, Integer> stateToInt;
    private static ReferenceArrayList<BlockState> blockStates;
    private static int count;
    private static ReadWriteLock incrementLock;
    
    public static void getBlocks() {
        BlockRepository.air = Blocks.AIR;
        BlockRepository.airID = getStateId(BlockRepository.air.defaultBlockState());
        BlockRepository.voidAir = Blocks.VOID_AIR;
        BlockRepository.voidAirID = getStateId(BlockRepository.voidAir.defaultBlockState());
        BlockRepository.pistonTechBlock = (MovingPistonBlock)Blocks.MOVING_PISTON;
        BlockRepository.water = Blocks.WATER;
        BlockRepository.lava = Blocks.LAVA;
        BlockRepository.ice = Blocks.ICE;
        BlockRepository.grassBlock = Blocks.GRASS_BLOCK;
        BlockRepository.oakLeaves = Blocks.OAK_LEAVES;
        BlockRepository.spruceLeaves = Blocks.SPRUCE_LEAVES;
        BlockRepository.birchLeaves = Blocks.BIRCH_LEAVES;
        BlockRepository.jungleLeaves = Blocks.JUNGLE_LEAVES;
        BlockRepository.acaciaLeaves = Blocks.ACACIA_LEAVES;
        BlockRepository.darkOakLeaves = Blocks.DARK_OAK_LEAVES;
        BlockRepository.grass = Blocks.GRASS;
        BlockRepository.fern = Blocks.FERN;
        BlockRepository.tallGrass = Blocks.TALL_GRASS;
        BlockRepository.largeFern = Blocks.LARGE_FERN;
        BlockRepository.reeds = Blocks.SUGAR_CANE;
        BlockRepository.vine = Blocks.VINE;
        BlockRepository.lilypad = Blocks.LILY_PAD;
        BlockRepository.cobweb = Blocks.COBWEB;
        BlockRepository.stickyPiston = Blocks.STICKY_PISTON;
        BlockRepository.piston = Blocks.PISTON;
        BlockRepository.redstone = Blocks.REDSTONE_WIRE;
        BlockRepository.ladder = Blocks.LADDER;
        BlockRepository.barrier = Blocks.BARRIER;
        BlockRepository.chorusPlant = Blocks.CHORUS_PLANT;
        BlockRepository.chorusFlower = Blocks.CHORUS_FLOWER;
        BlockRepository.biomeBlocksArray = new Block[] { BlockRepository.grassBlock, BlockRepository.oakLeaves, BlockRepository.spruceLeaves, BlockRepository.birchLeaves, BlockRepository.jungleLeaves, BlockRepository.acaciaLeaves, BlockRepository.darkOakLeaves, BlockRepository.grass, BlockRepository.fern, BlockRepository.tallGrass, BlockRepository.largeFern, BlockRepository.reeds, BlockRepository.vine, BlockRepository.lilypad, BlockRepository.tallFlower, BlockRepository.water };
        BlockRepository.biomeBlocks = new HashSet<Block>(Arrays.asList(BlockRepository.biomeBlocksArray));
        BlockRepository.shapedBlocksArray = new Block[] { BlockRepository.ladder, BlockRepository.vine };
        BlockRepository.shapedBlocks = new HashSet<Block>(Arrays.asList(BlockRepository.shapedBlocksArray));
        for (final Block block : Registry.BLOCK) {
            if (block instanceof DoorBlock || block instanceof SignBlock) {
                BlockRepository.shapedBlocks.add(block);
            }
        }
    }
    
    public static int getStateId(final BlockState blockState) {
        Integer id = BlockRepository.stateToInt.get(blockState);
        if (id == null) {
            synchronized (BlockRepository.incrementLock) {
                id = BlockRepository.stateToInt.get(blockState);
                if (id == null) {
                    id = BlockRepository.count;
                    BlockRepository.blockStates.add(blockState);
                    BlockRepository.stateToInt.put(blockState, id);
                    ++BlockRepository.count;
                }
            }
        }
        return id;
    }
    
    public static BlockState getStateById(final int id) {
        return (BlockState)BlockRepository.blockStates.get(id);
    }
    
    static {
        BlockRepository.airID = 0;
        BlockRepository.voidAirID = 0;
        BlockRepository.dry = Fluids.EMPTY.defaultFluidState();
        BlockRepository.biomeBlocksArray = new Block[] { BlockRepository.grassBlock, BlockRepository.oakLeaves, BlockRepository.spruceLeaves, BlockRepository.birchLeaves, BlockRepository.jungleLeaves, BlockRepository.acaciaLeaves, BlockRepository.darkOakLeaves, BlockRepository.grass, BlockRepository.fern, BlockRepository.tallGrass, BlockRepository.largeFern, BlockRepository.reeds, BlockRepository.vine, BlockRepository.lilypad, BlockRepository.tallFlower, BlockRepository.water };
        BlockRepository.shapedBlocksArray = new Block[] { BlockRepository.ladder, BlockRepository.vine };
        BlockRepository.stateToInt = new ConcurrentHashMap<BlockState, Integer>(1024);
        BlockRepository.blockStates = new ReferenceArrayList<BlockState>(16384);
        BlockRepository.count = 1;
        BlockRepository.incrementLock = new ReentrantReadWriteLock();
        final BlockState airBlockState = Blocks.AIR.defaultBlockState();
        BlockRepository.stateToInt.put(airBlockState, 0);
        BlockRepository.blockStates.add(airBlockState);
    }
}
