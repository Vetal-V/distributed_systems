package lpi.client.soap;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Connect  implements Closeable {

    private URL urlAddress;
    private IChatServer serverProxy;

    Connect(String[] args) throws MalformedURLException { //constructor
        if (args.length == 1) { //argument exist
            urlAddress = new URL(args[0]);
        } else { //no argument, connection with default parameters
            String defaultURL = "http://localhost:4321/chat?wsdl";
            urlAddress = new URL(defaultURL);
        }
    }

    void run() throws IOException {
        ChatServer serverWrapper = new ChatServer(urlAddress);  //create object of class ChatServer
        try{
            serverProxy = serverWrapper.getChatServerProxy(); //get the chat server proxy
        } catch (Exception e){
            System.out.println(e.getMessage()+"\nConnection close.");
        }

        CommandHandler connection = new CommandHandler(serverProxy); //create object of class CommandHandler
        connection.run(); //run method of CommandHandler

    }

    public void close() {serverProxy = null;}
}
