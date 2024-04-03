package lab1.banks.transaction.commands;

import lab1.banks.account.Account;
import lab1.banks.exceptions.AccountException;
import lombok.Getter;

@Getter
public class TopUpCommand extends Command {
    private final Account account;
    private final double value;

    public TopUpCommand(Account account, double value) {
        this.account = account;
        this.value = value;
    }

    @Override
    protected void onExecute() {
        account.topUp(value);
    }

    @Override
    protected void onRevert() {
        try {
            account.withdraw(value);
        } catch (AccountException e) {
        }
    }
}
