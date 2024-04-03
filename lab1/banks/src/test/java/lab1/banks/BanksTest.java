package lab1.banks;

import org.junit.jupiter.api.Test;

import lab1.banks.exceptions.AccountException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;

class BanksTest {
    private CentralBank centralBank = new CentralBank(LocalDate.of(2023, 2, 1));
    private TimeMachine timeMachine = new TimeMachine();
    private Map<String, Client> clientMap = new HashMap<>();

    @BeforeEach
    void init() {
        timeMachine.subscribe(centralBank);
        var rules = new DepositRules(0.01);
        rules.addMilestone(10d, 0.02);
        rules.addMilestone(50d, 0.034);
        rules.addMilestone(100d, 0.05);
        rules.addMilestone(1000d, 0.1);
        centralBank.addBank("Tinkoff", 0.02, 0.05, rules, 1600.43, 100, 123d);
        centralBank.addBank("Sber", 0.01, 0.06, rules, 1210.53, 500, 200d);

        clientMap.put("Marat", new Client("Marat", "Fakt", new ArrayList<>(), "", "LOL"));
        clientMap.put("Egor", new Client("Egor", "Mandragor", new ArrayList<>(), "Goto street", "GG"));

        centralBank.getBankByID(0).createDebtAccount(clientMap.get("Marat"));
        centralBank.getBankByID(1).createCreditAccount(clientMap.get("Egor"));
    }

    @Test
    void testTopUp() {
        assertDoesNotThrow(() -> centralBank.getBankByID(0).topUpAccount(0, 150d, ""));
        assertEquals(150d, centralBank.getAccountByID(0, 0).getMoney());
    }

    @Test
    void testWithdraw() {
        testTopUp();
        assertDoesNotThrow(() -> centralBank.getBankByID(0).withdrawFromAccount(0, 50d, ""));
        assertEquals(100d, centralBank.getAccountByID(0, 0).getMoney());
        assertThrowsExactly(AccountException.class, () -> centralBank.getBankByID(0).withdrawFromAccount(0, 200d, ""),
                "Not enough money");
    }

    @Test
    void testTransfer() {
        testTopUp();
        var from = centralBank.getAccountByID(0, 0);
        var to = centralBank.getAccountByID(1, 0);
        assertDoesNotThrow(() -> from.getClient().performTransfer(from, to, 50d, ""));
        assertEquals(100d, from.getMoney());
        assertEquals(50d, to.getMoney());
        assertThrowsExactly(AccountException.class, () -> from.getClient().performTransfer(from, to, 200d, ""),
                "Not enough money");
    }

    @Test
    void testRewind() {
        testTopUp();
        assertDoesNotThrow(() -> centralBank.getBankByID(1).withdrawFromAccount(0, 10d, ""));
        assertEquals(-10d, centralBank.getAccountByID(1, 0).getMoney());

        timeMachine.rewindToTheFuture(35);
        assertEquals(234d, centralBank.getAccountByID(0, 0).getMoney());
        assertEquals(-27.807999999999996d, centralBank.getAccountByID(1, 0).getMoney());

        timeMachine.rewindToThePast(35);
        assertEquals(0d, centralBank.getAccountByID(0, 0).getMoney());
        assertEquals(0d, centralBank.getAccountByID(1, 0).getMoney());
    }
}
