package lab1.app.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab1.app.Command;
import lab1.app.CommandException;
import lab1.app.CommandHandler;
import lab1.app.ObjectFormatter;
import org.jline.console.CmdDesc;
import org.jline.console.CommandInput;
import org.jline.console.CommandMethods;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.utils.AttributedString;

import lab1.app.ObjectFormatter.ObjectFormatterBuilder;
import lab1.banks.Client;
import lombok.AccessLevel;
import lombok.Getter;

public class ClientCommandRegistry extends AppCommandRegistry<Client> {
    @Getter
    private Map<String, Client> clientMap;
    @Getter(value = AccessLevel.PROTECTED)
    private Map<String, Command> cmdInfo = Map.of(
            "add",
            new Command(List.of("Add client to registry"),
                    new CmdDesc(
                            List.of(new AttributedString(
                                    "Prompts user for information about new client and adds it to registry")),
                            List.of(),
                            Map.of()),
                    new CommandMethods(this::add,
                            cmd -> List
                                    .of(new ArgumentCompleter(NullCompleter.INSTANCE, NullCompleter.INSTANCE)))),
            "list", new Command(List.of("List clients"),
                    new CmdDesc(
                            List.of(new AttributedString(
                                    "Print all clients from a registry")),
                            List.of(),
                            Map.of()),
                    new CommandMethods(this::list,
                            cmd -> List.of(new ArgumentCompleter(NullCompleter.INSTANCE, NullCompleter.INSTANCE)))));

    public ClientCommandRegistry() {
        this(ObjectFormatter.builder());
    }

    public ClientCommandRegistry(ObjectFormatterBuilder<Client> formatterBuilder) {
        super(formatterBuilder
                .withFieldSelector("Name", client -> client.getName())
                .withFieldSelector("Surname", client -> client.getSurname())
                .withFieldSelector("Address",
                        client -> client.getAddress().isBlank() ? "Not specified" : client.getAddress())
                .withFieldSelector("Passport",
                        client -> client.getPassport().isBlank() ? "Not specified" : client.getPassport()));
        registerAppCommands();
        clientMap = new HashMap<>();
    }

    private CommandHandler add(CommandInput cmdInput) {
        return reader -> {
            var nameAndSurname = reader.readLine("Provide name and surname: ").split(" ", 2);
            if (nameAndSurname[0].isBlank() || nameAndSurname[1].isBlank()) {
                throw new CommandException("Name and surname must not be empty");
            }

            var address = reader.readLine("Provide address (can be empty): ");
            var passport = reader.readLine("Provide passport (can be empty): ");

            clientMap.put(nameAndSurname[0], new Client(nameAndSurname[0], nameAndSurname[1], new ArrayList<>(), address, passport));
        };
    }

    private CommandHandler list(CommandInput cmdInput) {
        return reader -> {
            clientMap.values().forEach(client -> {
                reader.printAbove(getFormatter().format(client));
                reader.printAbove("");
            });
        };
    }
}
