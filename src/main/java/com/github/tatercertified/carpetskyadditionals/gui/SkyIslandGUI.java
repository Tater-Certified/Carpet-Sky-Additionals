package com.github.tatercertified.carpetskyadditionals.gui;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class SkyIslandGUI {
    private final ServerPlayerEntity player;

    public SkyIslandGUI(ServerPlayerEntity user, boolean admin) {
        this.player = user;
        if (admin) {
            new IslandListGUI(this.player, null, SkyIslandUtils.getAllIslandsWithoutVanilla(), 45, "admin");
        } else {
            openGUI();
        }
    }

    private SimpleGui gui;

    private void openGUI() {
        gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, true);

        gui.setTitle(Text.literal("Sky Island Dashboard"));

        gui.setSlot(0, new GuiElementBuilder().setItem(Items.GRASS_BLOCK).setName(Text.literal("Create An Island")).setCallback(clickType -> {
            gui.close();
            new TextEditorGUI(this.player, null, null);
        }));

        gui.setSlot(1, new GuiElementBuilder().setItem(Items.BARRIER).setName(Text.literal("Delete An Island")).setCallback(clickType -> {
            gui.close();
            new IslandListGUI(this.player, gui, SkyIslandUtils.getAllOwnersIslands(this.player), 45, "delete");
        }));

        gui.setSlot(2, new GuiElementBuilder().setItem(Items.ANVIL).setName(Text.literal("Rename An Island")).setCallback(clickType -> {
            gui.close();
            new IslandListGUI(this.player, null, SkyIslandUtils.getAllOwnersIslands(this.player), 45, "rename");
        }));

        gui.setSlot(3, new GuiElementBuilder().setItem(Items.LIGHT).setName(Text.literal("Manage Your Islands")).setCallback(clickType -> {
            gui.close();
            new IslandListGUI(this.player, gui, SkyIslandUtils.getAllOwnersIslands(this.player), 45, "manage");
        }));

        gui.setSlot(4, new GuiElementBuilder().setItem(Items.GREEN_CONCRETE).setName(Text.literal("Join An Island")).setCallback(clickType -> {
            gui.close();
            new IslandListGUI(this.player, gui, SkyIslandUtils.getAllNotUsersIslands(this.player), 45, "join");
        }));

        gui.setSlot(5, new GuiElementBuilder().setItem(Items.RED_CONCRETE).setName(Text.literal("Leave An Island")).setCallback(clickType -> {
            gui.close();
            new IslandListGUI(this.player, gui, SkyIslandUtils.getAllUsersIslands(this.player), 45, "leave");
        }));

        gui.setSlot(6, new GuiElementBuilder().setItem(Items.OAK_DOOR).setName(Text.literal("Visit An Island")).setCallback(clickType -> {
            gui.close();
            new IslandListGUI(this.player, null, SkyIslandUtils.getAllNotUsersIslands(this.player), 45, "visit");
        }));

        gui.setSlot(7, new GuiElementBuilder().setItem(Items.RED_BED).setName(Text.literal("Go To Your Island")).setCallback(clickType -> {
            gui.close();
            new IslandListGUI(this.player, null, SkyIslandUtils.getAllUsersIslands(this.player), 45, "teleport");
        }));

        gui.setSlot(8, new GuiElementBuilder().setItem(Items.DIAMOND_BLOCK).setName(Text.literal("Go To The Hub")).setCallback(clickType -> SkyIslandManager.teleportHub(player)));

        gui.open();
    }
}