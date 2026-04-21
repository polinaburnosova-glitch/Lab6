package client.commands;

import client.SimpleClient;
import common.model.Mood;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

public class FilterByMoodCommand implements Command {

    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        if (argument == null) {
            System.out.println("Ошибка: не указано настроение. Доступные: SORROW, LONGING, GLOOM, APATHY");
            return;
        }

        try {
            Mood mood = Mood.valueOf(argument.toUpperCase());
            Request request = new Request(CommandType.FILTER_BY_MOOD, new Object[]{mood});
            client.sendRequest(request);

            Response response = client.receiveResponse();

            if (response.isSuccess()) {
                System.out.println(response.getMessage());
            } else {
                System.err.println("Ошибка: " + response.getMessage());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: неверное настроение. Доступные: SORROW, LONGING, GLOOM, APATHY");
        }
    }
}
