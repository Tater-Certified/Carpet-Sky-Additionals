package com.github.tatercertified.carpetskyadditionals.gui;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AdminGUI {

    public AdminGUI(ServerPlayerEntity user, SkyIslandWorld island) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.HOPPER, user, true) {
            @Override
            public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                String name = element.getItemStack().getName().getString();
                if (element.getItemStack().getItem() == Items.GRAY_STAINED_GLASS_PANE) {
                    return super.onClick(index, type, action, element);
                }
                switch (name) {
                    case "Delete Island" -> SkyIslandManager.removeIsland(island);
                    case "Remove Member" -> new IslandMemberListGUI(user, this, island.getMembers(), 45, "remove", island);
                    case "Add Member" -> new IslandMemberListGUI(user, this, island.getMembers(), 45, "add", island);
                }

                return super.onClick(index, type, action, element);
            }
        };

        gui.setSlot(0, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));
        gui.setSlot(1, new GuiElementBuilder().setItem(Items.BARRIER).setName(Text.literal("Delete Island")));
        gui.setSlot(2, new GuiElementBuilder().setItem(Items.SKELETON_SKULL).setName(Text.literal("Remove Member")));
        gui.setSlot(3, new GuiElementBuilder().setItem(Items.PLAYER_HEAD).setName(Text.literal("Add Member")));
        gui.setSlot(4, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));
    }
}
