package com.tinblue.spacemod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * The station's gravity generator. Cosmetic in this proof of concept:
 * it glows and hums out particles so the station has a heart.
 */
public class GravityGeneratorBlock extends Block {
    public GravityGeneratorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.6;
            double y = pos.getY() + 1.0 + random.nextDouble() * 0.6;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.6;
            world.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.03, 0.0);
        }
    }
}
