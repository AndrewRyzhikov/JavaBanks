package lab1.banks;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import lab1.banks.account.Account;
import lab1.banks.account.CreditAccount;
import lab1.banks.account.DebtAccount;
import lab1.banks.account.DepositAccount;
import lab1.banks.exceptions.AccountException;
import lab1.banks.transaction.Transaction;
import lab1.banks.transaction.Transfer;
import lombok.Getter;

/**
 * Manages list of {@link Bank}s. Responsible for making interbank
 * {@link Transfer}s.
 * Can be subscribed to {@link TimeMachine} for listening on time events.
 */
@Getter
public class CentralBank implements Observer<TimeRewind> {
    private final Map<Integer, Bank> banks = new TreeMap<Integer, Bank>();
    private final DateWrapper today;

    public CentralBank(LocalDate today) {
        this.today = new DateWrapper(today);
    }

    public CentralBank() {
        this(LocalDate.now());
    }

    /**
     * Adds {@link Bank}
     *
     * @param name
     *            A name for new {@link Bank}
     * @param debtProcent
     *            Debt procent for {@link DebtAccount}s
     * @param creditCommission
     *            Credit commision which will be withdrawn when
     *            {@link CreditAccount} has negative amount of money
     * @param depositRules
     *            Deposit interests for {@link DepositAccount}s
     * @param restrictedAmount
     *            Maximum amount of money that could be used for unverified
     *            {@link Client}
     * @param holdDays
     *            Amount of days {@link DepositAccount} could not be withdrawn
     * @param creditLimit
     *            Credit limit for {@link CreditAccount}
     * @return added {@link Bank}
     */
    public Bank addBank(String name, double debtProcent, double creditCommission, DepositRules depositRules,
            double restrictedAmount, int holdDays, double creditLimit) {
        if (banks.values().stream().anyMatch(bank -> bank.getName() == name)) {
            throw new IllegalArgumentException(String.format("Bank with name %s already exists", name));
        }

        var newBank = new Bank(banks.size(), name, debtProcent, creditCommission, depositRules, restrictedAmount, today,
                holdDays, creditLimit);
        banks.put(banks.size(), newBank);
        return newBank;
    }

    public Bank getBankByID(int id) {
        return banks.get(id);
    }

    public Optional<Bank> findBankByName(String name) {
        return banks.values().stream().filter(bank -> bank.getName().equals(name)).findFirst();
    }

    public Account getAccountByID(int bankId, int accountID) {
        return banks.get(bankId).getAccountByID(accountID);
    }

    /**
     * Helper method to perform a {@link Transfer}.
     * 
     * @throws AccountException
     *             when {@link Transfer} cannot be performed
     */
    public void performTransfer(Account from, Account to, double amount, String description) throws AccountException {
        Transfer transfer = from.getBank().sendTransfer(from, to, amount, description);
        to.getBank().receiveTransfer(transfer);
        transfer.make();
    }

    @Override
    public void onComplete() {}

    @Override
    public void onError(Throwable error) {}

    @Override
    public void onNext(TimeRewind value) {
        for (int i = 0; i < value.getDays(); i++) {
            value.rewindDay(today);
            for (Bank bank : banks.values()) {
                try {
                    value.rewindForBank(bank);
                } catch (AccountException e) {
                    value.addException(today.getDate(), e);
                }
            }
        }
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {}
}
