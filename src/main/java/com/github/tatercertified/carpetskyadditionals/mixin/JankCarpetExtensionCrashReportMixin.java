package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public class JankCarpetExtensionCrashReportMixin {
    @Inject(method = "initCrashReport", at = @At("HEAD"))
    private static void gameStarted(CallbackInfo ci) {
        CarpetSkyAdditionals.init();
    }
}