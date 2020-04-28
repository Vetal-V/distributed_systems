package lpi.client.rest;

import javax.ws.rs.core.MediaType;
import java.io.Closeable;
import java.io.IOException;

public class Connect implements Closeable {

    private static final String defaultUrl = "http://localhost:8080/chat/server/";
    private String targetUrl;
    private javax.ws.rs.client.Client client;

    Connect(String[] args) { //constructor
        if (args.length == 1) { //argument exist
            targetUrl = args[0].trim() + "/chat/server/";
        } else { //no argument, connection with default parameters
            targetUrl = defaultUrl;
        }
    }

    void run() throws IOException {
        client = javax.ws.rs.client.ClientBuilder.newClient();

        try{ //check if URL if correct
            client.target(targetUrl+"ping") //ping to server
                    .request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        } catch (Exception err){
            System.out.println(err.getMessage() + "\nConnection close. Wrong URL.");
            return;
        }

        CommandHandler connection = new CommandHandler(client, targetUrl); //create object of class CommandHandler
        connection.run(); //run method of CommandHandler

    }
    @Override
    public void close() {
        if (client != null)
            client.close();
        System.out.println("Error connect to server.");
    }
}
