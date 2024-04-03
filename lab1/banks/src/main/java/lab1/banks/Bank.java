package lab1.banks;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lab1.banks.notifications.CreditCommission;
import lab1.banks.notifications.CreditLimit;
import lab1.banks.notifications.DeptPercent;
import lab1.banks.notifications.Notification;
import lab1.banks.account.Account;
import lab1.banks.account.CreditAccount;
import lab1.banks.account.DebtAccount;
import lab1.banks.account.DepositAccount;
import lab1.banks.exceptions.AccountException;
import lab1.banks.transaction.TopUp;
import lab1.banks.transaction.Transaction;
import lab1.banks.transaction.Transfer;
import lab1.banks.transaction.Withdraw;
import lombok.Getter;
import lombok.experimental.PackagePrivate;

/**
 * Represents a bank.
 * Holds all properties for new accounts such as debt interest, deposit
 * interests, credit limit, etc.
 * Used to create new {@link Account}s and perform {@link Transaction}s on them.
 */
@Getter
public class Bank {
    private final DepositRules depositRules;
    private final Map<Integer, Account> accounts = new HashMap<>();

    private final Map<DebtAccount, Double> debtInterests = new HashMap<>();
    private final Map<DepositAccount, Double> depositInterests = new HashMap<>();
    private final Map<CreditAccount, Double> commissions = new HashMap<>();
    private final Map<LocalDate, List<Transaction>> history = new HashMap<>();
    private final Map<Notification, List<Integer>> subscribers = new HashMap<>();

    private final DateWrapper today;

    private double debtPercent;
    private double creditCommissionFee;
    private double maxTransactionValueForUntrustworthy;
    private double creditLimit;

    private final int id;
    private final String name;
    private final int depositHoldDays;

    @PackagePrivate
    Bank(int id, String name, double debtPercent, double creditCommission, DepositRules depositRules,
         double restrictedAmount, DateWrapper date, int days, double creditLimit) {
        if (days <= 0) {
            throw new IllegalArgumentException("Hold days must be positive");
        }

        this.id = id;
        this.name = name;
        this.debtPercent = debtPercent;
        this.creditCommissionFee = creditCommission;
        this.maxTransactionValueForUntrustworthy = restrictedAmount;
        this.depositHoldDays = days;
        this.depositRules = depositRules;
        this.today = date;
        this.creditLimit = creditLimit;
    }

    void notify(Notification notification, double edit) {
        List<Integer> accountsSubs = subscribers.get(notification);

        for (int account : accountsSubs) {
            accounts.get(account).getClient().notify(notification.StateChanging() + String.format("%.2f", edit));
        }
    }

    public void setDebtPercent(double newInterest) {
        debtPercent = newInterest;
        notify(new DeptPercent(), newInterest);
    }

    public void setCreditCommissionFee(double newFee) {
        creditCommissionFee = newFee;
        notify(new CreditCommission(), newFee);
    }

    public void setCreditLimit(double newLimit) {
        creditLimit = newLimit;
        notify(new CreditLimit(), newLimit);
    }

    public void setMaxTransactionValueForUntrustworthy(double newRestriction) {
        maxTransactionValueForUntrustworthy = newRestriction;
        notify(new CreditLimit(), newRestriction);
    }

    /**
     * Returns a {@link List} of {@link Transaction}s at specified date
     */
    public List<Transaction> findTransactionsAt(LocalDate date) {
        return getTransactions(date);
    }

    /**
     * Creates a {@link DebtAccount} for {@link Client}.
     * <p>
     * Debt interest of this bank will be used for new account
     */
    public void createDebtAccount(Client client) {
        var newAccount = new DebtAccount(accounts.size(), this, client)
                .AddNotification(new DeptPercent());

        accounts.put(accounts.size(), newAccount);



        for (Notification notification : newAccount.getNotifications()) {
            if (!subscribers.containsValue(notification)) {
                subscribers.put(notification, new ArrayList<>());
            }
            subscribers.get(notification).add(accounts.size());
        }

        debtInterests.put(newAccount, 0d);
    }

    /**
     * Creates a {@link DepositAccount} for {@link Client}.
     * <p>
     * Deposit interests and hold days of this bank will be used for new account
     */
    public void createDepositAccount(Client client) {
        var newAccount = new DepositAccount(accounts.size(), this, client, today.getDate().plusDays(depositHoldDays))
                .AddNotification(new DeptPercent());

        depositInterests.put(newAccount, 0d);

        for (Notification notification : newAccount.getNotifications()) {
            if (!subscribers.containsValue(notification)) {
                subscribers.put(notification, new ArrayList<>());
            }
            subscribers.get(notification).add(accounts.size());
        }

        accounts.put(accounts.size(), newAccount);
    }

    /**
     * Creates a {@link CreditAccount} for {@link Client}.
     * <p>
     * Credit commision and limit of this bank will be used for new account
     */
    public void createCreditAccount(Client client) {
        var newAccount = new CreditAccount(accounts.size(), this, client, creditLimit)
                .AddNotification(new CreditLimit())
                .AddNotification(new CreditLimit());

        commissions.put(newAccount, 0d);

        for (Notification notification : newAccount.getNotifications()) {
            if (!subscribers.containsValue(notification)) {
                subscribers.put(notification, new ArrayList<>());
            }
            subscribers.get(notification).add(accounts.size());
        }

        accounts.put(accounts.size(), newAccount);
    }

