package server;

import common.network.Request;
import common.network.Response;
import common.network.CommandType;
import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class NonBlockingServer {

    private final int port;
    private final CommandExecutor commandExecutor;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;

    public NonBlockingServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() {
        try (ServerSocket localServerSocket = new ServerSocket()) {
            this.serverSocket = localServerSocket;
            localServerSocket.setReuseAddress(true);
            localServerSocket.bind(new InetSocketAddress(port));

            System.out.println("Сервер запущен на порту " + port);
            System.out.println("Ожидание подключений...");

            while (running.get()) {
                Socket client = localServerSocket.accept();
                clientPool.submit(() -> handleClient(client));
            }

        } catch (BindException e) {
            System.err.println("Порт " + port + " уже занят. Остановите процесс на этом порту или запустите сервер на другом порту.");
        } catch (SocketException e) {
            if (running.get()) {
                System.err.println("Ошибка сокета сервера: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        } finally {
            clientPool.shutdownNow();
        }
    }

    private void handleClient(Socket client) {
        try (Socket socket = client;
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Клиент подключился: " + socket.getRemoteSocketAddress());

            while (running.get() && !socket.isClosed()) {
                Request request;
                try {
                    request = (Request) ois.readObject();
                } catch (EOFException ignored) {
                    break;
                }

                System.out.println("Получена команда: " + request.getCommandType());
                Response response = commandExecutor.execute(request);
                sendResponse(oos, response);

                if (request.getCommandType() == CommandType.EXIT) {
                    System.out.println("Клиент завершил работу");
                    break;
                }
            }
        } catch (SocketException e) {
            System.out.println("Соединение с клиентом закрыто: " + e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        }

        System.out.println("Клиент отключился");
    }

    private void sendResponse(ObjectOutputStream oos, Response response) throws IOException {
        oos.writeObject(response);
        oos.flush();
        oos.reset();
        System.out.println("Ответ отправлен");
    }

    public void stop() {
        running.set(false);
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            clientPool.shutdownNow();
            System.out.println("Сервер остановлен");
        } catch (IOException e) {
            System.err.println("Ошибка при остановке: " + e.getMessage());
        }
    }
}
