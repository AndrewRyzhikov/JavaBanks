package lab1.banks.transaction;

import java.time.LocalDate;

import lab1.banks.account.Account;
import lab1.banks.transaction.commands.TopUpCommand;
import lab1.banks.exceptions.AccountException;

public class TopUp extends Transaction {
    public TopUp(Account account, double value, String description, LocalDate date) throws AccountException {
        super(new TopUpCommand(account, value), account, value, description, date);
    }
}
