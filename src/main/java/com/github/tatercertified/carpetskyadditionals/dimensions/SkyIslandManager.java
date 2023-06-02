package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import com.github.tatercertified.carpetskyadditionals.mixin.SkyIslandCommandInvoker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    public static void loadIslands(MinecraftServer server) {
        fantasy = Fantasy.get(server);

        for (ServerWorld world : server.getWorlds()) {
            Identifier id = world.getRegistryKey().getValue();
            if (Objects.equals(id.getNamespace(), CarpetSkyAdditionals.MOD_ID) && world.getDimension() == server.getOverworld().getDimension()) {
                // TODO not hardcode max_members
                SkyIslandWorld island = new SkyIslandWorld(id.getPath(), 2, server, fantasy);
                islands.add(island);
            } else if (world.getDimension() == server.getOverworld().getDimension()){
                SkyIslandWorld override = new VanillaWorldOverride(id.getPath(), -1, server, fantasy);
                islands.add(override);
            }
        }
    }

    public static void createIsland(String name, MinecraftServer server, ServerPlayerEntity creator) {
        SkyIslandWorld world = new SkyIslandWorld(name, 2, server, fantasy);
        islands.add(world);
        generateIslandStructure(world, server);
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
            // TODO implement this better so that it finds a suitable location to place the player
            ((PlayerIslandDataInterface)player).setHomeIsland(island);
            SkyIslandUtils.teleportToIsland(player, island.getOverworld(), GameMode.SURVIVAL);
            player.setStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 15), player);
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
        List<ServerPlayerEntity> all = new ArrayList<>();
        all.addAll(island.getOverworld().getPlayers());
        all.addAll(island.getNether().getPlayers());
        all.addAll(island.getEnd().getPlayers());

        MinecraftServer server;

        for (ServerPlayerEntity player : all) {
            server = player.getServer();
            if (((PlayerIslandDataInterface)player).getHomeIsland() == island) {
                ((PlayerIslandDataInterface) player).setHomeIsland(null);
            }
            SkyIslandUtils.teleportToIsland(player, server.getWorld(ServerWorld.OVERWORLD), GameMode.SURVIVAL);
            server.sendMessage(Text.literal("The Island you were on has been removed"));
        }
    }

    public static void teleportHome(ServerPlayerEntity player) {
        SkyIslandWorld home = ((PlayerIslandDataInterface)player).getHomeIsland();
        if (home != null) {
            SkyIslandUtils.teleportToIsland(player, home.getOverworld(), GameMode.SURVIVAL);
        } else {
            player.sendMessage(Text.literal("You are not a member of any Island"));
        }
    }
}
