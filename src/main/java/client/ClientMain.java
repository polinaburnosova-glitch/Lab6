package client;

import client.commands.CommandHandler;
import client.console.ConsoleInputReader;
import client.script.ScriptExecutor;
import common.EnvLoader;
import java.io.IOException;
import java.util.Scanner;

public class ClientMain {

    public static void main(String[] args) {
        String host = EnvLoader.get("CLIENT_HOST", "localhost");
        int port = EnvLoader.getInt("CLIENT_PORT", 8888);

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется " + port);
            }
        }

        SimpleClient client = new SimpleClient(host, port);
        ConsoleInputReader inputReader = new ConsoleInputReader();
        CommandHandler commandHandler = new CommandHandler(client, inputReader);
        ScriptExecutor scriptExecutor = new ScriptExecutor(client);

        try {
            client.connect();
            System.out.println("Подключено к серверу " + host + ":" + port);
            System.out.println("Введите help для списка команд");

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                String[] parts = input.split("\\s+", 2);
                String command = parts[0].toUpperCase();
                String argument = parts.length > 1 ? parts[1] : null;

                try {
                    if (command.equals("EXECUTE_SCRIPT")) {
                        if (argument == null) {
                            System.out.println("Ошибка: не указан файл");
                            continue;
                        }
                        scriptExecutor.execute(argument);
                        continue;
                    }

                    boolean shouldContinue = commandHandler.handle(command, argument);
                    if (!shouldContinue) {
                        break;
                    }

                } catch (IOException e) {
                    System.err.println("Ошибка связи с сервером: " + e.getMessage());
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Ошибка получения данных: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println("Ошибка: " + e.getMessage());
                }
            }

            client.disconnect();

        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу: " + e.getMessage());
            System.err.println("Убедитесь, что сервер запущен на " + host + ":" + port);
        }
    }
}