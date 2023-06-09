package com.github.tatercertified.carpetskyadditionals.carpet;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

public class CSAValidators {
    private CSAValidators() {}

    public static class NonZeroPositive<T extends Number> extends Validator<T> {

        @Override
        public T validate(@Nullable ServerCommandSource source, CarpetRule<T> changingRule, T newValue, String userInput) {
            return newValue.doubleValue() > 0 ? newValue : null;
        }

        @Override
        public String description() {
            return "Must be a positive, non-zero number";
        }
    }
}
