package com.github.tatercertified.carpetskyadditionals.command;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.EntityIslandDataInterface;
import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.*;

public class SkyIslandGUI {
    private final ServerPlayerEntity player;

    public SkyIslandGUI(ServerPlayerEntity user) {
        this.player = user;
        openGUI();
    }

    public SkyIslandGUI(ServerPlayerEntity user, boolean admin) {
        this.player = user;
        openPagedList(user, null, SkyIslandUtils.getAllIslandsWithoutVanilla(), "admin");
    }

    private SimpleGui gui;
    private void openGUI() {
        gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, true);

        gui.setTitle(Text.literal("Sky Island Dashboard"));

        gui.setSlot(0, new GuiElementBuilder().setItem(Items.GRASS_BLOCK).setName(Text.literal("Create An Island")).setCallback(clickType -> {
            gui.close();
            openTextEditor(player, null, null);
        }));

        gui.setSlot(1, new GuiElementBuilder().setItem(Items.BARRIER).setName(Text.literal("Delete An Island")).setCallback(clickType -> {
            gui.close();
            openPagedList(player, gui, SkyIslandUtils.getAllOwnersIslands(player), "delete");
        }));

        gui.setSlot(2, new GuiElementBuilder().setItem(Items.ANVIL).setName(Text.literal("Rename An Island")).setCallback(clickType -> {
            gui.close();
            openPagedList(player, null, SkyIslandUtils.getAllOwnersIslands(player), "rename");
        }));

