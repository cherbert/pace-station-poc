package com.tinblue.spacemod.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the whole space scene deterministically:
 * an empty void, the station at the origin, a scattering of asteroids,
 * and a spherical planet ~300 blocks east (+X) of the station.
 */
public class SpaceChunkGenerator extends ChunkGenerator {
    public static final MapCodec<SpaceChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource)
            ).apply(instance, SpaceChunkGenerator::new));

    // Planet — tweak freely.
    public static final int PLANET_X = 300;
    public static final int PLANET_Y = 110;
    public static final int PLANET_Z = 0;
    public static final double PLANET_RADIUS = 44.0;

    // A few asteroids floating between the station and the planet: {x, y, z, radius}
    private static final int[][] ASTEROIDS = {
            {70, 118, -18, 4},
            {105, 95, 25, 5},
            {140, 125, 8, 3},
            {175, 102, -30, 6},
            {205, 130, 20, 4},
            {235, 98, -10, 5},
            {120, 108, -45, 4},
            {90, 135, 40, 3},
    };

    public SpaceChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig,
                                                  StructureAccessor structureAccessor, Chunk chunk) {
        generate(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    private void generate(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int x = startX + lx;
                int z = startZ + lz;

                // --- Space station ---
                for (int y = 98; y <= 106; y++) {
                    BlockState state = StationBuilder.blockAt(x, y, z);
                    if (state != null) {
                        chunk.setBlockState(pos.set(x, y, z), state, false);
                    }
                }

                // --- Planet ---
                double dx = x - PLANET_X;
                double dz = z - PLANET_Z;
                double horizontal2 = dx * dx + dz * dz;
                double maxR = PLANET_RADIUS + 5.0;
                if (horizontal2 <= maxR * maxR) {
                    int span = (int) Math.ceil(Math.sqrt(maxR * maxR - horizontal2));
                    for (int y = PLANET_Y - span; y <= PLANET_Y + span; y++) {
                        BlockState state = planetBlockAt(x, y, z);
                        if (state != null) {
                            chunk.setBlockState(pos.set(x, y, z), state, false);
                        }
                    }
                }

                // --- Asteroids ---
                for (int[] asteroid : ASTEROIDS) {
                    int adx = x - asteroid[0];
                    int adz = z - asteroid[2];
                    int r = asteroid[3];
                    if (adx * adx + adz * adz > r * r) {
                        continue;
                    }
                    for (int y = asteroid[1] - r; y <= asteroid[1] + r; y++) {
                        int ady = y - asteroid[1];
                        if (adx * adx + ady * ady + adz * adz <= r * r) {
                            BlockState rock = ((x + y + z) % 5 == 0 ? Blocks.COBBLESTONE : Blocks.STONE).getDefaultState();
                            chunk.setBlockState(pos.set(x, y, z), rock, false);
                        }
                    }
                }
            }
        }
    }

    private BlockState planetBlockAt(int x, int y, int z) {
        double dx = x - PLANET_X;
        double dy = y - PLANET_Y;
        double dz = z - PLANET_Z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Cheap "terrain" bumps so the surface isn't a perfect sphere.
        double bumps = Math.sin(x * 0.11) + Math.sin(y * 0.13) + Math.sin(z * 0.17);
        double radius = PLANET_RADIUS + bumps * 1.5;

        if (distance > radius) {
            return null;
        }
        if (distance > radius - 1.5) {
            // Surface shell: mostly grass, rocky "peaks", the odd glowing crystal.
            if (((x * 31 + y * 17 + z * 13) & 127) == 0) {
                return Blocks.GLOWSTONE.getDefaultState();
            }
            return bumps > 1.4 ? Blocks.STONE.getDefaultState() : Blocks.GRASS_BLOCK.getDefaultState();
        }
        if (distance > radius - 4.5) {
            return Blocks.DIRT.getDefaultState();
        }
        if (distance > 6.0) {
            return Blocks.STONE.getDefaultState();
        }
        return Blocks.GLOWSTONE.getDefaultState(); // molten(ish) core
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess,
                      StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public int getWorldHeight() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinimumY() {
        return 0;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(0, new BlockState[]{Blocks.AIR.getDefaultState()});
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
    }
}
