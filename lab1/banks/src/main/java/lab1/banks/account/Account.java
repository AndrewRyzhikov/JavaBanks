package lab1.banks.account;

import lab1.banks.Bank;
import lab1.banks.Client;
import lab1.banks.exceptions.AccountException;
import lab1.banks.notifications.Notification;
import lab1.banks.transaction.Transaction;

import java.util.List;

/**
 * Holds information about account record in {@link Bank}.
 * {@link Transaction}s should be performed using {@link Bank}
 */
public interface Account<T> {
    int getId();

    Bank getBank();

    Client getClient();

    double getMoney();

    double getWithdrawCommission();
    T AddNotification(Notification notification);
    /**
     * Withdraw money from an {@link Account}
     * 
     * @param value
     *            Amount of money to withdraw
     * @throws AccountException
     *             in case withdrawal cannot be performed
     */
    void withdraw(double value) throws AccountException;

    /**
     * Top up an {@link Account}
     * 
     * @param value
     *            Amount of money to put
     */
    void topUp(double value);
}
