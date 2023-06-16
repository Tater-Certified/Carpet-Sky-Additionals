package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.carpet.CarpetSkyAdditionalsSettings;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import com.github.tatercertified.carpetskyadditionals.mixin.SkyIslandCommandInvoker;
import com.github.tatercertified.carpetskyadditionals.offline_player_utils.OfflinePlayerUtils;
import com.github.tatercertified.carpetskyadditionals.util.IslandNotCorrupterUpper;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import xyz.nucleoid.fantasy.Fantasy;

import java.util.List;
import java.util.Map;

public class SkyIslandManager {

    public static long the_counter;
    public static Fantasy fantasy;
    public static void loadIslands(MinecraftServer server, NbtList nbt) {
        fantasy = Fantasy.get(server);

        new VanillaWorldOverride(IslandNotCorrupterUpper.getDataVersion(), -1, "overworld", server, fantasy, server.getOverworld().getSeed(), new NbtCompound());

        if (nbt != null) {
            for (int i = 0; i < nbt.size(); i++) {
                NbtCompound compound = nbt.getCompound(i);

                SkyIslandWorld island = IslandNotCorrupterUpper.runIslandNotCorrupterUpper(compound, server, fantasy);
                island.loadNBT(compound);
            }
        }
        if (IslandNotCorrupterUpper.should_update_player_data) {
            IslandNotCorrupterUpper.migratePlayerData(server);
        }
    }

    public static void createIsland(String name, MinecraftServer server, ServerPlayerEntity creator) {
        SkyIslandWorld world = new SkyIslandWorld(IslandNotCorrupterUpper.getDataVersion(), updateCounter(server), name, server, fantasy, server.getOverworld().getRandom().nextLong(), new NbtCompound());
        world.setOwner(creator.getUuid());
        world.setOwnerName(creator.getName().getString());
        saveIslands(server);
        generateIslandStructure(world, server);
        world.generateEndPillars();
        BlockPos pos = new BlockPos(8, 63, 9);
        world.getOverworld().setBlockState(pos, Blocks.BEDROCK.getDefaultState());
        world.getOverworld().setSpawnPos(pos, 0.0F);
        joinIsland(world, creator, true);
    }

    public static void removeIsland(SkyIslandWorld island) {
        removeAllPlayersFromIsland(island);
        SkyIslandUtils.removeFromConverter(island);
        saveIslands(island.getServer());
        island.remove();
    }

    public static long updateCounter(MinecraftServer server) {
        the_counter++;
        IslandPersistentState state = IslandPersistentState.getServerState(server);
        state.counter = the_counter;
        state.markDirty();
        return the_counter;
    }

    public static void renameIsland(SkyIslandWorld island, String new_name) {
        island.setName(new_name);
        saveIslands(island.getServer());
    }

    private static void saveIslands(MinecraftServer server) {
        IslandPersistentState state = IslandPersistentState.getServerState(server);
        NbtList list = new NbtList();
        for (SkyIslandWorld island : SkyIslandUtils.getAllIslandsWithoutVanilla().values()) {
            list.add(island.getNBT());
        }
        state.islands = list;
        state.markDirty();
    }

    public static void joinIsland(SkyIslandWorld island, ServerPlayerEntity player, boolean creation) {
        if (island.tryAddMember(player, creation)) {
            if (!creation) {
                player.sendMessage(Text.literal("Request sent"));
            }
        } else {
            player.sendMessage(Text.literal("This Island has reached the max number of members: " + CarpetSkyAdditionalsSettings.maxPlayersPerIsland));
        }
    }

    public static void leaveIsland(SkyIslandWorld island, ServerPlayerEntity player) {
        island.removeMember(player);
        ((PlayerIslandDataInterface)player).removeHomeIsland(island);
        SkyIslandUtils.teleportToIsland(player, player.getServer().getWorld(ServerWorld.OVERWORLD), GameMode.SURVIVAL);
        player.sendMessage(Text.literal("You have been removed from this Island"));

        if (island.getMembersLowRamConsumption().isEmpty()) {
            removeIsland(island);
        }
    }

    public static void visitIsland(SkyIslandWorld island, ServerPlayerEntity player) {
        SkyIslandUtils.teleportToIsland(player, island.getOverworld(), GameMode.SPECTATOR);
    }

    private static void generateIslandStructure(SkyIslandWorld world, MinecraftServer server) {
        ServerWorld overworld = world.getOverworld();
        ChunkPos chunkPos = ChunkPos.ORIGIN;
        int x = chunkPos.getCenterX();
        int z = chunkPos.getCenterZ();
        overworld.getChunkManager().addTicket(ChunkTicketType.UNKNOWN, chunkPos, 2, chunkPos);
        Registry<ConfiguredFeature<?, ?>> configuredFeatureRegistry = server.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE);
        ConfiguredFeature<?, ?> skyIslandFeature = SkyIslandCommandInvoker.invokeGetIslandFeature(configuredFeatureRegistry);
        ChunkRandom random = new ChunkRandom(new CheckedRandom(0L));
        random.setCarverSeed(overworld.getSeed(), chunkPos.x, chunkPos.z);
        skyIslandFeature.generate(overworld, overworld.getChunkManager().getChunkGenerator(), random, new BlockPos(x, 0, z));
    }

    private static void removeAllPlayersFromIsland(SkyIslandWorld island) {
        Map<String, ServerPlayerEntity> all = island.getMembers();
        MinecraftServer server = island.getServer();

        for (ServerPlayerEntity player : all.values()) {
            if (OfflinePlayerUtils.isPlayerOnline(player, island.getServer())) {
                ((PlayerIslandDataInterface) player).removeHomeIsland(island);
                if (isOnIsland(player, island)) {
                    SkyIslandUtils.teleportToIsland(player, server.getWorld(ServerWorld.OVERWORLD), GameMode.SURVIVAL);
                }
                player.sendMessage(Text.literal(island.getName() + " has been removed!"));
            } else {
                NbtCompound player_data = OfflinePlayerUtils.getPlayerData(server, player);
                OfflinePlayerUtils.teleportOffline(player, new Vec3d(8.5, 64, 9.5));
                OfflinePlayerUtils.setDimension(player_data, server.getOverworld());
                OfflinePlayerUtils.setGameMode(player_data, GameMode.ADVENTURE);
                List<PlayerSkyIslandWorld> p_islands = OfflinePlayerUtils.readPlayerIslandsNbt(player_data);
                p_islands.remove(((PlayerIslandDataInterface) player).getPIsland(island));
                OfflinePlayerUtils.writePlayerIslandNbt(p_islands, player_data);
                OfflinePlayerUtils.savePlayerData(player, player_data, server);
            }
        }
    }

    public static void teleportHub(ServerPlayerEntity player) {
        SkyIslandUtils.teleportToIsland(player, player.getServer().getOverworld(), GameMode.ADVENTURE);
    }

    private static boolean isOnIsland(ServerPlayerEntity player, SkyIslandWorld island) {
        ServerWorld world = player.getWorld();
        return world == island.getOverworld() || world == island.getNether() || world == island.getEnd();
    }
}
