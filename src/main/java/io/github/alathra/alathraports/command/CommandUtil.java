package io.github.alathra.alathraports.command;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.alathra.alathraports.ports.enums.PortSize;

import java.util.stream.Stream;

public abstract class CommandUtil {

    public static Argument<PortSize> portSizeArgument(String nodeName) {
        return new CustomArgument<PortSize, String>(new StringArgument(nodeName), info -> {
            String rawInput = info.input();
            rawInput = rawInput.toUpperCase();
            PortSize portSize;

            try {
                portSize = PortSize.valueOf(rawInput);
            } catch (IllegalArgumentException e) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of("<Red>Invalid port size argument").build());
            }

            return portSize;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> Stream.of(PortSize.values()).map(PortSize::name).toList()));
    }
}
