package com.github.tatercertified.carpetskyadditionals.gui;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.EntityIslandDataInterface;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.Map;

public class IslandListGUI extends PagedGUI{
    private final SkyIslandWorld current;
    public IslandListGUI(ServerPlayerEntity user, SimpleGui parent_gui, Map<String, ?> map, int item_amount, String action_type) {
        super(user, parent_gui, map, item_amount, action_type);
        current = ((EntityIslandDataInterface)user).getCurrentIsland();
        // Inserted Ampflower assistance here
        init();
    }

    @Override
    public void addToGUI(Object item, SimpleGui gui) {
        if (item == current) {
            gui.addSlot(new GuiElementBuilder().setItem(Items.GLOWSTONE).setName(Text.literal(((SkyIslandWorld)item).getName())).addLoreLine(Text.literal("Owner: " + ((SkyIslandWorld)item).getOwnerName())));
        } else {
            gui.addSlot(new GuiElementBuilder().setItem(Items.GRASS_BLOCK).setName(Text.literal(((SkyIslandWorld)item).getName())).addLoreLine(Text.literal("Owner: " + ((SkyIslandWorld)item).getOwnerName())));
        }
    }

    @Override
    public void clickEvent(int index, ClickType type, SlotActionType action, GuiElementInterface element, String actions, SimpleGui current_gui, SimpleGui parent_gui, ServerPlayerEntity user) {
        if (element == null) {
            return;
        }
        String name = element.getItemStack().getName().getString();
        if (element.getItemStack().getItem() == Items.GRASS_BLOCK || element.getItemStack().getItem() == Items.GLOWSTONE) {
            SkyIslandWorld island = SkyIslandUtils.getSkyIsland(name);
            switch (actions) {
                case "delete" -> SkyIslandManager.removeIsland(island);
                case "join" -> SkyIslandManager.joinIsland(island, user, false);
                case "leave" -> SkyIslandManager.leaveIsland(island, user);
                case "visit" -> {
                    SkyIslandManager.visitIsland(island, user);
                    parent_gui = null;
                }
                case "teleport" -> {
                    SkyIslandUtils.teleportToIsland(user, island.getOverworld(), GameMode.SURVIVAL);
                    parent_gui = null;
                }
                case "manage" -> {
                    new IslandManagementGUI(user, parent_gui, island.getRequests(), 45, null, island);
                    parent_gui = null;
                }
                case "admin" -> new AdminGUI(user, island);
                //TODO Find a safe alternative to renaming islands
                case "rename" -> parent_gui = null;
            }
            current_gui.close();
            if (parent_gui != null) {
                parent_gui.open();
            }
        }
    }

    @Override
    public String setGUITitle() {
        return "Islands";
    }
}
