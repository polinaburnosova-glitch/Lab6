package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

public class ShowCommand implements Command {

    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.SHOW, null);
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
            if (response.getData() != null && !response.getData().isEmpty()) {
                System.out.println("\nЭлементы коллекции");
                for (Object obj : response.getData()) {
                    System.out.println(obj);
                }
            }
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}
