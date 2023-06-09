package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import com.github.tatercertified.carpetskyadditionals.carpet.CarpetSkyAdditionalsSettings;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import com.github.tatercertified.carpetskyadditionals.mixin.EnderDragonFightInvoker;
import com.github.tatercertified.carpetskyadditionals.offline_player_utils.OfflinePlayerUtils;
import com.github.tatercertified.carpetskyadditionals.util.DragonNbtConverter;
import com.github.tatercertified.carpetskyadditionals.util.VanillaRuntimeWorld;
import com.github.tatercertified.carpetskyadditionals.util.WanderingTraderManagerV2;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.ZombieSiegeManager;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.spawner.CatSpawner;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.spawner.PhantomSpawner;
import xyz.nucleoid.fantasy.*;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.VoidWorldProgressListener;

import java.util.*;

public class SkyIslandWorld implements EnderDragonFightInvoker{
    private final long identification;
    private final int data_version;
    private String name;
    private UUID owner;
    private String owner_name;
    private RuntimeWorldHandle overworld_handle;
    private RuntimeWorldHandle nether_handle;
    private RuntimeWorldHandle end_handle;
    private List<UUID> members = new ArrayList<>();
    private final List<UUID> invite_requests = new ArrayList<>();
    private final MinecraftServer server;
    private final long seed;
    private final EnderDragonFight.Data dragon_fight;
    private final WanderingTraderManagerV2 traderManager;

    public SkyIslandWorld(int data_version, long id, String name, MinecraftServer server, Fantasy fantasy, long seed, EnderDragonFight.Data dragon_fight, NbtCompound wanderingTrader) {
        this.data_version = data_version;
        this.identification = id;
        this.name = name;
        SkyIslandUtils.addToConverter(this);
        this.server = server;
        this.dragon_fight = dragon_fight;
        this.seed = seed;
        this.traderManager = new WanderingTraderManagerV2(wanderingTrader);
        createAllWorlds(server, fantasy);
    }
    public void createAllWorlds(MinecraftServer server, Fantasy fantasy) {
        // Overworld

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setDifficulty(Difficulty.HARD)
                .setSeed(seed)
                .setShouldTickTime(true)
                .setSunny(-1)
                .setGameRule(GameRules.DO_WEATHER_CYCLE, true)
                .setGameRule(GameRules.DO_INSOMNIA, true)
                .setGameRule(GameRules.DO_TRADER_SPAWNING, true)
                .setGameRule(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 0)
                .setWorldConstructor((server1, registryKey, config, style) -> new VanillaRuntimeWorld(
                        server1,
                        Util.getMainWorkerExecutor(),
                        ((MinecraftServerAccess) server1).getSession(),
                        new RuntimeWorldProperties(server1.getSaveProperties(), config),
                        registryKey,
                        config.createDimensionOptions(server1),
                        VoidWorldProgressListener.INSTANCE,
                        false,
                        BiomeAccess.hashSeed(config.getSeed()),
                        ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new ZombieSiegeManager(), traderManager),
                        config.shouldTickTime(),
                        null, style))
                .setGenerator(server.getOverworld().getChunkManager().getChunkGenerator());

