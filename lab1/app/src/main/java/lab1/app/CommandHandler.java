package lab1.app;

import org.jline.reader.LineReader;

@FunctionalInterface
public interface CommandHandler {
    void handle(LineReader reader);
}
