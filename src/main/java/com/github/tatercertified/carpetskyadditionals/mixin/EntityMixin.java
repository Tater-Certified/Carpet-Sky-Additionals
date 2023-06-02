package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.EntityIslandDataInterface;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.NetherPortal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityIslandDataInterface {

    @Shadow public abstract World getWorld();

    @Shadow public World world;

    @Shadow protected boolean inNetherPortal;

    @Shadow public abstract boolean hasVehicle();

    @Shadow protected int netherPortalTime;

    @Shadow public abstract void resetPortalCooldown();

    @Shadow public abstract int getMaxNetherPortalTime();

    @Shadow protected abstract void tickPortalCooldown();

    @Shadow public abstract double getX();

    @Shadow public abstract double getY();

    @Shadow public abstract double getZ();

    @Shadow protected abstract Optional<BlockLocating.Rectangle> getPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder);

    @Shadow protected BlockPos lastNetherPortalPosition;

    @Shadow protected abstract Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect);

    @Shadow public abstract Vec3d getVelocity();

    @Shadow public abstract float getYaw();

    @Shadow public abstract float getPitch();

    @Override
    public SkyIslandWorld getCurrentIsland() {
        return SkyIslandUtils.getSkyIsland((ServerWorld) this.getWorld());
    }

    /**
     * @author QPCrummer
     * @reason Temporary until I find a better solution
     */
    @Overwrite
    public void tickPortal() {
        if (this.world instanceof ServerWorld serverWorld) {
            int i = this.getMaxNetherPortalTime();
            SkyIslandWorld current = getCurrentIsland();

            if (this.inNetherPortal) {
                MinecraftServer minecraftServer = serverWorld.getServer();
                ServerWorld serverWorld2;
                if (serverWorld == current.getOverworld()) {
                    serverWorld2 = current.getNether();
                } else {
                    serverWorld2 = current.getOverworld();
                }

                if (serverWorld2 != null && minecraftServer.isNetherAllowed() && !this.hasVehicle() && this.netherPortalTime++ >= i) {
                    this.world.getProfiler().push("portal");
                    this.netherPortalTime = i;
                    this.resetPortalCooldown();
                    TeleportTarget target = netherTeleportTarget(serverWorld2, current);
                    FabricDimensions.teleport(((Entity)(Object)this), serverWorld2, target);
                    this.world.getProfiler().pop();
                }

                this.inNetherPortal = false;
            } else {
                if (this.netherPortalTime > 0) {
                    this.netherPortalTime -= 4;
                }

                if (this.netherPortalTime < 0) {
                    this.netherPortalTime = 0;
                }
            }

            this.tickPortalCooldown();
        }
    }

    private TeleportTarget netherTeleportTarget(ServerWorld destination, SkyIslandWorld current) {
        boolean isDestinationNether = destination == current.getNether();
        WorldBorder worldBorder = destination.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(this.world.getDimension(), destination.getDimension());
        BlockPos blockPos2 = worldBorder.clamp(this.getX() * d, this.getY(), this.getZ() * d);
        return this.getPortalRect(destination, blockPos2, isDestinationNether, worldBorder).map((rect) -> {
            BlockState blockState = this.world.getBlockState(this.lastNetherPortalPosition);
            Direction.Axis axis;
            Vec3d vec3d;
            if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                axis = blockState.get(Properties.HORIZONTAL_AXIS);
                BlockLocating.Rectangle rectangle = BlockLocating.getLargestRectangle(this.lastNetherPortalPosition, axis, 21, Direction.Axis.Y, 21, (pos) -> this.world.getBlockState(pos) == blockState);
                vec3d = this.positionInPortal(axis, rectangle);
            } else {
                axis = Direction.Axis.X;
                vec3d = new Vec3d(0.5, 0.0, 0.0);
            }

            return NetherPortal.getNetherTeleportTarget(destination, rect, axis, vec3d, ((Entity)(Object)this), this.getVelocity(), this.getYaw(), this.getPitch());
        }).orElse(null);
    }
}
