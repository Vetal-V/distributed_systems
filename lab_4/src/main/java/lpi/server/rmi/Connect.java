package lpi.server.rmi;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Connect{

    public static String defaultIp = "127.0.0.1"; //default ip address of server
    public static int defaultPort = 4321; //default port of server
    private String ip; //ip address of server from args
    private int port; //port of server from args

    Connect(String[] args){ //constructor
        if (args.length == 2){ //arguments exist
            ip = args[0];
            port = Integer.parseInt(args[1]);
        } else { //no argument, connection with default parameters
            ip = defaultIp;
            port = defaultPort;
        }
    }

    void run(){
        try{
            Registry registry = LocateRegistry.getRegistry(ip, port); //obtain the remote registry
            IServer proxy = (IServer) registry.lookup(IServer.RMI_SERVER_NAME); //create object of class IServer

            CommandHandler connection = new CommandHandler(proxy); //create object of class CommandHandler
            connection.main(); //run main method of CommandHandler
        } catch (RemoteException | NotBoundException e){
            System.out.println(e.getMessage()+"\nConnection close.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}