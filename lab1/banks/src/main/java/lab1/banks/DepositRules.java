package lab1.banks;

import lab1.banks.account.DepositAccount;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A helper for setting interests of {@link DepositAccount}
 */
public class DepositRules {
    private final NavigableMap<Double, Double> procentsMap = new TreeMap<Double, Double>();

    public DepositRules(double startingInterest) {
        procentsMap.put(0d, startingInterest);
    }

    public NavigableMap<Double, Double> getProcents() {
        return Collections.unmodifiableNavigableMap(procentsMap);
    }

    public void addMilestone(double money, double interest) {
        if (interest < 0) {
            throw new IllegalArgumentException("Deposit interest must be positive");
        }

        procentsMap.put(money, interest);
    }

    public double getInterest(double money) {
        return procentsMap.floorEntry(money).getValue();
    }
}
