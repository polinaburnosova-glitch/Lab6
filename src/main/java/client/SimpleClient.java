package client;

import common.model.User;
import common.network.Request;
import common.network.Response;
import java.io.*;
import java.net.Socket;
import java.net.ConnectException;

/**
 * Простой TCP-клиент для взаимодействия с сервером.
 *
 * <p>Обеспечивает установление соединения с сервером, отправку сериализованных
 * запросов и получение ответов. Поддерживает автоматическое переподключение
 * при временной недоступности сервера.</p>
 *
 * <p>Клиент использует блокирующие потоки ввода-вывода и работает по протоколу TCP.
 * Для обмена данными используются ObjectOutputStream и ObjectInputStream,
 * что позволяет передавать сериализованные объекты.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see Request
 * @see Response
 */
public class SimpleClient {
    /** Хост сервера (IP-адрес или доменное имя). */
    private final String host;
    /** Порт сервера для подключения. */
    private final int port;
    /** Сокет для соединения с сервером. */
    private Socket socket;
    /** Поток для отправки сериализованных объектов на сервер. */
    private ObjectOutputStream outputStream;
    /** Поток для чтения сериализованных объектов от сервера. */
    private ObjectInputStream inputStream;
    /** Флаг, указывающий на наличие активного соединения с сервером. */
    private boolean connected;
    /** Максимальное количество попыток подключения к серверу. */
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    /** Задержка между попытками переподключения в миллисекундах. */
    private static final int RECONNECT_DELAY_MS = 2000;
    private User currentUser;

    /**
     * Конструктор клиента.
     *
     * @param host хост сервера (например, "localhost" или IP-адрес)
     * @param port порт сервера (должен совпадать с портом сервера)
     */
    public SimpleClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.connected = false;
        this.currentUser = null;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Устанавливает соединение с сервером.
     *
     * <p>При недоступности сервера выполняет несколько попыток подключения
     * с заданной задержкой между ними. После успешного подключения
     * инициализирует потоки ввода-вывода.</p>
     *
     * @throws IOException если не удалось подключиться к серверу после всех попыток
     *                      или произошла ошибка ввода-вывода
     */
    public void connect() throws IOException {
        int attempts = 0;
        while (attempts < MAX_RECONNECT_ATTEMPTS) {
            try {
                System.out.println("Попытка подключения " + (attempts + 1) + " из " + MAX_RECONNECT_ATTEMPTS);
                socket = new Socket(host, port);

                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(socket.getInputStream());

                connected = true;
                System.out.println("Подключено к серверу " + host + ":" + port);
                return;
            } catch (ConnectException e) {
                attempts++;
                if (attempts >= MAX_RECONNECT_ATTEMPTS) {
                    throw new IOException("Не удалось подключиться к серверу после " + MAX_RECONNECT_ATTEMPTS + " попыток");
                }
                System.err.println("Сервер недоступен. Повтор через " + RECONNECT_DELAY_MS + " мс...");
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Прервано ожидание подключения");
                }
            }
        }
    }

    /**
     * Отправляет запрос на сервер.
     *
     * <p>Запрос сериализуется и отправляется через ObjectOutputStream.
     * После отправки поток сбрасывается для предотвращения накопления
     * ссылок на отправленные объекты.</p>
     *
     * @param request объект запроса для отправки
     * @throws IOException если соединение разорвано или произошла ошибка отправки
     */
    public void sendRequest(Request request) throws IOException {
        if (!connected || socket == null || socket.isClosed()) {
            throw new IOException("Нет соединения с сервером");
        }

        if (currentUser != null && request.getUser() == null) {
            request = new Request(
                    request.getCommandType(),
                    request.getArgs(),
                    currentUser
            );
        }

        outputStream.writeObject(request);
        outputStream.flush();
        outputStream.reset();
    }

    /**
     * Получает ответ от сервера.
     *
     * <p>Блокирующий метод, ожидающий получения сериализованного объекта
     * Response от сервера.</p>
     *
     * @return объект ответа от сервера
     * @throws IOException если соединение разорвано или произошла ошибка чтения
     * @throws ClassNotFoundException если не удалось десериализовать объект
     */
    public Response receiveResponse() throws IOException, ClassNotFoundException {
        if (!connected || socket == null || socket.isClosed()) {
            throw new IOException("Нет соединения с сервером");
        }
        return (Response) inputStream.readObject();
    }

    /**
     * Закрывает соединение с сервером и освобождает ресурсы.
     *
     * <p>Последовательно закрываются потоки ввода-вывода и сокет.
     * В случае ошибки при закрытии отдельных компонентов процесс
     * продолжается для закрытия остальных.</p>
     */
    public void disconnect() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        } finally {
            connected = false;
        }
    }

    /**
     * Проверяет наличие активного соединения с сервером.
     *
     * @return true если соединение установлено и сокет не закрыт, иначе false
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}