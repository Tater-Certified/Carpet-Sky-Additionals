package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.EntityIslandDataInterface;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
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
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityIslandDataInterface {

    @Shadow
    public abstract World getWorld();

    @Shadow
    private World world;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    protected abstract Optional<BlockLocating.Rectangle> getPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder);

    @Shadow
    protected BlockPos lastNetherPortalPosition;

    @Shadow
    protected abstract Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect);

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract float getYaw();

    @Shadow
    public abstract float getPitch();

    @Shadow
    @Nullable
    public abstract MinecraftServer getServer();

    @Override
    public SkyIslandWorld getCurrentIsland() {
        return SkyIslandUtils.getSkyIsland((ServerWorld) this.getWorld());
    }


    @Redirect(method = "tickPortal", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;NETHER:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectNetherRegistryKey() {
        return getCurrentIsland().getNether().getRegistryKey();
    }

    @Redirect(method = "tickPortal", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;OVERWORLD:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectOverworldRegistryKey() {
        return getCurrentIsland().getOverworld().getRegistryKey();
    }

    @Redirect(method = "tickPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"))
    private Entity moveToWorld(Entity instance, ServerWorld destination) {
        TeleportTarget target = netherTeleportTarget(destination, getCurrentIsland());
        return FabricDimensions.teleport(((Entity)(Object)this), destination, target);
    }

    @Unique
    private TeleportTarget netherTeleportTarget(ServerWorld destination, SkyIslandWorld current) {
        boolean isDestinationNether = destination == current.getNether();
        WorldBorder worldBorder = destination.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(this.world.getDimension(), destination.getDimension());
        BlockPos blockPos2 = worldBorder.clamp(this.getX() * d, this.getY(), this.getZ() * d);

        Optional<BlockLocating.Rectangle> portalRect = this.getPortalRect(destination, blockPos2, isDestinationNether, worldBorder);
        if (portalRect.isPresent()) {
            BlockState blockState = this.world.getBlockState(this.lastNetherPortalPosition);
            Direction.Axis axis = blockState.contains(Properties.HORIZONTAL_AXIS) ? blockState.get(Properties.HORIZONTAL_AXIS) : Direction.Axis.X;
            Vec3d vec3d = blockState.contains(Properties.HORIZONTAL_AXIS)
                    ? this.positionInPortal(axis, BlockLocating.getLargestRectangle(this.lastNetherPortalPosition, axis, 21, Direction.Axis.Y, 21, (pos) -> this.world.getBlockState(pos) == blockState))
                    : new Vec3d(0.5, 0.0, 0.0);

            return NetherPortal.getNetherTeleportTarget(destination, portalRect.get(), axis, vec3d, ((Entity) (Object) this), this.getVelocity(), this.getYaw(), this.getPitch());
        }

        return null;
    }
}
