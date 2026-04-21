package server;

import common.network.Request;
import common.network.Response;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Неблокирующий TCP-сервер для обработки клиентских запросов.
 *
 * <p>Сервер реализован с использованием Java NIO (Non-blocking I/O)
 * и паттерна Reactor. Использует один поток для обработки всех
 * подключений, что соответствует требованию ТЗ об однопоточном режиме.</p>
 *
 * <p>Сервер состоит из следующих модулей (реализованных внутри этого класса):
 * <ul>
 *   <li>Модуль приёма подключений - {@link #acceptConnection(SelectionKey)}</li>
 *   <li>Модуль чтения запроса - {@link #readRequest(SelectionKey)}</li>
 *   <li>Модуль обработки полученных команд - через {@link CommandExecutor}</li>
 *   <li>Модуль отправки ответов клиенту - {@link #sendResponse(SocketChannel, Response)}</li>
 * </ul>
 * </p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see CommandExecutor
 * @see Request
 * @see Response
 */
public class NonBlockingServer {

    /** Порт для прослушивания входящих подключений. */
    private final int port;

    /** Исполнитель команд для обработки запросов. */
    private final CommandExecutor commandExecutor;

    /** Флаг работы сервера. */
    private volatile boolean running = true;

    /** Селектор для мультиплексирования каналов. */
    private Selector selector;

    /** Серверный канал для приёма подключений. */
    private ServerSocketChannel serverChannel;

    /**
     * Хранилище для накопления данных от клиентов.
     * Используется для сборки полного сообщения, когда данные
     * приходят несколькими частями (фрагментация TCP).
     */
    private final Map<SocketChannel, ByteArrayOutputStream> clientBuffers = new ConcurrentHashMap<>();

    /**
     * Конструктор сервера.
     *
     * @param port порт для прослушивания
     * @param commandExecutor исполнитель команд
     */
    public NonBlockingServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    /**
     * Запускает сервер в неблокирующем режиме.
     *
     * <p>Метод открывает селектор и серверный канал, регистрирует
     * интерес к операциям принятия подключений и входит в основной
     * цикл обработки событий.</p>
     */
    public void start() {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Non-blocking сервер запущен на порту " + port);
            System.out.println("Ожидание подключений...");

            while (running) {
                selector.select(100);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    try {
                        if (key.isAcceptable()) {
                            acceptConnection(key);
                        } else if (key.isReadable()) {
                            readRequest(key);
                        }
                    } catch (Exception e) {
                        System.err.println("Ошибка: " + e.getMessage());
                        e.printStackTrace();
                        closeConnection(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Принимает новое входящее подключение.
     *
     * <p>Модуль приёма подключений. Принимает клиентский сокет,
     * настраивает его в неблокирующий режим и регистрирует интерес
     * к операциям чтения.</p>
     *
     * @param key ключ селектора для серверного канала
     * @throws IOException если произошла ошибка при принятии подключения
     */
    private void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        clientBuffers.put(clientChannel, new ByteArrayOutputStream());
        System.out.println("Клиент подключился: " + clientChannel.getRemoteAddress());
    }

    /**
     * Читает запрос от клиента.
     *
     * <p>Модуль чтения запроса. Читает данные из канала, накапливает
     * их в буфере и пытается десериализовать объект Request.
     * При успешной десериализации передаёт запрос на обработку.</p>
     *
     * @param key ключ селектора для клиентского канала
     * @throws IOException если произошла ошибка при чтении данных
     */
    private void readRequest(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        ByteArrayOutputStream baos = clientBuffers.get(channel);

        try {
            int bytesRead = channel.read(buffer);

            if (bytesRead == -1) {
                closeConnection(key);
                return;
            }

            if (bytesRead > 0) {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                baos.write(data);

                // Пытаемся десериализовать объект из накопленных данных
                byte[] allData = baos.toByteArray();
                try (ByteArrayInputStream bais = new ByteArrayInputStream(allData);
                     ObjectInputStream ois = new ObjectInputStream(bais)) {

                    Request request = (Request) ois.readObject();
                    System.out.println("Получена команда: " + request.getCommandType());

                    Response response = commandExecutor.execute(request);
                    sendResponse(channel, response);

                    // Очищаем буфер после успешного чтения
                    clientBuffers.put(channel, new ByteArrayOutputStream());

                } catch (EOFException | StreamCorruptedException e) {
                    // Данных ещё не достаточно, ждём следующего чтения
                    // Ничего не делаем, продолжаем накапливать
                } catch (ClassNotFoundException e) {
                    System.err.println("Ошибка десериализации: " + e.getMessage());
                    clientBuffers.put(channel, new ByteArrayOutputStream());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
            closeConnection(key);
        }
    }

    /**
     * Отправляет ответ клиенту.
     *
     * <p>Модуль отправки ответов. Сериализует объект Response
     * и отправляет его через канал клиенту.</p>
     *
     * @param channel клиентский канал
     * @param response объект ответа для отправки
     * @throws IOException если произошла ошибка при отправке
     */
    private void sendResponse(SocketChannel channel, Response response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(response);
        oos.flush();

        byte[] data = baos.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(data);

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        System.out.println("Ответ отправлен");
    }

    /**
     * Закрывает соединение с клиентом и освобождает ресурсы.
     *
     * @param key ключ селектора для закрываемого соединения
     */
    private void closeConnection(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            System.out.println("Закрыто соединение: " + channel.getRemoteAddress());
            clientBuffers.remove(channel);
            key.cancel();
            channel.close();
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии: " + e.getMessage());
        }
    }

    /**
     * Останавливает сервер и освобождает все ресурсы.
     * Закрывает селектор и серверный канал.
     */
    public void stop() {
        running = false;
        try {
            if (selector != null) selector.close();
            if (serverChannel != null) serverChannel.close();
            System.out.println("Сервер остановлен");
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}