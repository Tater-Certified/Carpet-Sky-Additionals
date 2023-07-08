package com.github.tatercertified.carpetskyadditionals.util;

import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DragonNbtConverter {
    public static NbtCompound toNBT(EnderDragonFight.Data fight_data) {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("NeedsStateScanning", fight_data.needsStateScanning());
        nbt.putBoolean("DragonKilled", fight_data.dragonKilled());
        nbt.putBoolean("PreviouslyKilled", fight_data.previouslyKilled());
        nbt.putBoolean("IsRespawning", fight_data.isRespawning());
        if (fight_data.dragonUUID().isPresent()) {
            nbt.putUuid("Dragon", fight_data.dragonUUID().get());
        }
        if (fight_data.exitPortalLocation().isPresent()) {
            nbt.put("ExitPortalLocation", blockPosToNBT(fight_data.exitPortalLocation().get()));
        }
        if (fight_data.gateways().isPresent()) {
            nbt.put("Gateways", intListToNBT(fight_data.gateways().get()));
        }
        return nbt;
    }

    public static EnderDragonFight.Data fromNBT(NbtCompound nbt) {
        boolean needsStateScanning = nbt.getBoolean("NeedsStateScanning");
        boolean dragonKilled = nbt.getBoolean("DragonKilled");
        boolean previouslyKilled = nbt.getBoolean("PreviouslyKilled");
        boolean isRespawning = nbt.getBoolean("IsRespawning");
        UUID dragon = nbt.getUuid("Dragon");
        BlockPos exitPortalLocation = nBTToBlockPos(nbt.getCompound("ExitPortalLocation"));
        List<Integer> gateways = nBTToIntList(nbt.getList("Gateways", NbtElement.INT_TYPE));
        return new EnderDragonFight.Data(needsStateScanning, dragonKilled, previouslyKilled, isRespawning, Optional.ofNullable(dragon), Optional.of(exitPortalLocation), Optional.of(gateways));
    }

    private static NbtCompound blockPosToNBT(BlockPos pos) {
        NbtCompound data = new NbtCompound();
        data.putInt("x", pos.getX());
        data.putInt("y", pos.getY());
        data.putInt("z", pos.getZ());
        return data;
    }

    private static NbtList intListToNBT(List<Integer> ints) {
        NbtList nbt = new NbtList();
        for (int i : ints) {
            nbt.add(NbtInt.of(i));
        }
        return nbt;
    }

    private static BlockPos nBTToBlockPos(NbtCompound data) {
        return new BlockPos(data.getInt("x"), data.getInt("y"), data.getInt("z"));
    }

    private static List<Integer> nBTToIntList(NbtList data) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            list.add(data.getInt(i));
        }
        return list;
    }
}