    public Account getAccountByID(int id) {
        return accounts.get(id);
    }

    /**
     * Tops up an {@link Account}.
     *
     * @param account     Account that will be topped up. Must be in this bank.
     * @param value       Amount of money to put
     * @param description Optional description for {@link Transaction}
     * @return new completed {@link TopUp}
     * @throws AccountException when transaction cannot be performed or account could not be
     *                          found
     */
    public Transaction topUpAccount(Account account, double value, String description) throws AccountException {
        throwIfAccountNotFound(account);

        var topUp = new TopUp(account, value, description, today.getDate());
        topUp.make();
        getTransactions(today.getDate()).add(topUp);

        return topUp;
    }

    /**
     * Withdraws from an {@link Account}.
     *
     * @param account     Account that will be withdrawn. Must be in this bank.
     * @param value       Amount of money to put
     * @param description Optional description for {@link Transaction}
     * @return new completed {@link Withdraw}
     * @throws AccountException when transaction cannot be performed or account could not be
     *                          found
     */
    public Transaction withdrawFromAccount(Account account, double value, String description) throws AccountException {
        throwIfAccountNotFound(account);

        var withdraw = new Withdraw(account, value, description, today.getDate());
        withdraw.make();
        getTransactions(today.getDate()).add(withdraw);

        return withdraw;
    }

    /**
     * Tops up an {@link Account}.
     *
     * @param id          ID of an account in this bank that will be topped up.
     * @param value       Amount of money to put
     * @param description Optional description for {@link Transaction}
     * @return new completed {@link TopUp}
     * @throws AccountException when transaction cannot be performed
     */
    public Transaction topUpAccount(int id, double value, String description) throws AccountException {
        return topUpAccount(accounts.get(id), value, description);
    }

    /**
     * Withdraws from an {@link Account}.
     *
     * @param id          ID of an account in this bank that will be topped up.
     * @param value       Amount of money to put
     * @param description Optional description for {@link Transaction}
     * @return new completed {@link Withdraw}
     * @throws AccountException when transaction cannot be performed
     */
    public Transaction withdrawFromAccount(int id, double value, String description) throws AccountException {
        return withdrawFromAccount(accounts.get(id), value, description);
    }

    /**
     * @param from        Source {@link Account}. Must be in this bank
     * @param to          Destination {@link Account}
     * @param value       Amount of money to transfer
     * @param description Optional description for {@link Transaction}
     * @return new {@link Transfer} while adding it to history
     * @throws AccountException when transaction cannot be performed or account could not be
     *                          found
     */

    public Transfer sendTransfer(Account from, Account to, double value, String description) throws AccountException {
        throwIfAccountNotFound(from);
        var transfer = new Transfer(from, to, value, description, today.getDate());
        getTransactions(today.getDate()).add(transfer);
        return transfer;
    }

    /**
     * Adds {@link Transfer} to {@link Transaction} history
     */
    public void receiveTransfer(Transfer transfer) throws AccountException {
        throwIfAccountNotFound(transfer.getTo());

        getTransactions(today.getDate()).add(transfer);
    }

    /**
     * This method should be executed daily
     */
    public void daily() {
        for (DebtAccount account : debtInterests.keySet()) {
            debtInterests.put(account, debtInterests.get(account) + account.getMoney() * debtPercent);
        }

        for (DepositAccount account : depositInterests.keySet()) {
            depositInterests.put(account,
                    depositInterests.get(account) + account.getMoney() * depositRules.getInterest(account.getMoney()));
        }

        for (CreditAccount account : commissions.keySet()) {
            if (account.getMoney() < 0) {
                commissions.put(account, commissions.get(account) + Math.abs(account.getMoney() * creditCommissionFee));
            }
        }
    }

    /**
     * This method should be executed when day is reverted
     */
    public void revertDay() {
        for (DebtAccount account : debtInterests.keySet()) {
            debtInterests.put(account, debtInterests.get(account) - account.getMoney() * debtPercent);
        }

        for (DepositAccount account : depositInterests.keySet()) {
            depositInterests.put(account,
                    depositInterests.get(account) - account.getMoney() * depositRules.getInterest(account.getMoney()));
        }

        for (CreditAccount account : commissions.keySet()) {
            commissions.put(account, commissions.get(account) - account.getMoney() * creditCommissionFee);
        }
    }

    /**
     * This method should be executed monthly.
     * Performs monthly {@link Transaction}s
     *
     * @throws AccountException when transaction could not be performed
     */
    public void monthly() throws AccountException {
        for (DebtAccount account : debtInterests.keySet()) {
            topUpAccount(account, debtInterests.get(account), "Interest Accrual");
            debtInterests.put(account, 0d);
        }

        for (DepositAccount account : depositInterests.keySet()) {
            topUpAccount(account, depositInterests.get(account), "Interest Accrual");
            depositInterests.put(account, 0d);
        }

        for (CreditAccount account : commissions.keySet()) {
            withdrawFromAccount(account, commissions.get(account), "Commision Fee");
            commissions.put(account, 0d);
        }
    }

    private void throwIfAccountNotFound(Account account) throws AccountException {
        if (!accounts.containsValue(account)) {
            throw new AccountException("Could not find account in bank", account);
        }
    }

    private List<Transaction> getTransactions(LocalDate date) {
        if (!history.containsKey(date)) {
            history.put(date, new ArrayList<Transaction>());
        }

        return history.get(date);
    }
}
