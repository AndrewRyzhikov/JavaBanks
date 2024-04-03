package lab1.banks.account;

import lab1.banks.Bank;
import lab1.banks.Client;
import lab1.banks.exceptions.AccountException;

import lab1.banks.notifications.Notification;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Credit account can be overdrafted, but if it is credit commision is applied.
 */
@Getter
public class CreditAccount implements Account<CreditAccount> {
    private final double creditLimit;
    private final int id;
    private final Bank bank;
    private final Client client;
    private double money;
    private final List<Notification> notifications = new ArrayList<>();

    public CreditAccount(int id, Bank bank, Client client, double limit) {
        this.id = id;
        this.bank = bank;
        this.client = client;
        this.creditLimit = limit;
    }

    public CreditAccount AddNotification(Notification notification) {
        notifications.add(notification);
        return this;
    }

    public double getWithdrawCommission() {
        return money < 0 ? bank.getCreditCommissionFee() : 0;
    }

    public void topUp(double value) {
        money += value;
    }

    public void withdraw(double value) throws AccountException {
        money -= value;
        if (money < -creditLimit) {
            throw new AccountException("Credit limit exceeded", this);
        }
    }
}
