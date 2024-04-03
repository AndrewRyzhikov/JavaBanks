package lab1.banks.exceptions;

import lab1.banks.account.Account;

public class AccountNotFoundException extends AccountException {
    public AccountNotFoundException(String message, Account account) {
        super(message, account);
    }

    public AccountNotFoundException(Account account) {
        this("Could not find account", account);
    }
}
