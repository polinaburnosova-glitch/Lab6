package server;

import server.database.DatabaseManager;
import server.database.HumanBeingDAO;
import server.manager.CollectionManager;
import common.EnvLoader;

public class ServerMain {

    public static void main(String[] args) {
        int port = EnvLoader.getInt("SERVER_PORT", 5555);
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется " + port);
            }
        }

        CollectionManager collectionManager = new CollectionManager();
        collectionManager.loadFromDatabase();

        CommandExecutor commandExecutor = new CommandExecutor(collectionManager);

        ThreadPoolServer server = new ThreadPoolServer(port, commandExecutor);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Завершение работы сервера...");
            DatabaseManager.closeConnection();
            server.stop();
        }));

        System.out.println("Сервер запущен на порту " + port);
        server.start();
    }
}