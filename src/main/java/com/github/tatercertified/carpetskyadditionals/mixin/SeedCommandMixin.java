package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.SeedCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Supplier;

@Mixin(SeedCommand.class)
public class SeedCommandMixin {

    /**
     * @author QPCrummer
     * @reason Temp. placeholder for Inject
     */
    @Overwrite
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder) CommandManager.literal("seed").requires((source) -> !dedicated || source.hasPermissionLevel(2))).executes((context) -> {
            long l;
            if (context.getSource() instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) context.getSource();
                l = SkyIslandUtils.getSkyIsland((ServerWorld) player.getWorld()).getSeed();
            } else {
                l = ((ServerCommandSource) context.getSource()).getWorld().getSeed();
            }
            Text text = Texts.bracketedCopyable(String.valueOf(l));
            ((ServerCommandSource)context.getSource()).sendFeedback((Supplier<Text>) Text.translatable("commands.seed.success", new Object[]{text}), false);
            return (int)l;
        }));
    }
}
