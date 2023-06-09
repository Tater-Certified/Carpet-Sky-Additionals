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
        SkyIslandWorld current = ((EntityIslandDataInterface)(Object)this).getCurrentIsland();
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
        SkyIslandWorld current = ((EntityIslandDataInterface)(Object)this).getCurrentIsland();
        PlayerSkyIslandWorld p_island = getPIsland(current);
        if (p_island == null) {
            return null;
        }
        Optional<Vec3d> optional = PlayerEntity.findRespawnPosition(current.getServer().getWorld(p_island.getSpawnDimension()), p_island.getSpawnPos(), this.spawnAngle, this.spawnForced, true);
        BlockPos pos;
        pos = optional.map(vec3d -> new BlockPos(Math.toIntExact(Math.round(vec3d.x)), Math.toIntExact(Math.round(vec3d.y)), Math.toIntExact(Math.round(vec3d.z)))).orElseGet(p_island::getSpawnPos);
        return pos;
    }

    /**
     * @author QPCrummer
     * @reason Same reason as above the one above
     */
    @Overwrite
    public void setSpawnPoint(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage) {
        SkyIslandWorld current = ((EntityIslandDataInterface)(Object)this).getCurrentIsland();
        PlayerSkyIslandWorld p_island = getPIsland(current);

        if (p_island == null) {
            this.sendMessage(Text.literal("You cannot set your spawn on this Island!"));
            return;
        }

        if (pos != null) {

            boolean bl = pos.equals(p_island.getSpawnPos()) && dimension.equals(p_island.getSpawnDimension());
            if (sendMessage && !bl) {
                this.sendMessage(Text.translatable("block.minecraft.set_spawn"));
            }

            p_island.setSpawnPos(pos, dimension);

            this.spawnAngle = angle;
            this.spawnForced = forced;

        } else {
            p_island.removeSpawnPos();

            this.spawnAngle = 0.0F;
            this.spawnForced = false;
        }
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
