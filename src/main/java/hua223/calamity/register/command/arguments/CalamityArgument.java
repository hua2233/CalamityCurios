package hua223.calamity.register.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CalamityArgument implements ArgumentType<String> {
    private final List<String> ENUMS = List.of("magic", "rage");

    public static String getType(CommandContext<CommandSourceStack> context, String type) {
        return context.getArgument(type, String.class);
    }

    public static CalamityArgument getInstance() {
        return new CalamityArgument();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String s = reader.readUnquotedString();
        if (ENUMS.contains(s)) {
            return s;
        }

        throw new DynamicCommandExceptionType(type ->
            Component.literal("无法被解析的命令参数:" + type)).create(s);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ENUMS, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return ENUMS;
    }
}
