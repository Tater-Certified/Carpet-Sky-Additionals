package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import com.github.tatercertified.carpetskyadditionals.offline_player_utils.OfflinePlayerUtils;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.*;

public class SkyIslandWorld {
    private String name;
    private int max_members;
    private UUID owner;
    private String owner_name;
    private RuntimeWorldHandle overworld_handle;
    private RuntimeWorldHandle nether_handle;
    private RuntimeWorldHandle end_handle;
    private List<UUID> members = new ArrayList<>();
    private final List<UUID> invite_requests = new ArrayList<>();
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
                .setShouldTickTime(true)
                .setGenerator(server.getOverworld().getChunkManager().getChunkGenerator());

        overworld_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, name), worldConfig);
        getOverworld().setSpawnPos(new BlockPos(8, 64, 9), 0.0F);

        // Nether

        RuntimeWorldConfig worldConfig1 = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_NETHER)
                .setDifficulty(Difficulty.HARD)
                .setSeed(seed)
                .setShouldTickTime(true)
                .setGenerator(Objects.requireNonNull(server.getWorld(World.NETHER)).getChunkManager().getChunkGenerator());

        nether_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, name + "-nether"), worldConfig1);

        // End

        RuntimeWorldConfig worldConfig2 = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_END)
                .setDifficulty(Difficulty.HARD)
                .setSeed(seed)
                .setShouldTickTime(true)
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

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return owner_name;
    }

    public void setOwnerName(String name) {
        this.owner_name = name;
    }

    public void setOwner(UUID owner) {
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

    public boolean tryAddMember(ServerPlayerEntity player, boolean creation) {
        if (this.members.size() < this.max_members) {
            if (creation) {
                acceptJoinRequest(player.getUuid(), true);
                SkyIslandUtils.teleportToIsland(player, this.getOverworld(), GameMode.SURVIVAL);
                player.sendMessage(Text.literal("Welcome to " + this.getName()));
            } else {
                addJoinRequest(player);
            }
            return true;
        }
        return false;
    }

    public void removeMember(ServerPlayerEntity removed) {
        this.members.remove(removed.getUuid());
    }

    private void setMembers(List<UUID> members_list) {
        this.members = members_list;
    }

    public Map<String, ServerPlayerEntity> getMembers() {
        Map<String, ServerPlayerEntity> map = new HashMap<>();
        for (UUID id : this.members) {
            Optional<ServerPlayerEntity> player = OfflinePlayerUtils.getPlayer(this.server, id);
            player.ifPresent(serverPlayerEntity -> map.put(serverPlayerEntity.getName().getString(), serverPlayerEntity));
        }
        return map;
    }

    public List<ServerPlayerEntity> getMembersLowRamConsumption() {
        List<ServerPlayerEntity> list = new ArrayList<>();
        for (UUID id : this.members) {
            Optional<ServerPlayerEntity> player = OfflinePlayerUtils.getPlayer(this.server, id);
            player.ifPresent(list::add);
        }
        return list;
    }

    public void addJoinRequest(ServerPlayerEntity requester) {
        UUID id = requester.getUuid();
        if (this.invite_requests.contains(id)){
            requester.sendMessage(Text.literal("You have already requested to join this server!"));
        } else {
            this.invite_requests.add(requester.getUuid());
            Optional<ServerPlayerEntity> owner = OfflinePlayerUtils.getPlayer(this.server, this.getOwner());

            if (owner.isPresent() && OfflinePlayerUtils.isPlayerOnline(owner.get(), this.server)) {
                ServerPlayerEntity owner_player = owner.get();
                owner_player.sendMessage(Text.literal(requester.getName().getString() + " has requested to join " + getName()));
            }
        }
    }

    public void rejectJoinRequest(UUID request) {
        this.invite_requests.remove(request);
    }

    public void acceptJoinRequest(UUID request, boolean creation) {
        this.invite_requests.remove(request);
        this.members.add(request);

        Optional<ServerPlayerEntity> requester = OfflinePlayerUtils.getPlayer(this.server, request);
        if (requester.isPresent()) {
            ServerPlayerEntity player = requester.get();
            if (OfflinePlayerUtils.isPlayerOnline(requester.get(), this.server)) {
                ((PlayerIslandDataInterface)player).addHomeIsland(this);
                if (!creation) {
                    player.sendMessage(Text.literal("Your request to " + this.getName() + " has been accepted"));
                }
            } else {
                NbtCompound player_data = OfflinePlayerUtils.getPlayerData(this.server, player);
                List<PlayerSkyIslandWorld> p_islands = OfflinePlayerUtils.readPlayerIslandsNbt(player_data);
                p_islands.add(new PlayerSkyIslandWorld(this));
                OfflinePlayerUtils.writePlayerIslandNbt(p_islands, player_data);
                OfflinePlayerUtils.savePlayerData(player, player_data, this.server);
            }
        }
    }

    public Map<String, ServerPlayerEntity> getRequests() {
        Map<String, ServerPlayerEntity> requests = new HashMap<>();

        for (UUID id : this.invite_requests) {
            Optional<ServerPlayerEntity> player = OfflinePlayerUtils.getPlayer(this.server, id);
            player.ifPresent(serverPlayerEntity -> requests.put(id.toString(), serverPlayerEntity));
        }
        return requests;
    }

    public void remove() {
        overworld_handle.delete();
        nether_handle.delete();
        end_handle.delete();
    }

    public NbtCompound getNBT() {
        NbtCompound compound = new NbtCompound();
        NbtInt max_members = NbtInt.of(getMaxMembers());
        NbtString owner = NbtString.of(getOwner().toString());
        NbtString owner_name = NbtString.of(getOwnerName());
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
        compound.put("owner-name", owner_name);
        compound.put("members", members_nbt);
        dragon_fight = getEnd().getEnderDragonFight().toNbt();
        compound.put("dragon_fight", dragon_fight);

        return compound;
    }

    public void loadNBT(NbtCompound compound) {
        setOwner(UUID.fromString(compound.getString("owner")));
        setOwnerName(compound.getString("owner-name"));

        List<UUID> players = new ArrayList<>();
        NbtList member_list = compound.getList("members", NbtElement.STRING_TYPE);
        for (int i = 0; i < member_list.size(); i++) {
            players.add(UUID.fromString(member_list.getString(i)));
        }

        setMembers(players);
    }
}
