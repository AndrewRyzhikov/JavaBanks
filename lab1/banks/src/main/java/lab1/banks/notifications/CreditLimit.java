package lab1.banks.notifications;

public class CreditLimit implements  Notification{
    private final String state = "Credit limit has changed by ";
    @Override
    public String StateChanging() {
        return state;
    }
}
