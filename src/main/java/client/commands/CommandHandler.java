package client.commands;

import client.SimpleClient;
import client.console.ConsoleInputReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    private final Map<String, Command> commands = new HashMap<>();
    private final SimpleClient client;

    public CommandHandler(SimpleClient client, ConsoleInputReader inputReader) {
        this.client = client;

        commands.put("ADD", new AddCommand(inputReader));
        commands.put("ADD_IF_MIN", new AddIfMinCommand(inputReader));
        commands.put("ADD_IF_MAX", new AddIfMaxCommand(inputReader));
        commands.put("UPDATE", new UpdateCommand(inputReader));
        commands.put("REMOVE_BY_ID", new RemoveByIdCommand());
        commands.put("FILTER_BY_MOOD", new FilterByMoodCommand());
        commands.put("FILTER_STARTS_WITH_SOUNDTRACK_NAME", new FilterBySoundtrackCommand());
        commands.put("SHOW", new ShowCommand());
        commands.put("INFO", new InfoCommand());
        commands.put("HELP", new HelpCommand());
        commands.put("MIN_BY_ID", new MinByIdCommand());
        commands.put("CLEAR", new ClearCommand());
        commands.put("REMOVE_FIRST", new RemoveFirstCommand());
        commands.put("EXIT", new ExitCommand());
    }

    public boolean handle(String command, String argument) throws IOException, ClassNotFoundException {
        Command cmd = commands.get(command);

        if (cmd == null) {
            System.out.println("Неизвестная команда. Введите help");
            return true;
        }

        cmd.execute(client, argument);

        return !command.equals("EXIT");
    }
}