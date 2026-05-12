package server;

import server.manager.CollectionManager;
import server.manager.FileManager;
import common.EnvLoader;
import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) {
        // 1. Порт из .env или аргументов командной строки
        int port = EnvLoader.getInt("SERVER_PORT", 5555);
        String collectionFile = EnvLoader.get("COLLECTION_FILE", "data.xml");
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется " + port);
            }
        }

        // 2. Загрузка коллекции из файла
        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager(collectionFile);

        try {
            var loaded = fileManager.loadCollection();
            if (loaded != null && !loaded.isEmpty()) {
                collectionManager.initializeCollection(loaded);
                System.out.println("Загружено " + collectionManager.size() + " элементов");
            } else {
                System.out.println("Создана пустая коллекция");
            }
        } catch (IOException e) {
            System.out.println("Создана пустая коллекция: " + e.getMessage());
        }

        // 3. Создание исполнителя команд
        CommandExecutor commandExecutor = new CommandExecutor(collectionManager, fileManager);

        // 4. Запуск неблокирующего сервера
        NonBlockingServer server = new NonBlockingServer(port, commandExecutor);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Завершение работы сервера...");
            System.out.println(commandExecutor.save());
            server.stop();
        }));

        System.out.println("Конфигурация сервера: порт=" + port + ", файл коллекции=" + collectionFile);
        server.start();
    }
}