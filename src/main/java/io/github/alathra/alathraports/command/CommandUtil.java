package io.github.alathra.alathraports.command;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.ports.Port;
import io.github.alathra.alathraports.ports.PortSize;
import io.github.alathra.alathraports.ports.PortsManager;

public abstract class CommandUtil {

    public static Argument<PortSize> portSizeArgument(String nodeName) {
        return new CustomArgument<PortSize, String>(new StringArgument(nodeName), info -> {
            PortSize size = PortsManager.getPortSizeByName(info.input());
            if (size == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of("<Red>Invalid port size argument").build());
            }
            return size;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info ->
            Settings.sizes.keySet().stream().toList()
        ));
    }

    public static Argument<Port> portArgument(String nodeName) {
        return new CustomArgument<Port, String>(new StringArgument(nodeName), info -> {
            Port port = PortsManager.getPortByName(info.input());
            if (port == null) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of("<Red>Invalid port name argument").build());
            }
            return port;
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
            PortsManager.getPorts().stream().map(Port::getName).toArray(String[]::new)
        ));
    }
}
