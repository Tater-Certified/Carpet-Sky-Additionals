package com.github.tatercertified.carpetskyadditionals.carpet;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

public class CarpetSkyAdditionalsSettings {
    @Rule(
            categories = {RuleCategory.SURVIVAL, "sky-island-tweaks"},
            strict = false
    )
    public static boolean sneakingGrowsPlants;

    @Rule(
            categories = {RuleCategory.SURVIVAL, "sky-island-tweaks"},
            strict = false,
            validators = CSAValidators.NonZeroPositive.class
    )
    public static int sneakGrowingProbability = 15;

    @Rule(
            categories = {RuleCategory.SURVIVAL, "sky-island-tweaks"},
            strict = false,
            validators = CSAValidators.NonZeroPositive.class
    )
    public static int sneakGrowingRadius = 2;

}
