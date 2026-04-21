package server;

import common.network.Request;
import common.network.Response;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class NonBlockingServer {

    private final int port;
    private final CommandExecutor commandExecutor;
    private volatile boolean running = true;
    private Selector selector;
    private ServerSocketChannel serverChannel;

    public NonBlockingServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() {
        try {
            selector = Selector.open();

            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Сервер запущен на порту " + port);

            while (running) {
                selector.select(100);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        acceptClient(key);
                    }

                    if (key.isReadable()) {
                        readClientData(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("Клиент подключился");
    }

    private void readClientData(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);

        int bytesRead = client.read(buffer);

        if (bytesRead == -1) {
            // Клиент отключился
            key.cancel();
            client.close();
            System.out.println("Клиент отключился");
            return;
        }

        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {

                Request request = (Request) ois.readObject();
                System.out.println("Команда: " + request.getCommandType());

                Response response = commandExecutor.execute(request);

                sendResponse(client, response);

            } catch (ClassNotFoundException e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void sendResponse(SocketChannel client, Response response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(response);
        oos.flush();

        ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
        while (buffer.hasRemaining()) {
            client.write(buffer);
        }
        System.out.println("Ответ отправлен");
    }

    public void stop() {
        running = false;
        try {
            if (selector != null) selector.close();
            if (serverChannel != null) serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}