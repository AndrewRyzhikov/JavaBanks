package lab1.app.registry;

import java.util.List;
import java.util.Map;

import lab1.app.Command;
import lab1.app.CommandException;
import lab1.app.CommandHandler;
import lab1.app.ObjectFormatter;
import lab1.app.completer.AccountCompleter;
import lab1.app.completer.BankCompleter;
import lab1.app.completer.ClientCompleter;
import org.jline.console.ArgDesc;
import org.jline.console.CmdDesc;
import org.jline.console.CommandInput;
import org.jline.console.CommandMethods;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;

import lab1.app.ObjectFormatter.ObjectFormatterBuilder;
import lab1.banks.account.Account;
import lab1.banks.Bank;
import lab1.banks.CentralBank;
import lab1.banks.Client;
import lab1.banks.exceptions.AccountException;
import lombok.AccessLevel;
import lombok.Getter;

public class AccountCommandRegistry extends AppCommandRegistry<Account> {
    private CentralBank centralBank;
    @Getter
    private Map<String, Client> clientMap;
    private BankCompleter bankCompleter;
    private ClientCompleter clientCompleter;
    private AccountCompleter accountCompleter;
    @Getter(value = AccessLevel.PROTECTED)
    private Map<String, Command> cmdInfo = Map.of(
            "add",
            new Command(List.of("Add account to bank"),
                    new CmdDesc(
                            List.of(new AttributedString(
                                    "Creates new account and adds it to bank")),
                            List.of(new ArgDesc(""), new ArgDesc("bank",
                                    List.of(new AttributedString("Bank where new account would be created"))),
                                    new ArgDesc("client", List.of(new AttributedString("Owner of the account"))),
                                    new ArgDesc("type", List.of(new AttributedString("Type of the account")))),
                            Map.of()),
                    new CommandMethods(this::add,
                            cmd -> List.of(new ArgumentCompleter(
                                    NullCompleter.INSTANCE,
                                    bankCompleter,
                                    clientCompleter,
                                    new StringsCompleter("debt", "deposit", "credit"),
                                    NullCompleter.INSTANCE)))),
            "list",
            new Command(List.of("List accounts"),
                    new CmdDesc(
                            List.of(new AttributedString(
                                    "Print all accounts from a registry")),
                            List.of(new ArgDesc(""),
                                    new ArgDesc("bank", List.of(new AttributedString("List accounts in a bank")))),
                            Map.of()),
                    new CommandMethods(this::list,
                            cmd -> List.of(new ArgumentCompleter(
                                    NullCompleter.INSTANCE,
                                    bankCompleter,
                                    NullCompleter.INSTANCE)))),
            "topup",
            new Command(List.of("Top up an account"),
                    new CmdDesc(
                            List.of(new AttributedString("Tops up an account to specified amount of money")),
                            List.of(new ArgDesc(""),
                                    new ArgDesc("account",
                                            List.of(new AttributedString("Account which will be topped up"))),
                                    new ArgDesc("amount", List.of(new AttributedString("Amount of money to add")))),
                            Map.of()),
                    new CommandMethods(this::topup,
                            cmd -> List.of(new ArgumentCompleter(
                                    NullCompleter.INSTANCE,
                                    accountCompleter,
                                    NullCompleter.INSTANCE)))),
            "withdraw",
            new Command(List.of("Withdraw an account"),
                    new CmdDesc(
                            List.of(new AttributedString("Withdraws specified amount of money from an")),
                            List.of(new ArgDesc(""),
                                    new ArgDesc("account",
                                            List.of(new AttributedString("Account which will be withdrawn"))),
                                    new ArgDesc("amount",
                                            List.of(new AttributedString("Amount of money to withdraw")))),
                            Map.of()),
                    new CommandMethods(this::withdraw,
                            cmd -> List.of(new ArgumentCompleter(
                                    NullCompleter.INSTANCE,
                                    accountCompleter,
                                    NullCompleter.INSTANCE)))),
            "transfer",
            new Command(List.of("Perform a transfer"),
                    new CmdDesc(
                            List.of(new AttributedString(
                                    "Withdraws specified amount of money from an account and adds to another")),
                            List.of(new ArgDesc(""),
                                    new ArgDesc("from",
                                            List.of(new AttributedString("Account which will be withdrawn"))),
                                    new ArgDesc("to",
                                            List.of(new AttributedString("Account which will be topped up"))),
                                    new ArgDesc("amount",
                                            List.of(new AttributedString("Amount of money to transfer")))),
                            Map.of()),
                    new CommandMethods(this::transfer,
                            cmd -> List.of(new ArgumentCompleter(
                                    NullCompleter.INSTANCE,
                                    accountCompleter,
                                    accountCompleter,
                                    new StringsCompleter("aldla", "gfds", "Gfdsa"),
                                    NullCompleter.INSTANCE)))));

