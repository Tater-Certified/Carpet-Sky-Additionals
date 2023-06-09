package com.github.tatercertified.carpetskyadditionals.gui;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;

public class TextEditorGUI {

    public TextEditorGUI(ServerPlayerEntity user, SimpleGui parent_gui, SkyIslandWorld island) {
        final String[] output = new String[1];
        SignGui editor = new SignGui(user) {
            @Override
            public void onClose() {
                super.onClose();
                output[0] = this.getLine(0).getString() + this.getLine(1).getString() + this.getLine(2).getString() + this.getLine(3).getString();
                if (!output[0].equals("")) {
                    if (island == null) {
                        SkyIslandManager.createIsland(output[0], player.getServer(), player);
                    } else {
                        SkyIslandManager.renameIsland(island, output[0]);
                    }
                }
                if (parent_gui != null) {
                    parent_gui.open();
                }
            }
        };
        editor.open();
    }
}
