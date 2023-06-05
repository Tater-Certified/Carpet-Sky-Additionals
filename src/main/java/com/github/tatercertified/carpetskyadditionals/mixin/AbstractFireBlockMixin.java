package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin {

    /**
     * @author QPCrummer
     * @reason No point in using something other than Overwrite
     */
    @Overwrite
    private static boolean isOverworldOrNether(World world) {
        SkyIslandWorld island = SkyIslandUtils.getSkyIsland((ServerWorld) world);
        return world == island.getOverworld() || world == island.getNether();
    }
}
