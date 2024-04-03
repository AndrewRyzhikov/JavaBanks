package lab1.app.registry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lab1.app.Command;
import lab1.app.ObjectFormatter;
import org.jline.console.CmdDesc;
import org.jline.console.impl.AbstractCommandRegistry;
import org.jline.utils.AttributedStyle;

import lab1.app.ObjectFormatter.ObjectFormatterBuilder;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class AppCommandRegistry<T> extends AbstractCommandRegistry {
    @Getter(value = AccessLevel.PROTECTED)
    private ObjectFormatter<T> formatter;

    public AppCommandRegistry() {
        this(ObjectFormatter.<T>builder());
    }

    public AppCommandRegistry(ObjectFormatterBuilder<T> formatterBuilder) {
        super();
        this.formatter = formatterBuilder.build();
    }

    protected abstract Map<String, Command> getCmdInfo();

    protected void registerAppCommands() {
        registerCommands(
                getCmdInfo().entrySet().stream()
                        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getMethods())));
    }

    @Override
    public List<String> commandInfo(String command) {
        return getCmdInfo().get(command).getInfo();
    }

    @Override
    public CmdDesc commandDescription(List<String> args) {
        return getCmdInfo().get(args.get(0)).getDescription();
    }
}
