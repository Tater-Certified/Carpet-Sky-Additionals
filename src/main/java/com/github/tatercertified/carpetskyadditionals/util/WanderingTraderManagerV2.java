package com.github.tatercertified.carpetskyadditionals.util;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.spawner.Spawner;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

public class WanderingTraderManagerV2 implements Spawner {
    private final Random random = Random.create();
    private int spawnTimer;
    private int spawnDelay;
    private int spawnChance;
    private final NbtCompound data;
    private UUID trader;

    public WanderingTraderManagerV2(NbtCompound data) {
        this.data = data;
        this.spawnTimer = 1200;
        this.spawnDelay = data.getInt("delay");
        this.spawnChance = data.getInt("chance");
        if (data.contains("trader")) {
            this.trader = data.getUuid("trader");
        }

        if (this.spawnDelay == 0 && this.spawnChance == 0) {
            this.spawnDelay = 24000;
            this.spawnChance = 25;
        }
    }

    @Override
    public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
        if (!world.getGameRules().getBoolean(GameRules.DO_TRADER_SPAWNING)) {
            return 0;
        } else if (--this.spawnTimer > 0) {
            return 0;
        } else {
            this.spawnTimer = 1200;
            this.spawnDelay -= 1200;
            if (this.spawnDelay > 0) {
                return 0;
            } else {
                this.spawnDelay = 24000;
                if (!world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
                    return 0;
                } else {
                    int i = this.spawnChance;
                    this.spawnChance = MathHelper.clamp(this.spawnChance + 25, 25, 75);
                    if (this.random.nextInt(100) > i) {
                        return 0;
                    } else if (this.trySpawn(world)) {
                        this.spawnChance = 25;
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }

    private boolean trySpawn(ServerWorld world) {
        PlayerEntity playerEntity = world.getRandomAlivePlayer();
        if (playerEntity == null) {
            return true;
        } else if (this.random.nextInt(10) != 0) {
            return false;
        } else {
            BlockPos blockPos = playerEntity.getBlockPos();
            PointOfInterestStorage pointOfInterestStorage = world.getPointOfInterestStorage();
            Optional<BlockPos> optional = pointOfInterestStorage.getPosition((poiType) -> poiType.matchesKey(PointOfInterestTypes.MEETING), (pos) -> true, blockPos, 48, PointOfInterestStorage.OccupationStatus.ANY);
            BlockPos blockPos2 = optional.orElse(blockPos);
            BlockPos blockPos3 = this.getNearbySpawnPos(world, blockPos2, 48);
            if (blockPos3 != null && this.doesNotSuffocateAt(world, blockPos3)) {
                if (world.getBiome(blockPos3).isIn(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
                    return false;
                }

                WanderingTraderEntity wanderingTraderEntity = EntityType.WANDERING_TRADER.spawn(world, blockPos3, SpawnReason.EVENT);
                if (wanderingTraderEntity != null) {
                    for(int j = 0; j < 2; ++j) {
                        this.spawnLlama(world, wanderingTraderEntity);
                    }

                    this.trader = wanderingTraderEntity.getUuid();
                    wanderingTraderEntity.setDespawnDelay(48000);
                    wanderingTraderEntity.setWanderTarget(blockPos2);
                    wanderingTraderEntity.setPositionTarget(blockPos2, 16);
                    return true;
                }
            }
            return false;
        }
    }

    private void spawnLlama(ServerWorld world, WanderingTraderEntity wanderingTrader) {
        BlockPos blockPos = this.getNearbySpawnPos(world, wanderingTrader.getBlockPos(), 4);
        if (blockPos != null) {
            TraderLlamaEntity traderLlamaEntity = EntityType.TRADER_LLAMA.spawn(world, blockPos, SpawnReason.EVENT);
            if (traderLlamaEntity != null) {
                traderLlamaEntity.attachLeash(wanderingTrader, true);
            }
        }
    }

    @Nullable
    private BlockPos getNearbySpawnPos(WorldView world, BlockPos pos, int range) {
        BlockPos blockPos = null;

        for(int i = 0; i < 10; ++i) {
            int j = pos.getX() + this.random.nextInt(range * 2) - range;
            int k = pos.getZ() + this.random.nextInt(range * 2) - range;
            int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, j, k);
            BlockPos blockPos2 = new BlockPos(j, l, k);
            if (SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, world, blockPos2, EntityType.WANDERING_TRADER)) {
                blockPos = blockPos2;
                break;
            }
        }

        return blockPos;
    }

    private boolean doesNotSuffocateAt(BlockView world, BlockPos pos) {
        Iterator<BlockPos> var3 = BlockPos.iterate(pos, pos.add(1, 2, 1)).iterator();

        BlockPos blockPos;
        do {
            if (!var3.hasNext()) {
                return true;
            }

            blockPos = var3.next();
        } while(world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty());

        return false;
    }

    public NbtCompound toNBT() {
        this.data.putInt("delay", this.spawnDelay);
        this.data.putInt("chance", this.spawnChance);
        if (this.trader != null) {
            this.data.putUuid("trader", this.trader);
        }
        return data;
    }

}
