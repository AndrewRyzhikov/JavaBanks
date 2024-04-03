package lab1.banks;

import java.time.LocalDate;

import lombok.Getter;

/**
 * Wraps immutable {@link LocalDate} so that all {@link CentralBank}s have same
 * date
 */
@Getter
public final class DateWrapper {
    private LocalDate date;
    public DateWrapper(LocalDate date) {
        this.date = date;
    }

    public int getDay() {
        return date.getDayOfMonth();
    }

    public void increment() {
        date = date.plusDays(1);
    }

    public void decrement() {
        date = date.plusDays(-1);
    }
}
