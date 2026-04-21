package server;

import server.manager.CollectionManager;
import server.manager.FileManager;
import common.network.Request;
import common.network.Response;
import common.network.ResponseStatus;
import common.EnvLoader;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class ServerMain {

    public static void main(String[] args) {
        int port = EnvLoader.getInt("SERVER_PORT", 8888);
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется 8080");
            }
        }

        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager("data.xml");

        try {
            var loaded = fileManager.loadCollection();
            if (loaded != null && !loaded.isEmpty()) {
                collectionManager.initializeCollection(loaded);
                System.out.println("Загружено " + collectionManager.size() + " элементов");
            } else {
                System.out.println("Создана пустая коллекция");
            }
        } catch (IOException e) {
            System.out.println("Создана пустая коллекция");
        }

        CommandExecutor commandExecutor = new CommandExecutor(collectionManager, fileManager);

        runServer(port, commandExecutor, collectionManager, fileManager);
    }

    private static void runServer(int port, CommandExecutor commandExecutor,
                                  CollectionManager collectionManager, FileManager fileManager) {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Сервер запущен на порту " + port);

            boolean running = true;
            while (running) {
                selector.select(100);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        SocketChannel client = serverChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("Клиент подключился");
                    }

                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(8192);

                        int bytesRead = client.read(buffer);
                        if (bytesRead == -1) {
                            key.cancel();
                            client.close();
                            System.out.println("Клиент отключился");
                            continue;
                        }

                        if (bytesRead > 0) {
                            buffer.flip();
                            byte[] data = new byte[buffer.remaining()];
                            buffer.get(data);

                            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                                 ObjectInputStream ois = new ObjectInputStream(bais)) {

                                Request request = (Request) ois.readObject();
                                Response response = commandExecutor.execute(request);

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ObjectOutputStream oos = new ObjectOutputStream(baos);
                                oos.writeObject(response);
                                oos.flush();

                                ByteBuffer respBuffer = ByteBuffer.wrap(baos.toByteArray());
                                while (respBuffer.hasRemaining()) {
                                    client.write(respBuffer);
                                }

                                if (request.getCommandType() == common.network.CommandType.EXIT) {
                                    key.cancel();
                                    client.close();
                                }

                            } catch (ClassNotFoundException e) {
                                System.err.println("Class not found: " + e.getMessage());
                            }
                        }
                    }
                }
            }

            selector.close();
            serverChannel.close();

        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}