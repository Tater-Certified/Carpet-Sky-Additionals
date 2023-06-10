package com.github.tatercertified.carpetskyadditionals.mixin.rules;

import com.github.tatercertified.carpetskyadditionals.carpet.CarpetSkyAdditionalsSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class sneakingGrowsPlants {


    @Shadow @Final protected Random random;

    @Shadow public World world;

    @Shadow public abstract int getBlockX();

    @Shadow public abstract int getBlockZ();

    @Shadow public abstract int getBlockY();

    // Carpet Rule sneakingGrowsPlants
    @Inject(method = "setSneaking", at = @At("TAIL"))
    private void setSneaking(boolean sneaking, CallbackInfo ci) {
        if (CarpetSkyAdditionalsSettings.sneakingGrowsPlants && sneaking && this.random.nextInt(CarpetSkyAdditionalsSettings.sneakGrowingProbability) == 0) {
            BlockPos pos1 = getRandomBlockPosInSquare();
            BlockState blockState = this.world.getBlockState(pos1);
            if (blockState.getBlock() instanceof Fertilizable fertilizable) {
                if (fertilizable.canGrow(this.world, this.random, pos1, blockState)) {
                    fertilizable.grow((ServerWorld) this.world, this.random, pos1, blockState);
                    simulateBonemealParticles(pos1);
                }
            }
        }
    }

    private BlockPos getRandomBlockPosInSquare() {
        int radius = CarpetSkyAdditionalsSettings.sneakGrowingRadius;

        int offsetX = getRandomOffset(radius);
        int offsetZ = getRandomOffset(radius);

        int blockX = this.getBlockX() + offsetX;
        int blockZ = this.getBlockZ() + offsetZ;
        int blockY = this.world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);

        int yCheck = MathHelper.abs(this.getBlockY() - blockY);

        if (yCheck > 3) {
            blockY = this.getBlockY();
        }

        return new BlockPos(blockX, blockY, blockZ);
    }

    private int getRandomOffset(int radius) {
        return this.random.nextInt(radius * 2 + 1) - radius;
    }

    private void simulateBonemealParticles(BlockPos pos) {

        BlockState blockState = this.world.getBlockState(pos);
        double e = blockState.getOutlineShape(this.world, pos).getMax(Direction.Axis.Y);

        for(int i = 0; i < 15; ++i) {
            double f = this.random.nextGaussian() * 0.02;
            double g = this.random.nextGaussian() * 0.02;
            double h = this.random.nextGaussian() * 0.02;
            double k = pos.getX() + this.random.nextDouble();
            double l = pos.getY() + this.random.nextDouble() * e;
            double m = pos.getZ() + this.random.nextDouble();

            ((ServerWorld) this.world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, k, l, m, 1, f, g, h, this.random.nextDouble());
        }
    }
}
