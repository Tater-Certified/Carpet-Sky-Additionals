package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.PlayerSkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.EntityIslandDataInterface;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import com.github.tatercertified.carpetskyadditionals.offline_player_utils.OfflinePlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements PlayerIslandDataInterface {
    @Shadow @Final public MinecraftServer server;

    @Shadow public abstract void sendMessage(Text message);

    @Shadow private float spawnAngle;
    @Shadow private boolean spawnForced;
    private List<PlayerSkyIslandWorld> homes = new ArrayList<>();
    private final ThreadLocal<PlayerSkyIslandWorld> p_island = new ThreadLocal<>();

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    public void readNBT(NbtCompound nbt, CallbackInfo ci) {
        this.homes = OfflinePlayerUtils.readPlayerIslandsNbt(nbt);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void writeNBT(NbtCompound nbt, CallbackInfo ci) {
        OfflinePlayerUtils.writePlayerIslandNbt(this.homes, nbt);
    }

    /**
     * @author QPCrummer
     * @reason This won't cause much damage
     */
    @Overwrite
    public RegistryKey<World> getSpawnPointDimension() {
        SkyIslandWorld current = ((EntityIslandDataInterface) this).getCurrentIsland();
        PlayerSkyIslandWorld p_island = getPIsland(current);
        if (p_island == null) {
            return World.OVERWORLD;
        }
        return p_island.getSpawnDimension();
    }

    /**
     * @author QPCrummer
     * @reason Same reason as above
     */
    @Overwrite
    public @Nullable BlockPos getSpawnPointPosition() {
        SkyIslandWorld current = ((EntityIslandDataInterface) this).getCurrentIsland();
        PlayerSkyIslandWorld p_island = getPIsland(current);
        if (p_island == null || p_island.getSpawnPos() == null) {
            return new BlockPos(8, 64, 9);
        }
        Optional<Vec3d> optional = PlayerEntity.findRespawnPosition(this.server.getWorld(p_island.getSpawnDimension()), p_island.getSpawnPos(), this.spawnAngle, this.spawnForced, true);
        BlockPos pos;

        if (optional.isPresent()) {
            Vec3d vec3d = optional.get();
            pos = new BlockPos(Math.toIntExact(Math.round(vec3d.x)), Math.toIntExact(Math.round(vec3d.y)), Math.toIntExact(Math.round(vec3d.z)));
        } else {
            pos = new BlockPos(8, 64, 9);
        }
        return pos;
    }

    @Inject(method = "setSpawnPoint", at = @At("HEAD"), cancellable = true)
    private void setSpawnPoint(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        SkyIslandWorld current = ((EntityIslandDataInterface) this).getCurrentIsland();
        p_island.set(getPIsland(current));

        if (p_island.get() == null) {
            this.sendMessage(Text.literal("You cannot set your spawn on this Island!"));
            ci.cancel();
        }
    }

    @ModifyArg(method = "setSpawnPoint", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;equals(Ljava/lang/Object;)Z"))
    private Object modifySpawnPos(Object par1) {
        return p_island.get().getSpawnPos();
    }

    @ModifyArg(method = "setSpawnPoint", at = @At(value = "INVOKE", target = "Ljava/lang/Object;equals(Ljava/lang/Object;)Z"))
    private Object modifySpawnDimension(Object obj) {
        return p_island.get().getSpawnDimension();
    }

    @Inject(method = "setSpawnPoint", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;spawnPointPosition:Lnet/minecraft/util/math/BlockPos;", ordinal = 1, shift = At.Shift.BEFORE), cancellable = true)
    private void setSpawnPos(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        p_island.get().setSpawnPos(pos, dimension);
        this.spawnAngle = angle;
        this.spawnForced = forced;
        ci.cancel();
    }

    @Inject(method = "setSpawnPoint", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;spawnPointPosition:Lnet/minecraft/util/math/BlockPos;", ordinal = 2, shift = At.Shift.BEFORE), cancellable = true)
    private void removeSpawnPos(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        p_island.get().removeSpawnPos();
        this.spawnAngle = 0.0F;
        this.spawnForced = false;
        ci.cancel();
    }
    @Override
    public PlayerSkyIslandWorld getPIsland(ServerWorld world) {
        SkyIslandWorld island = SkyIslandUtils.getSkyIsland(world);
        return getPIsland(island);
    }

    @Override
    public List<SkyIslandWorld> getHomeIslands() {
        List<SkyIslandWorld> islands = new ArrayList<>();
        for (PlayerSkyIslandWorld p_island : homes) {
            islands.add(p_island.getSkyIslandWorld());
        }
        return islands;
    }

    @Override
    public void addHomeIsland(SkyIslandWorld island) {
        homes.add(new PlayerSkyIslandWorld(island));
    }

    @Override
    public void removeHomeIsland(SkyIslandWorld island) {
        homes.remove(getPIsland(island));
    }

    @Override
    public List<PlayerSkyIslandWorld> getPlayerIslands() {
        return homes;
    }

    @Override
    public void setPlayerIslands(List<PlayerSkyIslandWorld> islands) {
        this.homes = islands;
    }

    @Override
    public PlayerSkyIslandWorld getPIsland(SkyIslandWorld island) {
        for (PlayerSkyIslandWorld p_island : homes) {
            if (p_island.getSkyIslandWorld() == island) {
                return p_island;
            }
        }
        return null;
    }

}
