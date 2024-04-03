package lab1.banks.transaction.commands;

import lab1.banks.account.Account;
import lab1.banks.exceptions.AccountException;
import lab1.banks.transaction.Transaction;
import lombok.Getter;

/**
 * Part of {@link Transaction}.
 * Forms a chain of commands performed over {@link Account}
 */
@Getter
public abstract class Command {
    public enum CommandState {
        Queued, Completed, Reverted,
    }

    private Command next;
    private Command prev;
    private CommandState state;

    public Command() {
        this.state = CommandState.Queued;
    }

    /**
     * Chains {@link Command} so that for this command it will be next
     * and for the next command previous will be this
     */
    public void chain(Command next) {
        this.next = next;
        this.next.prev = this;
    }

    /**
     * Executes a chain of {@link Command}s
     */
    public void execute() throws AccountException {
        if (state == CommandState.Completed) {
            return;
        }

        state = CommandState.Completed;
        try {
            onExecute();
        } catch (AccountException e) {
            revert();
            throw e;
        }

        if (next != null) {
            next.execute();
        }
    }

    /**
     * Reverts this and previous commands
     */
    void revert() {
        if (state != CommandState.Completed) {
            return;
        }

        onRevert();
        state = CommandState.Reverted;
        if (prev != null) {
            prev.revert();
        }
    }

    /**
     * Reverts the whole chain of commands
     */
    public void totalRevert() {
        Command last = this;
        while (last.next != null) {
            last = last.next;
        }

        last.revert();
    }

    /**
     * Operation over {@link Account} that will be executed when normal command
     * order is executed
     */
    protected abstract void onExecute() throws AccountException;

    /**
     * Operation over {@link Account} that will be executed when reversed command
     * order is executed
     */
    protected abstract void onRevert();
}