    public AccountCommandRegistry(CentralBank centralBank, Map<String, Client> clientMap) {
        this(centralBank, clientMap, ObjectFormatter.builder());
    }

    public AccountCommandRegistry(CentralBank centralBank, Map<String, Client> clientMap,
            ObjectFormatterBuilder<Account> formatterBuilder) {
        super(formatterBuilder
                .withFieldSelector("Bank", account -> account.getBank().getName())
                .withFieldSelector("ID", account -> account.getId())
                .withFieldSelector("Type", account -> account.getClass().getSimpleName())
                .withFieldSelector("Money", account -> String.format("%.2f", account.getMoney()))
                .withFieldSelector("Client", account -> String.format("%s %s", account.getClient().getName(),
                        account.getClient().getSurname())));
        registerAppCommands();
        this.centralBank = centralBank;
        this.clientMap = clientMap;
        this.bankCompleter = new BankCompleter(centralBank);
        this.clientCompleter = new ClientCompleter(clientMap);
        this.accountCompleter = new AccountCompleter(centralBank);
    }

    private Bank parseBank(String bankName) {
        return centralBank.findBankByName(bankName)
                .orElseThrow(() -> new CommandException(String.format("Bank '%s' does not exist", bankName)));
    }

    private Account parseAccount(String accountId) {
        var parts = accountId.split("\\.", 2);
        var bankName = parts[0];
        var id = Integer.parseInt(parts[1]);
        return parseBank(bankName).getAccountByID(id);
    }

    private CommandHandler add(CommandInput cmdInput) {
        return reader -> {
            var args = cmdInput.args();
            if (args.length < 3) {
                throw new CommandException("Missing arguments, at least three must be provided");
            }
            var bank = parseBank(args[0]);

            var client = clientMap.getOrDefault(args[1], null);
            if (client == null) {
                throw new CommandException(String.format("Client %s is not found", args[1]));
            }

            switch (args[2]) {
                case "deposit":
                    bank.createDepositAccount(client);
                    break;
                case "credit":
                    bank.createCreditAccount(client);
                    break;
                case "debt":
                    bank.createDebtAccount(client);
                    break;
                default:
                    throw new CommandException(String.format("Account type %s is invalid", args[2]));
            }
        };
    }

    private CommandHandler list(CommandInput cmdInput) {
        return reader -> {
            var args = cmdInput.args();
            if (args.length < 1) {
                centralBank.getBanks().values().stream().flatMap(bank -> bank.getAccounts().values().stream())
                        .forEachOrdered(acc -> {
                            reader.printAbove(getFormatter().format(acc));
                            reader.printAbove("");
                        });
                return;
            }

            parseBank(args[0]).getAccounts().values().forEach(acc -> {
                reader.printAbove(getFormatter().format(acc));
                reader.printAbove("");
            });
        };
    }

    private CommandHandler topup(CommandInput cmdInput) {
        return reader -> {
            var args = cmdInput.args();
            if (args.length < 2) {
                throw new CommandException("Not enough arguments: account and amount must be provided");
            }
            String description = "Account top up";
            if (args.length > 2) {
                description = args[2];
            }
            var account = parseAccount(args[0]);
            var amount = Double.parseDouble(args[1]);
            try {
                account.getBank().topUpAccount(account, amount, description);
            } catch (AccountException e) {
                throw new CommandException(e.getMessage());
            }
        };
    }

    private CommandHandler withdraw(CommandInput cmdInput) {
        return reader -> {
            var args = cmdInput.args();
            if (args.length < 2) {
                throw new CommandException("Not enough arguments: account and amount must be provided");
            }
            String description = "Account withdrawal";
            if (args.length > 2) {
                description = args[2];
            }
            var account = parseAccount(args[0]);
            var amount = Double.parseDouble(args[1]);
            try {
                account.getBank().withdrawFromAccount(account, amount, description);
            } catch (AccountException e) {
                throw new CommandException(e.getMessage());
            }
        };
    }

    private CommandHandler transfer(CommandInput cmdInput) {
        return reader -> {
            var args = cmdInput.args();
            if (args.length < 3) {
                throw new CommandException(
                        "Not enough arguments: source and destination account and amount must be provided");
            }
            String description = "Transfer";
            if (args.length > 3) {
                description = args[3];
            }
            var from = parseAccount(args[0]);
            var to = parseAccount(args[1]);
            var amount = Double.parseDouble(args[2]);
            try {
                from.getClient().performTransfer(from, to, amount, description);
            } catch (AccountException e) {
                throw new CommandException(e.getMessage());
            }
        };
    }
}
