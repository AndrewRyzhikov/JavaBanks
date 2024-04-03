package lab1.banks.account;

import lab1.banks.Bank;
import lab1.banks.Client;
import lab1.banks.exceptions.AccountException;
import lab1.banks.notifications.Notification;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Debt account has simple monthly passive income
 */
@Getter
public class DebtAccount implements Account<DebtAccount> {
    private final int id;
    private final Bank bank;
    private final Client client;
    private double money;
    private final List<Notification> notifications = new ArrayList<>();

    public DebtAccount(int id, Bank bank, Client client) {
        this.id = id;
        this.bank = bank;
        this.client = client;
    }

    public DebtAccount AddNotification(Notification notification) {
        notifications.add(notification);
        return this;
    }
    public double getWithdrawCommission() {
        return 0;
    }

    public void topUp(double value) {
        money += value;
    }

    public void withdraw(double value) throws AccountException {
        money -= value;
        if (money < 0) {
            throw new AccountException("Not enough money", this);
        }
    }
}
