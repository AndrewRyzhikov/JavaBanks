package lab1.banks.notifications;

public class MaxTransactionValue implements Notification{
    private final String state = "Max Transaction has changed by ";
    @Override
    public String StateChanging() {
        return state;
    }
}