        gui.setSlot(3, new GuiElementBuilder().setItem(Items.LIGHT).setName(Text.literal("Manage Your Islands")).setCallback(clickType -> {
            gui.close();
            openPagedList(player, gui, SkyIslandUtils.getAllOwnersIslands(player), "manage");
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

    private void openTextEditor(ServerPlayerEntity player, SimpleGui new_gui, SkyIslandWorld world) {
        final String[] output = new String[1];
        SignGui editor = new SignGui(player) {
            @Override
            public void onClose() {
                super.onClose();
                output[0] = this.getLine(0).getString() + this.getLine(1).getString() + this.getLine(2).getString() + this.getLine(3).getString();
                if (!output[0].equals("")) {
                    if (SkyIslandUtils.islandAvailable(output[0])) {
                        if (world == null) {
                            SkyIslandManager.createIsland(output[0], player.getServer(), player);
                        } else {
                            SkyIslandManager.renameIsland(world, output[0]);
                        }
                    } else {
                        player.sendMessage(Text.literal("That name is already taken, try another name"));
                        gui.open();
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

    private void openPagedList(ServerPlayerEntity player, SimpleGui new_gui, Map<String, SkyIslandWorld> islands, String click_function) {
        int maxListSize = 45;
        List<SimpleGui> gui_pages = new ArrayList<>();

        SimpleGui gui1 = null;
        int count = 0;

        SkyIslandWorld current = ((EntityIslandDataInterface)player).getCurrentIsland();

        if (!islands.isEmpty()) {
            for (SkyIslandWorld island : islands.values()) {
                if (count % maxListSize == 0) {
                    gui1 = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, true) {
                        @Override
                        public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                            if (element == null) {
                                return super.onClick(index, type, action, null);
                            }
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
                            } else if (element.getItemStack().getItem() == Items.GRASS_BLOCK || element.getItemStack().getItem() == Items.GLOWSTONE) {
                                switch (click_function) {
                                    case "delete" -> SkyIslandManager.removeIsland(SkyIslandUtils.getSkyIsland(name));
                                    case "join" -> SkyIslandManager.joinIsland(SkyIslandUtils.getSkyIsland(name), player, false);
                                    case "leave" -> SkyIslandManager.leaveIsland(SkyIslandUtils.getSkyIsland(name), player);
                                    case "visit" -> SkyIslandManager.visitIsland(SkyIslandUtils.getSkyIsland(name), player);
                                    case "teleport" -> SkyIslandUtils.teleportToIsland(player, SkyIslandUtils.getSkyIsland(name).getOverworld(), GameMode.SURVIVAL);
                                    case "manage" -> openManagementGUI(player, island);
                                    case "admin" -> openAdminGUI(island, player);
                                    //TODO Find a safe alternative to renaming islands
                                    //case "rename" -> openTextEditor(player, gui, SkyIslandUtils.getSkyIsland(name));
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

                if (island == current) {
                    gui1.addSlot(new GuiElementBuilder().setItem(Items.GLOWSTONE).setName(Text.literal(island.getName())).addLoreLine(Text.literal("Owner: " + island.getOwnerName())));
                } else {
                    gui1.addSlot(new GuiElementBuilder().setItem(Items.GRASS_BLOCK).setName(Text.literal(island.getName())).addLoreLine(Text.literal("Owner: " + island.getOwnerName())));
                }

                count++;
            }
        } else {
            gui1 = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, true) {
                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                    if (element == null) {
                        return super.onClick(index, type, action, null);
                    }
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

    private void openManagementGUI(ServerPlayerEntity player, SkyIslandWorld island) {
        int maxListSize = 45;
        List<SimpleGui> gui_pages = new ArrayList<>();

        SimpleGui gui1 = null;
        int count = 0;

        if (!island.getRequests().isEmpty()) {
            for (UUID uuid : island.getRequests()) {
                if (count % maxListSize == 0) {
                    gui1 = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, true) {
                        @Override
                        public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                            if (element == null) {
                                return super.onClick(index, type, action, null);
                            }
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
                            } else if (element.getItemStack().getItem() == Items.PLAYER_HEAD) {
                                if (type.isLeft) {
                                    island.acceptJoinRequest(uuid);
                                } else if (type.isRight) {
                                    island.rejectJoinRequest(uuid);
                                }
                            }
                            return super.onClick(index, type, action, element);
                        }
                    };
                    addControlBar(gui1, gui_pages.size() == 0, count < 45);
                    gui_pages.add(gui1);
                }
                GameProfile profile = island.getServer().getPlayerManager().getPlayer(uuid).getGameProfile();
                gui1.addSlot(new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(profile, island.getServer()).setName(Text.literal(profile.getName())));

                count++;
            }
        } else {
            gui1 = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, true) {
                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                    if (element == null) {
                        return super.onClick(index, type, action, null);
                    }
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
            if (!first && i == 45) {
                continue;
            }
            if (!last && i == 53) {
                continue;
            }
            if (i == 49) {
                continue;
            }
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE));
        }
    }

    private void openAdminGUI(SkyIslandWorld island, ServerPlayerEntity admin) {
        SimpleGui gui1 = new SimpleGui(ScreenHandlerType.HOPPER, admin, true) {
            @Override
            public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                String name = element.getItemStack().getName().getString();
                if (element.getItemStack().getItem() == Items.GRAY_STAINED_GLASS_PANE) {
                    return super.onClick(index, type, action, element);
                }
                switch (name) {
                    case "Delete Island" -> SkyIslandManager.removeIsland(island);
                    case "Remove Member" -> openIslandMemberGUI(admin, island, "remove");
                    case "Add Member" -> openIslandMemberGUI(admin, island, "add");
                }

                return super.onClick(index, type, action, element);
            }
        };

        gui1.setSlot(0, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));
        gui1.setSlot(1, new GuiElementBuilder().setItem(Items.BARRIER).setName(Text.literal("Delete Island")));
        gui1.setSlot(2, new GuiElementBuilder().setItem(Items.SKELETON_SKULL).setName(Text.literal("Remove Member")));
        gui1.setSlot(3, new GuiElementBuilder().setItem(Items.PLAYER_HEAD).setName(Text.literal("Add Member")));
        gui1.setSlot(4, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));
    }

    private void openIslandMemberGUI(ServerPlayerEntity admin, SkyIslandWorld island, String click_function) {
        int maxListSize = 45;
        List<SimpleGui> gui_pages = new ArrayList<>();

        SimpleGui gui1 = null;
        int count = 0;

        List<ServerPlayerEntity> players;
        if (Objects.equals(click_function, "remove")) {
            players = island.getMembers();
        } else {
            players = island.getServer().getPlayerManager().getPlayerList();
        }

        for (ServerPlayerEntity member : players) {
            if (count % maxListSize == 0) {
                gui1 = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, true) {
                    @Override
                    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                        if (element == null) {
                            return super.onClick(index, type, action, null);
                        }
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
                        } else if (element.getItemStack().getItem() == Items.PLAYER_HEAD) {
                            switch (click_function) {
                                case "remove" -> island.removeMember(member);
                                case "add" -> {
                                    if (!island.tryAddMember(member, true)) {
                                        island.setMaxMembers(island.getMaxMembers() + 1);
                                        island.tryAddMember(member, true);
                                    }
                                }
                            }
                        }
                        return super.onClick(index, type, action, element);
                    }
                };
                addControlBar(gui1, gui_pages.size() == 0, count < 45);
                gui_pages.add(gui1);
            }

            gui1.addSlot(new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(member.getGameProfile(), island.getServer()).setName(Text.literal(member.getName().getString())));

            count++;
        }

        gui_pages.get(0).open();
    }
}
