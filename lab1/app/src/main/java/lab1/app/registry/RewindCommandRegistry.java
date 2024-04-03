package lab1.app.registry;

import java.util.List;
import java.util.Map;

import lab1.app.Command;
import lab1.app.CommandException;
import lab1.app.CommandHandler;
import org.jline.console.ArgDesc;
import org.jline.console.CmdDesc;
import org.jline.console.CommandInput;
import org.jline.console.CommandMethods;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;

import lab1.banks.CentralBank;
import lab1.banks.TimeMachine;
import lab1.banks.TimeRewind;
import lombok.AccessLevel;
import lombok.Getter;

public class RewindCommandRegistry extends AppCommandRegistry<TimeRewind> {
    @Getter
    private TimeMachine timeMachine = new TimeMachine();

    @Getter(value = AccessLevel.PROTECTED)
    private Map<String, Command> cmdInfo = Map.of(
            "forward",
            new Command(List.of("Rewind forward"),
                    new CmdDesc(
                            List.of(new AttributedString(
                                    "Rewind time to the future")),
                            List.of(
                                    new ArgDesc("amount",
                                            List.of(new AttributedString(
                                                    "Amount of days/months/years to rewind (days is default)"))),
                                    new ArgDesc("unit", List.of(new AttributedString("Unit of time")))),
                            Map.of()),
                    new CommandMethods(this::forward,
                            cmd -> List
                                    .of(new ArgumentCompleter(
                                            NullCompleter.INSTANCE,
                                            NullCompleter.INSTANCE,
                                            new StringsCompleter("days", "months", "years"),
                                            NullCompleter.INSTANCE)))),
            "backward", new Command(List.of("Rewind backward"),
                    new CmdDesc(
                            List.of(new AttributedString(
                                    "Rewind time to the past")),
                            List.of(
                                    new ArgDesc("amount",
                                            List.of(new AttributedString(
                                                    "Amount of days/months/years to rewind (days is default)"))),
                                    new ArgDesc("unit", List.of(new AttributedString("Unit of time")))),
                            Map.of()),
                    new CommandMethods(this::backward,
                            cmd -> List
                                    .of(new ArgumentCompleter(
                                            NullCompleter.INSTANCE,
                                            NullCompleter.INSTANCE,
                                            new StringsCompleter("days", "months", "years"),
                                            NullCompleter.INSTANCE)))));

    public RewindCommandRegistry(CentralBank centralBank) {
        super();
        timeMachine.subscribe(centralBank);
        registerAppCommands();
    }

    @Override
    public String name() {
        return "Bank management";
    }

    private int getDaysFromInput(CommandInput input) {
        var args = input.args();
        if (args.length < 1) {
            throw new CommandException("Not enough arguments: at least amount of days must be specified");
        }
        var multiplier = 1;
        if (args.length > 1) {
            switch (args[1]) {
                case "days":
                    break;
                case "months":
                    multiplier = 30;
                    break;
                case "years":
                    multiplier = 365;
                    break;
            }
        }
        var days = Integer.parseInt(args[0]);
        return days * multiplier;
    }

    private CommandHandler forward(CommandInput input) {
        return reader -> {
            timeMachine.rewindToTheFuture(getDaysFromInput(input));
        };
    }

    private CommandHandler backward(CommandInput input) {
        return reader -> {
            timeMachine.rewindToThePast(getDaysFromInput(input));
        };
    }
}
