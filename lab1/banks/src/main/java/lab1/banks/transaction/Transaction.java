package lab1.banks.transaction;

import java.time.LocalDate;

import lab1.banks.account.Account;
import lab1.banks.transaction.commands.Command;
import lab1.banks.exceptions.AccountException;
import lombok.Getter;

@Getter
public class Transaction {
    private final double amount;
    private final String description;
    private final LocalDate transactionLocalDate;
    private final Command command;
    private final Account initiator;

    protected Transaction(Command command, Account initiator, double amount, String description, LocalDate date)
            throws AccountException {
        this.command = command;
        this.initiator = initiator;
        this.amount = amount;
        this.description = description;
        this.transactionLocalDate = date;

        if (amount > initiator.getBank().getMaxTransactionValueForUntrustworthy()
                && !initiator.getClient().isTrustworthy()) {
            throw new AccountException("Client is untrustworthy to make this transaction", initiator);
        }
    }

    public boolean isReverted() {
        return command.getState() == Command.CommandState.Reverted;
    }

    public void make() throws AccountException {
        command.execute();
    }

    public void revert() {
        command.totalRevert();
    }
}
