package lab1.app.completer;

import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import lab1.banks.CentralBank;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BankCompleter implements Completer {
    private CentralBank centralBank;

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        centralBank.getBanks().values().stream()
                .map(bank -> new Candidate(bank.getName()))
                .forEachOrdered(candidates::add);
    }
}
