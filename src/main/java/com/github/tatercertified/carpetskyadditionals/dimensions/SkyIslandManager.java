package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import com.github.tatercertified.carpetskyadditionals.mixin.SkyIslandCommandInvoker;
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
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import xyz.nucleoid.fantasy.Fantasy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SkyIslandManager {

    public static List<SkyIslandWorld> islands = new ArrayList<>();
    public static Fantasy fantasy;
    public static void loadIslands(MinecraftServer server, NbtList nbt) {
        fantasy = Fantasy.get(server);

        SkyIslandWorld vanilla = new VanillaWorldOverride("overworld", -1, server, fantasy, server.getOverworld().getSeed(), new NbtCompound());
        islands.add(vanilla);

        if (nbt != null) {
            for (int i = 0; i < nbt.size(); i++) {
                NbtCompound compound = nbt.getCompound(i);

                SkyIslandWorld island = new SkyIslandWorld(compound.getString("name"), compound.getInt("max_members"), server, fantasy, compound.getLong("seed"), compound.getCompound("dragon_fight"));
                island.loadNBT(compound);
                islands.add(island);
            }
        }
        //TODO TESTING
        System.out.println("TESTING*2*!!!!!!!! " + islands + ";    " + nbt);
    }

    public static void createIsland(String name, MinecraftServer server, ServerPlayerEntity creator) {
        SkyIslandWorld world = new SkyIslandWorld(name, 2, server, fantasy, null, new NbtCompound());
        islands.add(world);
        world.setOwner(creator.getName().getString());
        generateIslandStructure(world, server);
        BlockPos pos = new BlockPos(8, 63, 9);
        world.getOverworld().setBlockState(pos, Blocks.BEDROCK.getDefaultState());
        world.getOverworld().setSpawnPos(pos, 0.0F);
        joinIsland(world, creator);
    }

    public static void removeIsland(String name) {
        for (SkyIslandWorld island : islands) {
            if (Objects.equals(island.getName(), name)) {
                removeAllPlayersFromIsland(island);
                island.remove();
                islands.remove(island);
                return;
            }
        }
    }

    public static void joinIsland(SkyIslandWorld island, ServerPlayerEntity player) {
        if (island.tryAddMember(player)) {
            ((PlayerIslandDataInterface)player).addHomeIsland(island);
            SkyIslandUtils.teleportToIsland(player, island.getOverworld(), GameMode.SURVIVAL);
            player.changeGameMode(GameMode.SURVIVAL);
        } else {
            player.sendMessage(Text.literal("This Island has reached the max number of members: " + island.getMaxMembers()));
        }
    }

    public static void leaveIsland(SkyIslandWorld island, ServerPlayerEntity player) {
        island.removeMember(player);
        SkyIslandUtils.teleportToIsland(player, player.getServer().getWorld(ServerWorld.OVERWORLD), GameMode.SURVIVAL);
        player.sendMessage(Text.literal("You have been removed from this Island"));
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

    public static String listIslands() {
        List<String> strings = new ArrayList<>();
        for (SkyIslandWorld world : islands) {
            if (Objects.equals(world.getName(), "overworld")) {
                continue;
            }
            strings.add(world.getName());
        }
        return String.join(", ", strings);
    }

    private static boolean isOnIsland(SkyIslandWorld island, ServerPlayerEntity player) {
        return player.getWorld() == island.getOverworld() || player.getWorld() == island.getNether() || player.getWorld() == island.getEnd();
    }

    private static void removeAllPlayersFromIsland(SkyIslandWorld island) {
        List<ServerPlayerEntity> all = island.getMembers();
        MinecraftServer server = island.getServer();

        for (ServerPlayerEntity player : all) {
            if (isOnline(player, island.getServer())) {
                if (((PlayerIslandDataInterface) player).getHomeIslands().contains(island)) {
                    ((PlayerIslandDataInterface) player).removeHomeIsland(island);
                }
                SkyIslandUtils.teleportToIsland(player, server.getWorld(ServerWorld.OVERWORLD), GameMode.SURVIVAL);
                server.sendMessage(Text.literal("The Island you were on has been removed"));
            } else {
                NbtCompound data = server.getPlayerManager().loadPlayerData(player);
                assert data != null;
                SkyIslandUtils.offlineTeleportToHub(player, data);
            }
        }
    }

    private static boolean isOnline(ServerPlayerEntity player, MinecraftServer server) {
        return server.getPlayerManager().getPlayerList().contains(player);
    }

    public static void teleportHub(ServerPlayerEntity player) {
        SkyIslandUtils.teleportToIsland(player, player.getServer().getOverworld(), GameMode.ADVENTURE);
    }
}
