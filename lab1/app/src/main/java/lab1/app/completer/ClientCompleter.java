package lab1.app.completer;

import java.util.List;
import java.util.Map;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import lab1.banks.Client;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClientCompleter implements Completer {
    private Map<String, Client> clientMap;

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        clientMap.entrySet().stream()
                .map(entry -> new Candidate(entry.getKey(),
                        entry.getValue().getName() + " " + entry.getValue().getSurname(), null, null, null, null, true))
                .forEachOrdered(candidates::add);
    }

}
