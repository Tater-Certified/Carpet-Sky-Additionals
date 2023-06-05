package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SkyIslandWorld {
    private String name;
    private int max_members;
    private String owner;
    private RuntimeWorldHandle overworld_handle;
    private RuntimeWorldHandle nether_handle;
    private RuntimeWorldHandle end_handle;
    private List<UUID> members = new ArrayList<>();
    private final MinecraftServer server;
    private final long seed;
    private NbtCompound dragon_fight;

    public SkyIslandWorld(String name, int max_members, MinecraftServer server, Fantasy fantasy, long seed, NbtCompound dragon_fight) {
        this.name = name;
        this.max_members = max_members;
        this.server = server;
        this.dragon_fight = dragon_fight;
        this.seed = seed;
        createAllWorlds(server, fantasy);
    }
    public void createAllWorlds(MinecraftServer server, Fantasy fantasy) {
        // Overworld

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setDifficulty(Difficulty.HARD)
                .setSeed(seed)
                .setGenerator(server.getOverworld().getChunkManager().getChunkGenerator());

        overworld_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, name), worldConfig);
        getOverworld().setSpawnPos(new BlockPos(8, 64, 9), 0.0F);

        // Nether

        RuntimeWorldConfig worldConfig1 = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_NETHER)
                .setDifficulty(Difficulty.HARD)
                .setSeed(seed)
                .setGenerator(Objects.requireNonNull(server.getWorld(World.NETHER)).getChunkManager().getChunkGenerator());

        nether_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, name + "-nether"), worldConfig1);

        // End

        RuntimeWorldConfig worldConfig2 = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_END)
                .setDifficulty(Difficulty.HARD)
                .setSeed(seed)
                .setGenerator(server.getWorld(World.END).getChunkManager().getChunkGenerator());

        end_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, name + "-end"), worldConfig2);
        
    }

    public ServerWorld getOverworld() {
        return overworld_handle.asWorld();
    }

    public ServerWorld getNether() {
        return nether_handle.asWorld();
    }

    public ServerWorld getEnd() {
        return end_handle.asWorld();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getMaxMembers() {
        return max_members;
    }

    public void setMaxMembers(int new_max) {
        this.max_members = new_max;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public long getSeed() {
        return seed;
    }

    public NbtCompound getDragonFight() {
        return dragon_fight;
    }

    public void setDragonFight(NbtCompound data) {
        this.dragon_fight = data;
    }

    public boolean tryAddMember(ServerPlayerEntity player) {
        if (members.size() < max_members) {
            members.add(player.getUuid());
            return true;
        }
        return false;
    }

    public void removeMember(ServerPlayerEntity removed) {
        members.remove(removed.getUuid());
    }

    private void setMembers(List<UUID> members_list) {
        this.members = members_list;
    }

    public List<ServerPlayerEntity> getMembers() {
        List<ServerPlayerEntity> list = new ArrayList<>();
        for (UUID id : members) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(id);
            if (server.getUserCache().getByUuid(id).isPresent() && player == null) {
                player = server.getPlayerManager().createPlayer(server.getUserCache().getByUuid(id).get());
            }
            list.add(player);
        }
        return list;
    }

    public void remove() {
        overworld_handle.delete();
        nether_handle.delete();
        end_handle.delete();
    }

    public NbtCompound getNBT() {
        NbtCompound compound = new NbtCompound();
        NbtInt max_members = NbtInt.of(getMaxMembers());
        NbtString owner = NbtString.of(getOwner());
        NbtString name = NbtString.of(getName());
        NbtLong seed = NbtLong.of(getSeed());
        NbtList members_nbt = new NbtList();

        for (UUID member : members) {
            NbtString member_id = NbtString.of(member.toString());
            members_nbt.add(member_id);
        }

        compound.put("name", name);
        compound.put("seed", seed);
        compound.put("max_members", max_members);
        compound.put("owner", owner);
        compound.put("members", members_nbt);
        dragon_fight = getEnd().getEnderDragonFight().toNbt();
        compound.put("dragon_fight", dragon_fight);

        return compound;
    }

    public void loadNBT(NbtCompound compound) {
        setOwner(compound.getString("owner"));

        List<UUID> players = new ArrayList<>();
        NbtList member_list = compound.getList("members", NbtElement.STRING_TYPE);
        for (int i = 0; i < member_list.size(); i++) {
            players.add(UUID.fromString(member_list.getString(i)));
        }

        setMembers(players);
    }
}
