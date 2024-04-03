package lab1.banks.transaction.commands;

import lab1.banks.account.Account;
import lab1.banks.exceptions.AccountException;
import lombok.Getter;

@Getter
public class WithdrawCommand extends Command {
    private final Account account;
    private final double value;

    public WithdrawCommand(Account account, double value) {
        this.account = account;
        this.value = value;
    }

    @Override
    protected void onExecute() throws AccountException {
        account.withdraw(value);
    }

    @Override
    protected void onRevert() {
        account.topUp(value);
    }
}
