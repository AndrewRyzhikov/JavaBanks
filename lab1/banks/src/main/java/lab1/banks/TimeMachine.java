package lab1.banks;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * Used to generate time events for testing purposes
 */
public class TimeMachine {
    private final PublishSubject<TimeRewind> subject = PublishSubject.create();

    public void subscribe(Observer<TimeRewind> observer) {
        subject.subscribe(observer);
    }

    public TimeRewind rewindToTheFuture(int days) {
        return rewind(days, true);
    }

    public TimeRewind rewindToThePast(int days) {
        return rewind(days, false);
    }

    private TimeRewind rewind(int days, boolean forward) {
        var rewind = new TimeRewind(days, forward);
        subject.onNext(rewind);
        return rewind;
    }
}
