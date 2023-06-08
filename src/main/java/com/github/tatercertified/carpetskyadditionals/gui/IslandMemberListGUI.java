package com.github.tatercertified.carpetskyadditionals.gui;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class IslandMemberListGUI extends PagedGUI{
    private final SkyIslandWorld island;
    private final String click_function;
    public IslandMemberListGUI(ServerPlayerEntity user, SimpleGui parent_gui, Map<String, ?> map, int item_amount, String action_type, SkyIslandWorld island) {
        super(user, parent_gui, map, item_amount, action_type);
        this.island = island;
        this.click_function = action_type;
    }

    @Override
    public void addToGUI(Object item, SimpleGui gui) {
        gui.addSlot(new GuiElementBuilder(Items.PLAYER_HEAD)
                .setSkullOwner(((ServerPlayerEntity)item).getGameProfile(), island.getServer())
                .setName(Text.literal(((ServerPlayerEntity)item).getName().getString()))
                .setCallback(clickType -> {
                    switch (click_function) {
                        case "remove" -> island.removeMember(((ServerPlayerEntity)item));
                        case "add" -> {
                            if (!island.tryAddMember(((ServerPlayerEntity)item), true)) {
                                island.setMaxMembers(island.getMaxMembers() + 1);
                                island.tryAddMember(((ServerPlayerEntity)item), true);
                            }
                        }
                    }
                }));
    }
}
