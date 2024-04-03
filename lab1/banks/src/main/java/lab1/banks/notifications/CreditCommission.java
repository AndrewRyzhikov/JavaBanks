package lab1.banks.notifications;

public class CreditCommission implements Notification{
    private final String state = "Credit commission has changed by ";
    @Override
    public String StateChanging() {
        return state;
    }
}
