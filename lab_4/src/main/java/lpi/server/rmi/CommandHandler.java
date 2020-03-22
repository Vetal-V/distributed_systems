package lpi.server.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;


class CommandHandler{
    private IServer proxy; //object class of IServer
    private String loginId; //login id

    private Timer timer; //object class of Timer

    CommandHandler(IServer proxy){ //constructor of class CommandHandler
        this.proxy = proxy;
    }

    void main() throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  //input buffer stream from user

        boolean ifLoop = true;
        while (ifLoop) { //loop for entering command
            System.out.println("\nEnter the command (help - list of available commands): ");
            String command = br.readLine(); //read command of user

            switch (command){
                case "exit": //complete client execution
                    ifLoop = false;
                    try{
                        proxy.exit(loginId); //call the method exit of class IServer
                        timer.cancel(); //cancel thread of timer
                    } catch (IOException ignored) {}
                    break;

                case "ping": //command ping
                    System.out.println(" You choose the command ping.");
                    try{
                        proxy.ping(); //call the method ping of class IServer
                        System.out.println(" Ping successful.");
                    } catch(RemoteException ex) {
                        System.out.println(ex.getMessage());
                    }
                    break;
                case "echo": //command echo
                    System.out.println(" You choose the command echo.");
                    System.out.print(" Enter the echo text: ");
                    String echoText = br.readLine(); //read echo text from user

                    try{
                        String echoResponse = proxy.echo(echoText); //call the method echo of class IServer
                        System.out.println("Received from server: " + echoResponse);
                    } catch(RemoteException ex) {
                        System.out.println(ex.getMessage());
                    }
                    break;

                case "login": //command login
                    System.out.println(" You choose the command login.");
                    System.out.print(" Enter the login: ");
                    String loginName = br.readLine(); //read login from user
                    System.out.print(" Enter the password: ");
                    String loginPass = br.readLine(); //read password from user

                    try{
                        String loginResponse = proxy.login(loginName, loginPass); //call the method login of class IServer
                        if (loginResponse != null){ //successful login
                            System.out.println(" Successful login");
                            this.loginId = loginResponse;

                            ReceiveTimer receive = new ReceiveTimer(this.proxy, this.loginId); //create object of class ReceiveTimer
                            timer = new Timer(true); //init timer daemon
                            timer.scheduleAtFixedRate(receive, 0, 2000); //set frequency  for thread
                        }
                    } catch(RemoteException ex) {
                        System.out.println(ex.getMessage());
                    }
                    break;

                case "list": //command list
                    System.out.println(" You choose the command list.");

                    String[] listUsers = null;
                    try{
                        listUsers = proxy.listUsers(loginId); //call the method listUsers of class IServer
                    } catch(RemoteException ex) {
                        System.out.println(" This command requires login first.");
                        System.out.println(ex.getMessage());
                    }

                    if (listUsers != null){ //print the list of active user
                        System.out.print(" List of active user name: ");
                        for (String s : listUsers) System.out.print(s + " ");
                    }
                    System.out.println();
                    break;

                case "msg": //command msg
                    System.out.println(" You choose the command msg.");
                    System.out.print(" Enter the login of receiver: ");
                    String msgLogin = br.readLine(); //read login of receiver from user
                    System.out.print(" Enter the message: ");
                    String msgText = br.readLine(); //read message from user

                    IServer.Message msg =  new IServer.Message(msgLogin,  msgText); //create the object of class Message
                    try{
                        proxy.sendMessage(loginId, msg); //call the method sendMessage of class IServer
                        System.out.println(" Message sent successfully.");
                    } catch(RemoteException ex) {
                        System.out.println(" This command requires login first.");
                        System.out.println(ex.getMessage());
                    }
                    break;

                case "recMsg": //command receive message
                    System.out.println(" You choose the command receive message.");
                    if (ReceiveTimer.getCountMsg() == 0){ //no message
                        System.out.println(" You don't have message.");
                    } else if(ReceiveTimer.getCountMsg() == 1){ //one message
                        System.out.println(" You have the message:");
                        System.out.println("  Sender: " + ReceiveTimer.recMsg[0].getSender());
                        System.out.println("  Message: " + ReceiveTimer.recMsg[0].getMessage());
                        ReceiveTimer.setCountMsg(0); //reset the counter
                    } else { //more then one message
                        System.out.println(" You have messages:");
                        for(int i = 0; i < ReceiveTimer.getCountMsg(); i++ ) {
                            System.out.println("  Sender: " + ReceiveTimer.recMsg[i].getSender());
                            System.out.println("  Message: " + ReceiveTimer.recMsg[i].getMessage() + "\n");
                        }
                        ReceiveTimer.setCountMsg(0); //reset the counter
                    }
                    break;

                case "file": //command file
                    System.out.println(" You choose the command file.");
                    System.out.print(" Enter the login of receiver: ");
                    String fileReceiver = br.readLine(); //read login of receiver from user
                    System.out.print(" Enter the path to file: ");
                    String filePath = br.readLine(); //read the path to file from user

                    File file = new File(filePath); //create the object of class File
                    if (file.exists() && file.isFile()){ //checking for file existence
                        IServer.FileInfo fileInfo = new IServer.FileInfo(fileReceiver, file); //create the object of class FileInfo
                        try{
                            proxy.sendFile(loginId, fileInfo); //call the method sendFile of class IServer
                            System.out.println(" File sent successfully.");
                        }catch(RemoteException ex) {
                            System.out.println(" This command requires login first.");
                            System.out.println(ex.getMessage());
                        }

                    } else
                        System.out.println(" Wrong path to the file. Please, try again.");
                    break;

                case "recFile": //command receive file
                    System.out.println(" You choose the command receive file.");
                    String pathFile = "./receiveFile/"; //path to received file

                    if (ReceiveTimer.getCountFile() == 0) //no file
                        System.out.println(" You don't have waiting file.");
                    else {
                        File folderPath = new File(pathFile); //create object of class File
                        if (!folderPath.exists()){ //create folder if not exist
                            boolean dirCreated = folderPath.mkdirs();
                            if (dirCreated){
                                System.out.println(" Folder for files was created.");
                            }
                        } else if (ReceiveTimer.getCountFile() == 1){ // one waiting file
                            System.out.println(" You have waiting file:");
                            System.out.println("  Sender of file: " + ReceiveTimer.recFile[0].getSender());
                            String fileName = ReceiveTimer.recFile[0].getFilename();
                            System.out.println("  Filename: " + fileName);
                            ReceiveTimer.recFile[0].saveFileTo(folderPath); //call the method saveFileTo of class IServer
                            System.out.println(" The content was written to a file " + fileName + " at path /receiverFile/" + fileName);
                            ReceiveTimer.setCountFile(0); //reset the counter
                        } else { //more than one file
                            String[] fileNames = new String[ReceiveTimer.getCountFile()+1];
                            System.out.println(" You have waiting files:");
                            for (int i = 0; i < ReceiveTimer.getCountFile(); i++) {
                                fileNames[i] = ReceiveTimer.recFile[i].getFilename();
                                System.out.println("  Sender of file: " + ReceiveTimer.recFile[i].getSender());
                                System.out.println("  Filename: " + fileNames[i]);
                                ReceiveTimer.recFile[i].saveFileTo(folderPath); //call the method saveFileTo of class IServer
                                System.out.println(" The content was written to a file " + fileNames[i] + " at path /receiveFile/" + fileNames[i]);
                            }
                            ReceiveTimer.setCountFile(0); //reset the counter
                        }
                    }
                    break;

                case "help": //help command
                    System.out.println("Available command:");
                    System.out.println(" exit - shutdown client;");
                    System.out.println(" ping - testing connection;");
                    System.out.println(" echo - testing sending message;");
                    System.out.println(" login - log in on server or create new user;");
                    System.out.println(" list - array with active user names;");
                    System.out.println(" msg - send message to user;");
                    System.out.println(" file - send file to user;");
                    System.out.println(" recMsg - receive message for login user;");
                    System.out.println(" recFile - receive file for login user.");
                    break;

                default: //execute when wrong command
                    System.out.println("\nWrong command. Please, try again.");
            }
        }
    }
}

