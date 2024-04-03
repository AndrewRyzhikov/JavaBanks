package lab1.app;

import java.io.IOException;
import java.util.ArrayList;

import lab1.app.registry.AccountCommandRegistry;
import lab1.app.registry.BankCommandRegistry;
import lab1.app.registry.ClientCommandRegistry;
import lab1.app.registry.RewindCommandRegistry;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.widget.TailTipWidgets;
import org.jline.widget.TailTipWidgets.TipType;
import org.jline.widget.Widgets;

import lab1.app.ObjectFormatter.ObjectFormatterBuilder;
import lab1.banks.Client;
import lab1.banks.DepositRules;

public class App {
    public static void main(String[] args) throws IOException {
        Terminal term = TerminalBuilder.terminal();
        var systemRegistry = new SystemRegistryImpl(new DefaultParser(), term, null, null);
        systemRegistry.setCommandRegistries();

        var formatterBuilder = new ObjectFormatterBuilder<Object>()
                .withFieldStyle(
                        new AttributedStyle().foreground(AttributedStyle.BLUE).bold())
                .withValueStyle(new AttributedStyle().foreground(AttributedStyle.GREEN));

        var bankCmds = new BankCommandRegistry(formatterBuilder.forType());
        var clientCmds = new ClientCommandRegistry(formatterBuilder.forType());
        var accountCmds = new AccountCommandRegistry(bankCmds.getCentralBank(), clientCmds.getClientMap(),
                formatterBuilder.forType());
        var rewindCmds = new RewindCommandRegistry(bankCmds.getCentralBank());

        systemRegistry.register("bank", bankCmds);
        systemRegistry.register("client", clientCmds);
        systemRegistry.register("account", accountCmds);
        systemRegistry.register("rewind", rewindCmds);

        var reader = LineReaderBuilder.builder().terminal(term)
                .completer(systemRegistry.completer()).build();

        var w = new TailTipWidgets(reader, systemRegistry::commandDescription, 5, TipType.COMPLETER);
        KeyMap<Binding> keyMap = reader.getKeyMaps().get("main");
        keyMap.bind(new Reference(Widgets.TAILTIP_TOGGLE), KeyMap.alt("a"));
        w.enable();

        var rules = new DepositRules(0.01);
        rules.addMilestone(100d, 0.03);
        rules.addMilestone(500d, 0.05);
        rules.addMilestone(1000d, 0.07);
        rules.addMilestone(10000d, 0.1);
        bankCmds.getCentralBank().addBank("Tinkoff", 0.01, 0.07, rules, 5000, 70, 1000d);
        bankCmds.getCentralBank().addBank("Sberbank", 0.03, 0.05, rules, 1500, 300, 2000d);
        clientCmds.getClientMap().put("Ivan", new Client("Ivan", "Ivanov",  new ArrayList<>(),"", "12345"));
        clientCmds.getClientMap().put("Artem", new Client("Artem", "Kuznetsov", new ArrayList<>(),"Wall st.", "11111"));

        bankCmds.getCentralBank().getBankByID(0).createDebtAccount(clientCmds.getClientMap().get("Ivan"));
        bankCmds.getCentralBank().getBankByID(1).createCreditAccount(clientCmds.getClientMap().get("Artem"));

        while (true) {
            try {
                systemRegistry.cleanUp();
                var line = reader.readLine("> ");
                CommandHandler result = (CommandHandler) systemRegistry.execute(line);
                if (result != null) {
                    result.handle(reader);
                }
            } catch (CommandException e) {
                reader.printAbove(new AttributedString(e.getMessage(),
                        new AttributedStyle().foreground(AttributedStyle.RED).bold()));
            } catch (UserInterruptException | EndOfFileException e) {
                break;
            } catch (Exception | Error e) {
                systemRegistry.trace(true, e);
            }
        }
        systemRegistry.close();
        term.close();
    }
}
