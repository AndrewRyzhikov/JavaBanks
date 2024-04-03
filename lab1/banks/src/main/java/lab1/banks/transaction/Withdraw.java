package lab1.banks.transaction;

import java.time.LocalDate;

import lab1.banks.account.Account;
import lab1.banks.transaction.commands.WithdrawCommand;
import lab1.banks.exceptions.AccountException;

public class Withdraw extends Transaction {
    public Withdraw(Account account, double value, String description, LocalDate date) throws AccountException {
        super(new WithdrawCommand(account, value), account, value, description, date);
        this.getCommand().chain(new WithdrawCommand(account, account.getWithdrawCommission() * value));
    }
}
