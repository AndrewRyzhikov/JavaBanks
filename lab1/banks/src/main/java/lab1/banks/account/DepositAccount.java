package lab1.banks.account;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lab1.banks.Bank;
import lab1.banks.Client;
import lab1.banks.DepositRules;
import lab1.banks.exceptions.AccountException;
import lab1.banks.notifications.Notification;
import lombok.Getter;

/**
 * Interest of this account is dtermined by {@link DepositRules} and amount of
 * money held. Cannot bw withdrawn for some period of time.
 */
@Getter
public class DepositAccount implements Account<DepositAccount> {
    private final LocalDate holdExpiration;
    private final int id;
    private final Bank bank;
    private final Client client;
    private double money = 0;
    private final List<Notification> notifications = new ArrayList<>();

    public DepositAccount(int id, Bank bank, Client client, LocalDate expiration) {
        this.id = id;
        this.bank = bank;
        this.client = client;
        this.holdExpiration = expiration;
    }

    public DepositAccount AddNotification(Notification notification) {
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

        if (bank.getToday().getDate().isAfter(holdExpiration)) {
            throw new AccountException("Hold period has not ended yet", this);
        }
    }
}
