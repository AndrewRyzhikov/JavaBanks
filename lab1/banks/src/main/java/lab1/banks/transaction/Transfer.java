package lab1.banks.transaction;

import java.time.LocalDate;

import lab1.banks.account.Account;
import lab1.banks.transaction.commands.TopUpCommand;
import lab1.banks.transaction.commands.WithdrawCommand;
import lab1.banks.exceptions.AccountException;
import lombok.Getter;

@Getter
public class Transfer extends Transaction {
    private final Account to;
    public Transfer(Account from, Account to, double value, String description, LocalDate date)
            throws AccountException {
        super(new WithdrawCommand(from, value), from, value, description, date);

        this.to = to;

        this.getCommand().chain(new WithdrawCommand(from, from.getWithdrawCommission() * value));
        this.getCommand().chain(new TopUpCommand(to, value));
    }
}
