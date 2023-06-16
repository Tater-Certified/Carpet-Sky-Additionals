package com.github.tatercertified.carpetskyadditionals.dimensions;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlayerSkyIslandWorld {
    private final SkyIslandWorld representation;
    private BlockPos spawn_pos;
    private RegistryKey<World> spawn_dimension;

    public PlayerSkyIslandWorld(SkyIslandWorld island) {
        this.representation = island;
        this.spawn_dimension = island.getOverworld().getRegistryKey();
    }

    public SkyIslandWorld getSkyIslandWorld() {
        return this.representation;
    }
    public BlockPos getSpawnPos() {
        return spawn_pos;
    }

    public RegistryKey<World> getSpawnDimension() {
        return spawn_dimension;
    }

    public void setSpawnPos(BlockPos pos, RegistryKey<World> dimension) {
        this.spawn_pos = pos;
        this.spawn_dimension = dimension;
    }

    public void removeSpawnPos() {
        this.spawn_pos = null;
        this.spawn_dimension = representation.getOverworld().getRegistryKey();
    }

    public void fromNbt(NbtCompound compound) {
        if (compound.contains("spawn-pos") && compound.contains("spawn-dimension")) {
            NbtCompound blockpos = compound.getCompound("spawn-pos");
            RegistryKey<World> world = SkyIslandUtils.getDimensionFromString(compound.getString("spawn-dimension"));
            setSpawnPos(new BlockPos(blockpos.getInt("x"), blockpos.getInt("y"), blockpos.getInt("z")), world);
        }
    }

    public NbtCompound toNbt() {
        NbtCompound compound = new NbtCompound();
        NbtCompound blockpos = new NbtCompound();

        if (this.spawn_pos != null) {
            blockpos.putInt("x", this.spawn_pos.getX());
            blockpos.putInt("y", this.spawn_pos.getY());
            blockpos.putInt("z", this.spawn_pos.getZ());
        }

        compound.put("spawn-pos", blockpos);

        compound.putString("spawn-dimension", this.spawn_dimension.getValue().getPath());

        compound.putLong("island-id", this.representation.getIdentification());

        return compound;
    }
}
