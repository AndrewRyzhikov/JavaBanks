package lab1.app.completer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import lab1.banks.CentralBank;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AccountCompleter implements Completer {
    private CentralBank centralBank;

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        if (!line.word().endsWith(".")) {
            centralBank.getBanks().values().stream()
                    .map(bank -> new Candidate(
                            bank.getName() + ".",
                            bank.getName(),
                            null, null, null, null, false))
                    .forEachOrdered(candidates::add);
            return;
        }
        var bankName = line.word().substring(0, line.word().length() - 1);
        var bank = centralBank.findBankByName(bankName).orElse(null);
        if (bank == null) {
            return;
        }

        bank.getAccounts().entrySet().stream()
                .map(entry -> {
                    var id = entry.getKey();
                    var account = entry.getValue();
                    return new Candidate(
                            bankName + "." + String.valueOf(id),
                            new AttributedStringBuilder()
                                    .append(new AttributedString(account.getClass().getSimpleName(),
                                            new AttributedStyle().foreground(
                                                    AttributedStyle.YELLOW)))
                                    .append(" at ")
                                    .append(new AttributedString(bankName,
                                            new AttributedStyle().foreground(AttributedStyle.CYAN)))
                                    .append(" with ")
                                    .append(new AttributedString(String.format("%.2f$", account.getMoney()),
                                            new AttributedStyle().foreground(AttributedStyle.GREEN)))
                                    .toAnsi(),
                            null,
                            String.format("owned by %s %s",
                                    account.getClient().getName(),
                                    account.getClient().getSurname()),
                            null, null, true);
                }).forEachOrdered(candidates::add);
    }
}
