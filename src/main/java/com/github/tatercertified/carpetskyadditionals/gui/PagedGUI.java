package com.github.tatercertified.carpetskyadditionals.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PagedGUI {
    public PagedGUI(ServerPlayerEntity user, SimpleGui parent_gui, Map<String, ?> map, int item_amount, String action_type) {
        List<List<?>> chunks = breakMapIntoChunks(map, item_amount);
        List<SimpleGui> pages = new ArrayList<>();

        if (chunks.isEmpty()) {
            chunks.add(new ArrayList<>());
        }

        for (List<?> chunk : chunks) {
            SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, user, true) {
                @Override
                public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                    clickEvent(index, type, action, element, action_type, this, parent_gui, user);
                    return super.onClick(index, type, action, element);
                }
            };
            for (Object item : chunk) {
                addToGUI(item, gui);
            }
            pages.add(gui);
            addNavigationBar(gui, parent_gui, pages.indexOf(gui), chunks, pages);
        }
        pages.get(0).open();
    }

    private List<List<?>> breakMapIntoChunks(Map<String, ?> map, int chunkSize) {
        List<List<?>> chunks = new ArrayList<>();
        List<?> values = new ArrayList<>(map.values());

        for (int i = 0; i < values.size(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, values.size());
            List<?> chunk = values.subList(i, endIndex);
            chunks.add(chunk);
        }

        return chunks;
    }

    private void addNavigationBar(SimpleGui gui, SimpleGui parent_gui, int index, List<List<?>> chunks, List<SimpleGui> pages) {
        if (index != 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner("ewogICJ0aW1lc3RhbXAiIDogMTY2ODI2NDQ4MDc1OCwKICAicHJvZmlsZUlkIiA6ICJjYTk2OThjZjcxZmM0ZDdhOWRiMDk2MmU3MjI4OTgwZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeWRyZWdpb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTBlZjY4YTNjYjQyYzI4MDY2MGQ1NDVhZWNmOGRmYjcyNWQ5N2IwMjhhYmRhZTM1MDliNTEzMDZmMjQxYzc4NCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9")
                    .setName(Text.literal("Back"))
                    .setCallback(clickType -> {
                        gui.close();
                        pages.get(index - 1).open();
                    }));
        }
        if (!isLastPage(gui, chunks, pages)) {
            gui.setSlot(53, new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner("ewogICJ0aW1lc3RhbXAiIDogMTY2ODI2NDQ1MjUzOSwKICAicHJvZmlsZUlkIiA6ICI4YmM3MjdlYThjZjA0YWUzYTI4MDVhY2YzNjRjMmQyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJub3RpbnZlbnRpdmUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzU4ODE3Y2UyOTVhOWQ5ZGE2ZmQyYzAxZWRkMDhjYTEwMzAyNDZlNmM1NGY3ZTFiZDExYmZiZWE5ZjBiM2NhNSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9")
                    .setName(Text.literal("Next"))
                    .setCallback(clickType -> {
                        gui.close();
                        pages.get(index + 1).open();
                    }));
        }
        gui.setSlot(49, new GuiElementBuilder().setItem(Items.BARRIER)
                .setName(Text.literal("Exit"))
                .setCallback(clickType -> {
                    gui.close();
                    if (parent_gui != null) {
                        parent_gui.open();
                    }
                }));
        for (int i = 45; i < 54; i++) {
            if (gui.getSlot(i) == null) {
                gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE));
            }
        }
    }

    private boolean isLastPage(SimpleGui gui, List<List<?>> chunks, List<SimpleGui> pages) {
        int index = pages.indexOf(gui);
        return index == chunks.size() - 1;
    }

    public void addToGUI(Object item, SimpleGui gui) {
        // Override
    }

    public void clickEvent(int index, ClickType type, SlotActionType action, GuiElementInterface element, String actions, SimpleGui current, SimpleGui parent_gui, ServerPlayerEntity user) {
        // Override
    }
}
