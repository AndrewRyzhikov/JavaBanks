package lab1.banks;

import io.reactivex.rxjava3.annotations.NonNull;
import lab1.banks.account.Account;
import lab1.banks.exceptions.AccountException;
import lab1.banks.transaction.Transfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Builder.Default;
import lombok.Getter;

import java.util.List;

/**
 * A client can hold multiple {@link Account}s.
 * Verified client has non-empty address and passport.
 */
@Data
@AllArgsConstructor
@Builder(setterPrefix = "with")
public final class Client  {
    private final String name;
    private final String surname;
    @Getter
    private List<String> notifications;
    @Default
    private String address = "";
    @Default
    private String passport = "";
    /**
     * Checks whether this client is verified
     */
    public void performTransfer(Account from, Account to, double amount, String description) throws AccountException {
        Transfer transfer = from.getBank().sendTransfer(from, to, amount, description);
        to.getBank().receiveTransfer(transfer);
        transfer.make();
    }
    public boolean isTrustworthy() {
        return this.address != "" && this.passport != "";
    }
    public void notify(@NonNull String s) {
        notifications.add(s);
    }
}
