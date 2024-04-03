package lab1.banks.notifications;

public class DeptPercent implements Notification {
    private final String state = "Dept percent has changed by ";
    @Override
    public String StateChanging() {
        return state;
    }
}
