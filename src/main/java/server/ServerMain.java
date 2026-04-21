package server;

import server.manager.CollectionManager;
import server.manager.FileManager;
import common.network.Request;
import common.network.Response;
import common.network.CommandType;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Главный класс серверного приложения.
 *
 * <p>Обеспечивает запуск сервера, загрузку коллекции из файла,
 * обработку консольных команд сервера (save, exit) и управление
 * клиентскими подключениями.</p>
 *
 * <p>Сервер работает в многопоточном режиме: каждый клиент
 * обрабатывается в отдельном потоке. При завершении работы
 * автоматически сохраняет коллекцию в файл.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see CollectionManager
 * @see FileManager
 * @see CommandExecutor
 */
public class ServerMain {

    /** Флаг работы сервера. */
    private static volatile boolean running = true;

    /**
     * Точка входа в серверное приложение.
     *
     * <p>Аргументы командной строки:
     * <ul>
     *   <li>args[0] - порт для запуска сервера (по умолчанию 8080)</li>
     * </ul>
     * </p>
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        int port = 8080;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется 8080");
            }
        }

        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager("data.xml");

        // Загрузка коллекции из файла
        try {
            var loadedCollection = fileManager.loadCollection();
            if (loadedCollection != null && !loadedCollection.isEmpty()) {
                collectionManager.initializeCollection(loadedCollection);
                System.out.println("Коллекция загружена из файла data.xml");
                System.out.println("Загружено элементов: " + collectionManager.size());
            } else {
                System.out.println("Файл пуст или не найден. Создана пустая коллекция");
            }
        } catch (IOException e) {
            System.out.println("Не удалось загрузить коллекцию: " + e.getMessage());
            System.out.println("Создана пустая коллекция");
        }

        CommandExecutor commandExecutor = new CommandExecutor(collectionManager, fileManager);

        // Hook для автоматического сохранения коллекции при завершении работы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nСохранение коллекции...");
            try {
                fileManager.saveCollection(collectionManager.getCollection());
                System.out.println("Коллекция сохранена");
            } catch (IOException e) {
                System.err.println("Ошибка сохранения: " + e.getMessage());
            }
        }));

        // Запуск консольного потока для команд сервера
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (running) {
                try {
                    System.out.print("Сервер> ");
                    String command = scanner.nextLine().trim();
                    if (command.equalsIgnoreCase("save")) {
                        String result = commandExecutor.save();
                        System.out.println(result);
                    } else if (command.equalsIgnoreCase("exit")) {
                        System.out.println("Завершение работы сервера...");
                        running = false;
                        System.exit(0);
                    } else if (!command.isEmpty()) {
                        System.out.println("Доступные команды сервера: save, exit");
                    }
                } catch (Exception e) {
                    if (running) {
                        System.err.println("Ошибка ввода: " + e.getMessage());
                    }
                }
            }
            scanner.close();
        });
        consoleThread.setDaemon(true);
        consoleThread.start();

        // Запуск сервера для обработки клиентских подключений
        startServer(port, commandExecutor);
    }

    /**
     * Запускает сервер для приёма и обработки клиентских подключений.
     *
     * <p>Создаёт ServerSocket на указанном порту и входит в бесконечный
     * цикл ожидания подключений. Каждое новое подключение обрабатывается
     * в отдельном потоке.</p>
     *
     * @param port порт для прослушивания
     * @param commandExecutor исполнитель команд
     */
    private static void startServer(int port, CommandExecutor commandExecutor) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            System.out.println("Ожидание подключений...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Клиент подключился: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                    // Обрабатываем каждого клиента в отдельном потоке
                    new Thread(() -> handleClient(clientSocket, commandExecutor)).start();

                } catch (IOException e) {
                    if (running) {
                        System.err.println("Ошибка при подключении клиента: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось запустить сервер: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает соединение с одним клиентом.
     *
     * <p>В цикле читает запросы от клиента, выполняет команды через
     * {@link CommandExecutor} и отправляет ответы обратно.
     * При получении команды EXIT или разрыве соединения завершает работу.</p>
     *
     * @param clientSocket сокет клиента
     * @param commandExecutor исполнитель команд
     */
    private static void handleClient(Socket clientSocket, CommandExecutor commandExecutor) {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {

            while (running && !clientSocket.isClosed()) {
                try {
                    // Читаем запрос от клиента
                    Request request = (Request) ois.readObject();
                    System.out.println("Получена команда: " + request.getCommandType());

                    // Выполняем команду
                    Response response = commandExecutor.execute(request);

                    // Отправляем ответ клиенту
                    oos.writeObject(response);
                    oos.flush();
                    oos.reset();

                    // Если клиент запросил выход, завершаем обработку
                    if (request.getCommandType() == CommandType.EXIT) {
                        System.out.println("Клиент завершил работу");
                        break;
                    }
                } catch (EOFException e) {
                    // Клиент закрыл соединение
                    System.out.println("Клиент отключился");
                    break;
                } catch (ClassNotFoundException | IOException e) {
                    System.err.println("Ошибка при обработке запроса: " + e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при обмене данными с клиентом: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Соединение с клиентом закрыто");
            } catch (IOException e) {
                // ignore
            }
        }
    }
}