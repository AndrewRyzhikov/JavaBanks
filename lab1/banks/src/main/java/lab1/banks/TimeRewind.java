package lab1.banks;

import java.util.Collections;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import lab1.banks.exceptions.AccountException;
import lab1.banks.transaction.Transaction;
import lombok.Getter;

/**
 * Represent a set of consecutive time events produced by {@link TimeMachine}
 * During rewind, {@link AccountException}s can be thrown.
 * {@link TimeRewind} collects all {@link AccountException}s that occured during
 * rewind
 */
public class TimeRewind {
    private final Map<LocalDate, List<AccountException>> exceptions = new TreeMap<LocalDate, List<AccountException>>();
    @Getter
    private final int days;
    private boolean isForward = true;

    public TimeRewind(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Number of days must be positive");
        }

        this.days = days;
    }

    public TimeRewind(int days, boolean isForward) {
        this(days);
        this.isForward = isForward;
    }

    public Map<LocalDate, List<AccountException>> getExceptionsDuringRewind() {
        return exceptions.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(),
                        entry -> Collections.unmodifiableList(entry.getValue())));
    }

    public void rewindForBank(Bank bank) throws AccountException {
        if (isForward) {
            bank.daily();
            if (bank.getToday().getDay() == 1) {
                bank.monthly();
            }
        } else {
            for (Transaction transaction : bank.findTransactionsAt(bank.getToday().getDate()).stream()
                    .collect(Collector.of(ArrayDeque<Transaction>::new, (deq, t) -> deq.addFirst(t), (d1, d2) -> {
                        d2.addAll(d1);
                        return d2;
                    }))) {
                transaction.revert();
            }

            bank.revertDay();
        }
    }

    public void rewindDay(DateWrapper wrapper) {
        if (isForward) {
            wrapper.increment();
        } else {
            wrapper.decrement();
        }
    }

    public void addException(LocalDate date, AccountException e) {
        if (!exceptions.containsKey(date)) {
            exceptions.put(date, new ArrayList<AccountException>());
        }

        exceptions.get(date).add(e);
    }
}
