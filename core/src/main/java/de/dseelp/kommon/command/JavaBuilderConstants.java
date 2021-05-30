package de.dseelp.kommon.command;

import de.dseelp.kommon.command.arguments.*;
import kotlin.jvm.functions.Function1;

import java.util.UUID;

/**
 * @author DSeeLP
 */
public class JavaBuilderConstants {
    public static <S> JavaCommandBuilder<S> literal(String name, String[] aliases, Class<S> type) {
        return new JavaCommandBuilder<>(name, aliases);
    }

    public static <S> JavaCommandBuilder<S> literal(String name, Class<S> type) {
        return literal(name, new String[]{}, type);
    }

    public static <S> JavaCommandBuilder<S> argument(Argument<S, ?> argument, Class<S> type) {
        return new JavaCommandBuilder<>(argument);
    }


    public static BooleanArgument bool(String name) {
        return new BooleanArgument(name);
    }

    public static BooleanArgument bool(String name, String trueString, String falseString) {
        return new BooleanArgument(name, trueString, falseString);
    }


    public static IntArgument integerArgument(String name) {
        return new IntArgument(name);
    }

    public static IntArgument integerArgument(String name, Function1<CommandContext, Integer[]> completer) {
        return new IntArgument(name, completer);
    }

    public static DoubleArgument doubleArgument(String name) {
        return new DoubleArgument(name);
    }

    public static DoubleArgument doubleArgument(String name, Function1<CommandContext, Double[]> completer) {
        return new DoubleArgument(name, completer);
    }

    public static LongArgument longArgument(String name) {
        return new LongArgument(name);
    }

    public static LongArgument longArgument(String name, Function1<CommandContext, Long[]> completer) {
        return new LongArgument(name, completer);
    }


    public static UUIDArgument uniqueIdArgument(String name) {
        return new UUIDArgument(name);
    }

    public static UUIDArgument uniqueIdArgument(String name, Function1<CommandContext, UUID[]> completer) {
        return new UUIDArgument(name, completer);
    }


    public static StringArgument stringArgument(String name) {
        return new StringArgument(name);
    }

    public static StringArgument stringArgument(String name, Function1<CommandContext, String[]> completer) {
        return new StringArgument(name, completer);
    }
}
