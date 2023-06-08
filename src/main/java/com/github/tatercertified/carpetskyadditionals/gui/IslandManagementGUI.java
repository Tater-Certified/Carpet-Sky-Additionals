package com.github.tatercertified.carpetskyadditionals.gui;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.offline_player_utils.OfflinePlayerUtils;
import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class IslandManagementGUI extends PagedGUI{
    private final SkyIslandWorld sky_island;
    public IslandManagementGUI(ServerPlayerEntity user, SimpleGui parent_gui, Map<String, ?> map, int item_amount, String action_type, SkyIslandWorld island) {
        super(user, parent_gui, map, item_amount, action_type);
        sky_island = island;
        // Inserted Ampflower assistance here
        init();
    }

    @Override
    public void addToGUI(Object item, SimpleGui gui) {
        NbtCompound compound = OfflinePlayerUtils.getPlayerData(sky_island.getServer(), (ServerPlayerEntity)item);
        UUID uuid = OfflinePlayerUtils.getUUID(compound);
        Optional<GameProfile> profile_optional = OfflinePlayerUtils.getGameProfile(uuid, sky_island.getServer());
        if (profile_optional.isPresent()) {
            GameProfile profile = profile_optional.get();
            gui.addSlot(new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(profile, sky_island.getServer())
                    .setName(Text.literal(profile.getName()))
                    .setCallback(clickType -> {
                        if (clickType.isLeft) {
                            sky_island.acceptJoinRequest(uuid, false);
                        } else if (clickType.isRight) {
                            sky_island.rejectJoinRequest(uuid);
                        }
                    }));
        } else {
            gui.addSlot(new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Text.literal((uuid).toString()))
                    .setCallback(clickType -> {
                        if (clickType.isLeft) {
                            sky_island.acceptJoinRequest(uuid, false);
                        } else if (clickType.isRight) {
                            sky_island.rejectJoinRequest(uuid);
                        }
                    }));
        }
    }

    @Override
    public String setGUITitle() {
        return "Manage Invites";
    }
}
