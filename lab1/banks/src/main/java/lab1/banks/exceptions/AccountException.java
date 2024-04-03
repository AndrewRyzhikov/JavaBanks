package lab1.banks.exceptions;

import lab1.banks.account.Account;
import lombok.Getter;

@Getter
public class AccountException extends Exception {
    private final Account account;

    public AccountException(String message, Account account) {
        super(message);
        this.account = account;
    }
}
