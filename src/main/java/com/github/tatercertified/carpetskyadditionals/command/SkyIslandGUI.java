package com.github.tatercertified.carpetskyadditionals.command;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SkyIslandGUI {
    private final ServerPlayerEntity player;

    public SkyIslandGUI(ServerPlayerEntity user) {
        this.player = user;
        openGUI();
    }

    private SimpleGui gui;
    private void openGUI() {
        gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, true);

        gui.setTitle(Text.literal("Sky Island Dashboard"));

        gui.setSlot(0, new GuiElementBuilder().setItem(Items.GRASS_BLOCK).setName(Text.literal("Create An Island")).setCallback(clickType -> {
            gui.close();
            openTextEditor(player, null, true);
        }));

        gui.setSlot(1, new GuiElementBuilder().setItem(Items.BARRIER).setName(Text.literal("Delete An Island")).setCallback(clickType -> {
            gui.close();
            openPagedList(player, gui, SkyIslandUtils.getAllOwnersIslands(player), "delete");
        }));

        gui.setSlot(2, new GuiElementBuilder().setItem(Items.ANVIL).setName(Text.literal("Rename An Island")).setCallback(clickType -> {
            gui.close();
            openTextEditor(player, gui, false);
        }));
        //TODO Manage Islands
        gui.setSlot(3, new GuiElementBuilder().setItem(Items.LIGHT).setName(Text.literal("Manage Your Islands")).setCallback(clickType -> {
            gui.close();
            openPagedList(player, gui, SkyIslandUtils.getAllOwnersIslands(player), "");
        }));

        gui.setSlot(4, new GuiElementBuilder().setItem(Items.GREEN_CONCRETE).setName(Text.literal("Join An Island")).setCallback(clickType -> {
            gui.close();
            openPagedList(player, gui, SkyIslandUtils.getAllNotUsersIslands(player), "join");
        }));

        gui.setSlot(5, new GuiElementBuilder().setItem(Items.RED_CONCRETE).setName(Text.literal("Leave An Island")).setCallback(clickType -> {
            gui.close();
            openPagedList(player, gui, SkyIslandUtils.getAllUsersIslands(player), "leave");
        }));

        gui.setSlot(6, new GuiElementBuilder().setItem(Items.OAK_DOOR).setName(Text.literal("Visit An Island")).setCallback(clickType -> {
            gui.close();
            openPagedList(player, null, SkyIslandUtils.getAllNotUsersIslands(player), "visit");
        }));

        gui.setSlot(7, new GuiElementBuilder().setItem(Items.RED_BED).setName(Text.literal("Go To Your Island")).setCallback(clickType -> {
            gui.close();
            openPagedList(player, null, SkyIslandUtils.getAllUsersIslands(player), "teleport");
        }));

        gui.setSlot(8, new GuiElementBuilder().setItem(Items.DIAMOND_BLOCK).setName(Text.literal("Go To The Hub")).setCallback(clickType -> SkyIslandManager.teleportHub(player)));

        gui.open();
    }

    private void openTextEditor(ServerPlayerEntity player, SimpleGui new_gui, boolean creation) {
        final String[] output = new String[1];
        SignGui editor = new SignGui(player) {
            @Override
            public void onClose() {
                super.onClose();
                output[0] = this.getLine(0).getString() + this.getLine(1).getString() + this.getLine(2).getString() + this.getLine(3).getString();
                if (!output[0].equals("")) {
                    if (creation) {
                        if (!checkIfIslandNameIsAvailable(output[0])) {
                            player.sendMessage(Text.literal("That name is already taken, try another name"));
                        } else {
                            SkyIslandManager.createIsland(output[0], player.getServer(), player);
                        }
                    } else {
                        //TODO Rename code
                    }
                } else {
                    gui.open();
                }
                if (new_gui != null) {
                    new_gui.open();
                }
            }
        };
        editor.open();
    }

    private boolean checkIfIslandNameIsAvailable(String name) {
        for (SkyIslandWorld island : SkyIslandUtils.getAllIslands()) {
            if (Objects.equals(island.getName(), name)) {
                return false;
            }
        }
        return true;
    }

    private void openPagedList(ServerPlayerEntity player, SimpleGui new_gui, List<SkyIslandWorld> islands, String click_function) {
        int maxListSize = 45;
        List<SimpleGui> gui_pages = new ArrayList<>();

        SimpleGui gui1 = null;
        int count = 0;

        if (!islands.isEmpty()) {
            for (SkyIslandWorld island : islands) {
                if (count % maxListSize == 0) {
                    gui1 = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, true) {
                        @Override
                        public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                            String name = element.getItemStack().getName().getString();
                            if (Objects.equals(name, "Next")) {
                                SimpleGui next = gui_pages.get(gui_pages.indexOf(this) + 1);
                                this.close();
                                next.open();
                            } else if (Objects.equals(name, "Back")) {
                                SimpleGui back = gui_pages.get(gui_pages.indexOf(this) - 1);
                                this.close();
                                back.open();
                            } else if (Objects.equals(name, "Exit")) {
                                this.close();
                                gui.open();
                            } else if (element.getItemStack().getItem() == Items.GRASS_BLOCK) {
                                switch (click_function) {
                                    case "delete" -> SkyIslandManager.removeIsland(name);
                                    case "join" -> SkyIslandManager.joinIsland(SkyIslandUtils.getSkyIsland(name), player);
                                    case "leave" -> SkyIslandManager.leaveIsland(SkyIslandUtils.getSkyIsland(name), player);
                                    case "visit" -> SkyIslandManager.visitIsland(SkyIslandUtils.getSkyIsland(name), player);
                                    case "teleport" -> SkyIslandUtils.teleportToIsland(player, SkyIslandUtils.getSkyIsland(name).getOverworld(), GameMode.SURVIVAL);
                                }
                                this.close();
                                if (new_gui != null) {
                                    new_gui.open();
                                }
                            }
                            return super.onClick(index, type, action, element);
                        }
                    };
                    addControlBar(gui1, gui_pages.size() == 0, count < 45);
                    gui_pages.add(gui1);
                }

                gui1.addSlot(new GuiElementBuilder().setItem(Items.GRASS_BLOCK).setName(Text.literal(island.getName())).addLoreLine(Text.literal("Owner: " + island.getOwner())));

                count++;
            }
        } else {
            gui1 = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, true) {
                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                    String name = element.getItemStack().getName().getString();
                    if (Objects.equals(name, "Exit")) {
                        this.close();
                        gui.open();
                    }
                    return super.onClick(index, type, action, element);
                }
            };
            addControlBar(gui1, true, true);
            gui_pages.add(gui1);
        }

        gui_pages.get(0).open();

    }

    private void addControlBar(SimpleGui gui, boolean first, boolean last) {
        if (!first) {
            gui.setSlot(45, new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner("ewogICJ0aW1lc3RhbXAiIDogMTY2ODI2NDQ4MDc1OCwKICAicHJvZmlsZUlkIiA6ICJjYTk2OThjZjcxZmM0ZDdhOWRiMDk2MmU3MjI4OTgwZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeWRyZWdpb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTBlZjY4YTNjYjQyYzI4MDY2MGQ1NDVhZWNmOGRmYjcyNWQ5N2IwMjhhYmRhZTM1MDliNTEzMDZmMjQxYzc4NCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9").setName(Text.literal("Back")));
        }
        if (!last) {
            gui.setSlot(53, new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner("ewogICJ0aW1lc3RhbXAiIDogMTY2ODI2NDQ1MjUzOSwKICAicHJvZmlsZUlkIiA6ICI4YmM3MjdlYThjZjA0YWUzYTI4MDVhY2YzNjRjMmQyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJub3RpbnZlbnRpdmUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzU4ODE3Y2UyOTVhOWQ5ZGE2ZmQyYzAxZWRkMDhjYTEwMzAyNDZlNmM1NGY3ZTFiZDExYmZiZWE5ZjBiM2NhNSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9").setName(Text.literal("Next")));
        }
        gui.setSlot(49, new GuiElementBuilder().setItem(Items.BARRIER).setName(Text.literal("Exit")));
        for (int i = 45; i < 54; i++) {
            if (!first || !last) {
                continue;
            }
            if (i == 49) {
                continue;
            }
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE));
        }
    }
}
