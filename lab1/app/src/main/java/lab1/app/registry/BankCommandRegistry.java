package lab1.app.registry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lab1.app.Command;
import lab1.app.CommandHandler;
import lab1.app.ObjectFormatter;
import org.jline.console.CmdDesc;
import org.jline.console.CommandInput;
import org.jline.console.CommandMethods;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import lab1.app.ObjectFormatter.ObjectFormatterBuilder;
import lab1.banks.Bank;
import lab1.banks.CentralBank;
import lab1.banks.DepositRules;
import lombok.AccessLevel;
import lombok.Getter;

public class BankCommandRegistry extends AppCommandRegistry<Bank> {
    @Getter
    private CentralBank centralBank = new CentralBank();

    @Getter(value = AccessLevel.PROTECTED)
    private Map<String, Command> cmdInfo = Map.of(
            "add",
            new Command(List.of("Add bank to registry"),
                    new CmdDesc(
                            List.of(new AttributedString(
                                    "Prompts user for information about new bank and adds it to registry")),
                            List.of(),
                            Map.of()),
                    new CommandMethods(this::add,
                            cmd -> List
                                    .of(new ArgumentCompleter(NullCompleter.INSTANCE, NullCompleter.INSTANCE)))),
            "list", new Command(List.of("List banks"),
                    new CmdDesc(
                            List.of(new AttributedString(
                                    "Print all banks from a registry")),
                            List.of(),
                            Map.of()),
                    new CommandMethods(this::list,
                            cmd -> List.of(new ArgumentCompleter(NullCompleter.INSTANCE, NullCompleter.INSTANCE)))));

    public BankCommandRegistry() {
        this(ObjectFormatter.builder());
    }

    public BankCommandRegistry(ObjectFormatterBuilder<Bank> formatterBuilder) {
        super(formatterBuilder
                .withFieldSelector("ID", bank -> bank.getId())
                .withFieldSelector("Name", bank -> bank.getName())
                .withFieldSelector("Accounts", bank -> bank.getAccounts()
                        .values()
                        .stream()
                        .map(acc -> String.format(
                                "%s owned by %s %s with %.2f$",
                                acc.getClass().getSimpleName(),
                                acc.getClient().getName(),
                                acc.getClient().getSurname(),
                                acc.getMoney()))
                        .collect(Collectors.joining(", ", "[", "]")))
                .withFieldSelector("Deposit rules",
                        bank -> bank.getDepositRules().getProcents().entrySet().stream()
                                .map(entry -> String.format("%.2f%% from %.2f$", entry.getValue() * 100,
                                        entry.getKey()))
                                .collect(Collectors.joining(", ", "{", "}")))
                .withFieldSelector("Commision", bank -> String.format("%.2f%%", bank.getCreditCommissionFee() * 100))
                .withFieldSelector("Debt interest", bank -> String.format("%.2f%%", bank.getDebtPercent() * 100))
                .withFieldSelector("Credit limit", bank -> String.format("%.2f$", bank.getCreditLimit()))
                .withFieldSelector("Allowed transaction amount for unverified",
                        bank -> String.format("%.2f", bank.getMaxTransactionValueForUntrustworthy())));
        registerAppCommands();
    }

    @Override
    public String name() {
        return "Bank management";
    }

    private CommandHandler add(CommandInput cmdInput) {
        return reader -> {
            var name = reader.readLine("Enter a bank name: ");
            var debtProcent = Double.parseDouble(reader.readLine("Enter a debt procent: ")) / 100;
            var creditCommision = Double.parseDouble(reader.readLine("Enter credit commision fee: ")) / 100;
            var restrictedAmount = Double
                    .parseDouble(reader.readLine("Enter maximum allowed transaction for unverified clients: "));
            var holdDays = Integer.parseInt(reader.readLine("Enter amount of days for holding deposit accounts: "));

            reader.printAbove("Enter deposit rules, you can stop by entering an empty string or 'enough'");
            reader.printAbove(
                    "Milestones are specified in format 'n m', where n is a milestone and m is interest, both are floating point numbers");
            String input = "@";
            double currentMilestone = 0d;
            double currentInterest = Double.parseDouble(reader.readLine("Enter initial deposit interest: ")) / 100;
            double creditLimit = Double.parseDouble(reader.readLine("Enter credit limit: "));
            var rules = new DepositRules(currentInterest);

            while (true) {
                input = reader.readLine(
                        String.format("(current milestone %.2f%% at %.2f$+): ",
                                currentInterest * 100, currentMilestone));
                if (input.isBlank() || input == "enough") {
                    break;
                }
                var values = input.split(" ", 2);
                double nextMilestone = Double.parseDouble(values[0]);
                if (nextMilestone <= currentMilestone) {
                    reader.printAbove(new AttributedString("Milestones should be specified in ascending order.",
                            new AttributedStyle().foreground(AttributedStyle.YELLOW)));
                    continue;
                }
                currentInterest = Double.parseDouble(values[1]) / 100;
                currentMilestone = nextMilestone;
                rules.addMilestone(currentMilestone, currentInterest);
            }

            centralBank.addBank(name, debtProcent, creditCommision, rules, restrictedAmount, holdDays, creditLimit);
        };
    }

    private CommandHandler list(CommandInput input) {
        return reader -> {
            centralBank.getBanks().values().forEach(bank -> {
                reader.printAbove(getFormatter().format(bank));
                reader.printAbove("");
            });
        };
    }
}
