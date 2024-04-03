package lab1.app;

import lombok.Getter;

import java.util.List;

import org.jline.console.CmdDesc;
import org.jline.console.CommandMethods;

import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class Command {
    private List<String> info;
    private CmdDesc description;
    private CommandMethods methods;
}
