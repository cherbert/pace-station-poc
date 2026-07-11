package com.tinblue.spacemod.world;

import com.tinblue.spacemod.SpaceMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

/**
 * The space station as a pure function of coordinates, so the chunk generator
 * can build it across chunk borders without any bookkeeping.
 *
 * Layout: a 13x13 glass-domed main room centred on (0, 100, 0) with the gravity
 * generator in the middle, a short catwalk east, and an open docking pad where
 * the ship waits.
 */
public final class StationBuilder {
    private StationBuilder() {
    }

    public static BlockState blockAt(int x, int y, int z) {
        int ax = Math.abs(x);
        int az = Math.abs(z);

        // The gravity generator, heart of the station.
        if (x == 0 && z == 0 && y == 101) {
            return SpaceMod.GRAVITY_GENERATOR.getDefaultState();
        }

        // Main room: 13x13 footprint, floor at y=100, glass walls and roof.
        if (ax <= 6 && az <= 6) {
            boolean edge = ax == 6 || az == 6;
            if (y == 100) {
                if (ax == 4 && az == 4) {
                    return Blocks.SEA_LANTERN.getDefaultState(); // floor lights
                }
                return edge ? Blocks.IRON_BLOCK.getDefaultState() : Blocks.SMOOTH_QUARTZ.getDefaultState();
            }
            if (y >= 101 && y <= 104 && edge) {
                if (x == 6 && az <= 1 && y <= 103) {
                    return null; // doorway out to the docking catwalk
                }
                if (ax == 6 && az == 6) {
                    return Blocks.IRON_BLOCK.getDefaultState(); // corner pillars
                }
                return Blocks.GLASS.getDefaultState();
            }
            if (y == 105) {
                return edge ? Blocks.IRON_BLOCK.getDefaultState() : Blocks.GLASS.getDefaultState();
            }
            return null;
        }

        // Catwalk from the doorway to the pad.
        if (x >= 7 && x <= 10 && az <= 1 && y == 100) {
            return Blocks.IRON_BLOCK.getDefaultState();
        }

        // Open docking pad.
        if (x >= 11 && x <= 17 && az <= 3 && y == 100) {
            if ((x == 11 || x == 17) && az == 3) {
                return Blocks.SEA_LANTERN.getDefaultState(); // pad corner beacons
            }
            return Blocks.IRON_BLOCK.getDefaultState();
        }

        return null;
    }
}