        overworld_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, Long.toHexString(identification)), worldConfig);
        getOverworld().setSpawnPos(new BlockPos(8, 64, 9), 0.0F);

        // Nether

        RuntimeWorldConfig worldConfig1 = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_NETHER)
                .setDifficulty(Difficulty.HARD)
                .setSeed(seed)
                .setShouldTickTime(true)
                .setWorldConstructor((server1, registryKey, config, style) -> new VanillaRuntimeWorld(
                        server1,
                        Util.getMainWorkerExecutor(),
                        ((MinecraftServerAccess) server1).getSession(),
                        new RuntimeWorldProperties(server1.getSaveProperties(), config),
                        registryKey,
                        config.createDimensionOptions(server1),
                        VoidWorldProgressListener.INSTANCE,
                        false,
                        BiomeAccess.hashSeed(config.getSeed()),
                        ImmutableList.of(new ZombieSiegeManager()),
                        config.shouldTickTime(),
                        null, style))
                .setGenerator(Objects.requireNonNull(server.getWorld(World.NETHER)).getChunkManager().getChunkGenerator());

        nether_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, Long.toHexString(identification) + "-nether"), worldConfig1);

        // End

        RuntimeWorldConfig worldConfig2 = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_END)
                .setDifficulty(Difficulty.HARD)
                .setSeed(seed)
                .setShouldTickTime(true)
                .setWorldConstructor((server1, registryKey, config, style) -> new VanillaRuntimeWorld(
                        server1,
                        Util.getMainWorkerExecutor(),
                        ((MinecraftServerAccess) server1).getSession(),
                        new RuntimeWorldProperties(server1.getSaveProperties(), config),
                        registryKey,
                        config.createDimensionOptions(server1),
                        VoidWorldProgressListener.INSTANCE,
                        false,
                        BiomeAccess.hashSeed(config.getSeed()),
                        ImmutableList.of(new ZombieSiegeManager()),
                        config.shouldTickTime(),
                        null, style))
                .setGenerator(server.getWorld(World.END).getChunkManager().getChunkGenerator());

        end_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, Long.toHexString(identification) + "-end"), worldConfig2);
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

    public long getIdentification() {
        return this.identification;
    }

    private int getDataVersion() {
        return this.data_version;
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

    public MinecraftServer getServer() {
        return this.server;
    }

    public long getSeed() {
        return seed;
    }

    public EnderDragonFight.Data getDragonFight() {
        return dragon_fight;
    }

    public boolean tryAddMember(ServerPlayerEntity player, boolean creation) {
        if (this.members.size() < CarpetSkyAdditionalsSettings.maxPlayersPerIsland) {
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

    public void forceAddMember(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        this.invite_requests.remove(id);
        this.members.add(id);
    }

    public void removeMember(ServerPlayerEntity removed) {
        this.members.remove(removed.getUuid());
    }

    private void setMembers(List<UUID> members_list) {
        this.members = members_list;
    }

    public List<UUID> getMembersUUID() {
        return members;
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

    public void generateEndPillars() {
        invokeAreChunksLoaded();

        List<EndSpikeFeature.Spike> list = EndSpikeFeature.getSpikes(this.getEnd());
        for (EndSpikeFeature.Spike spike : list) {

            for (BlockPos blockPos : BlockPos.iterate(new BlockPos(spike.getCenterX() - 10, spike.getHeight() - 10, spike.getCenterZ() - 10), new BlockPos(spike.getCenterX() + 10, spike.getHeight() + 10, spike.getCenterZ() + 10))) {
                this.getEnd().removeBlock(blockPos, false);
            }

            EndSpikeFeatureConfig endSpikeFeatureConfig = new EndSpikeFeatureConfig(true, ImmutableList.of(spike), null);
            Feature.END_SPIKE.generateIfValid(endSpikeFeatureConfig, this.getEnd(), this.getEnd().getChunkManager().getChunkGenerator(), Random.create(), new BlockPos(spike.getCenterX(), 45, spike.getCenterZ()));
        }
    }

    public void remove() {
        overworld_handle.delete();
        nether_handle.delete();
        end_handle.delete();
    }


    public NbtCompound getNBT() {

        NbtCompound compound = new NbtCompound();
        NbtInt data_ver = NbtInt.of(getDataVersion());
        NbtLong id = NbtLong.of(getIdentification());
        NbtString owner = NbtString.of(getOwner().toString());
        NbtString owner_name = NbtString.of(getOwnerName());
        NbtString name = NbtString.of(getName());
        NbtLong seed = NbtLong.of(getSeed());
        NbtList members_nbt = new NbtList();

        for (UUID member : members) {
            NbtString member_id = NbtString.of(member.toString());
            members_nbt.add(member_id);
        }

        compound.put("id", id);
        compound.put("data_version", data_ver);
        compound.put("name", name);
        compound.put("seed", seed);
        compound.put("owner", owner);
        compound.put("owner-name", owner_name);
        compound.put("members", members_nbt);
        compound.put("dragon_fight", DragonNbtConverter.toNBT(getEnd().getEnderDragonFight().toData()));
        compound.put("trader", traderManager.toNBT());

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

    @Override
    public boolean invokeAreChunksLoaded() {
        return true;
    }
}
