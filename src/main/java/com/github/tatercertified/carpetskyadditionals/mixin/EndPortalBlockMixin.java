package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.EntityIslandDataInterface;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    /**
     * @author QPCrummer
     * @reason This will do for now
     */
    @Overwrite
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world instanceof ServerWorld && entity.canUsePortals() && VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), state.getOutlineShape(world, pos), BooleanBiFunction.AND)) {

            ServerWorld serverWorld;

            SkyIslandWorld current = ((EntityIslandDataInterface)entity).getCurrentIsland();
            // TODO TESTING
            if (entity instanceof ServerPlayerEntity) {
                entity.sendMessage(Text.literal("The name of the Island is: " + current.getName()));
            }
            // TODO TESTING
            if (world.getDimension() == current.getEnd().getDimension()) {
                serverWorld = current.getOverworld();
            } else {
                serverWorld = current.getEnd();
            }
            if (serverWorld == null) {
                return;
            }

            serverWorld.getChunk(pos);
            entity.moveToWorld(serverWorld);
        }
    }
}