class ReceiveTimer extends TimerTask  {

    private IServer proxy;//object class of IServer
    private String loginId; //login id

    private static int countMsg; //count of waiting message
    private static int countFile; //count of waiting file

    public static IServer.FileInfo[] recFile = new IServer.FileInfo[25]; //list of object IServer.FileInfo
    public static IServer.Message[] recMsg = new IServer.Message[50]; //list of object IServer.Message

    private static ArrayList<String> current = new ArrayList<>(); //array list of current user
    private static ArrayList <String> old = new ArrayList<>(); //array list of old user
    private static int count = 0;

    ReceiveTimer(IServer proxy, String loginId){ //constructor
        this.proxy = proxy;
        this.loginId = loginId;
    }

    public static int getCountMsg(){ //getter of private field countMsg
        return countMsg;
    }
    public static void setCountMsg(int countMsg){ //setter of private field countMsg
        ReceiveTimer.countMsg = countMsg;
    }
    public static int getCountFile(){ //getter of private field countFile
        return countFile;
    }
    public static void setCountFile(int countFile){ //setter of private field countFile
        ReceiveTimer.countFile = countFile;
    }

    @Override
    public void run() throws NullPointerException { //run method of timer
        //check waiting message
        IServer.Message recMsg = null;
        try {
            recMsg = proxy.receiveMessage(loginId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (recMsg != null){ //message exist
            countMsg++;
            try{
                ReceiveTimer.recMsg[countMsg-1] = recMsg;
            } catch (NullPointerException ignored) {}
            System.out.println("You have unread message: " + countMsg + ". To see the message enter the command recMsg.");
        }

        //check waiting file
        IServer.FileInfo recFile = null;
        try {
            recFile = proxy.receiveFile(loginId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (recFile != null){ //file exist
            countFile++;
            try{
                ReceiveTimer.recFile[countFile-1] = recFile;
            } catch (NullPointerException ignored) {}
            System.out.println("You have waiting file: " + countFile + ". To see waiting file enter the command recFile.");
        }

        //check new or log out user
        String[] listUsers = null;
        try{
            listUsers = proxy.listUsers(loginId);
        } catch(RemoteException ex) {
            System.out.println("This command requires login first.");
            System.out.println(ex.getMessage());
        }

        if (count == 0){ //record users to array list on first iteration
            assert listUsers != null;
            for(String i: listUsers){
                current.add(i);
                old.add(i);
            }
        } else { //second and subsequent iterations
            current.clear();
            assert listUsers != null;
            Collections.addAll(current, listUsers);

            if(current.size() > old.size()){ //new user
                current.removeAll(old);
                System.out.println("The user " + current.get(0) + " connected to the server.");
                current.clear(); //clear array list
                old.clear();
                for(String i: listUsers){
                    current.add(i);
                    old.add(i);
                }

            } else if (current.size() < old.size()){ //logout user
                old.removeAll(current);
                System.out.println("The user " + old.get(0) + " disconnected from server ");
                old.clear(); //clear array list
                current.clear();
                for(String i: listUsers) {
                    current.add(i);
                    old.add(i);
                }
            }
        }
        if (count == 0) //detect first iteration
            count++;
    }
}